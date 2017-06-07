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

import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.utils.UwsUtils;

public abstract class UwsAbstractScheduler implements UwsScheduler {

	public static final int DEFAULT_MAX_RUNNING_JOBS = 20;

	private static final Logger LOG = Logger.getLogger(UwsAbstractScheduler.class.getName());

	private String appid;
	private UwsExecutor executor;
	private int maxJobsRunning;
	
	public UwsAbstractScheduler(String appid, UwsExecutor executor){
		this.appid = appid;
		this.executor = executor;
		this.maxJobsRunning = -1;
	}
	
	public String getAppId(){
		return appid;
	}

	protected UwsExecutor getExecutor(){
		return executor;
	}
	
	protected int getMaxRunningJobs(){
		if(maxJobsRunning < 0){
			maxJobsRunning = getMaxRunningJobsInit();
		}
		return maxJobsRunning;
	}
	
	private int getMaxRunningJobsInit(){
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		String max = configuration.getProperty(UwsConfiguration.CONFIG_MAX_RUNNING_JOBS);
		if(max == null){
			return DEFAULT_MAX_RUNNING_JOBS;
		}else{
			try {
				return UwsUtils.getInt(max);
			} catch (UwsException e) {
				LOG.severe("Cannot set max running jobs. Invalid number: " + max + ". Using default value: " + DEFAULT_MAX_RUNNING_JOBS);
				return DEFAULT_MAX_RUNNING_JOBS;
 			}
		}
	}
	


}
