package esavo.uws.utils;

import java.util.logging.Logger;

import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;

/**
 * This class removes jobs based on the 'destructiontime' attribute.
 * If a job does not have 'destructiontime' (i.e. non anonymous jobs) the job is ignored.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobsDeletionManagerThread extends Thread {
	
	private static final Logger LOG = Logger.getLogger(UwsJobsDeletionManagerThread.class.getName());
	
	//public static final long DEFAULT_DESTRUCTION_TIME = 3 * 24 * 60 * 60 * 1000; //3days in milliseconds
	
	public static final long DEFAULT_CHECK_TIME = 24 * 60 * 60 * 1000; //24hrs. in milliseconds
	

	private long checkTime = DEFAULT_CHECK_TIME;
	private long deltaDestructionTime = UwsConfiguration.DEFAULT_DELTA_DESTRUCTION_TIME;
	private String appid;
	
	public UwsJobsDeletionManagerThread(String appid){
		this.appid = appid;
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid); 
		String checkTime = configuration.getProperty(UwsConfiguration.UWS_JOBS_REMOVAL_CHECK_TIME);
		String olderThanTime = configuration.getProperty(UwsConfiguration.UWS_JOBS_DELTA_DESTRUCTION_TIME);

		long lCheckTime;
		if(checkTime == null){
			lCheckTime = DEFAULT_CHECK_TIME;
		}else{
			try{
				lCheckTime = Long.parseLong(checkTime);
			}catch(NumberFormatException nfe){
				lCheckTime = DEFAULT_CHECK_TIME;
				LOG.warning("Cannot parse check time: " + checkTime + ". Using default check time: " + lCheckTime);
			}
		}
		
		this.checkTime = lCheckTime;
		this.deltaDestructionTime = UwsUtils.parseDestructionTime(olderThanTime);
	}
		

	long getCheckTime() {
		return checkTime;
	}

	long getDeltaDestructionTime() {
		return deltaDestructionTime;
	}

	String getCreatorIdentifier() {
		return appid;
	}

	public void run(){
		if(checkTime < 0){
			//do not check
			return;
		}
		//UwsManager manager = getManager();
		UwsManager manager = UwsUtils.getManager();
		String report;
		while(true){
			try {
				sleep(checkTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			report = manager.checkJobsRemovalProcedure();
			LOG.info("Check Jobs Removal report:\n" + report);
		}
	}
	
//	private UwsManager getManager(){
//		UwsManager manager = null;
//		while(true){
//			try {
//				sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				break;
//			}
//			try{
//				manager = UwsManager.getInstance();
//				break;
//			}catch(Exception e){
//				//ignore: the manager is not ready yet.
//			}
//		}
//		return manager;
//	}

	

}
