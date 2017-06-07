package esavo.uws.event;

import java.util.ArrayList;
import java.util.List;

import esavo.uws.owner.UwsJobOwner;

public class UwsEventsReaderManager {
	
	private static UwsEventsReaderManager _instance;

	private List<String> requestsToStop;

	public static synchronized UwsEventsReaderManager getInstance(){
		if(_instance == null){
			_instance = new UwsEventsReaderManager();
		}
		return _instance;
	}
	
	private UwsEventsReaderManager() {
		requestsToStop = new ArrayList<String>();
	}
	
	public void addRequestToStop(UwsJobOwner user){
		addRequestToStop(user.getId(), user.getSession());
	}
	
	public synchronized void addRequestToStop(String userid, String sessionid){
		String id = getSuitableId(userid, sessionid);
		if(requestsToStop.contains(id)){
			return;
		}
		requestsToStop.add(id);
	}
	
	public boolean isStopRequested(UwsJobOwner user){
		return isStopRequested(user.getId(), user.getSession());
	}
	
	public synchronized boolean isStopRequested(String userid, String sessionid){
		String id = getSuitableId(userid, sessionid);
		return requestsToStop.contains(id);
	}
	
	public boolean getStopRequested(UwsJobOwner user){
		return getStopRequested(user.getId(), user.getSession());
	}
	
	public synchronized boolean getStopRequested(String userid, String sessionid){
		String id = getSuitableId(userid, sessionid);
		//returns 'true' if the element exists and it was removed
		//returns 'false' if the element does not exist.
		return requestsToStop.remove(id);
	}
	
	private String getSuitableId(String userid, String sessionid){
		return userid + "|" + sessionid;
	}

}
