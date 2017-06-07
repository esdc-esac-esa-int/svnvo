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
package esavo.uws.creator;

import java.util.Date;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.factory.UwsFactory;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.jobs.utils.UwsJobRestartArgs;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class UwsDefaultCreator implements UwsCreator {
	
	private String appid;
	
	public UwsDefaultCreator(String appid){
		this.appid = appid;
	}
	
	@Override
	public UwsJob createJob(UwsJobOwner owner, String listid, int priority) throws UwsException {
		//UwsFactory factory = UwsManager.getInstance(appid).getFactory();
		UwsFactory factory = UwsManager.getInstance().getFactory();
		String appid = factory.getAppId();
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setOwner(owner);
		args.setListid(listid);
		args.setPriority(priority);
		args.setStorage(factory.getStorageManager());
		args.setJobsListManager(UwsJobsListManager.getInstance(appid));
		args.setEventsManager(factory.getEventsManager());
		UwsJob job = new UwsJob(args);
		
		//Destruction time:
		if(UwsUtils.isAnonymous(owner.getId())){
			//only for anonymous jobs
			Date destructionTime = getDestructionTime(appid);
			if(destructionTime != null){
				//save previous value
				boolean enableUpdates = job.getStatusManager().getEnableUpdates();
				job.setEnableUpdates(false);
				try {
					job.setDestructionTime(destructionTime);
				} catch (UwsException e) {
					// cannot happend because 'enableUpdates' is disabled
				}
				//restore previous value
				job.setEnableUpdates(enableUpdates);
			}
		}
		
		factory.getEventsManager().setEventTime(owner, UwsEventType.JOB_CREATED_EVENT);

		return job;
	}
	
	private Date getDestructionTime(String appid){
		UwsConfiguration config = UwsConfigurationManager.getConfiguration(appid);
		String destructionTimeProp = config.getProperty(UwsConfiguration.UWS_JOBS_DELTA_DESTRUCTION_TIME);
		long deltaDestructionTime = UwsUtils.parseDestructionTime(destructionTimeProp);
		long destructionTime = deltaDestructionTime + System.currentTimeMillis();
		return new Date(destructionTime);
	}

	@Override
	public UwsJob createJob(String jobid, UwsJobOwner owner, String listid,	String locationid, List<UwsJobResultMeta> results, int priority) {
		//UwsFactory factory = UwsManager.getInstance(appid).getFactory();
		UwsFactory factory = UwsManager.getInstance().getFactory();
		String appid = factory.getAppId();
		UwsJobRestartArgs args = new UwsJobRestartArgs();
		args.setAppid(appid);
		args.setJobid(jobid);
		args.setOwner(owner);
		args.setListid(listid);
		args.setLocationid(locationid);
		args.setResults(results);
		args.setPriority(priority);
		args.setStorage(factory.getStorageManager());
		args.setJobsListManager(UwsJobsListManager.getInstance(appid));
		args.setEventsManager(factory.getEventsManager());
		return new UwsJob(args);
	}
	
	@Override
	public String toString(){
		return "Default creator for application '"+appid+"'";
	}

}
