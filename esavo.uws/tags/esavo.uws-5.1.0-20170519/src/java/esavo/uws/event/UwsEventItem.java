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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UwsEventItem {
	
	public static final boolean CONSUME_EVENT = true;
	public static final boolean DO_NOT_CONSUME_EVENT = false;
	
	private String userid;
	private Map<UwsEventType, Long> events;
	private long creationTime;
	
	public UwsEventItem(String userid){
		this.userid = userid;
		this.events = new HashMap<UwsEventType, Long>();
		this.creationTime = System.currentTimeMillis();
	}
	
	public boolean hasEvent(UwsEventType eventType){
		return events.containsKey(eventType);
	}
	
	public long getEventTime(UwsEventType eventType, boolean consume){
		Long l = events.get(eventType);
		if (l == null) {
			return -1;
		} else {
			if(consume){
				events.remove(eventType);
			}
			return l;
		}
	}
	
	public Map<Integer, Long> getEventTimes(boolean consume){
		Map<Integer, Long> times = new HashMap<Integer, Long>();
		for(Entry<UwsEventType, Long> e: events.entrySet()){
			times.put(e.getKey().getCode(), e.getValue());
		}
		if(consume){
			events.clear();
		}
		return times;
	}
	
	public void setEvent(UwsEventType eventType){
		events.put(eventType, System.currentTimeMillis());
	}
	
	public String getUser(){
		return userid;
	}
	
	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String toString(){
		return userid + " (created on '"+creationTime+"'). Events: " + events.toString();
	}

}
