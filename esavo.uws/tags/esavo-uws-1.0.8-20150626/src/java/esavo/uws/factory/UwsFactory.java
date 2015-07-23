package esavo.uws.factory;

import java.io.File;
import java.util.List;

import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.creator.UwsCreator;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.notifications.UwsNotificationsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.scheduler.UwsScheduler;
import esavo.uws.security.UwsSecurity;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareManager;
import esavo.uws.storage.UwsStorage;

public interface UwsFactory {
	
	/**
	 * @return the application identifier.
	 */
	public String getAppId();
	
	/**
	 * 
	 * @return the storage location (where results and job/owner metadata are saved).
	 */
	public File getStorageDir();

	/**
	 * 
	 * @return the UWS manager
	 */
	public UwsManager getUwsManager();
	
	/**
	 * @return the security manager
	 */
	public UwsSecurity getSecurityManager();

	/**
	 * @return the storage manager
	 */
	public UwsStorage getStorageManager();

	/**
	 * @return the configuration
	 */
	public UwsConfiguration getConfiguration();

	/**
	 * Returns the suitable executor depending on the job parameters.<br/>
	 * @return the executor
	 */
	public UwsExecutor getExecutor();

	/**
	 * @return the scheduler
	 */
	public UwsScheduler getScheduler();
	
	/**
	 * @return the creator for jobs.
	 */
	public UwsCreator getCreator();
	
	/**
	 * @return the output handler
	 */
	public UwsOutputResponseHandler getOutputHandler();
	
	/**
	 * @return the jobs owners manager
	 */
	public UwsJobsOwnersManager getJobsOwnersManager();
	
	/**
	 * @return the share manager
	 */
	public UwsShareManager getShareManager();
	
	/**
	 * 
	 * @return the events manager
	 */
	public UwsEventsManager getEventsManager();
	
	/**
	 * 
	 * @return the notifications manager
	 */
	public UwsNotificationsManager getNotificationsManager();
	
	
	/**
	 * Returns a list of groups to be added to the list of groups the user belongs to (extra groups)
	 * @param user
	 * @return
	 */
	public List<UwsShareGroup> getAvailableGroups(String userid);


}
