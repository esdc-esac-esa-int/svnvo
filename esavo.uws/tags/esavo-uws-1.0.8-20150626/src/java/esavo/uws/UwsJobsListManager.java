package esavo.uws;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import esavo.uws.jobs.UwsJob;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class UwsJobsListManager {

	private static final Logger LOG = Logger.getLogger(UwsJobsListManager.class.getName());
	//private static Map<String, UwsJobsListManager> lists;
	private static UwsJobsListManager list;
	
//	public static synchronized UwsJobsListManager getInstance(String appid){
//		if(lists == null){
//			lists = new HashMap<String, UwsJobsListManager>();
//		}
//		UwsJobsListManager umjl = lists.get(appid);
//		if(umjl == null){
//			umjl = new UwsJobsListManager(appid);
//			lists.put(appid, umjl);
//		}
//		return umjl;
//	}

	public static synchronized UwsJobsListManager getInstance(String appid){
		if(list == null){
			list = new UwsJobsListManager(appid);
		}
		return list;
	}

	private String appid;
	private int runningJobsCounter;
	private Map<String, UwsJobThread> runningJobs;
	
	private UwsJobsListManager(String appid){
		this.appid = appid;
		runningJobsCounter = 0;
		runningJobs = new HashMap<String, UwsJobThread>();
	}
	
	public String getAppId(){
		return appid;
	}
	
	public synchronized int getNumberOfJobsInMemory(){
		return runningJobs.size();
	}
	
	public synchronized void addRunningJob(String jobid, UwsJobThread jobThread){
		runningJobs.put(jobid, jobThread);
	}
	
	public synchronized UwsJobThread removeRunningJob(String jobid){
		return runningJobs.remove(jobid);
	}
	
	public synchronized UwsJobThread getRunningJob(String jobid){
		return runningJobs.get(jobid);
	}
	
	public synchronized int getJobsCounter(){
		return runningJobsCounter;
	}
	
	public synchronized void increaseJobsCounter(){
		runningJobsCounter++;
	}
	
	public synchronized void decreaseJobsCounter(){
		runningJobsCounter--;
		if(runningJobsCounter < 0){
			runningJobsCounter = 0;
		}
	}
	
	public synchronized boolean stopAndRemoveRunningJob(String jobid, UwsStorage storage) {
		UwsJobThread jobThread = removeRunningJob(jobid);
		if(jobThread == null){
			//job not running
			return false;
		}else{
			jobThread.abortJob();
			UwsJob job = jobThread.getJob();
			//wait until the thread is finished
			try {
				//UwsUtils.waitUntilThreadIsTerminated(jobThread);
				jobThread.waitUntilThreadIsFinished();
			} catch (InterruptedException e) {
				LOG.severe("Cannot wait until job thread is terminated, when removing job '"+job.getJobId()+"'." + e.getMessage() + 
						"\n" + UwsUtils.dumpStackTrace(e) + "\nContinue with job removal anyway...");
			}
			try {
				//this method removes all job metadata and data (output dir)
				storage.removeJobMetaDataAndOutputData(job);
			} catch (UwsException e) {
				LOG.severe("Cannot remove job '"+job.getJobId()+"'." + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
				return false;
			}
			return true;
		}
	}
	
	public synchronized void reset(){
		runningJobsCounter = 0;
		runningJobs.clear();
	}
	
	@Override
	public String toString(){
		return "Running Jobs List for app: '"+appid+"'. Size: "+runningJobs.size()+". Num running jobs: " + runningJobsCounter;
	}

}
