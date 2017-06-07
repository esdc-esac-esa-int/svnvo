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
package esavo.uws.utils;

import java.util.List;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsCleaner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsStorage;

public class UwsDirectoriesCleanerThread  extends Thread {
	
	private static final Logger LOG = Logger.getLogger(UwsDirectoriesCleanerThread.class.getName());
	

	private String appid;
	
	public UwsDirectoriesCleanerThread(String appid){
		this.appid = appid;
	}
	
	public void run(){
		UwsManager manager = UwsUtils.getManager();
		clean(manager);
	}
	
	/**
	 * Clean all orphan jobs and all old empty files of all owners.
	 * @param appid
	 * @throws UwsException
	 */
	private void clean(UwsManager manager) {
		LOG.info("Removing orphan jobs and old empty directories...");
		
		UwsConfiguration config = manager.getFactory().getConfiguration();
		String sDeltaTime = config.getProperty(UwsConfiguration.CONFIG_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME);
		long deltaTime = UwsConfiguration.DEFAULT_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME;
		if(sDeltaTime != null){
			try{
				deltaTime = Long.parseLong(sDeltaTime);
			}catch(NumberFormatException nfe){
				deltaTime =  UwsConfiguration.DEFAULT_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME;
				LOG.severe("Wrong old empty directories comparison time '"+sDeltaTime+"', using default value: " + deltaTime);
			}
		}
		
		UwsStorage storage = manager.getFactory().getStorageManager();
		
		UwsJobsOwnersFilter filter = null; //no filter
		long offset = -1; //no limits
		long limit = -1;
		List<UwsJobOwner> owners;
		try{
			owners = storage.retrieveOwners(filter, offset, limit);
		}catch(UwsException e){
			LOG.severe("Cannot retrieve owners from storage: " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
			return;
		}
		if(owners == null){
			LOG.info("No owners to check.");
			return;
		}
		UwsEventsManager eventsManager = manager.getFactory().getEventsManager();
		for(UwsJobOwner owner: owners){
			try {
				if(UwsJobsCleaner.cleanOwner(storage, owner, appid, deltaTime)){
					eventsManager.setEventTime(owner, UwsEventType.QUOTA_FILE_UPDATED_EVENT);
				}
			} catch (UwsException e) {
				LOG.severe("Cannot check orphan jobs and old empty directoris for owner '"+ owner.getId() +"': "+ e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
			}
		}
		
		LOG.info("Removing orphan jobs and old empty directories finished.");
	}


}
