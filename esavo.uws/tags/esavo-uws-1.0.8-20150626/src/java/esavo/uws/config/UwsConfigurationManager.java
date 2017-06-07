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
package esavo.uws.config;

public class UwsConfigurationManager {
	
	//private static Map<String, UwsConfiguration> configurations = new HashMap<String, UwsConfiguration>();
	private static UwsConfiguration config;
	
	public static synchronized UwsConfiguration getConfiguration(String appid){
		//UwsConfiguration config = configurations.get(appid);
		if(config == null){
			config = new UwsDefaultConfiguration(appid);
			//configurations.put(appid, config);
		}
		return config;
	}

}
