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
package esavo.uws.utils.test.uws;

import esavo.uws.UwsException;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;

public class DummyUwsScheduler implements UwsScheduler {
	
	public static final int TEST_DEFAULT_PRIORITY = 0;
	
	private DummyUwsExecutor executor;
	
	private int defaultPriority = TEST_DEFAULT_PRIORITY;
	private boolean aborted = false;
	private UwsJobThread jobThread;
	private SchedulerMode mode = SchedulerMode.ALL;
	private boolean startThread = false;
	
	public DummyUwsScheduler(DummyUwsExecutor executor){
		this.executor = executor;
	}
	
	public void setDefaultPriority(int defaultPriority){
		this.defaultPriority = defaultPriority;
		jobThread = null;
	}
	
	public void reset(){
		defaultPriority = TEST_DEFAULT_PRIORITY;
		aborted = false;
		jobThread = null;
		startThread = false;
	}
	
	public UwsJobThread getJobThread(){
		return jobThread;
	}
	
	public boolean isAborted(){
		return aborted;
	}

	@Override
	public boolean abort(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		aborted = true;
		return false;
	}

	@Override
	public boolean notifyJobArrival(UwsJobThread jobThread) throws UwsException {
		this.jobThread = jobThread;
		if(startThread){
			jobThread.start();
		}
		return true;
	}

	@Override
	public boolean notifyJobFinished(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		return true;
	}


	@Override
	public int getDefaultPriority(String listid) {
		return defaultPriority;
	}
	
	public Object execute() throws InterruptedException, UwsException{
		return executor.execute(jobThread.getJob());
	}

	@Override
	public void setSchedulerMode(SchedulerMode mode) {
		this.mode=mode;
		
	}

	@Override
	public SchedulerMode getSchedulerMode() {
		return mode;
	}

	/**
	 * @return the startThread
	 */
	public boolean isStartThread() {
		return startThread;
	}

	/**
	 * @param startThread the startThread to set
	 */
	public void setStartThread(boolean startThread) {
		this.startThread = startThread;
	}


}
