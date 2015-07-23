package esavo.uws.utils;

import java.util.List;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.UwsStorage;

public class UwsRestartPendingJobsThread extends Thread {
	
	private static final Logger LOG = Logger.getLogger(UwsRestartPendingJobsThread.class.getName());
	
	private String appid;
	
	public UwsRestartPendingJobsThread(String appid){
		this.appid = appid;
	}
	
	public void run(){
		//UwsManager manager = getManager();
		UwsManager manager = UwsUtils.getManager();
		restartPendingJobs(manager);
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
//				//manager = UwsManager.getInstance(appid);
//				manager = UwsManager.getInstance();
//				break;
//			}catch(Exception e){
//				//ignore: the manager is not ready yet.
//			}
//		}
//		return manager;
//	}

	private void restartPendingJobs(UwsManager manager) throws IllegalStateException {
		LOG.info("Starting pending jobs...");
		UwsStorage storageManager = manager.getFactory().getStorageManager();
		//1. Load pending jobs
		List<UwsJob> pendingJobs = null;
		try {
			pendingJobs = storageManager.getPendingJobs();
		} catch (UwsException e1) {
			e1.printStackTrace();
		}
		
		if(pendingJobs != null && pendingJobs.size() > 0){
			LOG.info("Found: " + pendingJobs.size() + " pending job(s).");
			for(UwsJob job: pendingJobs){
				try {
					//clean results data and launch the job
					if(UwsUtils.isJobValidForAppid(job.getJobId(), appid)){
						manager.restartJob(job);
					}
				} catch (UwsException e) {
					e.printStackTrace();
					throw new RuntimeException("Unable to restore jobs", e);
				}
			}
		} else {
			LOG.info("No pending jobs found.");
		}
		LOG.info("Starting pending jobs finished.");
	}
	
//	/**
//	 * Checks the job belongs to this application.
//	 * @param job
//	 * @return
//	 */
//	private boolean validJob(UwsJob job){
//		String id = job.getJobId();
//		return id.endsWith(appid);
//	}
	

}
