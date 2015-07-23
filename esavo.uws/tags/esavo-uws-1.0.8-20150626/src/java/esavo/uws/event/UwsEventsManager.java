package esavo.uws.event;

import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

/**
 * Manages UWS events.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsEventsManager {
	
//	/**
//	 * Returns the time associated to an event. If the event is not found, a event is created and the current time is set to it.<br/>
//	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}
//	 * @param userid
//	 * @param eventCode
//	 * @return
//	 * @throws UwsException
//	 */
//	public long getOrCreateTimeForEvent(String userid, int eventTypeCode) throws UwsException;
	
	/**
	 * Returns the time associated to an event. -1 if no time for this event type is found (i.e. there are no events of this type raised)<br/>
	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}<br/>
	 * @param userid
	 * @param eventCode
	 * @return
	 * @throws UwsException
	 */
	public long getTimeForEvent(UwsJobOwner user, int eventTypeCode) throws UwsException;
	
//	/**
//	 * Updates the time associated to the specified event.<br/>
//	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}
//	 * @param userid
//	 * @param eventTypeCode
//	 * @throws UwsException
//	 */
//	public void setEventTime(String userid, int eventTypeCode) throws UwsException;

	/**
	 * Updates the time associated to the specified event.<br/>
	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}
	 * @param user
	 * @param eventTypeCode
	 * @throws UwsException
	 */
	public void setEventTime(UwsJobOwner user, int eventTypeCode) throws UwsException;
	
//	/**
//	 * Returns the time associated to an event. If the event is not found, a event is created and the current time is set to it.
//	 * @param userid
//	 * @param eventType
//	 * @return
//	 */
//	public long getOrCreateTimeForEvent(String userid, UwsEventType eventType);
	
	/**
	 * Returns the time associated to an event. -1 if no time for this event type is found (i.e. there are no events of this type raised)<br/>
	 * @param userid
	 * @param eventType
	 * @return
	 */
	public long getTimeForEvent(UwsJobOwner user, UwsEventType eventType);
	
	public Map<Integer,Long> getTimesForEvents(UwsJobOwner user);
	
	/**
	 * Updates the time associated to the specified event.
	 * @param userid
	 * @param eventType
	 */
	public void setEventTime(UwsJobOwner user, UwsEventType eventType);
	
	/**
	 * Removes all the events related to the specified user.
	 * @param userid
	 */
	public void removeEventItem(UwsJobOwner user);
	
	/**
	 * Returns the events types registry
	 * @return
	 */
	public UwsEventTypesRegistry getEventsTypeRegistry();
	
	/**
	 * Old events removal procedure.
	 * @param deltaDestructionTime to check whether events creation times are old enough to be removed.
	 * @return
	 */
	public String checkEventsRemovalProcedure(long deltaDestructionTime);
	

}
