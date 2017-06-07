package esavo.tap;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsDefaultEventType;
import esavo.uws.event.UwsEventTypesRegistry;
import esavo.uws.factory.UwsDefaultFactory;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.utils.jdbc.UwsDatabaseProperties;
import esavo.uws.utils.jdbc.UwsJdbcManager;
import esavo.uws.utils.jdbc.UwsJdbcSingleton;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.QueryChecker;
import esavo.tap.db.DBConnection;
import esavo.tap.db.TapJDBCPooledConnection;
import esavo.tap.formatter.FitsFormatter;
import esavo.tap.formatter.GzipBinary2VotableFormatter;
import esavo.tap.formatter.JsonFormatter;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.formatter.SVFormat;
import esavo.tap.formatter.SVFormatter;
import esavo.tap.formatter.TapDefaultOutputHandler;
import esavo.tap.formatter.VoTableFormatter;
import esavo.tap.metadata.TAPMetadataWriter;
import esavo.tap.metadata.TAPMetadataWriterJson;
import esavo.tap.metadata.TAPMetadataWriterXml;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.upload.Uploader;

public abstract class AbstractTAPFactory extends UwsDefaultFactory implements TAPFactory {

	protected final TAPService service;
	protected UwsJobOwner owner;
	
	private OutputFormat[] formats;
	
	private TAPMetadataWriter[] tapMetadataWriters;


	protected AbstractTAPFactory(TAPService service, String appid, File storageDir, UwsConfiguration configuration) throws NullPointerException {
		super(appid, storageDir, configuration);

		this.service = service;
		
		outputHandler = new TapDefaultOutputHandler(service,appid);
		
		// List all available outputs (VOTable & CSV):
		formats = new OutputFormat[5];
		//formats[0] = new ResultSet2VotableFormatter(this);
		formats[0] = new GzipBinary2VotableFormatter(service);
		formats[0].setShortMimeType("votable");
		formats[1] = new VoTableFormatter(service);
		formats[2] = new SVFormatter(service,SVFormat.COMMA_SEPARATOR);
		formats[3] = new JsonFormatter(service);
		formats[4] = new FitsFormatter(service);
		
		tapMetadataWriters = new TAPMetadataWriter[2];
		tapMetadataWriters[0] = new TAPMetadataWriterXml(service);
		tapMetadataWriters[1] = new TAPMetadataWriterJson();
		
		
		
		UwsDefaultEventType uet;
		UwsEventTypesRegistry eventsTypesRegistry = getEventsManager().getEventsTypeRegistry();
		uet = new UwsDefaultEventType(TAPFactory.TABLE_CREATED_EVENT, "Table created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(TAPFactory.TABLE_UPDATED_EVENT, "Table updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(TAPFactory.TABLE_REMOVED_EVENT, "Table removed event");
		eventsTypesRegistry.register(uet);
		
		//sets job http parameters handler (handles UPLOAD, PARAM, URI...)
		getCreator().addJobHttpParametersHandler(new TapJobHttpParametersHandler());

		//TAP creator
		jobsOwnersManager = new TapDefaultJobsOwnersManager(appid, storageManager, shareManager, this, service);

		
		// Job pools creation
		//Jobs Sync
		UwsDatabaseProperties databasePropertiesJobsSync = new UwsDatabaseProperties(configuration);
		databasePropertiesJobsSync.setMaxActive(TAPService.TAP_JDBC_DEFAULT_JOBS_SYNC_CONNECTIONS);
		databasePropertiesJobsSync.updateProperties(configuration, TAPService.TAP_POOL_JOBS_SYNC_PROPERTY_PREFIX);
		UwsJdbcManager.createInstance(appid, TAPService.TAP_JDBC_STORAGE_JOBS_SYNC_POOL_ID, databasePropertiesJobsSync);

		//Jobs Async
		UwsDatabaseProperties databasePropertiesJobsAsync = new UwsDatabaseProperties(configuration);
		databasePropertiesJobsAsync.setMaxActive(TAPService.TAP_JDBC_DEFAULT_JOBS_ASYNC_CONNECTIONS);
		databasePropertiesJobsAsync.updateProperties(configuration, TAPService.TAP_POOL_JOBS_ASYNC_PROPERTY_PREFIX);
		UwsJdbcManager.createInstance(appid, TAPService.TAP_JDBC_STORAGE_JOBS_ASYNC_POOL_ID, databasePropertiesJobsAsync);
		
		

	}

	@Override
	public long[] getExecutionTimeLimits(String listId, UwsJobOwner owner) throws esavo.uws.UwsException {
		long[] uwsLimits = super.getExecutionTimeLimits(listId, owner);
		
		
		UwsJdbcSingleton pool;
		
		if("async".equalsIgnoreCase(listId)){
			pool=UwsJdbcManager.getInstance(TAPService.TAP_JDBC_STORAGE_JOBS_ASYNC_POOL_ID);
		}else{
			pool=UwsJdbcManager.getInstance(TAPService.TAP_JDBC_STORAGE_JOBS_SYNC_POOL_ID);
		}
		
		long dbTimeout = pool.getDatabaseProperties().getTimeOutSeconds();
		
		long[] limits = new long[2];
		
		limits[0] = Math.min(uwsLimits[0], dbTimeout);
		limits[1] = Math.min(uwsLimits[1], dbTimeout);
		
		return limits;
	};
	
	@Override
	public DBConnection createDBConnection(String jobID, String poolid) throws TAPException {
		UwsJdbcSingleton poolManager = UwsJdbcManager.getInstance(poolid);
		UwsDatabaseProperties databaseProperties = poolManager.getDatabaseProperties();
		try {
			return new TapJDBCPooledConnection(service, poolManager.getConnection(), databaseProperties.getTimeOutMillis());
		} catch (SQLException e) {
			throw new TAPException(e);
		}
	}
	
	@Override
	public ADQLQueryFactory createQueryFactory(UwsJobOwner owner) throws TAPException{
		this.owner = owner;
		return new ADQLQueryFactory();
	}

	@Override
	public QueryChecker createQueryChecker(TAPSchema uploadSchema, UwsJobOwner owner, boolean includeAccessibleSharedItems) throws TAPException {
		return new DBCheckerOnDemand(service, owner, includeAccessibleSharedItems, uploadSchema);
	}
	
	public Uploader createUploader() throws TAPException {
		return new Uploader(service);
	}
	
	public OutputFormat getOutputFormat(String format){
		for(OutputFormat f : formats){
			if (f.getMimeType().equalsIgnoreCase(format) || f.getShortMimeType().equalsIgnoreCase(format))
				return f;
		}
		return null;

	}
	
	@Override
	public TAPMetadataWriter getMetadataWriter(String format){
		if(format==null || format.trim().isEmpty()){
			return tapMetadataWriters[0];
		}
		for(TAPMetadataWriter writer : tapMetadataWriters){
			if (writer.getFormat().equalsIgnoreCase(format)){
				return writer;
			}
		}
		return tapMetadataWriters[0];

	}
	
	public OutputFormat[] getOutputFormats(){
		return formats;
	}
	
	public void setOutputFormats(OutputFormat[] formats){
		this.formats=formats;
	}

	@Override
	public List<UwsShareGroup> getAvailableGroups(String user) {
		//TODO uncomment when public group capability is available
//		String id = getConfiguration().getProperty(TAPService.PUBLIC_GROUP_ID_PROPERTY);
//		String title = "Public Group";
//		String description = "Puglic Group";
//		String creator = getConfiguration().getProperty(TAPService.PUBLIC_GROUP_OWNER_ID_PROPERTY);
//		UwsShareGroup group = new UwsShareGroup(id, title, description, creator);
//		List<UwsShareGroup> groups = new ArrayList<UwsShareGroup>();
//		groups.add(group);
//		return groups;
		
		return null;
	}
}
