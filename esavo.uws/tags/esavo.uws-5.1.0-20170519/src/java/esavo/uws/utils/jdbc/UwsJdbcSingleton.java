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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Database pool creator. See {@link UwsJdbcStorage}
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJdbcSingleton {
	

	//private static Map<String, UwsJdbcStorageSingleton> instances = new HashMap<String, UwsJdbcStorageSingleton>();
	
	//private static UwsJdbcSingleton instance;
    
    private DataSource datasource = null;
    private String appid = null;
    private String properties = null;
    private UwsDatabaseProperties databaseProperties;
    
    /**
     * This is used for test-harnesses.
     * To set a Connection, use {@link #setDummyConnection(Connection)}}
     */
    private Connection dummyConnection = null;
    
    /*package*/ UwsJdbcSingleton(String appid, Connection dummyConnection){
    	this.appid = appid;
    	this.dummyConnection = dummyConnection;
    	this.properties = "DummyConn";
    	this.databaseProperties = new UwsDatabaseProperties("dummy","dummy", "dummy", "dummy");
    }
    
    /**
     * connectionUrl = "jdbc:postgresql://"+server+":"+port+"/"+dbname;
     * driver = "org.postgresql.Driver"
     * @param appid
     * @param creator
     * @param connectionUrl
     * @param driver
     * @param owner
     * @param pwd
     */
    /*package*/ UwsJdbcSingleton(String appid, UwsDatabaseProperties databaseProperties) {
    	this.appid = appid;
    	this.databaseProperties = databaseProperties;
    	if (dummyConnection != null){
    		//if dummyConnection is not null, we do not want to use a real database framework.
    		//No instance is created and 'dummyConnection' is returned when {@link getConnection()} is called.
    		return;
    	}

    	String connectionUrl = databaseProperties.getUrlConnection();
    	String driver = databaseProperties.getDriver();
    	String user = databaseProperties.getUser();
    	String pwd = databaseProperties.getPwd();
    	int initialSize = databaseProperties.getInitialSize();
    	int maxActive = databaseProperties.getMaxActive();
    	long queryTimeout = databaseProperties.getTimeOutSeconds();
    	properties = "UWS Jdbc app '"+appid+"': " + connectionUrl;

    	PoolProperties p = new PoolProperties();
        p.setUrl(connectionUrl);
        p.setDriverClassName(driver);
        p.setUsername(user);
        p.setPassword(pwd);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(maxActive);
        p.setInitialSize(initialSize);
        p.setMaxWait(60000);
        p.setRemoveAbandonedTimeout((int)queryTimeout);
        //p.setRemoveAbandonedTimeout(2400); //40 min.
        p.setMinEvictableIdleTimeMillis(30000);
        // Abandoned connections will be logged but not removed
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(false);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer;"+
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

//    /**
//     * 
//     * @param appid
//     * @param creator
//     * @param connectionUrl
//     * @param driver
//     * @param user
//     * @param pwd
//     * @return
//     */
//    /*package*/ static synchronized UwsJdbcSingleton getInstance(String appid, UwsDatabaseProperties databaseProperties) {
//    	if(instance == null){
//    		instance = new UwsJdbcSingleton(appid, databaseProperties);
//    	}
//    	return instance;
//    }

    /**
     * To be used by test-harnesses.<br/>
     * If connection is not null, this object will be returned when calling {@link #getInstance()}
     * @param connection
     */
    public void setDummyConnection(Connection connection){
    	dummyConnection = connection;
    }

//	public long getDatabaseTimeOut() {
//		return databaseProperties.getTimeOut();
//		//(configuration.hasProperty(UwsConfiguration.DB_TIMEOUT_PROP)) {
//	}
    
    public String getAppId(){
    	return appid;
    }
    
//    public UwsCreator getCreator(){
//    	return databaseProperties.getUwsJobCreator();
//    }

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
    
    /**
     * Detailed information about this pool
     * @return detailed information about this pool
     */
    public String info(){
    	if(datasource != null){
    		int active = datasource.getActive();
    		int wait = datasource.getWaitCount();
    		return "\tappid: '" + appid + "'\n" + databaseProperties.toString() + "\n\tcurrent active: " + active + "\n\tcurrent waiting: " + wait;
    	} else {
    		return databaseProperties == null ? "\tDummy" : "\tappid: '" + appid + "'\n" + databaseProperties.toString();
    	}
    }

	/**
	 * @return the databaseProperties
	 */
	public UwsDatabaseProperties getDatabaseProperties() {
		return databaseProperties;
	}

}
