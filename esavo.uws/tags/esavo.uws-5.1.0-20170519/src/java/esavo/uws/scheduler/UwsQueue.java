package esavo.uws.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * User queues are never deleted
 * @author jsegovia
 *
 */
public class UwsQueue {
	
	private List<UwsUserQueue> userQueues;
	private int latestUsedQueueIndex;
	private UwsRoundRobinManager roundRobin;
	
	public UwsQueue(){
		userQueues = new ArrayList<UwsUserQueue>();
		roundRobin = new UwsRoundRobinManager(userQueues.size());
	}
	
	public synchronized void markCheckPoint(){
		roundRobin.markCheckPoint();
	}
	
	public synchronized boolean isCheckPointAchieved(){
		return roundRobin.isCheckPointReached();
	}
	
	public synchronized void addJob(UwsJobThread jobThread, String queuedUser){
		UwsUserQueue userQueue = getUserQueue(queuedUser);
		userQueue.addJob(jobThread);
	}
	
	public synchronized UwsQueueItem popJob(){
		int currentSize = userQueues.size();
		if(currentSize < 1){
			return null;
		}
		
		int rrIndex = roundRobin.getIndex();
		UwsUserQueue userQueue = userQueues.get(rrIndex);
		latestUsedQueueIndex = rrIndex;
		roundRobin.incrementIndex();
		
		UwsQueueItem item = userQueue.pop();
		return item;
	}
	
	private synchronized UwsUserQueue getUserQueue(String queuedUser){
		for(UwsUserQueue userQueue: userQueues){
			if(queuedUser.equals(userQueue.getQueuedUser())){
				return userQueue;
			}
		}
		UwsUserQueue userQueue = new UwsUserQueue(queuedUser);
		userQueues.add(userQueue);
		roundRobin.addOneSlot();
		return userQueue;
	}
	
	public synchronized void unPop(UwsQueueItem item){
		//Users are never deleted from jobsPerUser
		UwsUserQueue userQueue = userQueues.get(latestUsedQueueIndex);
		userQueue.unpop(item);
	}
	
	public synchronized UwsQueueItem removeJob(UwsJobThread jobThread, String queuedUser){
		UwsUserQueue userQueue = getUserQueue(queuedUser);
		return userQueue.removeJob(jobThread);
	}
	
}
