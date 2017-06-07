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

import java.util.HashMap;
import java.util.Map;

public class UwsDefaultConfiguration implements UwsConfiguration {
	
	private Map<String, String> properties;
	private String appid;
	
	public UwsDefaultConfiguration(String appid){
		this.appid = appid;
		properties = new HashMap<String, String>();
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return properties.containsKey(propertyName);
	}
	
	@Override
	public long getLongProperty(String propertyName) throws NumberFormatException{
		return Long.parseLong(properties.get(propertyName));
	}

	@Override
	public int getIntProperty(String propertyName) throws NumberFormatException{
		return Integer.parseInt(properties.get(propertyName));
	}

	@Override
	public String toString(){
		return "Configuration properties: " + properties.size();
	}

	@Override
	public void clear() {
		properties.clear();
	}
}
