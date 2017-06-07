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

public class UwsQueueItem {
	
	/**
	 * queuedUser is not jobOwner. queuedUser for anonymous can be grouped by IP, for instance. 
	 */
	private String queuedUser;
	private UwsJobThread jobThread;
	private long queueTime;
	
	public UwsQueueItem(UwsJobThread jobThread, String queuedUser){
		this.queuedUser = queuedUser;
		this.jobThread = jobThread;
		this.queueTime = System.currentTimeMillis();
	}
	
	/**
	 * @return the jobThread
	 */
	public UwsJobThread getJobThread() {
		return jobThread;
	}

	/**
	 * @return the queueTime
	 */
	public long getQueueTime() {
		return queueTime;
	}
	
	public String getQueuedUser(){
		return queuedUser;
	}

}
