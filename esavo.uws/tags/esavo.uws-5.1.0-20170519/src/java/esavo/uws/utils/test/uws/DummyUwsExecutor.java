package esavo.uws.utils.test.uws;

import java.util.HashMap;
import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;

public class DummyUwsExecutor implements UwsExecutor {
	
	private boolean canceled;
	private UwsJob job;
	private boolean executed;
	private Object executedObject;
	private boolean generateUwsException;
	private boolean generateInterruptedException;
	private Map<String, UwsJobResultMeta> resultsForJobs;
	private Map<String, UwsJobErrorSummaryMeta> errorsForJobs;
	
	public DummyUwsExecutor(){
		resultsForJobs = new HashMap<String, UwsJobResultMeta>();
		errorsForJobs = new HashMap<String, UwsJobErrorSummaryMeta>();
	}
	
	public void reset(){
		canceled = false;
		job = null;
		executed = false;
		executedObject = null;
		generateUwsException = false;
		generateInterruptedException = false;
		resultsForJobs.clear();
		errorsForJobs.clear();
	}
	
	public void setResultForJob(String jobid, UwsJobResultMeta result){
		resultsForJobs.put(jobid, result);
	}
	
	public void setErrorForJob(String jobid, UwsJobErrorSummaryMeta error){
		errorsForJobs.put(jobid, error);
	}
	
	public void setExecutedObject(Object o){
		this.executedObject = o;
	}
	
	public UwsJob getJob(){
		return this.job;
	}
	
	public void setGenerateUwsException(boolean generate){
		generateUwsException = generate;
	}
	
	public void setGenerateInterruptedException(boolean generate){
		generateInterruptedException = generate;
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
		UwsJobErrorSummaryMeta error = errorsForJobs.get(job.getJobId());
		job.setErrorSummary(error);
		UwsJobResultMeta result = resultsForJobs.get(job.getJobId());
		if(result != null){
			job.addResult(result);
		}
		return executedObject;
	}

}
