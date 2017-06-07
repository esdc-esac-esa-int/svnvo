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
