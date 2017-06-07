package esavo.uws.notifications;

import java.util.List;
import java.util.Set;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsNotificationsDeletionManagerThread;
import esavo.uws.utils.UwsUtils;

public class UwsDefaultNotificationsManager implements UwsNotificationsManager {
	
	private String appid;
	private UwsStorage storageManager;

	public UwsDefaultNotificationsManager(String appid, UwsStorage storageManager){
		this.appid = appid;
		this.storageManager = storageManager;

		UwsNotificationsDeletionManagerThread deletionManagerThread = new UwsNotificationsDeletionManagerThread(appid);
		deletionManagerThread.setDaemon(true);
		deletionManagerThread.start();
	}
	
	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}


	@Override
	public String toString(){
		return "NotificationsManager for application " + appid;
	}

	@Override
	public UwsNotificationItem createNotification(int type, int subtype, String msg, Set<String> users) throws UwsException{
		if(type>0 && (users == null || users.size() < 1) ){
			//nobody to notify
			return null;
		}
		String id = UwsUtils.getUniqueIdentifier(appid);
		UwsNotificationItem item = new UwsNotificationItem(id, type, subtype, msg, users);
		storageManager.createNotification(item);
		if(users != null){
			UwsJobOwner ownerTmp;
			for(String userid: users){
				ownerTmp = new UwsJobOwner(userid, 0);
				UwsManager.getInstance().getFactory().getEventsManager().setEventTime(ownerTmp, UwsEventType.NOTIFICATION_CREATED_EVENT);
			}
		}else if(type<0){
			UwsManager.getInstance().getFactory().getEventsManager().setEventTime(UwsJobOwner.ALL_USERS_OWNER, UwsEventType.NOTIFICATION_CREATED_EVENT);
		}
		return item;
	}
	
	@Override
	public List<UwsNotificationItem> getNotificationsForUser(UwsJobOwner user) throws UwsException{
		return storageManager.getNotificationsForUser(user.getId());
	}

	@Override
	public void markNotificationAsRead(UwsJobOwner user, List<String> notificationid) throws UwsException{
		storageManager.deleteNotificationRelation(user.getId(), notificationid);
		UwsManager.getInstance().getFactory().getEventsManager().setEventTime(user, UwsEventType.NOTIFICATION_REMOVED_EVENT);
	}

	@Override
	public int deleteSystemWarningNotifications(List<String> notificationids) throws UwsException{
		return storageManager.deleteSystemWarningNotifications(notificationids);
	}

	@Override
	public synchronized String checkNotificationsRemovalProcedure(long deltaDestructionTime) {
		StringBuilder sb = new StringBuilder("Notifications removal procedure starts...\n");
		long currentTime = System.currentTimeMillis();
		try{
			int removedNotifications = storageManager.deleteNotifications(currentTime, deltaDestructionTime);
			sb.append("Removed: ").append(removedNotifications).append("\n");
		}catch(UwsException e){
			sb.append(e.getMessage());
		}
		sb.append("Notifications removal procedure finished.");
		return sb.toString();
	}


}
