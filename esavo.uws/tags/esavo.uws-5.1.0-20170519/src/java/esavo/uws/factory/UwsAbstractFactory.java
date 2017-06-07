/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.factory;

import java.io.File;
import java.util.List;

import esavo.uws.UwsException;
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
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;

public abstract class UwsAbstractFactory implements UwsFactory {
	
	public static final int DEFAULT_EXEC_DURATION_LIMIT_SECONDS = 0; //ms. 0=no limit

	private String appid;
	private File storageDir;
	protected UwsConfiguration configuration;
	
	protected UwsManager uwsManager;
	protected UwsSecurity securityManager;
	protected UwsStorage storageManager;
	protected UwsExecutor executor;
	protected UwsScheduler scheduler;
	protected UwsCreator creator;
	protected UwsOutputResponseHandler outputHandler;
	protected UwsJobsOwnersManager jobsOwnersManager;
	protected UwsShareManager shareManager;
	protected UwsEventsManager eventsManager;
	protected UwsNotificationsManager notificationsManager;
	
	/**
	 * Constructor
	 * @param appid application identifier.
	 * @param storageDir storage directory.
	 */
	public UwsAbstractFactory(String appid, File storageDir, UwsConfiguration configuration) {
		this.appid = appid;
		if(!isValidStorageDir(storageDir)){
			this.storageDir = getDefaultStorageDir();
		}else{
			this.storageDir = storageDir;
		}
		this.configuration = configuration;
	}
	
	/**
	 * Checks whether the storage directory is valid.
	 * @param storageDir
	 * @return
	 */
	private boolean isValidStorageDir(File storageDir){
		if(storageDir == null){
			return false;
		}
		String name = storageDir.getName().trim();
		if(name.startsWith("${") && name.endsWith("}")){
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the default uws storage directory: System.property("user.home") + {@link UwsConfiguration#DEFAULT_UWS_STORAGE_DIR}
	 * @return
	 */
	public File getDefaultStorageDir(){
		String userDir = System.getProperty(UwsConfiguration.DEFAULT_DIRECTORY_JAVA_PROPERTY);
		File fTmp = new File(userDir);
		File f = new File(fTmp.getAbsolutePath(), UwsConfiguration.DEFAULT_UWS_STORAGE_DIR);
		return f;
	}

	/**
	 * @return the application identifier.
	 */
	public String getAppId(){
		return appid;
	}
	
	@Override
	public UwsManager getUwsManager(){
		return uwsManager;
	}

	/**
	 * @return the storage directory.
	 */
	public File getStorageDir(){
		return storageDir;
	}

	@Override
	public UwsSecurity getSecurityManager() {
		return securityManager;
	}

	@Override
	public UwsStorage getStorageManager() {
		return storageManager;
	}

	@Override
	public UwsConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public UwsExecutor getExecutor() {
		return executor;
	}
	
	@Override 
	public long[] getExecutionTimeLimits(String listId, UwsJobOwner owner) throws UwsException {
		UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
		
		long limit;
		if("async".equalsIgnoreCase(listId)){
			limit  = quota.getAsyncMaxExecTime();
		} else {
			limit  = quota.getSyncMaxExecTime();
		}
		
		return new long[]{limit,limit};
		
	}

	@Override
	public UwsScheduler getScheduler() {
		return scheduler;
	}

	@Override
	public UwsCreator getCreator() {
		return creator;
	}

	@Override
	public UwsOutputResponseHandler getOutputHandler() {
		return outputHandler;
	}

	@Override
	public UwsJobsOwnersManager getJobsOwnersManager() {
		return jobsOwnersManager;
	}
	
	@Override
	public UwsShareManager getShareManager() {
		return shareManager;
	}
	
	@Override
	public UwsEventsManager getEventsManager() {
		return eventsManager;
	}
	
	@Override
	public UwsNotificationsManager getNotificationsManager() {
		return notificationsManager;
	}
	
	@Override
	public List<UwsShareGroup> getAvailableGroups(String user) {
		return null;
	}
	
	@Override
	public String toString(){
		return "UWS factory for application: '"+appid+"'" +
				"\nStorage: " + (storageManager == null ? "None" : storageManager.toString()) +
				"\nSecurity: " + (securityManager == null ? "None" : securityManager.toString()) +
				"\nExecutor: " + (executor == null ? "None" : executor.toString()) + 
				"\nScheduler: " + (scheduler == null ? "None" : scheduler.toString()) +
				"\nCreator: " + (creator == null ? "None" : creator.toString()) + 
				"\nOutput: " + (outputHandler == null ? "None" : outputHandler.toString()) +
				"\nOwners: " + (jobsOwnersManager == null ? "None" : jobsOwnersManager.toString()) +
				"\nShare: " + (shareManager == null ? "None" : shareManager.toString()) +
				"\nEvents: " + (eventsManager == null ? "None" : eventsManager.toString()) +
				"\nNotifications: " + (notificationsManager == null ? "None" : notificationsManager.toString())
				;
	}

}
