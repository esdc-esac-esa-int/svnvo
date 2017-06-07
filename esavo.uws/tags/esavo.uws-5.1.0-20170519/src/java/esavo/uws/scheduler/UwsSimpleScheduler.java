package esavo.uws.scheduler;

import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class UwsSimpleScheduler extends UwsAbstractScheduler implements UwsScheduler {
	
	public static final long DEFAULT_CHECK_TIME = 1000;
	
	public static final String PRIORITY_QUEUE_IDENTIFIER = "__UWS_PRIORITY_1_ID";
	
	public static final int DEFAULT_PRIORITY = 2; //Normal priority.
	
	private static final Logger LOG = Logger.getLogger(UwsSimpleScheduler.class.getName());
	
	private UwsQueue queuePriority;
	private UwsQueue queueNormal;
	//private int rrIndex;
	private String listid;
	private boolean groupAnonymousJobs;

	/**
	 * Constructor
	 * @param appid
	 */
	
	public UwsSimpleScheduler(String appid, String listid, String maxRunningJobsPropertyId){
		super(appid, maxRunningJobsPropertyId);
		this.queuePriority = new UwsQueue();
		this.queueNormal = new UwsQueue();
		this.listid = listid;
		//this.jobsCounter = null;
		//this.rrIndex = 0;
		this.groupAnonymousJobs = UwsUtils.getGroupAnonymousJobs(appid);
	}
	
	@Override
	public synchronized boolean notifyJobArrival(UwsJobThread jobThread) throws UwsException {
		boolean result = enqueue(jobThread);
		if(result){
			executeSchedulerProcedure();
		}
		return result;
	}

	@Override
	public synchronized boolean notifyJobFinished(UwsJobThread jobThread) {
		executeSchedulerProcedure();
		return true;
	}

	private synchronized boolean enqueue(UwsJobThread jobThread) throws UwsException {
		UwsJob job = jobThread.getJob();
		int priority = job.getPriority();
		job.setPhase(UwsJobPhase.QUEUED);
		switch(priority){
		case 0:
			//run now:
			jobThread.startIfRequired();
			break;
		case 1:
			//priority queue
			queuePriority.addJob(jobThread, PRIORITY_QUEUE_IDENTIFIER);
			break;
		case 2:
		default:
			String queueUser = getValidUserQueueIdentifier(jobThread.getJob().getOwner());
			queueNormal.addJob(jobThread, queueUser);
			break;
		}
		
		return true;
	}
	
	private synchronized void executeSchedulerProcedure(){
		SchedulerMode mode = getSchedulerMode();
		if(mode == SchedulerMode.NONE){
			return;
		}
		
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(getAppId(), listid);
		int currentJobsRunning = jobsListManager.getNumberOfRunningJobs();
		int maxJobsRunning = getMaxRunningJobs();
		
		int counter = currentJobsRunning;
		
		//1. all jobs in queuePriority must be launched
		counter = runQueue(queuePriority, counter, maxJobsRunning);
		//2. Algorithm to schedule level 2 jobs
		runQueue(queueNormal, counter, maxJobsRunning);
	}

	
//	private synchronized void executeSchedulerProcedure(){
//		if(getSchedulerMode()==SchedulerMode.NONE){
//			return;
//		}
//		
//		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(getAppId(), listid);
//		int currentJobsRunning = jobsListManager.getNumberOfRunningJobs();
//		int maxJobsRunning = getMaxRunningJobs(maxRunningJobsPropertyId);
//		
//		int counter = currentJobsRunning;
//		
//		//1. all jobs in queuePriority must be launched
//		counter = runPriorityOne(counter, maxJobsRunning);
//		if(counter < 0){
//			return;
//			
//		}
//		if(counter >= maxJobsRunning){
//			return;
//		}
//		//2. Algorithm to schedule level 2 jobs
//		runPriorityTwo(counter, maxJobsRunning);
//	}
	
//	private int runPriorityOne(int counter, int maxJobsRunning){
//		UwsQueueItem queueItem;
//		UwsJobThread jobThread;
//		//Checks max jobs running in parallel
//		
//		List<UwsQueueItem> rejectedItems = new ArrayList<UwsQueueItem>();
//		for(;;){
//			//If below number of jobs running in parallel, run next job
//			if(counter >= maxJobsRunning){
//				//-1 stop
//				return -1;
//			}
//			queueItem = queuePriority.popFistJob(PRIORITY_QUEUE_IDENTIFIER);
//			if(queueItem == null){
//				break;
//			}else{
//				
//				jobThread = queueItem.getJobThread();
//				if(checkExecutable(jobThread)){
//					jobThread.startIfRequired();
//					counter++;
//				}else{
//					rejectedItems.add(0,queueItem);
//				}
//			}
//		}
//		
//		//Unpop rejected
//		for(UwsQueueItem item:rejectedItems){
//			queuePriority.unpop(item);
//		}
//		
//		//continue (counter >= 0)
//		return counter;
//	}
	
	private int runQueue(UwsQueue queue, int counter, int maxJobsRunning){
		queue.markCheckPoint();
		UwsQueueItem item;
		UwsJobThread jobThread;
		boolean somethingExecuted = false;
		while(counter < maxJobsRunning){
			item = queue.popJob();
			if(item == null){
				//no more jobs for the current user
				//check a complete rr
				if(queue.isCheckPointAchieved()){
					if(somethingExecuted){
						somethingExecuted = false;
						continue;
					}else{
						break;
					}
				}
//				if(!somethingExecuted && queue.isCheckPointAchieved()){
//					break;
//				}
				continue;
			}
			jobThread = item.getJobThread();
			if(checkExecutable(jobThread)){
				jobThread.startIfRequired();
				counter++;
				updateQueuedTimeStats(item.getQueueTime());
				somethingExecuted = true;
			}else {
				queue.unPop(item);
			}
		}
		return counter;
	}
	
	
//	private int runPriorityTwo(int counter, int maxJobsRunning){
//		UwsJobThread jobThread;
//		int newCounter = counter;
//		UwsQueueItem item;
//		while (newCounter < maxJobsRunning){
//			List<UwsQueueItem> jobsPerUser = queueNormal.popFirstJobs(getSchedulerMode());
//			for(int i = 0; i < jobsPerUser.size(); i++){
//				item = jobsPerUser.get(i);
//				if(item == null){
//					jobThread = null;
//				}else{
//					jobThread = item.getJobThread();
//				}
//				if(jobThread != null){
//					if(checkExecutable(jobThread)){
//						jobThread.startIfRequired();
//						newCounter++;
//						updateQueuedTimeStats(item.getQueueTime());
//					}else{
//						queueNormal.unpop(item);
//					}
//				}
//				if(newCounter >= maxJobsRunning){
//					//end of rr: unpop remaining jobs
//					break;
//				}
//			}
//		}
//		
//		return newCounter;
//		
//		UwsJobThread jobThread;
//		List<UwsQueueItem> jobsPerUser = queueNormal.popFirstJobs();
//		UwsQueueItem item;
//		int jobsSize = jobsPerUser.size();
//		if(jobsSize < 1){
//			return counter;
//		}
//		if(rrIndex >= jobsSize){
//			rrIndex = 0;
//		}
//		int endIndex = rrIndex;
//		for(;;){
//			item = jobsPerUser.get(rrIndex);
//			if(item == null){
//				jobThread = null;
//			}else{
//				jobThread = item.getJobThread();
//			}
//			//Checks max jobs running in parallel
//			if(counter >= maxJobsRunning){
//				//Unput pending jobs
//				unpopJobs(jobsPerUser, rrIndex, endIndex);
//				//-1 stop
//				return -1;
//			}
//			rrIndex++;
//			if(rrIndex >= jobsSize){
//				rrIndex = 0;
//			}
//			if(jobThread != null){
//				if(checkExecutable(jobThread)){
//					jobThread.startIfRequired();
//					counter++;
//					updateQueuedTimeStats(item.getQueueTime());
//				}else{
//					queueNormal.unpop(item);
//				}
//			}
//			//Check round end
//			if(rrIndex == endIndex){
//				//One round done.
//				return counter;
//			}
//		}
//	}
	
//	private int runPriorityTwo(int counter, int maxJobsRunning){
//		UwsJobThread jobThread;
//		List<UwsQueueItem> jobsPerUser = queueNormal.popFirstJobs();
//		UwsQueueItem item;
//		int jobsSize = jobsPerUser.size();
//		if(jobsSize < 1){
//			return counter;
//		}
//		if(rrIndex >= jobsSize){
//			rrIndex = 0;
//		}
//		int endIndex = rrIndex;
//		for(;;){
//			item = jobsPerUser.get(rrIndex);
//			if(item == null){
//				jobThread = null;
//			}else{
//				jobThread = item.getJobThread();
//			}
//			//Checks max jobs running in parallel
//			if(counter >= maxJobsRunning){
//				//Unput pending jobs
//				unpopJobs(jobsPerUser, rrIndex, endIndex);
//				//-1 stop
//				return -1;
//			}
//			rrIndex++;
//			if(rrIndex >= jobsSize){
//				rrIndex = 0;
//			}
//			if(jobThread != null){
//				if(checkExecutable(jobThread)){
//					jobThread.startIfRequired();
//					counter++;
//					updateQueuedTimeStats(item.getQueueTime());
//				}else{
//					queueNormal.unpop(item);
//				}
//			}
//			//Check round end
//			if(rrIndex == endIndex){
//				//One round done.
//				return counter;
//			}
//		}
//	}
	
	private void updateQueuedTimeStats(long queueTime){
		long currentTime = System.currentTimeMillis();
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(getAppId(), listid);
		long delta = currentTime - queueTime;
		jobsListManager.updateMaxQueuedTimeMillis(delta);
	}
	
	private boolean checkExecutable(UwsJobThread jobThread){
		SchedulerMode mode = getSchedulerMode();
		switch(mode){
		case NONE:
			return false;
		case ALL:
			return true;
		case ADMIN:
			UwsJobOwner jobOwner = jobThread.getOwner();
			return jobOwner.isAdmin();
		default:
			return false;
		}
	}
	
//	private void unpopJobs(List<UwsQueueItem> jobsPerUser, int currentIndex, int endPos){
//		UwsQueueItem item;
//		int size = jobsPerUser.size();
//		for(;;){
//			item = jobsPerUser.get(currentIndex);
//			if(item != null){
//				queueNormal.unpop(item);
//			}
//			currentIndex++;
//			if(currentIndex >= size){
//				currentIndex = 0;
//			}
//			if(currentIndex == endPos){
//				break;
//			}
//		}
//	}
	
	private String getValidUserQueueIdentifier(UwsJobOwner owner){
		String user = owner.getId();
		if(UwsUtils.ANONYMOUS_USER.equalsIgnoreCase(user)){
			if(!groupAnonymousJobs){
				//do not group anonymous users
				//get IP for each anonymous user instead
				String ip = owner.getIp();
				if(ip != null && !ip.isEmpty()){
					return user + "_" + ip;
				}
			}
			return user;
		}
		if(PRIORITY_QUEUE_IDENTIFIER.equals(user)){
			return user + "*";
		} else {
			return user;
		}
	}
	
	@Override
	public synchronized boolean abort(UwsJobThread jobThread){
		UwsQueueItem queueItem = null;
		int priority = jobThread.getJob().getPriority();
		switch(priority){
		case 0:
			queueItem = null;
			break;
		case 1:
			queueItem = queuePriority.removeJob(jobThread, PRIORITY_QUEUE_IDENTIFIER);
			break;
		case 2:
			String queueUser = getValidUserQueueIdentifier(jobThread.getJob().getOwner());
			queueItem = queueNormal.removeJob(jobThread, queueUser);
		default:
			break;
		}
		return queueItem != null;
	}

	@Override
	public int getDefaultPriority(String listid) {
		return DEFAULT_PRIORITY;
	}
	
	@Override
	public synchronized void setSchedulerMode(SchedulerMode mode) {
		super.setSchedulerMode(mode);
		executeSchedulerProcedure();		
	}

	@Override
	public String toString(){
		return "UWS Simple scheduler: '"+listid+"', Max jobs running allowed: " + getMaxRunningJobs();
	}

}
