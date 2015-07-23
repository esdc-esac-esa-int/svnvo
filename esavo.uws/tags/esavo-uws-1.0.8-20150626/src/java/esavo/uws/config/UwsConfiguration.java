package esavo.uws.config;

public interface UwsConfiguration {
	
	public static final String VERSION = "1.0.7_20150326";
	
	public static final long DEFAULT_DELTA_DESTRUCTION_TIME = 3 * 24 * 60 * 60 * 1000; //3 days in milliseconds
	
	public static final long DEFAULT_OLD_EMPTY_DIRECTORIES_COMPARISON_TIME = 30 * 24 * 60 * 60 * 1000; //30 days is milliseconds

	public static final long DEFAULT_DELTA_EVENTS_REMOVAL_TIME = 24 * 60 * 60 * 1000; //1 days in milliseconds
	
	//Notifications
	public static final long DEFAULT_DELTA_NOTIFICATIONS_REMOVAL_TIME = 30 * 24 * 60 * 60 * 1000; //1 month in milliseconds
	
	
	public static final String CONFIG_APP_ID = "uws.app.id";
	public static final String CONFIG_PROPERTY_STORAGE = "uws.app.storage_dir";
	
	public static final String CONFIG_MAX_RUNNING_JOBS = "uws.max_running_jobs";
	
	//public static final String CONFIG_FIXED_BASE_URL = "uws.fixed_base_url";
	
	//In ms.
	public static final String CONFIG_EXEC_DURATION_LIMIT = "uws.exec_duration_limit";
	
	public static final String CONFIG_USE_DB    = "uws.db.usage";
	
	public static final String DB_SERVER_PROP   = "uws.jdbc.dbServer";
	public static final String DB_PORT_PROP     = "uws.jdbc.dbPort";
	public static final String DB_NAME_PROP     = "uws.jdbc.dbName";
	public static final String DB_OWNER_PROP    = "uws.jdbc.dbOwner";
	public static final String DB_PWD_PROP      = "uws.jdbc.dbPwd";
	
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

}
