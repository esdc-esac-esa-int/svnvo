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

import java.util.List;
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
//	 * Returns the time associated to an event. -1 if no time for this event type is found (i.e. there are no events of this type raised)<br/>
//	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}<br/>
//	 * @param userid
//	 * @param eventCode
//	 * @return
//	 * @throws UwsException
//	 */
//	public long getTimeForEvent(UwsJobOwner user, int eventTypeCode) throws UwsException;
	
	/**
	 * Updates the time associated to the specified event.<br/>
	 * eventTypeCode must exists in {@link UwsEventTypesRegistry}
	 * @param user
	 * @param eventTypeCode
	 * @throws UwsException
	 */
	public void setEventTime(UwsJobOwner user, int eventTypeCode) throws UwsException;

//	/**
//	 * Returns the time associated to an event. -1 if no time for this event type is found (i.e. there are no events of this type raised)<br/>
//	 * @param userid
//	 * @param eventType
//	 * @return
//	 */
//	public long getTimeForEvent(UwsJobOwner user, UwsEventType eventType);
	
	/**
	 * Returns the events for the user.
	 * After a call to this method, the manager can reorganize or delete the events.
	 * In order to obtain a list of events without modifying the queue, call {@link #reportEvents(UwsJobOwner)}
	 * @param user
	 * @return
	 */
	public Map<Integer,Long> getTimesForEvents(UwsJobOwner user);
	
	/**
	 * DO NOT CALL THIS METHOD. This method should be called by UwsManager only.
	 * Returns the events for the user. The queue is not updated.
	 * The usual way to access events is by calling {@link #getTimesForEvents(UwsJobOwner)}
	 * @param user
	 * @return
	 */
	public Map<Integer,Long> reportEvents(UwsJobOwner user);
	
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
	
	public List<String> getAllSessionsForUser(String userid);
	

}
