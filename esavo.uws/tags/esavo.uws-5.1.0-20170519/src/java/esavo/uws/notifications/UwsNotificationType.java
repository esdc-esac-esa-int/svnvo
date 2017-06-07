package esavo.uws.notifications;

/**
 * For UWS, reserved from 0 to 999
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsNotificationType {
	
	//Notifications with NEGATIVE CODES are shown to ALL USERS
	
	//System warnings notifications: -1xx
	public static final int SYSTEM_WARNING_NOTIFICATION = -100;
	
}
