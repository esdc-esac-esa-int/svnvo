package esavo.uws.notifications;

import java.util.List;
import java.util.Set;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

public interface UwsNotificationsManager {

	/**
	 * 
	 * @param type
	 * @param msg
	 * @param users
	 * @return
	 * @throws UwsException
	 */
	public UwsNotificationItem createNotification(int type, int subtype, String msg, Set<String> users) throws UwsException;

	/**
	 * 
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public List<UwsNotificationItem> getNotificationsForUser(UwsJobOwner user) throws UwsException;
	
	/**
	 * 
	 * @param userid
	 * @param notificationid
	 * @throws UwsException
	 */
	public void markNotificationAsRead(UwsJobOwner user, List<String> notificationid) throws UwsException;
	
	
	/**
	 * 
	 * @param deltaDestructionTime
	 * @return
	 */
	public String checkNotificationsRemovalProcedure(long deltaDestructionTime);
	

}
