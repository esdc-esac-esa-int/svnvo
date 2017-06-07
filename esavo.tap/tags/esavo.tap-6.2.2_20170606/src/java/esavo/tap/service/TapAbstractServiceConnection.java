package esavo.tap.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import esavo.tap.LimitUnit;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.db.DBConnection;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.parameters.TAPParameters;
import esavo.tap.resource.TAP;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

public abstract class TapAbstractServiceConnection implements TAPService {
	
	public static final String TAP_OUTPUT_LIMIT_ROWS = "tap.output_limit_rows";
	public static final int DEFAULT_OUTPUT_LIMIT_ROWS = -1; //disabled
	
	public static final long DEFAULT_RETENTION_PERIOD = 259200000; //ms. 3 days (= 3*24*60*60*1000=259200000)
	public static final long DEFAULT_EXEC_DURATION_LIMIT = 0; //ms. 0=no limit


	private static final Logger LOG = Logger.getLogger(TapAbstractServiceConnection.class.getName());
	
	private String appid;
	private ArrayList<String> coordSys;
	private List<String> uwsJobsParametersToIgnore;
	private UwsManager uwsManager;
	private UwsConfiguration configuration;
	private TAPFactory factory;
	private TAP tap;
	private TAPSchemaInfo defaultTapSchemaInfo;
	
	private Properties tapProperties = new Properties();
	
	public TapAbstractServiceConnection(String appid) throws UwsException, TAPException {
		this.appid = appid;
		
		uwsJobsParametersToIgnore = new ArrayList<String>();
		uwsJobsParametersToIgnore.add(TAPParameters.PARAM_SESSION);

		configuration = UwsConfigurationManager.getConfiguration(appid);
		
		coordSys = new ArrayList<String>(2);
		coordSys.add("ICRS");
		coordSys.add("ICRS BARYCENTER");
		
		defaultTapSchemaInfo = createDefaultTapSchemaInfo();
		
		try{
			loadPropertiesFile(tapProperties, "tap.properties");
		}catch(TAPException e){
			tapProperties = new Properties();
		}
		
		initService();
	}
	
	protected abstract void initService() throws UwsException, TAPException;
	
	protected void initService(TAPFactory factory) throws UwsException, TAPException{
		this.factory = factory;
		uwsManager = factory.getUwsManager();
		tap = new TAP(this);
	}
	
	private TAPSchemaInfo createDefaultTapSchemaInfo(){
		TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		tapSchemaInfo.setTapSchemasTableName("all_schemas");
		tapSchemaInfo.setTapTablesTableName("all_tables");
		tapSchemaInfo.setTapColumnsTableName("all_columns");
		tapSchemaInfo.setTapKeysTableName("all_keys");
		tapSchemaInfo.setTapFunctionsTableName("all_functions");
		tapSchemaInfo.setTapFunctionsArgumentsTableName("all_functions_arguments");
		return tapSchemaInfo;
	}
	
	protected File getStorageDir(){
		File storageDir = null;
		String storageDirPropertyValue = configuration.getProperty(UwsConfiguration.CONFIG_PROPERTY_STORAGE);
		if(storageDirPropertyValue != null && !storageDirPropertyValue.isEmpty()){
			if(!storageDirPropertyValue.startsWith("${")){
				storageDir = new File(storageDirPropertyValue);
			}
		}
		return storageDir;
	}
	
	public String getAppId(){
		return appid;
	}
	
	public TAP getTap(){
		return tap;
	}
	
	public UwsManager getUwsManager(){
		return uwsManager;
	}

	@Override
	public TAPFactory getFactory() {
		return factory;
	}

	@Override
	public UwsConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public TAPSchemaInfo getTapSchemaInfo(UwsJobOwner user) throws TAPException {
		//Test whether the user has an associated schema info.
		//If not, a clone of the default one is returned.
		if(user != null){
			DBConnection dbConn = factory.createDBConnection("TAP(Owner schema creation)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			try{
				String tapSchemaName = dbConn.getUserTapSchema(user);
				if (tapSchemaName != null){
					TAPSchemaInfo tsi = new TAPSchemaInfo(defaultTapSchemaInfo);
					tsi.setTapSchemaName(tapSchemaName);
					return tsi;
				}
			}finally{
				if(dbConn != null){
					try{
						dbConn.close();
					}catch(Exception e){
						LOG.severe("Cannot close DB connection when retrieving schema for user: " + user.getId());
					}
				}
			}
		}
		return new TAPSchemaInfo(defaultTapSchemaInfo);
	}

	@Override
	public String getVoDataServiceSchemaExtension() {
		return null;
	}

	@Override
	public String getVoFunctionsSchema() {
		return null;
	}

	@Override
	public int[] getRetentionPeriod() {
		//Retention period in ms.
		String s = configuration.getProperty(UwsConfiguration.UWS_JOBS_DELTA_DESTRUCTION_TIME);
		long retentionPeriod = DEFAULT_RETENTION_PERIOD; //in ms.
		if(s != null && !"".equals(s)){
			retentionPeriod = Long.parseLong(s);
		}
		//Return in seconds
		int r = (int)(retentionPeriod / 1000);
		return new int[]{r, r};	// default = max = 7 days (7 * 24 * 3600, time in seconds)
	}

	@Override
	public long[] getExecutionDuration(String listId, UwsJobOwner owner) throws UwsException {
		return factory.getExecutionTimeLimits(listId, owner);
	}

	@Override
	public int[] getOutputLimit() {
		UwsSecurity security = factory.getSecurityManager();
		UwsJobOwner user;
		
		// If registered user do not limit results. Limits are controlled by user's quota.
		try {
			user = security.getUser(UwsConfiguration.IGNORE_USER_SESSION);
			if(user!=null && !user.getId().equals("anonymous")){
				return new int[]{-1,-1};
			}
		} catch (UwsException e) {}
		
		String s = configuration.getProperty(TAP_OUTPUT_LIMIT_ROWS);
		int limit = DEFAULT_OUTPUT_LIMIT_ROWS;
		if(s != null && !"".equals(s)){
			limit = Integer.parseInt(s);
		}
		return new int[]{limit,limit};
	}

	@Override
	public LimitUnit[] getOutputLimitType() {
		return new LimitUnit[]{LimitUnit.Row, LimitUnit.Row};
	}

	@Override
	public int[] getUploadLimit() {
		// If registered user do not limit uploads. Limits are controlled by user's quota.
		UwsSecurity security = factory.getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(UwsConfiguration.IGNORE_USER_SESSION);
			if(user!=null && !user.getId().equals("anonymous")){
				return new int[]{-1,-1};
			}
		} catch (UwsException e) {}
		
		int limit = configuration.getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
		return new int[]{limit,limit};
	}

	@Override
	public LimitUnit[] getUploadLimitType() {
		return new LimitUnit[]{LimitUnit.Byte, LimitUnit.Byte};
	}

	@Override
	public Collection<String> getCoordinateSystems() {
		return coordSys;
	}

	@Override
	public List<String> getUwsJobsToIgnoreParameters() {
		return uwsJobsParametersToIgnore;
	}

	@Override
	public String getProjectTapName() {
		return getProperty(UwsConfiguration.CONFIG_APP_ID);
	}

	@Override
	public List<String> getAvailableSchemas(UwsJobOwner owner) {
		if(owner != null && owner.getAuthUsername() != null){
			String userSchemaName = TAPMetadata.getUserSchema(owner);
			List<String> schemas = new ArrayList<String>();
			schemas.add(userSchemaName);
			return schemas;
		}else{
			return null;
		}
	}

	@Override
	public String getUserUploadTableSpace() {
		return configuration.getProperty(TAPService.CONF_PROP_TAP_USER_UPLOAD_TABLESPACE);
	}

	@Override
	public String getProperty(String propertyName){
		return configuration.getProperty(propertyName);
	}

	@Override
	public String getTapVersion() {
		String tapVersion = tapProperties.getProperty(VERSION_PROP);
		if(tapVersion==null){
			return "n/a";
		}
		return tapVersion;
	}

	protected void loadPropertiesFile(Properties properties, String propFileName) throws TAPException{
		InputStream inputStream = null;
		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new TAPException("Property file '" + propFileName + "' not found in the classpath");
			}
		} catch (IOException e) {
			throw new TAPException("Error reading '" + propFileName + "'",e);
		} finally {
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
