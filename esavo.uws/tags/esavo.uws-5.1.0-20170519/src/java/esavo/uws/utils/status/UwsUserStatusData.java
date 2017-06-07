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
