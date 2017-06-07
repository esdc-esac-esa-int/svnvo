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
import esavo.uws.jobs.UwsJob;

public class DummyUwsExecutor implements UwsExecutor {
	
	private boolean canceled;
	private UwsJob job;
	private boolean executed;
	private Object executedObject;
	private boolean generateUwsException;
	private boolean generateInterruptedException;
	
	public void reset(){
		canceled = false;
		job = null;
		executed = false;
		executedObject = null;
		generateUwsException = false;
		generateInterruptedException = false;
	}
	
	public void setExecutedObject(Object o){
		this.executedObject = o;
	}
	
	public UwsJob getJob(){
		return this.job;
	}

	@Override
	public void cancel(UwsJob job) {
		this.job = job;
		canceled = true;
	}

	@Override
	public Object execute(UwsJob job) throws InterruptedException, UwsException {
		this.job = job;
		if(generateUwsException){
			throw new UwsException("Requested exception");
		}
		if(generateInterruptedException){
			throw new InterruptedException("Requested exception");
		}
		executed = true;
		return executedObject;
	}

}
