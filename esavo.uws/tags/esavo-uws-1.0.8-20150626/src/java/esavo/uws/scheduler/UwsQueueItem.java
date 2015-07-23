package esavo.uws.scheduler;

public class UwsQueueItem {
	
	private String queueUser;
	private UwsJobThread jobThread;
	
	public UwsQueueItem(String queueUser, UwsJobThread jobThread){
		this.queueUser = queueUser;
		this.jobThread = jobThread;
	}
	
	/**
	 * @return the queue user identifier
	 */
	public String getQueueUser() {
		return queueUser;
	}
	/**
	 * @return the jobThread
	 */
	public UwsJobThread getJobThread() {
		return jobThread;
	}

}
