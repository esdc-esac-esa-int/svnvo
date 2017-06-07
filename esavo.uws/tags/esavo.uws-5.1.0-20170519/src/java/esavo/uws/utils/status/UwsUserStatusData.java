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
package esavo.uws.utils.status;

import java.util.HashMap;
import java.util.Map;

public class UwsUserStatusData {
	
	private Map<String, UwsStatusData> statusMap;
	
	private long lastUpdate;
	private long taskid;
	private UwsUserInfo userInfo;
	
	public UwsUserStatusData(long taskid, UwsUserInfo userInfo){
		this.taskid = taskid;
		this.userInfo = userInfo;
		statusMap = new HashMap<String, UwsStatusData>();
		updateTime();
	}
	
	public long getLastUpdate(){
		return this.lastUpdate;
	}
	
	public long getTaskId(){
		return this.taskid;
	}
	
	public UwsUserInfo getUserInfo(){
		return this.userInfo;
	}
	
	/**
	 * THIS FUNCTION UPDATES ACCESS TIME
	 * @param data
	 */
	public synchronized void updateStatus(UwsStatusData data){
		statusMap.put(data.getType(), data);
		updateTime();
	}
	
	/**
	 * THIS FUNCTION UPDATES ACCESS TIME
	 * @param type
	 * @return
	 */
	public synchronized UwsStatusData getStatus(String type){
		updateTime();
		return statusMap.get(type);
	}
	
	/**
	 * THIS FUNCTION UPDATES ACCESS TIME
	 * The task type is removed
	 * @param type
	 * @return
	 */
	public synchronized UwsStatusData consumeStatus(String type){
		updateTime();
		UwsStatusData sd = statusMap.remove(type);
		return sd;
	}
	
	/**
	 * THIS FUNCTION UPDATES ACCESS TIME
	 * @param type
	 * @return
	 */
	public synchronized Map<String, UwsStatusData> getAllStatus(){
		updateTime();
		Map<String, UwsStatusData> map = new HashMap<String, UwsStatusData>(statusMap);
		return map;
	}
	
	/**
	 * THIS FUNCTION UPDATES ACCESS TIME
	 * @param type
	 * @return
	 */
	public synchronized Map<String, UwsStatusData> consumeAllStatus(){
		updateTime();
		Map<String, UwsStatusData> map = new HashMap<String, UwsStatusData>(statusMap);
		statusMap.clear();
		return map;
	}
	
	private synchronized void updateTime(){
		lastUpdate = System.currentTimeMillis();
	}
	
	@Override
	public String toString(){
		return "Task id: " + this.taskid + ", User info: " + this.userInfo + ", last update: " + this.lastUpdate;
	}

}
