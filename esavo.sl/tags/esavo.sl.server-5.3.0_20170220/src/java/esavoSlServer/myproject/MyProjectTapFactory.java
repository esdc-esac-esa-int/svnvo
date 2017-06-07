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

import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceFactory;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;

/**
 * Template class, in case you need a factory.<br/>
 * It is instantiated in MyProjectTapService class.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class MyProjectTapFactory extends TapServiceFactory {

	public MyProjectTapFactory(EsacTapService service, String appid, File storageDir, UwsConfiguration configuration)
			throws NullPointerException, UwsException {
		super(service, appid, storageDir, configuration);
	}

}
