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
package esavo.uws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import esavo.uws.event.UwsEventType;
import esavo.uws.factory.UwsDefaultFactory;
import esavo.uws.factory.UwsFactory;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;
import esavo.uws.share.UwsShareUser;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsDirectoriesCleanerThread;
import esavo.uws.utils.UwsJobsDeletionManagerThread;
import esavo.uws.utils.UwsRestartPendingJobsThread;
import esavo.uws.utils.UwsUtils;

/**
 * UWS manager.
 * <p>Initialization:
 * A call to one {@link #getManager(UwsFactory)} must be performed. 
 * Following access to the manager must be done through {@link #getInstance(String)}}
 * 
 * <p>See default handlers in {@link UwsSimpleExecutorFactory} (and {@link UwsDefaultFactory}).<br/>
 * Probably, you want to create your own factory by creating an extension of {@link UwsDefaultFactory} and adding your own
 * executor and scheduler.
 * 
 * <p>Adding a job:
 * <pre><tt>
 * UwsJobOwner owner = new UwsJobOwner("userid");
 * String listid = "async";
 * int priority = 2;
 * 
 * uwsManager = UwsManager.getInstance("appid");
 * UwsJob job = manager.getCreator().createJob(owner, listid, priority);
 * //or UwsJob = factory.getCreator().createJob(owner, listid, priority);
 * 
 * UwsJobParameter params = job.getParameters();
 * //add parameters...
 * 
 * manager.startJob(job);
 * </tt></pre>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsManager {
	
	private static final Logger LOG = Logger.getLogger(UwsManager.class.getName());
	
	public static final String SYNC_LIST = "sync";

	//private static Map<String, UwsManager> managers = new HashMap<String, UwsManager>();
	private static UwsManager manager;
	
	private String appid;
	private UwsFactory factory;
	private UwsJobsListManager jobsListManager;

	private UwsManager(UwsFactory factory){
		this.factory = factory;
		this.appid = factory.getAppId();
		this.jobsListManager = UwsJobsListManager.getInstance(appid);
		
		LOG.info("UWS manager created: " + this.toString());
		
		UwsRestartPendingJobsThread pendingJobsThread = new UwsRestartPendingJobsThread(appid);
		pendingJobsThread.setDaemon(true);
		pendingJobsThread.start();
		
		UwsJobsDeletionManagerThread deletionManagerThread = new UwsJobsDeletionManagerThread(appid);
		deletionManagerThread.setDaemon(true);
		deletionManagerThread.start();
		
		UwsDirectoriesCleanerThread directoriesCleanerThread = new UwsDirectoriesCleanerThread(appid);
		directoriesCleanerThread.setDaemon(true);
		directoriesCleanerThread.start();
	}
	
	/**
	 * Returns a UWS application manager using the specified factory.
	 * If the manager already exists, the previous instance is returned WITHOUT changing the factory.
	 * @param factory UWS factory. This parameter is ignored if the manager already exists.
	 * @return
	 * @see #getInstance(String)
	 */
	public static synchronized UwsManager getManager(UwsFactory factory){
//		String appid = factory.getAppId();
//		UwsManager manager = managers.get(appid);
//		if(manager == null){
//			manager = new UwsManager(factory);
//			managers.put(appid, manager);
//		}
//		return manager;
		if(manager == null){
			manager = new UwsManager(factory);
		}
		return manager;
	}

	/**
	 * For test-harnesses.<br/>
	 * Returns a UWS application manager using the specified factory.
	 * If the manager already exists, the previous instance is returned WITHOUT changing the factory.
	 * @param factory UWS factory. This parameter is ignored if the manager already exists.
	 * @return
	 * @see #getInstance(String)
	 */
	public static synchronized UwsManager createManager(UwsFactory factory){
		manager = new UwsManager(factory);
		return manager;
	}

//	/**
//	 * Utility method. Call this once the manager has been initialized.<br/>
//	 * The manager can be properly initialized by calling:
//	 * <ul>
//	 * <li>{@link #getManager(UwsFactory)}</li>
//	 * </ul>
//	 * @param appid application identifier
//	 * @param configuration application configuration handler
//	 * @return
//	 */
//	public static synchronized UwsManager getInstance(String appid){
//		UwsManager manager = managers.get(appid);
//		if(manager == null){
//			throw new RuntimeException("Application manager not defined for application '"+appid+"'!");
//		}
//		return manager;
//	}
	
	/**
	 * Utility. The first call must be {@link #getManager(UwsFactory)}
	 * @return
	 */
	public static synchronized UwsManager getInstance(){
		if(manager == null){
			throw new RuntimeException("Application manager not defined!");
		}
		return manager;
	}

	public String getAppId(){
		return appid;
	}

	public UwsFactory getFactory(){
		return factory;
	}

	/**
	 * Removes a job. If the job is running, the job is stopped and removed.<br/>
	 * Checks <code>user</code> permissions. To avoid security checks, call {@link #removeJob(String)}
	 * @param jobid
	 * @param user
	 * @throws UwsException
	 */
	public void removeJob(String jobid, UwsJobOwner currentUser) throws UwsException{
		removeJob(jobid, currentUser, true);
	}
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.<br/>
	 * Does not perform any security check. To perform security checks, call {@link #removeJob(String, UwsJobOwner)}<br/>
	 * This method is called by some internal procedures to clean old jobs.
	 * @param jobid
	 * @param user
	 * @throws UwsException
	 */
	private void removeJob(String jobid) throws UwsException{
		removeJob(jobid, null, false);
	}
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.
	 * Checks <code>user</code> permissions if performSecurityCheck is true
	 * @param jobid
	 * @param user
	 * @param performSecurityCheck
	 * @throws UwsException
	 */
	private synchronized void removeJob(String jobid, UwsJobOwner currentUser, boolean performSecurityCheck) throws UwsException{
		UwsJob job = loadJobInternal(jobid);
		UwsJobOwner owner = job.getOwner();
		//check privileges if required
		if(performSecurityCheck && !UwsUtils.checkValidAccess(owner, currentUser)){
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to remove job '" + jobid + "'");
		}
		UwsStorage storage = factory.getStorageManager();
		if(!jobsListManager.stopAndRemoveRunningJob(jobid, storage)){
			removeFromStorage(job);
		}
		//job is removed: update events
		factory.getEventsManager().setEventTime(currentUser, UwsEventType.JOB_REMOVED_EVENT);
	}
	
	private void raiseSuitableNonAccessException(UwsJobOwner user, String msg) throws UwsException{
		if(user.getId().equals(UwsUtils.ANONYMOUS_USER)){
			//send 401 + headers for basic authentication
			throw new UwsException(UwsOutputResponseHandler.UNAUTHORIZED, "User '" + user + "' " + msg);
		} else {
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "User '" + user + "' " + msg);
		}
	}
	
	/**
	 * Loads a job.
	 * Checks <code>user</code> permissions.
	 * @param jobid
	 * @param currentUser
	 * @return
	 * @throws UwsException
	 */
	public synchronized UwsJob tryLoadJob(String jobid, UwsJobOwner currentUser) throws UwsException{
		UwsJob job = loadJobInternal(jobid);
		UwsJobOwner owner = job.getOwner();
		//check privileges
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to load job '" + jobid + "'");
		}
		return job;
	}
	
	/**
	 * DOES NOT CHECK USER PERMISSIONS: To check, use: {@link #tryLoadJob(UwsJob, UwsJobOwner)}
	 * @param jobid
	 * @return
	 * @throws UwsException raised if the job is not found.
	 */
	private synchronized UwsJob loadJobInternal(String jobid) throws UwsException{
		UwsJobThread jobThread = jobsListManager.getRunningJob(jobid);
		if(jobThread == null){
			UwsStorage storageManager = factory.getStorageManager();
			UwsJob job;
			try{
				job = storageManager.getJobMeta(jobid);
			}catch(UwsException e){
				int code = e.getCode();
				if(code < 0){
					throw new UwsException(UwsOutputResponseHandler.NOT_FOUND, "Job '"+jobid+"' not found.", e);
				}else{
					throw e;
				}
			}
			if(job == null){
				throw new UwsException(UwsOutputResponseHandler.NOT_FOUND, "Job '"+jobid+"' not found.");
			}else{
				return job;
			}
		}else{
			return jobThread.getJob();
		}
	}

	private void removeFromStorage(UwsJob job){
		UwsStorage storageManager = factory.getStorageManager();
		try {
			//this method removes all job metadata and data (output dir)
			storageManager.removeJobMetaDataAndOutputData(job);
		} catch (UwsException e) {
			e.printStackTrace();
			LOG.severe("Cannot remove job '"+job.toString()+"' from storage: " + e.getMessage());
		}
	}

	/**
	 * This method should be called when starting the UWS system in order to restart those jobs that are in pending status.<br/>
	 * This method should be called only once per job.<br/>
	 * Job output data are removed before the job is started.<br/>
	 * @param job job to restart.
	 * @return 'true' if the job is restarted.
	 * @throws UwsException
	 */
	public synchronized boolean restartJob(UwsJob job) throws UwsException {
		UwsStorage storageManager = factory.getStorageManager();
		storageManager.removeJobOutputData(job);
		addJob(job);
		String jobid = job.getJobId();
		return startJobInternal(jobid, jobsListManager.getRunningJob(jobid));
	}
	
	/**
	 * Adds a job to the execution system.<br/>
	 * The job is persisted in its initial state.<br/>
	 * This manager is added as listener of the job in order to obtain further job changes.<br/>
	 * This method enables job listeners (by calling {@link UwsJob#setEnableNotifications(boolean)}).<br/>
	 * Job phase is set to PENDING.<br/>
	 * The job is not launched. In order to launch the job, a call to {@link #startJob(String)} is necessary.<br/>
	 * @param job the job to add.
	 * @throws UwsException
	 */
	public void addJob(UwsJob job) throws UwsException {
		LOG.info("Starting job: " + job);
		UwsStorage storageManager = factory.getStorageManager();
		
		//Persist job
		storageManager.addNewJobMetaIfNeeded(job);
		
		//Enable notifications
		job.setEnableUpdates(true);
		
		//Initial phase
		job.setPhase(UwsJobPhase.PENDING);
		
		//Create thread and add it to the manager running jobs list
		UwsJobThread jobThread = new UwsJobThread(job, factory.getExecutor());
		jobsListManager.addRunningJob(job.getJobId(), jobThread);
	}
	
	/**
	 * Starts a job. The job must be already added (by calling {@link #addJob(UwsJob)}).
	 * The job is scheduled to be started.
	 * @param jobid
	 * @param currentUser
	 * @return
	 * @throws UwsException
	 */
	public synchronized boolean startJob(String jobid, UwsJobOwner currentUser) throws UwsException {
		//UwsJobThread jobThread = getRunningJob(jobid);
		UwsJobThread jobThread = jobsListManager.getRunningJob(jobid);
		if(jobThread == null){
			UwsJob job = tryLoadJob(jobid, currentUser); //will raise a 'not found' exception if the job does not exists
			throw new UwsException("Job '"+jobid+"' cannot be started in phase: " + job.getPhase().name());
		}
		//check privileges
		UwsJobOwner owner = jobThread.getJob().getOwner();
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
//			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
//					"User '" + currentUser.getId() + "' does not have enough privileges to starts job '" + jobid + "'");
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to start job '" + jobid + "'");
		}
		return startJobInternal(jobid, jobThread);
	}
	
	/**
	 * Schedules the job.<br/>
	 * The job must be already added to the UwsManager running jobs list (by calling {@link #addJob(UwsJob)}). Otherwise, 
	 * this method will raise an exception.<br/> 
	 * The job will be enqueued at the scheduler it will be launched when the scheduler decides the job can be run.<br/>
	 * Job phase is set to QUEUED. No further changes are allowed (attributes/parameters changes).<br/>
	 * @param jobid
	 * @param jobThread
	 * @return
	 * @throws UwsException 
	 */
	private synchronized boolean startJobInternal(String jobid, UwsJobThread jobThread) throws UwsException{
		if(jobThread == null){
			throw new UwsException("Job '"+jobid+"' is not ready to be started.");
		} else {
			if(jobThread.hasStarted()){
				return false;
			}else{
				UwsScheduler scheduler = factory.getScheduler();
				return scheduler.enqueue(jobThread);
			}
		}
	}
	
	/**
	 * Aborts a job. The job must be already added (by calling {@link #addJob(UwsJob)}).
	 * The job is set to {@link UwsJobPhase#ABORTED}.
	 * @param jobid
	 * @param currentUser
	 * @return
	 * @throws UwsException
	 */
	public synchronized void abortJob(String jobid, UwsJobOwner currentUser) throws UwsException {
		//1. check the job is in 'ready to run/running' list
		UwsJobThread jobThread = jobsListManager.getRunningJob(jobid);
		if(jobThread == null){
			//the job cannot be aborted because it is not in the running queue.
			//An exception will be raised.
			//Try to load the job to obtain details.
			//If the job exists, a 'cannot abort' exception is raised.
			//If the job does not exist, a 'not found' exception is raised instead.
			UwsJob job = tryLoadJob(jobid, currentUser); //will raise a 'not found' exception if the job does not exists
			throw new UwsException("Job '"+jobid+"' cannot be aborted in phase: " + job.getPhase().name());
		}
		
		//2. check privileges
		UwsJobOwner owner = jobThread.getJob().getOwner();
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
//			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
//					"User '" + currentUser.getId() + "' does not have enough privileges to abort job '" + jobid + "'");
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to abort job '" + jobid + "'");
		}
		
		//3. OK, abort job:
		
		//Remove from the scheduler
		UwsScheduler scheduler = factory.getScheduler();
		//Returns 'true' if the job was found in the scheduler queue
		scheduler.abort(jobThread);
		
		//Remove from 'ready to run/running' list
		jobsListManager.removeRunningJob(jobid);
		
		//Abort the thread if it is running
		//this method set the job to phase ABORT
		jobThread.abortJob();
		
		//4. Update job status (phase)
		jobThread.getJob().setPhase(UwsJobPhase.ABORTED);
	}
	

	
	/**
	 * Tries to update a job attribute.<br/>
	 * Checks <code>user</code> permissions.
	 * <p>A job can be modified if its thread is not running yet.
	 * @param jobid
	 * @param currentUser
	 * @param attributeName
	 * @param value
	 */
	public void tryUpdateJobAttribute(String jobid, UwsJobOwner currentUser, UwsJobAttribute attributeName, Object value) throws UwsException {
		//UwsJobThread jobThread = getRunningJob(jobid);
		UwsJobThread jobThread = jobsListManager.getRunningJob(jobid);
		if(jobThread == null){
			//Only jobs in the 'pending' queue can be updated.
			//Jobs in pending queue must be also in 'running jobs' list.
			throw new UwsException("Job '"+jobid+"' cannot be updated.");
		}
		UwsJobOwner owner = jobThread.getJob().getOwner();
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
//			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
//					"User '" + currentUser.getId() + "' does not have enough privileges to update attribute '"+attributeName.name()+
//					"' of job '" + jobid + "'");
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to update attribute '"+
					attributeName.name()+"' of job '" + jobid + "'");
		}
		//valid access
		jobThread.updateAttribute(attributeName, value);
	}
	
	/**
	 * Tries to update a job attribute.
	 * Checks <code>user</code> permissions.
	 * <p>A job can be modified if its thread is not running yet.
	 * @param jobid
	 * @param currentUser
	 * @param parameterName
	 * @param value
	 */
	public void tryUpdateJobParameters(String jobid, UwsJobOwner currentUser, Map<String, Object> parameters) throws UwsException {
		//UwsJobThread jobThread = getRunningJob(jobid);
		UwsJobThread jobThread = jobsListManager.getRunningJob(jobid);
		if(jobThread == null){
			//Only jobs in 'pending' queue can be updated.
			//A job in 'pending' queue must be in 'running jobs' list also.
			throw new UwsException("Job '"+jobid+"' cannot be updated.");
		}
		UwsJobOwner owner = jobThread.getJob().getOwner();
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
//			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
//					"User '" + currentUser.getId() + "' does not have enough privileges to update parameters of job '" + jobid + "'");
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to update parameters of job '" + jobid + "'");
		}
		jobThread.updateParameters(parameters);
	}
	
	/**
	 * Returns a job based on the jobName and currentUser (and session id, associated
	 * to the user: {@link UwsJobOwner#getSession()}, if it is provided).
	 * @param listName
	 * @param currentUser
	 * @return a jobs list. Can be null.
	 * @throws UwsException
	 */
	public List<UwsJob> getJobList(String listName, UwsJobOwner currentUser) throws UwsException {
//		List<String> validUsers = new ArrayList<String>();
//		String userid = currentUser.getId();
//		validUsers.add(userid);
//		if(!UwsUtils.isAnonymous(userid)){
//			//add anonymous jobs also
//			validUsers.add(UwsUtils.ANONYMOUS_USER);
//		}
//		UwsStorage storage = getFactory().getStorageManager(); 
//		List<UwsJob> jobs = storage.getJobsByList(listName, validUsers);
//		return jobs;
		
		List<UwsOwnerSessionFilter> filter = new ArrayList<UwsOwnerSessionFilter>();
		String userid = currentUser.getId();
		filter.add(new UwsOwnerSessionFilter(userid, currentUser.getSession()));
		UwsStorage storage = getFactory().getStorageManager();
		List<UwsJob> jobs = storage.getJobsByList(listName, filter, appid);
		return jobs;
	}
	
	/**
	 * Executes a sync job. Output data (result/error) is saved in job directory.
	 * Job directory is created! it must be removed once the data is served.
	 * @param job
	 * @throws UwsException
	 */
	public void executeSyncJob(UwsJob job) throws UwsException {
		job.getStatusManager().setEnableUpdates(false);
		UwsJobThread jobThread = new UwsJobThread(job, getFactory().getExecutor());
		jobThread.start();
		try {
			jobThread.join(0);
		} catch (InterruptedException e) {
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Job thread interrupted.", e);
		}
		if(job.getPhase().equals(UwsJobPhase.ERROR)){
			UwsJobErrorSummaryMeta error = job.getErrorSummary();
			if(error == null){
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unknow error");
			}else{
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, error.getMessage(), error.getExceptionOutputFormat());
			}
		}
	}
	
	public String checkJobsRemovalProcedure() {
		StringBuilder sb = new StringBuilder();
		
		long currentTime = System.currentTimeMillis();

		UwsStorage storage = getFactory().getStorageManager();
		
		LOG.info("Jobs removal procedure starts...");
		sb.append(UwsUtils.formatDate(new Date(System.currentTimeMillis()))).append('\n');
		sb.append("Jobs removal procedure starts...\n");

		List<String> jobsToRemove = null;
		try {
			jobsToRemove = storage.getOldJobs(appid, currentTime);
		} catch (UwsException e) {
			LOG.severe("Cannot obtain old jobs:" + e.getMessage());
			sb.append("Cannot obtain old jobs:").append(e.getMessage()).append('\n');
			return sb.toString();
		}
		
		if (jobsToRemove != null && jobsToRemove.size() > 0) {
			LOG.info("Jobs removal procedure: number of 'old jobs': " + jobsToRemove.size());
			sb.append("Jobs removal procedure: number of 'old jobs': ").append(jobsToRemove.size()).append('\n');
			// Call removal functionality
			for (String jobid : jobsToRemove) {
				try {
					LOG.info("Removing job: " + jobid);
					sb.append("Removing job: ").append(jobid).append('\n');
					removeJob(jobid);
				} catch (UwsException e) {
					LOG.severe("Cannot remove job " + jobid + " due to: " + e.getMessage());
					sb.append("Cannot remove job ").append(jobid).append(" due to: ").append(e.getMessage()).append('\n');
				}
			}
		}else{
			LOG.info("Jobs removal procedure: number of 'old jobs': None");
			sb.append("Jobs removal procedure: number of 'old jobs': None\n");
		}
		
		LOG.info("Jobs removal procedure finished.");
		sb.append("Jobs removal procedure finished.\n");
		return sb.toString();
	}
	
	/**
	 * Returns the user details related to sharing information.
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public UwsShareUser getSharedUser(String userid) throws UwsException {
		return getFactory().getShareManager().getSharedUser(userid);
	}
	
	/**
	 * Returns the number of jobs in memory (phase = PENDING, QUEUE, RUNNING, HELD, SUSPENDED)
	 * @return the number of jobs in memory 
	 */
	public int getNumberOfJobsInMemory(){
		return jobsListManager.getNumberOfJobsInMemory();
	}


	@Override
	public String toString(){
		return "UWS Manager for application '" + appid + "'\n" + factory.toString();
	}
	
}
