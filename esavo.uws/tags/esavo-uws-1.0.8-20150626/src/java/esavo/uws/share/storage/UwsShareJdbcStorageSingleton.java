package esavo.uws.share.storage;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import esavo.uws.config.UwsConfiguration;

/**
 * Database pool creator. See {@link UwsShareJdbcStorage}
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsShareJdbcStorageSingleton {
	
	private static UwsShareJdbcStorageSingleton instance;
    
    private DataSource datasource = null;
    private String appid = null;
    private String properties = null;
    
    /**
     * This is used for test-harnesses.
     * To set a Connection, use {@link #setDummyConnection(Connection)}}
     */
    private static Connection dummyConnection = null;
    
    private UwsShareJdbcStorageSingleton(String appid, UwsConfiguration configuration) {
    	this.appid = appid;
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
    
    public static synchronized UwsShareJdbcStorageSingleton getInstance(String appid, UwsConfiguration configuration) {
    	if(instance == null){
    		instance = new UwsShareJdbcStorageSingleton(appid, configuration);
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
