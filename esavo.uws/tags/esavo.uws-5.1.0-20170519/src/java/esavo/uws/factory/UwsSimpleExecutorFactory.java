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

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.executor.UwsDefaultExecutor;
import esavo.uws.scheduler.UwsDefaultScheduler;

/**
 * Default factory. It defines the following handlers (See {@link UwsDefaultFactory} to obtain a list of other handlers defined):
 * <ul>
 * <li>executor</li>
 * <li>scheduler</li>
 * <li>uwsManager<li>
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsSimpleExecutorFactory extends UwsDefaultFactory {

	/**
	 * Constructor
	 * @param appid
	 * @param storageDir
	 * @param configuration
	 * @throws UwsException 
	 */
	public UwsSimpleExecutorFactory(String appid, File storageDir, UwsConfiguration configuration) throws UwsException {
		super(appid, storageDir, configuration);
		executor = new UwsDefaultExecutor(appid);
		scheduler = new UwsDefaultScheduler(appid);
		uwsManager = UwsManager.getManager(this);
	}

}
