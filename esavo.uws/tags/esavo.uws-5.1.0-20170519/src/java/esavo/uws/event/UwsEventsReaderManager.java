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
