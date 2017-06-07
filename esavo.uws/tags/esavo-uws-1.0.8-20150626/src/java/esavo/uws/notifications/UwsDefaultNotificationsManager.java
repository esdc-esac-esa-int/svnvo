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
		if(users == null || users.size() < 1){
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
