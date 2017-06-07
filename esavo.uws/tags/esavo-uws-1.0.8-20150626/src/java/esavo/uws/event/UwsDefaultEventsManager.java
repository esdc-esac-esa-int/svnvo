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
package esavo.uws.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsEventsDeletionManagerThread;
import esavo.uws.utils.UwsUtils;

/**
 * Manages events (job creation, deletion
 * Initializes a default event types registry with jobs and share event types.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsDefaultEventsManager implements UwsEventsManager {
	
	private String appid;
	private Map<String, UwsEventItem> usersEventsMap;
	private UwsEventTypesRegistry eventsTypesRegistry;
	
	public UwsDefaultEventsManager(String appid){
		this.appid = appid;
		usersEventsMap = new HashMap<String, UwsEventItem>();
		eventsTypesRegistry = new UwsEventTypesRegistry();
		initializeDefaultRegistry();
		
		UwsEventsDeletionManagerThread deletionManagerThread = new UwsEventsDeletionManagerThread(appid);
		deletionManagerThread.setDaemon(true);
		deletionManagerThread.start();
	}

	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}
	
//	@Override
//	public synchronized long getOrCreateTimeForEvent(String userid, int eventTypeCode) throws UwsException {
//		UwsEventType eventType = getEventType(eventTypeCode);
//		if(eventType == null){
//			throw new UwsException("Event type code '"+eventTypeCode+"' not found in registry.");
//		}
//		return getOrCreateTimeForEvent(userid, eventType);
//	}
	
	@Override
	public synchronized long getTimeForEvent(UwsJobOwner user, int eventTypeCode) throws UwsException {
		UwsEventType eventType = getEventType(eventTypeCode);
		if(eventType == null){
			throw new UwsException("Event type code '"+eventTypeCode+"' not found in registry.");
		}
		return getTimeForEvent(user, eventType);
	}
	
//	@Override
//	public synchronized long getOrCreateTimeForEvent(String userid, UwsEventType eventType){
//		UwsEventItem eventItem = getEventItem(userid);
//		return eventItem.getOrCreateEventTime(eventType);
//	}
	
	@Override
	public synchronized long getTimeForEvent(UwsJobOwner user, UwsEventType eventType){
		String userid = getSuitableUserId(user); 
		UwsEventItem eventItem = getEventItem(userid);
		return eventItem.getEventTime(eventType);
	}
	
	@Override
	public synchronized Map<Integer,Long> getTimesForEvents(UwsJobOwner user){
		String userid = getSuitableUserId(user); 
		UwsEventItem eventItem = getEventItem(userid);
		return eventItem.getEventTimes();
	}
	
	@Override
	public synchronized void setEventTime(UwsJobOwner user, int eventTypeCode) throws UwsException {
		UwsEventType eventType = getEventType(eventTypeCode);
		if(eventType == null){
			throw new UwsException("Event type code '"+eventTypeCode+"' not found in registry.");
		}
		setEventTime(user, eventType);
	}
	
	@Override
	public synchronized void setEventTime(UwsJobOwner user, UwsEventType eventType){
		String userid = getSuitableUserId(user); 
		UwsEventItem eventItem = getEventItem(userid);
		eventItem.setEvent(eventType);
	}
	
	/**
	 * If the user does not exists, a new entry is created.
	 * @param userid
	 * @return
	 */
	private synchronized UwsEventItem getEventItem(String userid){
		UwsEventItem eventItem = usersEventsMap.get(userid);
		if(eventItem == null){
			eventItem = new UwsEventItem(userid);
			usersEventsMap.put(userid, eventItem);
		}
		return eventItem;
	}
	
	@Override
	public synchronized void removeEventItem(UwsJobOwner user){
		String userid = getSuitableUserId(user); 
		usersEventsMap.remove(userid);
	}
	
	@Override
	public UwsEventTypesRegistry getEventsTypeRegistry() {
		return eventsTypesRegistry;
	}
	
	@Override
	public synchronized String checkEventsRemovalProcedure(long deltaDestructionTime) {
		StringBuilder sb = new StringBuilder("Events removal procedure starts...\n");
		long currentTime = System.currentTimeMillis();
		long tmp;
		List<String> toRemove = new ArrayList<String>();
		for(Entry<String, UwsEventItem> e: usersEventsMap.entrySet()){
			tmp = e.getValue().getCreationTime() + deltaDestructionTime;
			if(tmp < currentTime){
				toRemove.add(e.getKey());
			}
		}
		for(String id: toRemove){
			sb.append("Removing events for: ").append(id).append('\n');
			usersEventsMap.remove(id);
		}
		sb.append("Events removal procedure finished.");
		return sb.toString();
	}

	private void initializeDefaultRegistry(){
		UwsEventType uet;

		//Jobs
		uet = new UwsDefaultEventType(UwsEventType.JOB_CREATED_EVENT, "Job created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.JOB_UPDATED_EVENT, "Job updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.JOB_REMOVED_EVENT, "Job removed event");
		eventsTypesRegistry.register(uet);

		//Sharing
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_CREATED_EVENT, "Share items created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_UPDATED_EVENT, "Share items updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_REMOVED_EVENT, "Share items removed event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_CREATED_EVENT, "Share groups created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_UPDATED_EVENT, "Share groups updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_REMOVED_EVENT, "Share groups removed event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_CREATED_EVENT, "Share users created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_UPDATED_EVENT, "Share users updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_REMOVED_EVENT, "Share users removed event");
		eventsTypesRegistry.register(uet);
		
		//Public group
		uet = new UwsDefaultEventType(UwsEventType.PUBLIC_GROUP_VIEW_TABLE, "View public group item event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.PUBLIC_GROUP_HIDE_TABLE, "Hide public group item event");
		eventsTypesRegistry.register(uet);
		
		
		//Login
		uet = new UwsDefaultEventType(UwsEventType.LOGIN_IN_EVENT, "Login in event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.LOGIN_OUT_EVENT, "Login out event");
		eventsTypesRegistry.register(uet);
		
		//Quota
		uet = new UwsDefaultEventType(UwsEventType.QUOTA_DB_UPDATED_EVENT, "Quota database event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.QUOTA_FILE_UPDATED_EVENT, "Quota file event");
		eventsTypesRegistry.register(uet);
		

//		//Tables this is not part of UWS. It belongs to TAP
//		uet = new UwsDefaultEventType(UwsEventType.TABLE_CREATED_EVENT, "Table created event");
//		eventsTypesRegistry.register(uet);
//		uet = new UwsDefaultEventType(UwsEventType.TABLE_UPDATED_EVENT, "Table updated event");
//		eventsTypesRegistry.register(uet);
//		uet = new UwsDefaultEventType(UwsEventType.TABLE_REMOVED_EVENT, "Table removed event");
//		eventsTypesRegistry.register(uet);
		
		//Notifications
		uet = new UwsDefaultEventType(UwsEventType.NOTIFICATION_CREATED_EVENT, "Notification created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.NOTIFICATION_REMOVED_EVENT, "Notification removed event");
		eventsTypesRegistry.register(uet);
	}
	
	public UwsEventType getEventType(int code){
		return eventsTypesRegistry.getEventType(code);
	}
	
	/**
	 * If the user is anonymous, returns 'anonymous|session_id'. The user identifier is returned otherwise.
	 * @param user
	 * @return
	 */
	public static String getSuitableUserId(UwsJobOwner user){
		if(user == null){
			return null;
		}
		String userid = user.getId();
		if(UwsUtils.isAnonymous(userid)){
			String session = user.getSession();
			if(session == null || "".equals(userid)){
				return userid;
			}else{
				return userid+"|"+session;
			}
		}else{
			return userid;
		}
	}

	
	@Override
	public String toString(){
		return "EventsManager for application " + appid + ".\n" + eventsTypesRegistry.toString();
	}

}
