/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobStatusManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsErrorType;
import esavo.uws.utils.UwsUtils;

public class UwsJobThread extends Thread {
	
	public final static ThreadGroup tg = new ThreadGroup("UWS_GROUP");

	private static final Logger LOG = Logger.getLogger(UwsJobThread.class.getName());
	
	public static final boolean FORCE_JOB_INTERRUPTION=true;
	public static final boolean DO_NOT_FORCE_JOB_INTERRUPTION=false;
	
	private UwsJob job;
	private UwsExecutor executor;
	private UwsScheduler scheduler;
	private UwsJobStatusManager jobStatusManager;
	private JobTimeOut jobTimeOut;
	
	/**
	 * This flag is true when the job is being removed. In that case, no phase change must be allowed.
	 */
	private boolean cleaningJob;
	
	private boolean started;
	
	private boolean scheduled;
	
	public UwsJobThread(UwsJob job, UwsExecutor executor, UwsScheduler scheduler){
		super(job.getJobId());
		this.setName("UwsJob"+job.getJobId());
		this.job = job;
		this.executor = executor;
		this.scheduler = scheduler;
		this.cleaningJob = false;
		this.jobStatusManager = job.getStatusManager();
		this.started = false;
		this.scheduled = false;
	}
	
	public boolean scheduleAbort(){
		if(scheduler != null){
			return scheduler.abort(this);
		}else{
			return false;
		}
	}
	
	public synchronized boolean scheduleThread() throws UwsException{
		if(!scheduled){
			scheduled = scheduler.notifyJobArrival(this);
		}
		return scheduled;
	}
	
	public UwsJob getJob(){
		return job;
	}
	
	public UwsJobOwner getOwner(){
		return job.getOwner();
	}
	
	public String getUserId(){
		return job.getOwner().getId();
	}
	
	public synchronized boolean isFinished(){
		if(cleaningJob){
			//job is being removed
			return true;
		}
		return job.isPhaseFinished() && isStopped();
	}

	public synchronized boolean hasStarted(){
		return started;
	}
	
	public synchronized void setStarted(){
		started = true;
	}
	
	public synchronized boolean startIfRequired(){
		if(hasStarted()){
			return false;
		}
		start();
		return true;
	}
	
	public synchronized void waitUntilThreadIsFinished() throws InterruptedException{
		if(hasStarted()){
			UwsUtils.waitUntilThreadIsTerminated(this);
		}else{
			return;
		}
	}
	
	public synchronized void updateAttribute(UwsJobAttribute attributeName, Object value) throws UwsException {

		// If attribute is ExecutionDuration, it can be modified during execution.
		if(attributeName.equals(UwsJobAttribute.ExecutionDuration)){
			long newExecTime = (Long)value;
			long maxExecTime = UwsManager.getInstance().getFactory().getExecutionTimeLimits(job.getListid(), job.getOwner())[1];
			if(maxExecTime>0 && newExecTime > maxExecTime ){
				throw new UwsException("Cannot update attribute '"+attributeName.name()+"' of job " +
						"'"+job.getJobId()+"' because Execution Duration cannot be bigger than user's time quota ("+maxExecTime+" sec)");
			}
			if(hasStarted()){
				
				long currDurationMillis = System.currentTimeMillis() - job.getStartTime().getTime();
				long currDurationSeconds = (long) ((float)currDurationMillis/1000);
				long remaining = newExecTime-currDurationSeconds;
				
				if(jobTimeOut != null){
					jobTimeOut.interrupt();
				}

				if(remaining<=0){
					//Kill the thread gracefully 
					remaining=1;
				}
				

				jobTimeOut = new JobTimeOut(remaining, this);
				jobTimeOut.start();
			}
		}else{
		
			if(hasStarted()){
				throw new UwsException("Cannot update attribute '"+attributeName.name()+"' of job " +
						"'"+job.getJobId()+"' because it has started already.");
			}
		}
				
		job.updateAttribute(attributeName, value);
	}
	
	public synchronized void updateParameters(Map<String,Object> parameters) throws UwsException {
		if(hasStarted()){
			throw new UwsException(UwsOutputResponseHandler.PERMISSION_DENIED, "Cannot update parameters of job " +
					"'"+job.getJobId()+"' because it has started already.");
		}else{
			job.addOrUpdateParameters(parameters);
		}
	}
	
	public void run(){
		setStarted();
		if(!changePhase(UwsJobPhase.EXECUTING)){
			notifyJobFinished();
			return;
		}
		if(!setStartTime()){
			notifyJobFinished();
			return;
		}
		//exec max duration in seconds
		long maxDuration = job.getExecutionDuration();
		if(maxDuration > 0){
			//set execution limit
			jobTimeOut = new JobTimeOut(maxDuration, this);
			jobTimeOut.start();
		}
		try {
			if(cleaningJob){
				//the job is being removed.
				notifyJobFinished();
				return;
			}
			
			// Check user context consistency
			//checkUserContextConsistency();
			
			
			executor.execute(job);
			if(jobTimeOut != null){
				jobTimeOut.interrupt();
			}
		} catch (InterruptedException e) {
			LOG.severe("Interrupted job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			setEndTime();
			changePhase(UwsJobPhase.ABORTED);

			notifyJobFinished();
			return;
		} catch (UwsException e) {
			LOG.severe("Error executing job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			setEndTime();
			changePhase(UwsJobPhase.ERROR);
			
			notifyJobFinished();
			return;
		} catch (Exception e){
			LOG.severe("Unknown error executing job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			setEndTime();
			
			
			changePhase(UwsJobPhase.ERROR);
			
			notifyJobFinished();
			return;
		}
		setEndTime();
		changePhase(UwsJobPhase.COMPLETED);
		notifyJobFinished();
	}
	
	public synchronized void abortJob(boolean forceInterruption){
		setCleaningJob(true);
		if(jobTimeOut != null){
			jobTimeOut.interrupt();
		}
		try {
			job.cancel();
		} catch (UwsException e) {
			LOG.severe("Error canceling job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
		}
		try {
			executor.cancel(job);
		} catch (UwsException e) {
			LOG.severe("Error canceling job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
		}
		// INTERRUPTING THE THREAD HERE CAUSED THE OUTPUT TO BE INCOMPLETE, BECAUSE MESSAGES CLOSING THE VOTABLE
		// WHERE NOT WRITTEN. INTERRUPTION REMOVED: FINALIZATION OF THREAD IS ENSURED WITH THE 
		// LimitedResultSetStarTable AND TapWriter THAT MONITOR THE JOB STATUS AND STOP WRITTING ONCE THE JOB IS ABORTED.
		if(forceInterruption){
			interrupt();
		}
	}

	private boolean changePhase(UwsJobPhase phase){
		if(cleaningJob){
			//the job is being removed: it does not matter the phase. Abort changing phase.
			return true;
		}
		try {
			job.setPhase(phase);
		} catch (UwsException e) {
			e.printStackTrace();
			jobStatusManager.updateStatusError("Error when updating job phase", e);
			return false;
		}
		return true;
	}
	
	private boolean setEndTime(){
		if(cleaningJob){
			//the job is being removed: it does not matter the new parameter value. Ignore.
			return true;
		}
		try {
			job.setEndTime(new Date());		
		} catch (UwsException e) {
			e.printStackTrace();
			jobStatusManager.updateStatusError("Error when updating job end time", e);
			return false;
		}
		return true;
	}
	
	private boolean setStartTime(){
		if(cleaningJob){
			//the job is being removed: it does not matter the new parameter value. Ignore.
			return true;
		}
		try {
			job.setStartTime(new Date());		
		} catch (UwsException e) {
			e.printStackTrace();
			jobStatusManager.updateStatusError("Error when updating job end time", e);
			return false;
		}
		return true;
	}
	
	private synchronized void setCleaningJob(boolean cleaningJob){
		this.cleaningJob = cleaningJob;
	}
	
	private final boolean isStopped(){
		return !isAlive() || isInterrupted();
	}
	
	private final class JobTimeOut extends Thread {
		private String id;
		//in seconds
		private long executionDuration;
		private UwsJobThread parent;
		
		/**
		 * 
		 * @param executionDuration in secodns
		 * @param parent
		 */
		public JobTimeOut(long executionDuration, UwsJobThread parent){
			super(UwsJobThread.tg, "TimeOut_"+job.getJobId());
			this.id = "TimeOut_"+job.getJobId();
			this.parent = parent;
			this.executionDuration = executionDuration;
		}
		
		public String getIdentifier(){
			return id;
		}
		
		@Override
		public void run(){
			if(cleaningJob){
				//the job is being removed
				return;
			}
			try{
				parent.join(executionDuration*1000);
				if(cleaningJob){
					//the job is being removed
					return;
				}
				if (!isFinished()){
					UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta("Maximum execution time ("+job.getExecutionDuration()+" s) reached. Job aborted.", UwsErrorType.FATAL);
					try {
						job.setErrorSummary(errorSummary);
					} catch (UwsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Jobs aborted by timeout should finish gracefully
					abortJob(DO_NOT_FORCE_JOB_INTERRUPTION);
				}
			}catch(InterruptedException ie){
				//ignore: this is called because the user wants to interrupt the thread.
			}
		}
	}
	
	@Override
	public String toString(){
		return "Job thread for job: " + job.toString() + (jobTimeOut == null ? "" : "\nTimeout thread: " + jobTimeOut.getIdentifier());
	}
	
	/**
	 * Checks wether the job owner is the same as the user in security context.
	 * @throws UwsException
	 */
	public void checkUserContextConsistency() throws UwsException{
		UwsSecurity security = UwsManager.getInstance().getFactory().getSecurityManager();
		UwsJobOwner contextOwner = security.getUser(UwsConfiguration.IGNORE_USER_SESSION);
		UwsJobOwner jobOwner = this.job.getOwner();
		
		if(contextOwner==null){
			throw new UwsException("Non existent user context.");
		}
		if(!contextOwner.getId().equals(jobOwner.getId())){
			throw new UwsException("Job owner inconsistent with user context.");
		}
	}
	
	private void notifyJobFinished(){
		if(scheduler!=null){
			scheduler.notifyJobFinished(this);
		}

	}
	
}
