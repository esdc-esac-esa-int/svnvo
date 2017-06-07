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

import java.util.HashMap;
import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.UwsJob;

public class UwsDefaultScheduler extends UwsAbstractScheduler implements UwsScheduler {
	
	private Map<String, UwsSimpleScheduler> schedulers;
	
	public UwsDefaultScheduler(String appid){
		super(appid,null);
		schedulers = new HashMap<String, UwsSimpleScheduler>();
		schedulers.put(UwsConfiguration.SYNC_LIST_ID, new UwsSimpleScheduler(appid, UwsConfiguration.SYNC_LIST_ID, UwsConfiguration.CONFIG_SYNC_MAX_RUNNING_JOBS));
		schedulers.put(UwsConfiguration.ASYNC_LIST_ID, new UwsSimpleScheduler(appid, UwsConfiguration.ASYNC_LIST_ID, UwsConfiguration.CONFIG_ASYNC_MAX_RUNNING_JOBS));
	}

	@Override
	public boolean notifyJobArrival(UwsJobThread jobThread) throws UwsException {
		UwsSimpleScheduler sch = getSuitableScheduler(jobThread);
		return sch.notifyJobArrival(jobThread);
	}

	@Override
	public boolean notifyJobFinished(UwsJobThread jobThread) {
		UwsSimpleScheduler sch = getSuitableScheduler(jobThread);
		return sch.notifyJobFinished(jobThread);
	}

	@Override
	public boolean abort(UwsJobThread jobThread) {
		UwsSimpleScheduler sch = getSuitableScheduler(jobThread);
		return sch.abort(jobThread);
	}

	@Override
	public int getDefaultPriority(String listid) {
		UwsSimpleScheduler sch = schedulers.get(listid);
		if(sch == null){
			throw new IllegalArgumentException("Unknonw list '"+listid+"'");
		}
		return sch.getDefaultPriority(listid);
	}

	private UwsSimpleScheduler getSuitableScheduler(UwsJobThread jobThread){
		return getSuitableScheduler(jobThread.getJob());
	}
	
	private UwsSimpleScheduler getSuitableScheduler(UwsJob job){
		String listid = job.getListid();
		if(listid == null){
			throw new IllegalArgumentException("No jobs list found for job id: " + job.getJobId());			
		}
		String schedulerid = listid.toLowerCase();
		return schedulers.get(schedulerid);
	}
	
	@Override
	public synchronized void setSchedulerMode(SchedulerMode mode) {
		super.setSchedulerMode(mode);
		for(UwsSimpleScheduler sch: schedulers.values()){
			sch.setSchedulerMode(mode);
		}
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("Default scheduler for application '"+super.getAppId()+"'\n");
		boolean firstTime = true;
		for(UwsSimpleScheduler sch: schedulers.values()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append('\n');
			}
			sb.append('\t').append(sch.toString());
		}
		return sb.toString();
	}


}
