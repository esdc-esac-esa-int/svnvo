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

import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Manages events (job creation, deletion...)
 * Based on user sessions.
 * When events are read, the events are removed.
 * When an event for a user enters, the event is copied to all user sessions
 * Initializes a default event types registry with jobs and share event types.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsDefaultEventsManagerBySessions extends UwsAbstractEventsManager implements UwsEventsManager {
	
	/**
	 * Map<user_id, Map<session_id, events> >
	 */
	private Map<String, Map<String,UwsEventItem>> usersEventsMapBySession;
	
	public UwsDefaultEventsManagerBySessions(String appid){
		super(appid, UwsAbstractEventsManager.USE_DELETION_THREAD);
		usersEventsMapBySession = new HashMap<String, Map<String,UwsEventItem>>();
	}

//	@Override
//	public synchronized long getTimeForEvent(UwsJobOwner user, UwsEventType eventType){
//		UwsEventItem eventItem = getEventItem(user);
//		return eventItem.getEventTime(eventType, UwsEventItem.CONSUME_EVENT);
//	}
	
	
	@Override
	public Map<Integer, Long> reportEvents(UwsJobOwner user) {
		return getTimesForEvents(user, UwsEventItem.DO_NOT_CONSUME_EVENT);
	}

	@Override
	public Map<Integer,Long> getTimesForEvents(UwsJobOwner user){
		Map<Integer,Long> events = null;
		UwsEventsReaderManager eventsReaderManager = UwsEventsReaderManager.getInstance();
		while(!eventsReaderManager.getStopRequested(user)){
			events = getTimesForEvents(user, UwsEventItem.CONSUME_EVENT);
			if(events.size() > 0){
				break;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				break;
			}
		}
		return events;
	}
	
	private synchronized Map<Integer, Long> getTimesForEvents(UwsJobOwner user, boolean consumeEvents){
		UwsEventItem eventItem = getEventItem(user);
		//if no events are available (already consumed) it returns an empty hashmap
		return eventItem.getEventTimes(consumeEvents);
	}
	
	@Override
	public synchronized void setEventTime(UwsJobOwner user, UwsEventType eventType){
		boolean isLoginEvent = checkLoginEvent(eventType);
		if(UwsUtils.isAnonymous(user) || isLoginEvent){
			//set event for the current session only
			setEventTimeForSingleSession(user, eventType);
		}else{
			//set event for all sessions
			setEventTimeForAllSessions(user, eventType);
		}
	}
	
	/**
	 * Returns 'true' if the event is LOGIN_IN/LOGIN_OUT
	 * @param eventType
	 * @return
	 */
	private boolean checkLoginEvent(UwsEventType eventType){
		int code = eventType.getCode();
		return code == UwsEventType.LOGIN_IN_EVENT || code == UwsEventType.LOGIN_OUT_EVENT;
	}
	
	/**
	 * Sets event for current session only
	 * @param user
	 * @param eventType
	 */
	private void setEventTimeForSingleSession(UwsJobOwner user, UwsEventType eventType){
		//set event for the current session only
		UwsEventItem eventItem = getEventItem(user);
		eventItem.setEvent(eventType);
	}

	/**
	 * Sets event for all user sessions
	 * @param user
	 * @param eventType
	 */
	private void setEventTimeForAllSessions(UwsJobOwner user, UwsEventType eventType){
		//set event for all sessions
		String userid = user.getId();
		String currentSession = getSuitableSessionId(user);
		Map<String, UwsEventItem> eventsBySessions = getEventItemsByUser(userid);
		String session;
		UwsEventItem eventItem;
		boolean sessionFound = false;
		for(Entry<String, UwsEventItem> e: eventsBySessions.entrySet()){
			session = e.getKey();
			if(currentSession.equals(session)){
				sessionFound = true;
			}
			eventItem = getEventItem(user);
			eventItem.setEvent(eventType);
		}
		if(!sessionFound){
			//create session and event
			eventItem = new UwsEventItem(userid);
			eventItem.setEvent(eventType);
			eventsBySessions.put(currentSession, eventItem);
		}
	}
	
	/**
	 * If the user does not exists, a new entry is created.
	 * @param userid
	 * @return
	 */
	private synchronized UwsEventItem getEventItem(UwsJobOwner user){
		String userid = user.getId();
		String session = getSuitableSessionId(user);
		Map<String, UwsEventItem> eventsBySessions = getEventItemsByUser(userid);
		UwsEventItem eventItem = eventsBySessions.get(session);
		if(eventItem == null){
			eventItem = new UwsEventItem(userid);
			eventsBySessions.put(session, eventItem);
		}
		return eventItem;
	}
	
	/**
	 * Returns a map of sessions and events. If the map is not available, a new map is created.
	 * @param userid
	 * @return
	 */
	private synchronized Map<String, UwsEventItem> getEventItemsByUser(String userid){
		Map<String, UwsEventItem> eventsBySessions = usersEventsMapBySession.get(userid);
		if(eventsBySessions == null){
			eventsBySessions = new HashMap<String, UwsEventItem>();
			usersEventsMapBySession.put(userid, eventsBySessions);
		}
		return eventsBySessions;
	}
	
	@Override
	public synchronized void removeEventItem(UwsJobOwner user){
		String userid = user.getId();
		Map<String, UwsEventItem> eventsBySessions = usersEventsMapBySession.get(userid);
		if(eventsBySessions == null){
			return;
		}
		String session = user.getSession();
		if(session != null){
			eventsBySessions.remove(session);
		}
	}
	
	@Override
	public synchronized String checkEventsRemovalProcedure(long deltaDestructionTime) {
		StringBuilder sb = new StringBuilder("Events removal procedure starts...\n");
		long currentTime = System.currentTimeMillis();
		long tmp;
		List<String> toRemove = new ArrayList<String>();
		Map<String, UwsEventItem> items;
		String id;
		for(Entry<String, Map<String,UwsEventItem>> e: usersEventsMapBySession.entrySet()){
			items = e.getValue();
			if(items == null){
				continue;
			}
			id = e.getKey();
			for(Entry<String, UwsEventItem> i: items.entrySet()){
				tmp = i.getValue().getCreationTime() + deltaDestructionTime;
				if(tmp < currentTime){
					toRemove.add(id + "|" + e.getKey());
				}
			}
		}
		String session;
		String user;
		for(String key: toRemove){
			sb.append("Removing events for: ").append(key).append('\n');
			String[] i = key.split("|");
			user = i[0];
			session = i[1];
			usersEventsMapBySession.get(user).remove(session);
		}
		sb.append("Events removal procedure finished.");
		return sb.toString();
	}
	
	private String getSuitableSessionId(UwsJobOwner user){
		String sessionId = user.getSession();
		if(sessionId == null){
			sessionId = user.getId();
		}
		return sessionId;
	}

	
	@Override
	public String toString(){
		return "DefaultEventsManagerBySessions: " + super.toString();
	}

	@Override
	public synchronized List<String> getAllSessionsForUser(String userid) {
		// TODO Auto-generated method stub
		Map<String, UwsEventItem> eventsBySessions = usersEventsMapBySession.get(userid);
		if(eventsBySessions == null){
			return null;
		}
		List<String> sessions = new ArrayList<String>(); 
		for(String session: eventsBySessions.keySet()){
			sessions.add(session);
		}
		return sessions;
	}

}
