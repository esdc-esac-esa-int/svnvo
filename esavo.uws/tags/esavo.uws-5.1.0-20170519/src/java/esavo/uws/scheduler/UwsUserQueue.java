package esavo.uws.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jsegovia
 *
 */
public class UwsUserQueue {
	
	/**
	 * queuedUser is not jobOwner. queuedUser for anonymous can be grouped by IP, for instance. 
	 */
	private String queuedUser;
	private List<UwsQueueItem> jobs;
	
	public UwsUserQueue(String queuedUser){
		this.queuedUser = queuedUser;
		jobs = new ArrayList<UwsQueueItem>();
	}
	
	public String getQueuedUser(){
		return queuedUser;
	}
	
	public void addJob(UwsJobThread jt){
		UwsQueueItem qi = new UwsQueueItem(jt, queuedUser);
		jobs.add(qi);
	}
	
	public UwsQueueItem removeJob(UwsJobThread jobThread){
		int index = findJob(jobThread.getJob().getJobId());
		if(index < 0){
			return null;
		}else{
			return jobs.remove(index);
		}

	}
	
	public UwsQueueItem pop(){
		if(jobs.size() > 0){
			return jobs.remove(0);
		} else {
			return null;
		}
	}
	
	public void unpop(UwsQueueItem item){
		if(jobs.size() == 0){
			jobs.add(item);
		}else{
			jobs.add(0, item);
		}
	}
	
	private int findJob(String id){
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
