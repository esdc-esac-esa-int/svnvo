package esavo.uws.utils.test.uws;

import esavo.uws.UwsException;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;

public class DummyUwsScheduler implements UwsScheduler {
	
	public static final int TEST_DEFAULT_PRIORITY = 0;
	
	private DummyUwsExecutor executor;
	
	private int defaultPriority = TEST_DEFAULT_PRIORITY;
	private boolean aborted = false;
	private UwsJobThread jobThread;
	private SchedulerMode mode = SchedulerMode.ALL;
	private boolean startThread = false;
	
	public DummyUwsScheduler(DummyUwsExecutor executor){
		this.executor = executor;
	}
	
	public void setDefaultPriority(int defaultPriority){
		this.defaultPriority = defaultPriority;
		jobThread = null;
	}
	
	public void reset(){
		defaultPriority = TEST_DEFAULT_PRIORITY;
		aborted = false;
		jobThread = null;
		startThread = false;
	}
	
	public UwsJobThread getJobThread(){
		return jobThread;
	}
	
	public boolean isAborted(){
		return aborted;
	}

	@Override
	public boolean abort(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		aborted = true;
		return false;
	}

	@Override
	public boolean notifyJobArrival(UwsJobThread jobThread) throws UwsException {
		this.jobThread = jobThread;
		if(startThread){
			jobThread.start();
		}
		return true;
	}

	@Override
	public boolean notifyJobFinished(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		return true;
	}


	@Override
	public int getDefaultPriority(String listid) {
		return defaultPriority;
	}
	
	public Object execute() throws InterruptedException, UwsException{
		return executor.execute(jobThread.getJob());
	}

	@Override
	public void setSchedulerMode(SchedulerMode mode) {
		this.mode=mode;
		
	}

	@Override
	public SchedulerMode getSchedulerMode() {
		return mode;
	}

	/**
	 * @return the startThread
	 */
	public boolean isStartThread() {
		return startThread;
	}

	/**
	 * @param startThread the startThread to set
	 */
	public void setStartThread(boolean startThread) {
		this.startThread = startThread;
	}


}
