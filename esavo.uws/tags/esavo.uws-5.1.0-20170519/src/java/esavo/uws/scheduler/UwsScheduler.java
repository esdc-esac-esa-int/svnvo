package esavo.uws.scheduler;

import esavo.uws.UwsException;

public interface UwsScheduler {
	
//	/**
//	 * This method must be called to start the scheduler.
//	 * @param jobsCounter
//	 */
//	public void start(UwsRunningJobsCounter jobsCounter);
	
	public enum SchedulerMode { 
		ALL, 
		ADMIN,
		NONE
	}
	
//	/**
//	 * 
//	 * @param job
//	 */
//	public boolean enqueue(UwsJobThread jobThread) throws UwsException;
	

	/** 
	 * @param jobThread job to add
	 * Notify a new job arrived
	 */
	public boolean notifyJobArrival(UwsJobThread jobThread) throws UwsException;
	
	
	/** 
	 * @param jobThread job finished
	 * Notify a job finished execution
	 */
	
	public boolean notifyJobFinished(UwsJobThread jobThread);
	

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
	 * @param listid
	 * @return
	 */
	public int getDefaultPriority(String listid);
	
	
	/**
	 * Sets the scheduler mode
	 * @param mode
	 * @return
	 */
	public void setSchedulerMode(SchedulerMode mode);
	
	/**
	 * Gets the current scheduler mode.
	 * @return
	 */
	public SchedulerMode getSchedulerMode();
	
	
}
