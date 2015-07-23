package esavo.tap.test.uws;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;

public class DummyUwsScheduler implements UwsScheduler {
	
	private static final int TEST_DEFAULT_PRIORITY = 0;
	
	private DummyUwsExecutor executor;
	
	private int defaultPriority = TEST_DEFAULT_PRIORITY;
	private boolean aborted = false;
	private UwsJobThread jobThread;
	
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
	}
	
	public UwsJobThread getJobThread(){
		return jobThread;
	}

	@Override
	public boolean abort(UwsJobThread jobThread) {
		this.jobThread = jobThread;
		aborted = true;
		return false;
	}

	@Override
	public boolean enqueue(UwsJobThread jobThread) throws UwsException {
		this.jobThread = jobThread;
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return defaultPriority;
	}
	
	public Object execute() throws InterruptedException, UwsException{
		return executor.execute(jobThread.getJob());
	}

}
