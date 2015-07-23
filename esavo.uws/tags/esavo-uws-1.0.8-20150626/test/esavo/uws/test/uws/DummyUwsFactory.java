package esavo.uws.test.uws;

import java.io.File;

import esavo.uws.UwsJobsListManager;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.creator.UwsDefaultCreator;
import esavo.uws.factory.UwsAbstractFactory;
import esavo.uws.factory.UwsFactory;
import esavo.uws.output.UwsDefaultOutputHandler;
import esavo.uws.owner.UwsDefaultJobsOwnersManager;
import esavo.uws.storage.fs.UwsStorageFileSystem;
import esavo.uws.storage.jdbc.UwsJdbcStorage;
import esavo.uws.storage.jdbc.UwsJdbcStorageSingleton;
import esavo.uws.test.database.DummyDatabaseConnection;

public class DummyUwsFactory extends UwsAbstractFactory implements UwsFactory {
	
	public enum StorageType{
		database,
		file,
		fake
	}

	private DummyDatabaseConnection uwsDbConnection;
	
	public DummyUwsFactory(String appid, File storageDir, UwsConfiguration configuration, StorageType storageType) {
		super(appid, storageDir, configuration);
		//should be defined before creator.
		eventsManager = new DummyUwsEventsManager();
		creator = new UwsDefaultCreator(appid);
		securityManager = new DummyUwsSecurityManager();
		switch(storageType){
		case database:
			uwsDbConnection = new DummyDatabaseConnection();
			UwsJdbcStorageSingleton.setDummyConnection(uwsDbConnection);
			storageManager = new UwsJdbcStorage(appid, storageDir, creator, configuration);
			break;
		case file:
			storageManager = new UwsStorageFileSystem(appid, storageDir, creator);
			break;
		default:
			//File uploadDir = new File(storageDir, "__UPLOADS__");
			//uploadDir.mkdirs();
			storageManager = new DummyUwsStorageManager(storageDir);
			break;
		}
		executor = new DummyUwsExecutor();
		scheduler = new DummyUwsScheduler((DummyUwsExecutor)executor);
		outputHandler = new UwsDefaultOutputHandler(appid);
		shareManager = new DummyUwsShareManager();
		jobsOwnersManager = new UwsDefaultJobsOwnersManager(appid, storageManager,shareManager);
		notificationsManager = new DummyUwsNotificationsManager();
		//force new manager
		UwsManager.createManager(this);
	}

	public DummyDatabaseConnection getDatabaseConnection(){
		return uwsDbConnection;
	}
	
	public void clear(){
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(getAppId());
		config.clear();
		reset();
	}
	
	public void reset(){
		String appid = getAppId();
		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid);
		listManager.reset();
		((DummyUwsExecutor)executor).reset();
		if(storageManager instanceof DummyUwsStorageManager){
			((DummyUwsStorageManager)storageManager).reset();
		}
		((DummyUwsScheduler)scheduler).reset();
		((DummyUwsSecurityManager)securityManager).reset();
	}




	
}
