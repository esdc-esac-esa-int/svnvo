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
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobStatusManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class UwsJobThread extends Thread {
	
	public final static ThreadGroup tg = new ThreadGroup("UWS_GROUP");

	private static final Logger LOG = Logger.getLogger(UwsJobThread.class.getName());
	
	private UwsJob job;
	private UwsExecutor executor;
	private UwsJobStatusManager jobStatusManager;
	private JobTimeOut jobTimeOut;
	
	/**
	 * This flag is true when the job is being removed. In that case, no phase change must be allowed.
	 */
	private boolean cleaningJob;
	
	private boolean started;
	
	public UwsJobThread(UwsJob job, UwsExecutor executor){
		this.job = job;
		this.executor = executor;
		this.cleaningJob = false;
		jobStatusManager = job.getStatusManager();
		this.started = false;
	}
	
	public UwsJob getJob(){
		return job;
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
		if(hasStarted()){
			throw new UwsException("Cannot update attribute '"+attributeName.name()+"' of job " +
					"'"+job.getJobId()+"' because it has started already.");
		}else{
			job.updateAttribute(attributeName, value);
		}
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
			return;
		}
		if(!setStartTime()){
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
				return;
			}
			
			// Check user context consistency
			checkUserContextConsistency();
			
			
			executor.execute(job);
			if(jobTimeOut != null){
				jobTimeOut.interrupt();
			}
		} catch (InterruptedException e) {
			LOG.severe("Interrupted job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			execAbortJob();
			return;
		} catch (UwsException e) {
			LOG.severe("Error executing job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			setEndTime();
			changePhase(UwsJobPhase.ERROR);
			return;
		} catch (Exception e){
			LOG.severe("Unknown error executing job: " + job + "\n" + UwsUtils.dumpStackTrace(e));
			setEndTime();
			changePhase(UwsJobPhase.ERROR);
			return;
		}
		setEndTime();
		changePhase(UwsJobPhase.COMPLETED);
	}
	
	private void execAbortJob(){
		setEndTime();
		changePhase(UwsJobPhase.ABORTED);
	}
	
	public synchronized void abortJob(){
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
		interrupt();
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
					abortJob();
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
		UwsJobOwner contextOwner = UwsManager.getInstance().getFactory().getSecurityManager().getUser();
		UwsJobOwner jobOwner = this.job.getOwner();
		
		if(contextOwner.getId()!=jobOwner.getId()){
			throw new UwsException("Job owner inconsistent with user context.");
		}
		
	}
}
