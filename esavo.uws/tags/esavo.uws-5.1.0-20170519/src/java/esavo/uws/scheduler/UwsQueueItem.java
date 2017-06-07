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
