package esavo.uws.config;

import javax.servlet.http.HttpServletRequest;

public interface UwsConfiguration {
	
	public static final String VERSION_PROP = "uws.version";
	
	public static final long DEFAULT_DELTA_DESTRUCTION_TIME = 3 * 24 * 60 * 60 * 1000; //3 days in milliseconds
	
	public static final long DEFAULT_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME = 30 * 24 * 60 * 60 * 1000; //30 days is milliseconds

	public static final long DEFAULT_DELTA_EVENTS_REMOVAL_TIME = 24 * 60 * 60 * 1000; //1 days in milliseconds
	
	//Notifications
	public static final long DEFAULT_DELTA_NOTIFICATIONS_REMOVAL_TIME = 30 * 24 * 60 * 60 * 1000; //1 month in milliseconds

	public static final String ASYNC_LIST_ID = "async";
	public static final String SYNC_LIST_ID = "sync";
	public static final String[] DEFAULT_VALID_LIST_IDS = {ASYNC_LIST_ID, SYNC_LIST_ID};
	
	
	public static final String CONFIG_APP_ID = "uws.app.id";
	public static final String CONFIG_PROPERTY_STORAGE = "uws.app.storage_dir";

	public static final String CONFIG_VALID_LIST_IDS = "uws.valid_list_ids";

	//public static final String CONFIG_MAX_RUNNING_JOBS = "uws.max_running_jobs";
	public static final String CONFIG_ASYNC_MAX_RUNNING_JOBS = "uws.async_max_running_jobs";
	public static final String CONFIG_SYNC_MAX_RUNNING_JOBS = "uws.sync_max_running_jobs";
	public static final String CONFIG_PREFIX_MAX_QUEUED_JOBS = "uws.";
	public static final String CONFIG_SUFFIX_MAX_QUEUED_JOBS = "_max_queued_jobs";
	
	//public static final String CONFIG_FIXED_BASE_URL = "uws.fixed_base_url";
	
	//In s.
	public static final String CONFIG_OWNER_ASYNC_EXEC_DURATION_LIMIT_SECONDS = "uws.owner_async_exec_duration_limit";
	public static final String CONFIG_OWNER_SYNC_EXEC_DURATION_LIMIT_SECONDS = "uws.owner_sync_exec_duration_limit";
	
	//public static final String CONFIG_USE_DB    = "uws.db.usage";
	
	//public static final String DB_SERVER_PROP   = "uws.jdbc.dbServer";
	//public static final String DB_PORT_PROP     = "uws.jdbc.dbPort";
	//public static final String DB_NAME_PROP     = "uws.jdbc.dbName";
	public static final String DB_DRIVER_PROP = "uws.jdbc.driver";
	public static final String DB_URL_PROP = "uws.jdbc.url";
	
	public static final String DB_OWNER_PROP    = "uws.jdbc.dbOwner";
	public static final String DB_PWD_PROP      = "uws.jdbc.dbPwd";
	public static final String DB_TIMEOUT_PROP  = "uws.jdbc.dbTimeout";
	public static final String DB_CONNECTIONS_PROP  = "uws.jdbc.dbConnection";
	
	public static final String DB_ID_TIMEOUT = "dbTimeout";
	public static final String DB_ID_MAX_ACTIVE = "dbMaxActive";
	public static final String DB_ID_USER = "dbOwner";
	public static final String DB_ID_PWD = "dbPwd";
	public static final String DB_ID_DRIVER = "driver";
	public static final String DB_ID_URL = "url";
	
	public static final long DEFAULT_DB_TIMEOUT = 1800000;  //(ms) 30 minutes
	public static final int DEFAULT_DB_MAX_ACTIVE = 90;
	
	public static final String CONFIG_UPLOAD_ENABLED = "uws.upload.enabled";

	//In Bytes
	public static final String CONFIG_UPLOAD_MAX_SIZE = "uws.upload.max_size";
	
	//In MB
	public static final String CONFIG_DB_QUOTA = "uws.db.quota";
	//In MB
	public static final String CONFIG_FILES_QUOTA = "uws.files.quota";
	
	public static final String UWS_JOBS_REMOVAL_CHECK_TIME = "uws.jobs_removal_check_time";
	public static final String UWS_JOBS_DELTA_DESTRUCTION_TIME = "uws.jobs_delta_destruction_time";
	
	public static final String CONFIG_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME = "uws.old_empty_dirs_cmp_time";

	public static final String UWS_EVENTS_REMOVAL_CHECK_TIME = "uws.events_removal_check_time";
	public static final String UWS_EVENTS_DELTA_DESTRUCTION_TIME = "uws.events_delta_destruction_time";
	
	public static final String UWS_NOTIFICATIONS_REMOVAL_CHECK_TIME = "uws.notifications_removal_check_time";
	public static final String UWS_NOTIFICATIONS_DELTA_DESTRUCTION_TIME = "uws.notifications_delta_destruction_time";
	
	//LDAP
    public static final String LDAP_SERVER = "uws.ldap.server";
    public static final String LDAP_SEARCH_BASE = "uws.ldap.search_base";
    public static final String LDAP_USERNAME = "uws.ldap.usr";
    public static final String LDAP_PASSWORD = "uws.ldap.pwd";
    
    
    public static final String RESTART_PENDING_JOBS = "uws.restart.pending_jobs";
    
	/**
	 * User home java property.
	 */
	public static final String DEFAULT_DIRECTORY_JAVA_PROPERTY = "user.home";
	
	public static final String DEFAULT_UWS_STORAGE_DIR = ".uws/storage";

	//POOLS
	public static final String UWS_POOL_MANAGEMENT_PROPERTY_PREFIX = "uws.jdbc.management";
	public static final String UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID = "uws_management_pool_storage";
	
	public static final int UWS_JDBC_DEFAULT_MANAGEMENT_CONNECTIONS = 100;
	
	public static final String UWS_CONFIG_PROP_USE_SIMPLE_EVENTS_MANAGER = "uws.use_simple_events_manager";
	
	public static final HttpServletRequest IGNORE_USER_SESSION = null;
	
	public static final String CONFIG_GROUP_ANONYMOUS_JOBS = "uws.anonymous_jobs_grouped";
	public static final boolean DEFAULT_GROUP_ANONYMOUS_JOBS = true;


	/**
	 * Returns the required property. Null if the property does not exist
	 * @param propertyName
	 * @return
	 */
	public String getProperty(String propertyName);
	
	/**
	 * Sets the specified property with the specified value.
	 * @param propertyName
	 * @param propertyValue
	 */
	public void setProperty(String propertyName, String propertyValue);
	
	/**
	 * Checks whether the specified property exists.
	 * @param propertyName
	 * @return
	 */
	public boolean hasProperty(String propertyName);
	
	/**
	 * Checks whether the specified property exists and it is not a '$...' or '@...' value
	 * @param propertyName
	 * @return
	 */
	public boolean hasValidPropertyValue(String propertyName);
	
	/**
	 * Removes all properties.
	 */
	public void clear();

	/**
	 * Returns a long property.
	 * @param propertyName
	 * @return
	 * @throws NumberFormatException
	 */
	public long getLongProperty(String propertyName) throws NumberFormatException;
	
	/**
	 * Returns an integer property.
	 * @param propertyName
	 * @return
	 * @throws NumberFormatException
	 */
	public int getIntProperty(String propertyName) throws NumberFormatException;

	/**
	 * Returns a boolean property.
	 * @param propertyName
	 * @return
	 * @throws NumberFormatException
	 */
	public boolean getBooleanProperty(String propertyName) throws NumberFormatException;
	
	
	/**
	 * Returns the UWS version
	 * @return String Version of UWS library
	 */
	public String getUwsVersion();

}
