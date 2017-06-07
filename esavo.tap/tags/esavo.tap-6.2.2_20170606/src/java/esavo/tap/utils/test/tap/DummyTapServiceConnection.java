package esavo.tap.utils.test.tap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import esavo.tap.LimitUnit;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.formatter.GzipBinary2VotableFormatter;
import esavo.tap.formatter.JsonFormatter;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.formatter.SVFormat;
import esavo.tap.formatter.SVFormatter;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.resource.TAP;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class DummyTapServiceConnection implements TAPService{
	
	public static final String TAP_URL_BASE = "esac.archive.gacs.sl.tap.actions.tapUrlBase";

	public static final String TAP_VODATASERVICE_SCHEMA_EXTENSION = "esac.archive.gacs.sl.tap.actions.voDataServiceSchemaExtension";
	
	public static final String TAP_VOFUNCTIONS_SCHEMA = "esac.archive.gacs.sl.tap.actions.voFunctionsSchema";

	public static final String TAP_SCHEMAS_USE_VIEWS =  "esac.archive.gacs.sl.tap.actions.tapSchemaUseViews";
	
	//public static final String TAP_JOBS_REMOVAL_CHECK_TIME = "esac.archive.gacs.sl.tap.actions.tapJobsRemovalCheckTime";
	
	//public static final String TAP_JOBS_OLDER_THAN_TIME = "esac.archive.gacs.sl.tap.actions.tapJobsOlderThanTime";

	
	public static final String APP_ID = "esac.archive.gacs.sl.tap.app.id";
	
	public static final String TAP_TEMP_LOCATION_PROP = "esac.archive.gacs.sl.tap.actions.tapTempLocation";
	//public static final String TAP_JOBS_MANAGER_ID = "esac.archive.gacs.sl.tap.actions.jobsManagerIdentifier";
	
	public static final String TAP_APP_PATH_PROP = "esac.archive.gacs.sl.tap.actions.tapAppPath";
	public static final String TAP_STAT_GRAPHS_DIRECTORY = "esac.archive.gacs.sl.tap.actions.statGraphsDirectory";
	public static final String TAP_ASYNC_THREADS = "esac.archive.gacs.sl.tap.TapServiceConfiguration.asyncThreadsNumber";
	
	public static final String UPLOAD_DIR_PROPERTY = "esac.archive.gacs.sl.fileupload.FileUploadServletConfig.uploadDir";

	public static final String MANAGER_DEFAULT_QUOTA_DB_PROP = "esac.archive.gacs.sl.tap.actions.Manager.quotaDb"; 
	public static final String MANAGER_DEFAULT_QUOTA_FILE_PROP = "esac.archive.gacs.sl.tap.actions.Manager.quotaFile";
	
	//
	//public static final int DEFAULT_UPLOAD_LIMIT_ROWS = 10000;
	//public static final int DEFAULT_OUTPUT_LIMIT_ROWS = 100000;

	public static final String TAP_OUTPUT_LIMIT_ROWS = "esac.archive.gacs.sl.tap.output_limit_rows";
	public static final int DEFAULT_OUTPUT_LIMIT_ROWS = -1; //disabled
	
	public static final long DEFAULT_RETENTION_PERIOD = 259200000; //ms. 3 days (= 3*24*60*60*1000=259200000)
	public static final long DEFAULT_EXEC_DURATION_LIMIT = 0; //ms. 0=no limit
	
	//public static final boolean USE_STORAGE_DATABASE = true;
	//public static final boolean USE_STORAGE_FILESYSTEM = false;
	
	private String appid;
	private DummyTapServiceFactory factory;
	private String voDataServiceSchemaExtension;
	private String voFunctionsSchema;
	private TAP tap;
	private List<String> coordSys;
	private TAPSchemaInfo tapSchemaInfo;
	private List<String> uwsJobsParametersToIgnore;
	private String userUploadTableSpace;
	
	public DummyTapServiceConnection(String appid, StorageType storageType) throws UwsException, TAPException{
		this.appid = appid;
		
		uwsJobsParametersToIgnore = new ArrayList<String>();
		uwsJobsParametersToIgnore.add("session");
		
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		//set the storage
		
		File f = new File(appid);
		if(f.exists()){
			clearStorage(f);
		}
		f.mkdirs();
		configuration.setProperty(UwsConfiguration.CONFIG_PROPERTY_STORAGE, f.getAbsolutePath());
		
		File storageDir = new File(configuration.getProperty(UwsConfiguration.CONFIG_PROPERTY_STORAGE));
		
		//create factory
		//factory = new TapServiceFactory(this, appid, storageDir, configuration);
		factory = new DummyTapServiceFactory(this, appid, storageDir, configuration, storageType);
		
		//initialize manager (this is done just once)
		//uwsManager = UwsManager.getManager(factory);
		//UwsManager uwsManager = factory.getUwsManager();
		
		//UwsConfiguration config = uwsManager.getFactory().getConfiguration();
		
		//this.defaultHostPort = configuration.getProperty(TAP_URL_BASE);
		this.voDataServiceSchemaExtension = configuration.getProperty(TAP_VODATASERVICE_SCHEMA_EXTENSION);
		this.voFunctionsSchema = configuration.getProperty(TAP_VOFUNCTIONS_SCHEMA);
		this.tapSchemaInfo = createTapSchemaInfo(configuration);
		
		// List all allowed coordinate systems:
		coordSys = new ArrayList<String>(2);
		coordSys.add("ICRS");
		coordSys.add("ICRS BARYCENTER");
		

		//String checkRemovalTime = configuration.getProperty(TapServiceConnection.TAP_JOBS_REMOVAL_CHECK_TIME);
		//String olderThanTime = configuration.getProperty(TapServiceConnection.TAP_JOBS_OLDER_THAN_TIME);

		//tapJobsDeletionManager = new TapJobsDeletionManager(appid, checkRemovalTime, olderThanTime, this);
		
		tap = new TAP(this);

	}
	
//	public DummyDatabaseConnection getDatabaseConnection(){
//		return factory.getDatabaseConnection();
//	}
	
	public void clearStorage(){
		File storageDir = getFactory().getStorageDir();
		clearStorage(storageDir);
	}
	
	private void clearStorage(File f){
		if(f == null){
			return;
		}
		if(f.isDirectory()){
			for(File fTmp: f.listFiles()){
				clearStorage(fTmp);
			}
		}
		f.delete();
	}
	
	private TAPSchemaInfo createTapSchemaInfo(UwsConfiguration config){
		TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();

		String tmpValue = config.getProperty(TAP_SCHEMAS_USE_VIEWS);
		if(tmpValue != null && Boolean.parseBoolean(tmpValue)){
			tapSchemaInfo.setTapSchemasTableName("all_schemas");
			tapSchemaInfo.setTapTablesTableName("all_tables");
			tapSchemaInfo.setTapColumnsTableName("all_columns");
			tapSchemaInfo.setTapKeysTableName("all_keys");
			tapSchemaInfo.setTapFunctionsTableName("all_functions");
			tapSchemaInfo.setTapFunctionsArgumentsTableName("all_functions_arguments");
		}

		return tapSchemaInfo;
	}

	
	public TAP getTap(){
		return tap;
	}

	/* ****************** */
	/* COORDINATE SYSTEMS */
	/* ****************** */
	@Override
	public Collection<String> getCoordinateSystems() {
		return coordSys;
	}


	/* ********************* */
	/* SERVICE CONFIGURATION */
	/* ********************* */

	@Override
	public int[] getRetentionPeriod() {
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(appid);
		//Retention period in ms.
		//String s = config.getProperty(TapServiceConnection.TAP_JOBS_OLDER_THAN_TIME);
		String s = config.getProperty(UwsConfiguration.UWS_JOBS_DELTA_DESTRUCTION_TIME);
		long retentionPeriod = DEFAULT_RETENTION_PERIOD; //in ms.
		if(s != null && !"".equals(s)){
			retentionPeriod = Long.parseLong(s);
		}
		//Return in seconds
		int r = (int)(retentionPeriod / 1000);
		return new int[]{r, r};	// default = max = 7 days (7 * 24 * 3600, time in seconds)
		//return new int[]{60,60}; //test: 1 minute
	}
	
	@Override
	public int[] getOutputLimit() {
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(appid);
		String s = config.getProperty(TAP_OUTPUT_LIMIT_ROWS);
		int limit = DEFAULT_OUTPUT_LIMIT_ROWS;
		if(s != null && !"".equals(s)){
			limit = Integer.parseInt(s);
		}
		return new int[]{limit,limit};
	}

	@Override
	public LimitUnit[] getOutputLimitType() {
		return new LimitUnit[]{LimitUnit.Row, LimitUnit.Row};
		//return new LimitUnit[]{LimitUnit.Byte, LimitUnit.Byte};
	}

	@Override
	public TAPFactory getFactory() {
		return factory;
	}
	
	@Override
	public UwsConfiguration getConfiguration(){
		return factory.getConfiguration();
	}

	
	@Override
	public int[] getUploadLimit() {
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(appid);
//		String s = config.getProperty(TapServiceConnection.TAP_UPLOAD_LIMIT_ROWS);
//		int limit = TapServiceConnection.DEFAULT_UPLOAD_LIMIT_ROWS;
//		if(s != null && !"".equals(s)){
//			limit = Integer.parseInt(s);
//		}
		int limit = config.getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
		return new int[]{limit,limit};
	}

	@Override
	public LimitUnit[] getUploadLimitType() {
		//return new LimitUnit[]{LimitUnit.Row, LimitUnit.Row};
		return new LimitUnit[]{LimitUnit.Byte, LimitUnit.Byte};
	}

//	@Override
//	public Iterator<OutputFormat> getOutputFormats() {
//		return Arrays.asList(formats).iterator();
//	}

	@Override
	public TAPSchemaInfo getTapSchemaInfo(UwsJobOwner user){
		return tapSchemaInfo;
	}

	@Override
	public String getVoDataServiceSchemaExtension() {
		return voDataServiceSchemaExtension;
	}

	public String getProperty(String propertyName){
		UwsConfiguration config = getFactory().getConfiguration();
		return config.getProperty(propertyName);
	}

	@Override
	public List<String> getUwsJobsToIgnoreParameters() {
		return uwsJobsParametersToIgnore;
	}

	@Override
	public String getVoFunctionsSchema() {
		return voFunctionsSchema;
	}

	@Override
	public String getProjectTapName() {
		return appid;
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
		return userUploadTableSpace;
	}
	
	public void setUserUploadTableSpace(String userUploadTableSpace) {
		this.userUploadTableSpace = userUploadTableSpace;
	}

	@Override
	public long[] getExecutionDuration(String listId, UwsJobOwner owner)
			throws UwsException {
		return factory.getExecutionTimeLimits(listId, owner);

	}

	@Override
	public String getTapVersion() {
		return "TEST";
	}
	
}
