package esavo.uws.scheduler;

import esavo.uws.UwsException;

public interface UwsScheduler {
	
//	/**
//	 * This method must be called to start the scheduler.
//	 * @param jobsCounter
//	 */
//	public void start(UwsRunningJobsCounter jobsCounter);
	
	/**
	 * 
	 * @param job
	 */
	public boolean enqueue(UwsJobThread jobThread) throws UwsException;
	
	
	/**
	 * Aborts a job.<br/>
	 * If the job is found in a queue (not running yet), returns 'true' (removed)<br/>
	 * If the job is not found in a queue (already running), returns 'false' (not removed)<br/>
	 * @param jobThread
	 * @return
	 */
	public boolean abort(UwsJobThread jobThread);
	
	/**
	 * Returns the default priority.
	 * @return
	 */
	public int getDefaultPriority();
	
	
}
