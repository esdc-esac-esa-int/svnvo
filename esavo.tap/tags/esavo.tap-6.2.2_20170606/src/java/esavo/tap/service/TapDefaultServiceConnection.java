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
package esavo.tap.service;

import java.io.File;

import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPService;
import esavo.uws.UwsException;

/**
 * Default TAP service connection.
 * <p>In case you want to return different tap_schema for each user, overwrite {@link #getTapSchemaInfo(esavo.uws.owner.UwsJobOwner)}:
 * <pre><tt>
 * 	public TAPSchemaInfo getTapSchemaInfo(UwsJobOwner user) {
 *      switch(user.get...){
 *      case a: ...
 *      case b: ...
 *		default: 
 *           return defaultTapSchemaInfo;
 *      }
 *	}
 * </tt></pre>
 * 
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TapDefaultServiceConnection extends TapAbstractServiceConnection implements TAPService {
	
	private static TapDefaultServiceConnection service;
	
	public static synchronized TapDefaultServiceConnection getInstance(String appid) throws UwsException, TAPException{
		if(service == null){
			service = new TapDefaultServiceConnection(appid);
		}
		return service;
	}
	
	private TapDefaultServiceConnection(String appid) throws UwsException, TAPException{
		super(appid);
	}

	protected void initService() throws UwsException, TAPException{
		File storageDir = getStorageDir();
		TAPFactory factory = new TapDefaultServiceFactory(this, getAppId(), storageDir, getConfiguration());
		initService(factory);
	}

}
