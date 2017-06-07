package esavo.uws.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Manages events (job creation, deletion
 * Initializes a default event types registry with jobs and share event types.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsDefaultEventsManager extends UwsAbstractEventsManager implements UwsEventsManager {
	
	private Map<String, UwsEventItem> usersEventsMap;
	
	public UwsDefaultEventsManager(String appid){
		super(appid, UwsAbstractEventsManager.USE_DELETION_THREAD);
		usersEventsMap = new HashMap<String, UwsEventItem>();
	}

//	@Override
//	public synchronized long getTimeForEvent(UwsJobOwner user, UwsEventType eventType){
//		String userid = getSuitableUserId(user); 
//		UwsEventItem eventItem = getEventItem(userid);
//		return eventItem.getEventTime(eventType, UwsEventItem.DO_NOT_CONSUME_EVENT);
//	}
	

	@Override
	public Map<Integer, Long> reportEvents(UwsJobOwner user) {
		return getTimesForEvents(user);
	}

	
	@Override
	public synchronized Map<Integer,Long> getTimesForEvents(UwsJobOwner user){
		String userid = getSuitableUserId(user); 
		UwsEventItem eventItem = getEventItem(userid);
		return eventItem.getEventTimes(UwsEventItem.DO_NOT_CONSUME_EVENT);
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

	
	/**
	 * If the user is anonymous, returns 'anonymous|session_id'. The user identifier is returned otherwise.
	 * @param user
	 * @return
	 */
	static String getSuitableUserId(UwsJobOwner user){
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
		return "DefaultEventsManager: " + super.toString();
	}

	@Override
	public List<String> getAllSessionsForUser(String userid) {
		return null;
	}

}
