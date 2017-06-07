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
