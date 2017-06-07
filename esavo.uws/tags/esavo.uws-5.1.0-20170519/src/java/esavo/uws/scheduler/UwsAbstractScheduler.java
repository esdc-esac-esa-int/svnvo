package esavo.uws.scheduler;

import java.util.logging.Logger;

import javax.security.auth.login.Configuration;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

public abstract class UwsAbstractScheduler implements UwsScheduler {

	public static final int DEFAULT_MAX_RUNNING_JOBS = 20;

	private static final Logger LOG = Logger.getLogger(UwsAbstractScheduler.class.getName());

	private String appid;
	private int maxJobsRunning;
	private String maxRunningJobsPropertyId;
	
	private SchedulerMode schedulerMode;
	
	
	public UwsAbstractScheduler(String appid, String maxRunningJobsPropertyId){
		this.appid = appid;
		this.maxJobsRunning = -1;
		this.maxRunningJobsPropertyId = maxRunningJobsPropertyId;
		this.schedulerMode = SchedulerMode.ALL;
	}
	
	public String getAppId(){
		return appid;
	}
	
	protected int getMaxRunningJobs(){
		if(maxJobsRunning < 0){
			maxJobsRunning = getMaxRunningJobsInit();
		}
		return maxJobsRunning;
	}
	
	private int getMaxRunningJobsInit(){
		if(maxRunningJobsPropertyId==null){
			return DEFAULT_MAX_RUNNING_JOBS;
		}
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		//String max = configuration.getProperty(UwsConfiguration.CONFIG_MAX_RUNNING_JOBS);
		String max = configuration.getProperty(maxRunningJobsPropertyId);
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

	
	@Override
	public synchronized void setSchedulerMode(SchedulerMode mode) {
		this.schedulerMode=mode;
	}

	@Override
	public synchronized SchedulerMode getSchedulerMode() {
		return this.schedulerMode;
	}


}
