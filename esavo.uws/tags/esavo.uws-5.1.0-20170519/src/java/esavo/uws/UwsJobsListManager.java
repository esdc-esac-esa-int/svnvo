package esavo.uws;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import esavo.uws.jobs.UwsJob;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class UwsJobsListManager {

	private static final Logger LOG = Logger.getLogger(UwsJobsListManager.class.getName());
	private static Map<String, UwsJobsListManager> list;
	
	public static synchronized UwsJobsListManager getInstance(String appid, String listid){
		if(list == null){
			list = new HashMap<String, UwsJobsListManager>();
		}
		UwsJobsListManager uwsList = list.get(listid);
		if(uwsList == null){
			uwsList = new UwsJobsListManager(appid, listid);
			list.put(listid, uwsList);
		}
		return uwsList;
	}

	public synchronized static Map<String, Long> getNumberOfJobs(){
		Map<String, Long> numJobs = new LinkedHashMap<String, Long>();
		if(list == null){
			return numJobs;
		}
		for(Entry<String, UwsJobsListManager> e: list.entrySet()){
			numJobs.put(e.getKey() + " (in memory)", new Long(e.getValue().getNumberOfJobsInMemory()));
			numJobs.put(e.getKey() + " (running)", new Long(e.getValue().getNumberOfRunningJobs()));
			numJobs.put(e.getKey() + " (max num jobs in memory)", new Long(e.getValue().getMaxJobsInMemory()));
			numJobs.put(e.getKey() + " (max queued time ms.)", +e.getValue().getMaxQueuedTimeMillis());
			numJobs.put(e.getKey() + " (max allowed queued jobs)", new Long(e.getValue().getMaxQueuedJobs()));
		}
		return numJobs;
	}
	
	public synchronized static void resetLists(){
		if(list == null){
			return;
		}
		for(UwsJobsListManager l: list.values()){
			l.reset();
		}
	}
	
	private String appid;
	private String listid;
	private int runningJobsCounter;
	private Map<String, UwsJobThread> inMemoryJobs;
	private int maxJobsInMemory;
	private long maxQueuedTimeMillis;
	
	/**
	 * Max. allowed queued jobs:
	 * <ul>
	 * <li>-1: disabled (all jobs can be queued)
	 * <li>0: no jobs are allowed/queued
	 * <li>n: queued jobs allowed (not running: just queued)
	 * </ul>
	 */
	private int maxQueuedJobs;
	
	private UwsJobsListManager(String appid, String listid){
		this.appid = appid;
		this.listid = listid;
		inMemoryJobs = new HashMap<String, UwsJobThread>();
		maxQueuedJobs = UwsUtils.getMaxQueuedJobs(this);
		reset();
	}
	

	public String getAppId(){
		return appid;
	}
	
	public String getListId(){
		return listid;
	}
	
	public synchronized int getNumberOfJobsInMemory(){
		return inMemoryJobs.size();
	}
	
	public synchronized void addInMemoryJob(String jobid, UwsJobThread jobThread){
		inMemoryJobs.put(jobid, jobThread);
		if(inMemoryJobs.size() > maxJobsInMemory){
			maxJobsInMemory = inMemoryJobs.size();
		}
	}
	
	public synchronized UwsJobThread removeInMemoryJob(String jobid){
		return inMemoryJobs.remove(jobid);
	}
	
	public synchronized UwsJobThread getInMemoryJob(String jobid){
		return inMemoryJobs.get(jobid);
	}
	
	public synchronized int getNumberOfRunningJobs(){
		return runningJobsCounter;
	}
	
	public synchronized void increaseNumberOfRunningJobs(){
		runningJobsCounter++;
	}
	
	public synchronized void decreaseNumberOfRunningJobs(){
		runningJobsCounter--;
		if(runningJobsCounter < 0){
			runningJobsCounter = 0;
		}
	}
	
	public synchronized int getMaxJobsInMemory(){
		return maxJobsInMemory;
	}
	
	public synchronized long getMaxQueuedTimeMillis(){
		return maxQueuedTimeMillis;
	}
	
	public synchronized void updateMaxQueuedTimeMillis(long deltaMillis){
		if (deltaMillis > maxQueuedTimeMillis){
			maxQueuedTimeMillis = deltaMillis;
		}
	}
	
	public synchronized boolean stopAndRemoveRunningJob(String jobid, UwsStorage storage) {
		UwsJobThread jobThread = removeInMemoryJob(jobid);
		if(jobThread == null){
			//job not running
			return false;
		}else{
			jobThread.abortJob(UwsJobThread.FORCE_JOB_INTERRUPTION);
			UwsJob job = jobThread.getJob();
			//wait until the thread is finished
			try {
				//UwsUtils.waitUntilThreadIsTerminated(jobThread);
				jobThread.waitUntilThreadIsFinished();
			} catch (InterruptedException e) {
				LOG.severe("Cannot wait until job thread is terminated, when removing job '"+job.getJobId()+"'." + e.getMessage() + 
						"\n" + UwsUtils.dumpStackTrace(e) + "\nContinue with job removal anyway...");
			}
			boolean useStorage = job.getStatusManager().getUseStorage();
			if(useStorage){
				try {
					//this method removes all job metadata and data (output dir)
					storage.removeJobMetaDataAndOutputData(job);
				} catch (UwsException e) {
					LOG.severe("Cannot remove job '"+job.getJobId()+"'." + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
					return false;
				}
			}
			return true;
		}
	}
	
	public synchronized void reset(){
		runningJobsCounter = 0;
		maxJobsInMemory = 0;
		maxQueuedTimeMillis = 0;
		inMemoryJobs.clear();
	}
	
	public synchronized boolean canQueueJob(){
		if(maxQueuedJobs < 0){
			//no property value: all jobs can be queued
			return true;
		}
		
		int numJobsInMemory = getNumberOfJobsInMemory();
		int numRunningJobs = getNumberOfRunningJobs();
		int numQueuedJobs = numJobsInMemory - numRunningJobs;

		return numQueuedJobs < maxQueuedJobs;
	}
	
	/*package*/int getMaxQueuedJobs(){
		return maxQueuedJobs;
	}
	
	/*package*/void setMaxQueuedJobs(int maxQueuedJobs) {
		this.maxQueuedJobs = maxQueuedJobs;
	}

	
	@Override
	public String toString(){
		return "Running Jobs List for app: '"+appid+"', list: '"+listid+
				"': Size: "+inMemoryJobs.size()+". Num running jobs: " + runningJobsCounter +
				", max jobs in memory: " + maxJobsInMemory + ", max queued time (ms): " + maxQueuedTimeMillis + 
				", max queued jobs: " + maxQueuedJobs;
	}

}
