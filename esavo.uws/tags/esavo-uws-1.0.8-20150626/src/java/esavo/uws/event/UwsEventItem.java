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
	
//	public long getOrCreateEventTime(UwsEventType eventType){
//		Long l = events.get(eventType);
//		if(l == null){
//			l = System.currentTimeMillis();
//			events.put(eventType, l);
//		}
//		return l;
//	}
	
	public long getEventTime(UwsEventType eventType){
		Long l = events.get(eventType);
		if (l == null) {
			return -1;
		} else {
			return l;
		}
	}
	
	public Map<Integer, Long> getEventTimes(){
		Map<Integer, Long> times = new HashMap<Integer, Long>();
		for(Entry<UwsEventType, Long> e: events.entrySet()){
			times.put(e.getKey().getCode(), e.getValue());
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
