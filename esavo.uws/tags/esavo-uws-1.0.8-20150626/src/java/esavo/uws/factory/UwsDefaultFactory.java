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

import esavo.uws.config.UwsConfiguration;
import esavo.uws.creator.UwsDefaultCreator;
import esavo.uws.event.UwsDefaultEventsManager;
import esavo.uws.notifications.UwsDefaultNotificationsManager;
import esavo.uws.output.UwsDefaultOutputHandler;
import esavo.uws.owner.UwsDefaultJobsOwnersManager;
import esavo.uws.security.UwsDefaultSecurityManager;
import esavo.uws.share.UwsDefaultShareManager;
import esavo.uws.storage.fs.UwsStorageFileSystem;
import esavo.uws.storage.jdbc.UwsJdbcStorage;

/**
 * Do not use this class, use {@link UwsSimpleExecutorFactory} because the scheduler and executor must be defined.
 * Creates a set of default managers (this class does not define an executor, scheduler nor uwsManager):
 * <ul>
 * <li>Creator: {@link UwsDefaultCreator}</li>
 * <li>Security manager: {@link UwsDefaultSecurityManager}</li>
 * <li>Storage manager: {@link UwsStorageFileSystem} or {@link UwsJdbcStorage} (depending on property {@link UwsConfiguration#CONFIG_USE_DB})</li>
 * <li>Output manager: {@link UwsDefaultOutputHandler}</li>
 * <li>Jobs Owners manager: {@link UwsDefaultJobsOwnersManager}</li>
 * <li>Share manager: {@link UwsDefaultShareManager}</li>
 * <li>Events manager: {@link UwsDefaultEventsManager}</li>
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public abstract class UwsDefaultFactory extends UwsAbstractFactory implements UwsFactory {
	

	public UwsDefaultFactory(String appid, File storageDir, UwsConfiguration configuration){
		super(appid, storageDir, configuration);
		//events manager should be defined before the creator manager
		eventsManager = new UwsDefaultEventsManager(appid);
		creator = new UwsDefaultCreator(appid);
		securityManager = new UwsDefaultSecurityManager(appid);
		if(useDatabase(configuration)){
			storageManager = new UwsJdbcStorage(appid, storageDir, creator, configuration);
		}else{
			storageManager = new UwsStorageFileSystem(appid, storageDir, creator);
		}
		notificationsManager = new UwsDefaultNotificationsManager(appid, storageManager);
		outputHandler = new UwsDefaultOutputHandler(appid);
		shareManager = new UwsDefaultShareManager(appid, configuration, eventsManager, notificationsManager);
		jobsOwnersManager = new UwsDefaultJobsOwnersManager(appid, storageManager, shareManager);
	}
	
	private boolean useDatabase(UwsConfiguration configuration){
		if(configuration.hasProperty(UwsConfiguration.CONFIG_USE_DB)){
			return Boolean.parseBoolean(configuration.getProperty(UwsConfiguration.CONFIG_USE_DB));
		}
		return false;
	}


}
