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

import java.util.List;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class UwsDefaultScheduler extends UwsAbstractScheduler implements UwsScheduler {
	
	public static final long DEFAULT_CHECK_TIME = 1000;
	
	public static final String PRIORITY_QUEUE_IDENTIFIER = "__UWS_PRIORITY_1_ID";
	
	public static final int DEFAULT_PRIORITY = 2; //Normal priority.
	
	private static final Logger LOG = Logger.getLogger(UwsDefaultScheduler.class.getName());
	
	private class CheckThread extends Thread {
		private long checkTime;
		private boolean run;
		
		CheckThread(long checkTime){
			this.checkTime = checkTime;
			this.run = true;
		}
		
		public void run(){
			while(run){
				try {
					sleep(checkTime);
				} catch (InterruptedException e) {
					break;
				}
				try{
					executeSchedulerProcedure();
				}catch(Exception e){
					//ignore
					LOG.severe("Exception in scheduler procedure: " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
				}
			}
		}
		
		void endCheck(){
			run = false;
			this.interrupt();
		}
	}
	
	private UwsQueue queuePriority;
	private UwsQueue queueNormal;
	private CheckThread checkTread;
	//private UwsRunningJobsCounter jobsCounter;
	//private boolean useThread;
	private int rrIndex;

	/**
	 * Constructor
	 * @param appid
	 * @param executor
	 */
	public UwsDefaultScheduler(String appid, UwsExecutor executor){
		this(appid, executor, true);
	}
	
	/**
	 * package: test-harness: the check thread can be disabled by setting 'useThread' argument to false
	 * @param appid
	 * @param executor
	 * @param useThread
	 */
	UwsDefaultScheduler(String appid, UwsExecutor executor, boolean useThread) {
		super(appid, executor);
		queuePriority = new UwsQueue();
		queueNormal = new UwsQueue();
		if(useThread){
			checkTread = new CheckThread(DEFAULT_CHECK_TIME);
			checkTread.setDaemon(true);
			checkTread.start();
		}
		//this.jobsCounter = null;
		this.rrIndex = 0;
	}
	
//	@Override
//	public void start(UwsRunningJobsCounter jobsCounter) {
//		this.jobsCounter = jobsCounter;
//		this.rrIndex = 0;
//	}
	
	@Override
	public synchronized boolean enqueue(UwsJobThread jobThread) throws UwsException {
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
//		if(jobsCounter == null){
//			//no jobs counter: cannot count jobs.
//			return;
//		}

//		int currentJobsRunning = jobsCounter.getCurrentRunningJobs();
//		int maxJobsRunning = jobsCounter.getMaxRunningJobs();

		//int currentJobsRunning = UwsJobsCounter.getJobsCounter(getAppId());
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(getAppId());
		int currentJobsRunning = jobsListManager.getJobsCounter();
		int maxJobsRunning = getMaxRunningJobs();
		
		int counter = currentJobsRunning;
		
		//1. all jobs in queuePriority must be launched
		counter = runPriorityOne(counter, maxJobsRunning);
		if(counter < 0){
			return;
			
		}
		if(counter >= maxJobsRunning){
			return;
		}
		//2. Algorithm to schedule level 2 jobs
		runPriorityTwo(counter, maxJobsRunning);
	}
	
	private int runPriorityOne(int counter, int maxJobsRunning){
		UwsQueueItem queueItem;
		UwsJobThread jobThread;
		//Checks max jobs running in parallel
		for(;;){
			//If below number of jobs running in parallel, run next job
			if(counter >= maxJobsRunning){
				//-1 stop
				return -1;
			}
			queueItem = queuePriority.popFistJob(PRIORITY_QUEUE_IDENTIFIER);
			if(queueItem == null){
				break;
			}else{
				jobThread = queueItem.getJobThread();
				jobThread.startIfRequired();
				counter++;
			}
		}
		//continue (counter >= 0)
		return counter;
	}
	
	private int runPriorityTwo(int counter, int maxJobsRunning){
		UwsJobThread jobThread;
		List<UwsQueueItem> jobsPerUser = queueNormal.popFirstJobs();
		UwsQueueItem item;
		int jobsSize = jobsPerUser.size();
		if(jobsSize < 1){
			return counter;
		}
		if(rrIndex >= jobsSize){
			rrIndex = 0;
		}
		int endIndex = rrIndex;
		for(;;){
			item = jobsPerUser.get(rrIndex);
			if(item == null){
				jobThread = null;
			}else{
				jobThread = item.getJobThread();
			}
			//Checks max jobs running in parallel
			if(counter >= maxJobsRunning){
				//Unput pending jobs
				unpopJobs(jobsPerUser, rrIndex, endIndex);
				//-1 stop
				return -1;
			}
			rrIndex++;
			if(rrIndex >= jobsSize){
				rrIndex = 0;
			}
			if(jobThread != null){
				jobThread.startIfRequired();
				counter++;
			}
			//Check round end
			if(rrIndex == endIndex){
				//One round done.
				return counter;
			}
		}
	}
	
	private void unpopJobs(List<UwsQueueItem> jobsPerUser, int currentIndex, int endPos){
		UwsQueueItem item;
		int size = jobsPerUser.size();
		for(;;){
			item = jobsPerUser.get(currentIndex);
			if(item != null){
				queueNormal.unpop(item);
			}
			currentIndex++;
			if(currentIndex >= size){
				currentIndex = 0;
			}
			if(currentIndex == endPos){
				break;
			}
		}
	}
	
	private String getValidUserQueueIdentifier(UwsJobOwner owner){
		String user = owner.getId();
		if(UwsUtils.ANONYMOUS_USER.equalsIgnoreCase(user)){
			return user + "_" + owner.getSession();
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
	public int getDefaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public String toString(){
		return "Default scheduler for application '"+super.getAppId()+"'";
	}


}
