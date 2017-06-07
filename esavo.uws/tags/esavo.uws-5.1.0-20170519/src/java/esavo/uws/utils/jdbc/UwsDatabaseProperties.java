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
package esavo.uws.utils.jdbc;

import esavo.uws.config.UwsConfiguration;
import esavo.uws.creator.UwsCreator;
import esavo.uws.utils.UwsUtils;

public class UwsDatabaseProperties {
	
	private String driver;
	private String user;
	private String pwd;
	private String urlConnection;
	private UwsCreator uwsJobCreator;
	private long timeOut;
	private int initialSize;
	private int maxActive;

	public UwsDatabaseProperties(UwsConfiguration configuration){
		this(configuration.getProperty(UwsConfiguration.DB_DRIVER_PROP),
				configuration.getProperty(UwsConfiguration.DB_URL_PROP),
				configuration.getProperty(UwsConfiguration.DB_OWNER_PROP),
				configuration.getProperty(UwsConfiguration.DB_PWD_PROP));
	}

	
	public UwsDatabaseProperties(String driver, String urlConnection, String user, String pwd){
		this.driver = driver;
		this.urlConnection = urlConnection;
		this.user = user;
		this.pwd = pwd;
		timeOut = UwsConfiguration.DEFAULT_DB_TIMEOUT;
		maxActive = UwsConfiguration.DEFAULT_DB_MAX_ACTIVE;
		initialSize = 1;
	}
	
	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the pwd
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * @return the urlConnection
	 */
	public String getUrlConnection() {
		return urlConnection;
	}

	/**
	 * @param urlConnection the urlConnection to set
	 */
	public void setUrlConnection(String urlConnection) {
		this.urlConnection = urlConnection;
	}

	/**
	 * @return the uwsJobCreator
	 */
	public UwsCreator getUwsJobCreator() {
		return uwsJobCreator;
	}

	/**
	 * @param uwsJobCreator the uwsJobCreator to set
	 */
	public void setUwsJobCreator(UwsCreator uwsJobCreator) {
		this.uwsJobCreator = uwsJobCreator;
	}

	/**
	 * @return the timeOut in ms
	 */
	public long getTimeOutSeconds() {
		return timeOut;
	}

	/**
	 * @return the timeOut in ms
	 */
	public long getTimeOutMillis() {
		return timeOut*1000;
	}
	
	/**
	 * @param timeOut the timeOut to set in ms
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}
	
	
	/**
	 * Initial pool size
	 * @return
	 */
	public int getInitialSize() {
		return initialSize;
	}


	/**
	 * Set initial pool size
	 * @return
	 */
	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}


	/**
	 * @return the maxActive
	 */
	public int getMaxActive() {
		return maxActive;
	}

	/**
	 * @param maxActive the maxActive to set
	 */
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	
	/**
	 * Updates properties based on the provided prefix
	 * @param configuration
	 * @param prefix
	 */
	public void updateProperties(UwsConfiguration configuration, String prefix){
		String prop;
		String value;
		//driver
		prop = prefix + "." + UwsConfiguration.DB_ID_DRIVER;
		if(hasProperty(configuration, prop)){
			value = configuration.getProperty(prop);
			setDriver(value);
		}
		//url
		prop =  prefix + "." + UwsConfiguration.DB_ID_URL;
		if(hasProperty(configuration, prop)){
			value = configuration.getProperty(prop);
			setUrlConnection(value);
		}
		//user
		prop =  prefix + "." + UwsConfiguration.DB_ID_USER;
		if(hasProperty(configuration, prop)){
			value = configuration.getProperty(prop);
			setUser(value);
		}
		//pwd
		prop =  prefix + "." + UwsConfiguration.DB_ID_PWD;
		if(hasProperty(configuration, prop)){
			value = configuration.getProperty(prop);
			setPwd(value);
		}
		//max active
		prop =  prefix + "." + UwsConfiguration.DB_ID_MAX_ACTIVE;
		if(hasProperty(configuration, prop)){
			int maxActive = UwsUtils.getIntFromProperty(configuration, prop, this.maxActive);
			setMaxActive(maxActive);
		}
		//timeout
		prop =  prefix + "." + UwsConfiguration.DB_ID_TIMEOUT;
		if(hasProperty(configuration, prop)){
			long timeOut = UwsUtils.getLongFromProperty(configuration, prop, this.timeOut);
			setTimeOut(timeOut);
		}
	}
	
	private boolean hasProperty(UwsConfiguration configuration, String id){
		if(!configuration.hasProperty(id)){
			return false;
		}
		String value = configuration.getProperty(id);
		if(value.startsWith("@") || value.startsWith("$") || value.isEmpty()){
			return false;
		}else{
			return true;
		}
	}
	
	@Override
	public String toString(){
		return "\tUrl: " + urlConnection + "\n\tuser: " + user + "\n\tDriver: " + driver + "\n\ttimeout: " + timeOut + "\n\tmaxActive: " + maxActive;  
	}
	
}
