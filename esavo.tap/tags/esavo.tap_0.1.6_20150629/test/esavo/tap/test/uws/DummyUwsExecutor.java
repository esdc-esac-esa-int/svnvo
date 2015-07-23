package esavo.tap.test.uws;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;

public class DummyUwsExecutor implements UwsExecutor {
	
	private boolean canceled;
	private UwsJob job;
	private boolean executed;
	private Object executedObject;
	private boolean generateUwsException;
	private boolean generateInterruptedException;
	
	public void reset(){
		canceled = false;
		job = null;
		executed = false;
		executedObject = null;
		generateUwsException = false;
		generateInterruptedException = false;
	}
	
	public void setExecutedObject(Object o){
		this.executedObject = o;
	}
	
	public UwsJob getJob(){
		return this.job;
	}

	@Override
	public void cancel(UwsJob job) {
		this.job = job;
		canceled = true;
	}

	@Override
	public Object execute(UwsJob job) throws InterruptedException, UwsException {
		this.job = job;
		if(generateUwsException){
			throw new UwsException("Requested exception");
		}
		if(generateInterruptedException){
			throw new InterruptedException("Requested exception");
		}
		executed = true;
		return executedObject;
	}

}
