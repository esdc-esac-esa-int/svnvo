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
package esavoSlServer.myproject;

import java.io.File;

import esavo.sl.tap.actions.EsacAbstractTapServiceConnection;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceFactory;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.uws.UwsException;

/**
 * THIS CLASS IS NOT USED
 * <p>If you want to use a class like this one, to custom your service,
 * you must specify this class by using the property:<br/>
 * <code>esavo.sl.service.class</code>
 * <p>Example:
 * <pre><tt>
 * esavo.sl.service.class=myproject.MyProjectTapService
 * </tt></pre>
 * <p>The framework will create this class as a service by reflection.
 * <p>You may create your own factory
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class MyProjectTapService extends EsacAbstractTapServiceConnection implements EsacTapService {
	
	public MyProjectTapService(String appid) throws UwsException, TAPException{
		super(appid);
	}

	@Override
	protected void initService() throws UwsException, TAPException {
		File storageDir = getStorageDir();
		//In case you want to use your own factory:
		//TAPFactory factory = new MyProjectTapFactory();
		TAPFactory factory = new TapServiceFactory(this, getAppId(), storageDir, getConfiguration());
		initService(factory);
	}

}
