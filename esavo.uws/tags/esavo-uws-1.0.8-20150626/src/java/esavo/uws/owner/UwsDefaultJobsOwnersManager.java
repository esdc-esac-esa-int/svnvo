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
package esavo.uws.owner;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareUser;
import esavo.uws.storage.UwsStorage;

public class UwsDefaultJobsOwnersManager implements UwsJobsOwnersManager {
	
	public static final int DEFAULT_QUOTA_DB = 100000000; //Bytes
	public static final int DEFAULT_QUOTA_FILE = 1000000000; //Bytes

	private String appid;
	private UwsStorage storage;
	private UwsShareManager shareManager;
	
	public UwsDefaultJobsOwnersManager(String appid, UwsStorage storage, UwsShareManager shareManager){
		this.appid = appid;
		this.storage = storage;
		this.shareManager = shareManager;
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public synchronized UwsJobOwner loadOrCreateOwner(String ownerid) throws UwsException {
		//getOwnerIfAvailable returns 'null' if the user is not found (no exception is raised)
		UwsJobOwner owner = storage.getOwnerIfAvailable(ownerid);
		if(owner == null){
			owner = createDefaultOwner(ownerid, appid);
			storage.addOwner(owner);
		} else {
			UwsShareUser shareUser = shareManager.getSharedUser(ownerid);
			if(shareUser != null){
				owner.setName(shareUser.getName());
			}
		}
		return owner;
	}

	@Override
	public synchronized boolean updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException {
		storage.updateOwnerParameter(owner, parameterName);
		return true;
	}

	@Override
	public boolean updateOwnerRoles(UwsJobOwner owner) throws UwsException {
		storage.updateOwnerRoles(owner);
		return true;
	}
	
	
	public static UwsJobOwner createDefaultOwner(String ownerid, String appid) throws UwsException{
		UwsJobOwner owner = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsJobOwnerParameters parameters = owner.getParameters();
		if(parameters == null){
			parameters = new UwsJobOwnerParameters();
			owner.setParameters(parameters);
		}
		
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(appid);
		long l;
		
		//quota db
		l = getLongFromProperty(config, UwsConfiguration.CONFIG_DB_QUOTA, DEFAULT_QUOTA_DB);
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA, new Long(l));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE, new Long(0));
		
		//quota files
		l = getLongFromProperty(config, UwsConfiguration.CONFIG_FILES_QUOTA, DEFAULT_QUOTA_FILE);
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA, new Long(l));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE, new Long(0));
		
		return owner;
	}

	private static long getLongFromProperty(UwsConfiguration config, String propertyName, long defaultValue) {
		String s = config.getProperty(propertyName);
		if (s == null) {
			return defaultValue;
		} else {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException nfe) {
				return defaultValue;
			}
		}
	}

	@Override
	public String toString(){
		return "Default Owners Manager for application '"+appid+"'";
	}

}
