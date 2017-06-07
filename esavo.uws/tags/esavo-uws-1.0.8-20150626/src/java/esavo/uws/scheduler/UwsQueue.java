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
package esavo.uws.scheduler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UwsQueue {
	
	//Each user has a job queue
	private Map<String, List<UwsQueueItem>> jobs;
	
	public UwsQueue(){
		jobs = new LinkedHashMap<String, List<UwsQueueItem>>();
	}
	
	/**
	 * queueUser can be different than the owner user.<br/>
	 * For instance, if the user is anonymous, it is necessary to append the session id because they are different users.
	 * @param jobThread
	 * @param queueUser
	 */
	public synchronized void addJob(UwsJobThread jobThread, String queueUser){
		List<UwsQueueItem> userJobs = getJobList(queueUser);
		userJobs.add(new UwsQueueItem(queueUser, jobThread));
	}
	
	public synchronized UwsQueueItem popFistJob(String queueUser){
		List<UwsQueueItem> userJobs = jobs.get(queueUser);
		if(userJobs == null){
			return null;
		}else{
			if(userJobs.size() > 0){
				return userJobs.remove(0);
			}else{
				return null;
			}
		}
	}
	
	public synchronized List<UwsQueueItem> popFirstJobs(){
		List<UwsQueueItem> jobsPerUser = new ArrayList<UwsQueueItem>();
		//jobs is a LinkedhashMap: it preserves the order.
		for(String queueUser: jobs.keySet()){
			jobsPerUser.add(popFistJob(queueUser));
		}
		return jobsPerUser;
	}
	
	private synchronized List<UwsQueueItem> getJobList(String queueUser){
		List<UwsQueueItem> userJobs = jobs.get(queueUser);
		if(userJobs == null){
			userJobs = new ArrayList<UwsQueueItem>();
			jobs.put(queueUser, userJobs);
		}
		return userJobs;
	}
	
	public synchronized void unpop(UwsQueueItem item){
		if(item == null){
			return;
		}
		List<UwsQueueItem> jobsPerUser = getJobList(item.getQueueUser());
		if(jobsPerUser.size() == 0){
			jobsPerUser.add(item);
		}else{
			jobsPerUser.add(0, item);
		}
	}
	
	public synchronized UwsQueueItem removeJob(UwsJobThread jobThread, String queueUser){
		List<UwsQueueItem> userJobs = getJobList(queueUser);
		int index = findJob(userJobs, jobThread.getJob().getJobId());
		if(index < 0){
			return null;
		}else{
			return userJobs.remove(index);
		}
	}
	
	private int findJob(List<UwsQueueItem> jobs, String id){
		UwsQueueItem item;
		for(int i = 0; i < jobs.size(); i++){
			item = jobs.get(i);
			if(id.equals(item.getJobThread().getJob().getJobId())){
				return i;
			}
		}
		return -1;
	}
	
	
}
