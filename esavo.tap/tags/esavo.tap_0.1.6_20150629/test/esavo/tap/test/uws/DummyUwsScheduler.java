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
package esavo.tap.test.uws;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;

public class DummyUwsScheduler implements UwsScheduler {
	
	private static final int TEST_DEFAULT_PRIORITY = 0;
	
	private DummyUwsExecutor executor;
	
	private int defaultPriority = TEST_DEFAULT_PRIORITY;
	private boolean aborted = false;
	private UwsJobThread jobThread;
	
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
	}
	
	public UwsJobThread getJobThread(){
		return jobThread;
	}

	@Override
	public boolean abort(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		aborted = true;
		return false;
	}

	@Override
	public boolean enqueue(UwsJobThread jobThread) throws UwsException {
		this.jobThread = jobThread;
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return defaultPriority;
	}
	
	public Object execute() throws InterruptedException, UwsException{
		return executor.execute(jobThread.getJob());
	}

}
