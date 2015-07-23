package esavo.uws.event;

/**
 * For UWS, reserved from 0 to 999
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsEventType {
	
	//Events code: Type*100+Subtype
	
	//Jobs related events: 1xx
	public static final int JOB_CREATED_EVENT = 100;
	public static final int JOB_UPDATED_EVENT = 101;
	public static final int JOB_REMOVED_EVENT = 102;
	
	//Share related events: 2xx
	public static final int SHARE_ITEMS_CREATED_EVENT = 210;
	public static final int SHARE_ITEMS_UPDATED_EVENT = 211;
	public static final int SHARE_ITEMS_REMOVED_EVENT = 212;
	
	public static final int SHARE_GROUPS_CREATED_EVENT = 220;
	public static final int SHARE_GROUPS_UPDATED_EVENT = 221;
	public static final int SHARE_GROUPS_REMOVED_EVENT = 222;
	
	public static final int SHARE_USERS_CREATED_EVENT = 230;
	public static final int SHARE_USERS_UPDATED_EVENT = 231;
	public static final int SHARE_USERS_REMOVED_EVENT = 232;
	
	public static final int PUBLIC_GROUP_VIEW_TABLE    = 240;
	public static final int PUBLIC_GROUP_HIDE_TABLE    = 241;

	
	//Login related events: 3xx
	public static final int LOGIN_IN_EVENT = 300;
	public static final int LOGIN_OUT_EVENT = 301;

	//Quota events: 4xx
	public static final int QUOTA_DB_UPDATED_EVENT = 401;
	public static final int QUOTA_FILE_UPDATED_EVENT = 402;
	
	//Notification events: 5xx
	public static final int NOTIFICATION_CREATED_EVENT = 500;
	public static final int NOTIFICATION_REMOVED_EVENT = 501;
	
	//Above 1000: to be used by UWS users
	
	public int getCode();
	public String getDescription();

}
