package esavo.uws.utils;

import java.util.logging.Logger;

import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;

/**
 * This class removes old events.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsNotificationsDeletionManagerThread extends Thread {
	
	private static final Logger LOG = Logger.getLogger(UwsNotificationsDeletionManagerThread.class.getName());
	
	public static final long DEFAULT_CHECK_TIME = 24 * 60 * 60 * 1000; //1 day in milliseconds

	private long checkTime = DEFAULT_CHECK_TIME;
	private long deltaDestructionTime = UwsConfiguration.DEFAULT_DELTA_NOTIFICATIONS_REMOVAL_TIME;
	private String appid;
	
	public UwsNotificationsDeletionManagerThread(String appid){
		this.appid = appid;
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid); 
		String checkTime = configuration.getProperty(UwsConfiguration.UWS_NOTIFICATIONS_REMOVAL_CHECK_TIME);
		String olderThanTime = configuration.getProperty(UwsConfiguration.UWS_NOTIFICATIONS_DELTA_DESTRUCTION_TIME);

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
		long lOlderThanTime;
		if(olderThanTime == null){
			lOlderThanTime = UwsConfiguration.DEFAULT_DELTA_NOTIFICATIONS_REMOVAL_TIME;
		}else{
			try{
				lOlderThanTime = Long.parseLong(olderThanTime);
			}catch(NumberFormatException nfe){
				lOlderThanTime = UwsConfiguration.DEFAULT_DELTA_NOTIFICATIONS_REMOVAL_TIME;
				LOG.warning("Cannot parse check time: " + olderThanTime + ". Using default check time: " + lOlderThanTime);
			}
		}
		
		this.checkTime = lCheckTime;
		this.deltaDestructionTime = lOlderThanTime;
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
			report = manager.getFactory().getNotificationsManager().checkNotificationsRemovalProcedure(deltaDestructionTime);
			LOG.info("Check Notifications Removal report:\n" + report);
		}
	}

}
