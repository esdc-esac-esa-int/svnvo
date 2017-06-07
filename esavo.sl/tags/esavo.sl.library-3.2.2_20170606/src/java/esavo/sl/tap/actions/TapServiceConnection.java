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
package esavo.sl.tap.actions;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;

public class TapServiceConnection extends EsacAbstractTapServiceConnection implements EsacTapService {
	
	private static EsacTapService service;
	
	public static synchronized EsacTapService getInstance(String appid) throws UwsException, TAPException{
		if(service == null){
			if(!startUserService(appid)){
				//use default service
				service = new TapServiceConnection(appid);
			}
		}
		return service;
	}
	
	private static boolean startUserService(String appid) throws UwsException, TAPException{
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		String userServiceClass = configuration.getProperty(ESAVO_SL_SERVICE_CLASS);
		if(userServiceClass != null && !userServiceClass.isEmpty()){
			//try to create the service by reflection
			//method: constructor(String appid)
			Class<?> clazz = null;
			try {
				clazz = Class.forName(userServiceClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new UwsException("Service class '"+userServiceClass+"' not found: " + e.getMessage(), e);
			}
			Constructor<?> constructor = null;
			try {
				constructor = clazz.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new UwsException("Service constructor for class '"+userServiceClass+"' not found: " + e.getMessage(), e);
			} catch (SecurityException e) {
				throw new UwsException("Service constructor for class '"+userServiceClass+"' not visible: " + e.getMessage(), e);
			}
			Object obj = null;
			try {
				obj = constructor.newInstance(new Object[]{appid});
			} catch (InstantiationException e) {
				throw new UwsException("Service instantion error for class '"+userServiceClass+"': " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new UwsException("Service illegal access error for class '"+userServiceClass+"': " + e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new UwsException("Service illegal argument error for class '"+userServiceClass+"': " + e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new UwsException("Service invocation error for class '"+userServiceClass+"': " + e.getMessage(), e);
			}
			try{
				service = (EsacTapService) obj;
			}catch (ClassCastException e){
				throw new UwsException("Invalid Service class '"+userServiceClass+"': " + e.getMessage(), e);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private TapServiceConnection(String appid) throws UwsException, TAPException{
		super(appid);
	}

	@Override
	protected void initService() throws UwsException, TAPException {
		File storageDir = getStorageDir();
		TAPFactory factory = new TapServiceFactory(this, getAppId(), storageDir, getConfiguration());
		initService(factory);
	}

}
