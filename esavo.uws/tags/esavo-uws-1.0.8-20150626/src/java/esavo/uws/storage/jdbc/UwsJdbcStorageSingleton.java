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
package esavo.uws.storage.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import esavo.uws.config.UwsConfiguration;
import esavo.uws.creator.UwsCreator;

/**
 * Database pool creator. See {@link UwsJdbcStorage}
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJdbcStorageSingleton {
	

	//private static Map<String, UwsJdbcStorageSingleton> instances = new HashMap<String, UwsJdbcStorageSingleton>();
	
	private static UwsJdbcStorageSingleton instance;
    
    private DataSource datasource = null;
    private String appid = null;
    private UwsCreator creator = null;
    private String properties = null;
    
    /**
     * This is used for test-harnesses.
     * To set a Connection, use {@link #setDummyConnection(Connection)}}
     */
    private static Connection dummyConnection = null;
    
    private UwsJdbcStorageSingleton(String appid, UwsConfiguration configuration, UwsCreator creator) {
    	this.appid = appid;
    	this.creator = creator;
    	if (dummyConnection != null){
    		//if dummyConnection is not null, we do not want to use a real database framework.
    		//No instance is created and 'dummyConnection' is returned when {@link getConnection()} is called.
    		return;
    	}

    	String server = configuration.getProperty(UwsConfiguration.DB_SERVER_PROP);
    	String port = configuration.getProperty(UwsConfiguration.DB_PORT_PROP);
    	String dbname = configuration.getProperty(UwsConfiguration.DB_NAME_PROP);
    	String owner = configuration.getProperty(UwsConfiguration.DB_OWNER_PROP);
    	String pwd = configuration.getProperty(UwsConfiguration.DB_PWD_PROP);

    	String connectionUrl = "jdbc:postgresql://"+server+":"+port+"/"+dbname; 
    	
    	properties = "UWS Jdbc app '"+appid+"': " + connectionUrl;

    	PoolProperties p = new PoolProperties();
        p.setUrl(connectionUrl);
        p.setDriverClassName("org.postgresql.Driver");
        p.setUsername(owner);
        p.setPassword(pwd);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(90);
        p.setInitialSize(10);
        p.setMaxWait(60000);
        //p.setRemoveAbandonedTimeout(300); //5 min.
        p.setRemoveAbandonedTimeout(2400); //40 min.
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        datasource= new DataSource();
        datasource.setPoolProperties(p); 	
    	
    }
    
//    public static synchronized UwsJdbcStorageSingleton getInstance(String appid, UwsConfiguration configuration, UwsCreator creator) {
//    	UwsJdbcStorageSingleton pool = instances.get(appid);
//    	if(pool == null){
//    		pool = new UwsJdbcStorageSingleton(appid, configuration, creator);
//    		instances.put(appid, pool);
//    	}
//    	return pool;
//    }

    public static synchronized UwsJdbcStorageSingleton getInstance(String appid, UwsConfiguration configuration, UwsCreator creator) {
    	if(instance == null){
    		instance = new UwsJdbcStorageSingleton(appid, configuration, creator);
    	}
    	return instance;
    }

    /**
     * To be used by test-harnesses.<br/>
     * If connection is not null, this object will be returned when calling {@link #getInstance()}
     * @param connection
     */
    public static void setDummyConnection(Connection connection){
    	dummyConnection = connection;
    }

    
    public String getAppId(){
    	return appid;
    }
    
    public UwsCreator getCreator(){
    	return creator;
    }

    // functionality methods
    
    public Connection getConnection() throws SQLException{
    	if (dummyConnection != null) {
    		return dummyConnection;
    	} else {
    		return this.datasource.getConnection();
    	}
    }
    
    @Override
    public String toString(){
    	return properties;
    }

}
