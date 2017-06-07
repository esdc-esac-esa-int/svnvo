package esavo.uws;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.factory.UwsDefaultFactory;
import esavo.uws.factory.UwsFactory;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobStatusManager;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.scheduler.UwsScheduler;
import esavo.uws.share.UwsShareUser;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsDirectoriesCleanerThread;
import esavo.uws.utils.UwsErrorType;
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
	
	private static final boolean NOTIFY_EVENTS = true;
	private static final boolean DO_NOT_NOTIFY_EVENTS = false;
	private static final boolean PERFORM_SECURITY_CHECKS = true;
	private static final boolean DO_NOT_PERFORM_SECURITY_CHECKS = false;

	//private static Map<String, UwsManager> managers = new HashMap<String, UwsManager>();
	private static UwsManager manager;
	
	private String appid;
	private UwsFactory factory;
	//private UwsJobsListManager jobsListManager;

	private UwsManager(UwsFactory factory) throws UwsException{
		this.factory = factory;
		this.appid = factory.getAppId();
		//this.jobsListManager = UwsJobsListManager.getInstance(appid);
		
		//will raise an exception if a manager is not set
		checkManagers();
		
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
	 * @throws UwsException 
	 * @see #getInstance(String)
	 */
	public static synchronized UwsManager getManager(UwsFactory factory) throws UwsException{
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
	 * @throws UwsException 
	 * @see #getInstance(String)
	 */
	public static synchronized UwsManager createManager(UwsFactory factory) throws UwsException{
		manager = new UwsManager(factory);
		return manager;
	}

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
	public void removeJob(String jobid, String listid, UwsJobOwner currentUser) throws UwsException{
		removeJob(jobid, listid, currentUser, PERFORM_SECURITY_CHECKS, NOTIFY_EVENTS);
	}
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.<br/>
	 * Does not perform any security check. To perform security checks, call {@link #removeJob(String, UwsJobOwner)}<br/>
	 * This method is called by some internal procedures to clean old jobs.
	 * @param jobid
	 * @param user
	 * @throws UwsException
	 */
	private void removeJob(String jobid, String listid) throws UwsException{
		removeJob(jobid, listid, null, DO_NOT_PERFORM_SECURITY_CHECKS, NOTIFY_EVENTS);
	}
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.<br/>
	 * Does not perform any security check. To perform security checks, call {@link #removeJob(String, UwsJobOwner)}<br/>
	 * This method is called by some internal procedures to clean old jobs.
	 * @param jobid
	 * @param user
	 * @throws UwsException
	 */
	public void removeJob(UwsJob job) throws UwsException {
		removeJob(job, null, DO_NOT_PERFORM_SECURITY_CHECKS, NOTIFY_EVENTS);
	}
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.
	 * Checks <code>user</code> permissions if performSecurityCheck is true
	 * @param jobid
	 * @param user
	 * @param performSecurityCheck
	 * @throws UwsException
	 */
	private synchronized void removeJob(String jobid, String listid, UwsJobOwner currentUser, boolean performSecurityCheck, boolean notify) throws UwsException{
		UwsJob job = loadJobInternal(jobid, listid);
		removeJob(job, currentUser, performSecurityCheck, notify);
	}
	
	
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.
	 * Checks <code>user</code> permissions if performSecurityCheck is true
	 * @param job
	 * @param user
	 * @param performSecurityCheck
	 * @throws UwsException
	 */
	private synchronized void removeJob(UwsJob job, UwsJobOwner currentUser, boolean performSecurityCheck, boolean notify) throws UwsException{
		String jobid = job.getJobId();
		UwsJobOwner owner = job.getOwner();
		//check privileges if required
		if(performSecurityCheck && !UwsUtils.checkValidAccess(owner, currentUser)){
			raiseSuitableNonAccessException(currentUser, "does not have enough privileges to remove job '" + jobid + "'");
		}
		UwsStorage storage = factory.getStorageManager();
		String listid = job.getListid();
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, listid);
		if(!jobsListManager.stopAndRemoveRunningJob(jobid, storage)){
			removeFromStorage(job);
		}
		if(notify){
			//job is removed: update events
			factory.getEventsManager().setEventTime(currentUser, UwsEventType.JOB_REMOVED_EVENT);
		}
	}
	
	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.<br/>
	 * Checks <code>user</code> permissions. To avoid security checks, call {@link #removeJob(String)}
	 * @param jobids
	 * @param user
	 * @throws UwsException
	 */
	public void removeJobs(List<String> jobids, String listid, UwsJobOwner currentUser) throws UwsException{
		removeJobs(jobids, listid, currentUser, PERFORM_SECURITY_CHECKS);
	}

	
	/**
	 * Removes a job. If the job is running, the job is stopped and removed.
	 * Checks <code>user</code> permissions if performSecurityCheck is true
	 * @param jobids
	 * @param user
	 * @param performSecurityCheck
	 * @throws UwsException
	 */
	private synchronized void removeJobs(List<String> jobids, String listid, UwsJobOwner currentUser, boolean performSecurityCheck) throws UwsException{
		if(jobids == null){
			return;
		}
		if(jobids.size() < 1){
			return;
		}
		for(String id: jobids){
			removeJob(id, listid, currentUser, performSecurityCheck, DO_NOT_NOTIFY_EVENTS);
		}
		//jobs are removed: update events
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
		UwsJob job = loadJobInternal(jobid, UwsConfiguration.ASYNC_LIST_ID);
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
	private synchronized UwsJob loadJobInternal(String jobid, String listid) throws UwsException{
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, listid);
		UwsJobThread jobThread = jobsListManager.getInMemoryJob(jobid);
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

		//set new phase 'PENDING'. it does not matter it is saved (storage) because 'PENDING' jobs are retrieved when starting up the server
		addJob(job);
		
		String origPhaseParam = null;
		UwsJobParameters params = job.getParameters();
		if(params != null){
			origPhaseParam = params.getStringParameter("phase");
		}
		
		if("run".equalsIgnoreCase(origPhaseParam)){
			//phase queued is set in startJob (scheduler)
			String jobid = job.getJobId();
			UwsJobOwner owner = job.getOwner();
			return startJob(jobid, owner);
		}
		
		return false;
//
//		
//		String jobid = job.getJobId();
//		String listid = job.getListid();
//		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, listid);
//		return startJobInternal(jobid, jobsListManager.getInMemoryJob(jobid));
	}
	
	/**
	 * Adds a job to the execution system.<br/>
	 * The job is persisted in its initial state.<br/>
	 * This manager is added as listener of the job in order to obtain further job changes.<br/>
	 * This method enables job listeners (by calling {@link UwsJob#setEnableNotifications(boolean)}).<br/>
	 * Job phase is set to PENDING.<br/>
	 * The job is not launched. In order to launch the job, a call to {@link #startJob(String)} is necessary.<br/>
	 * A jobThread is created and added to the list in memory.<br/>
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
		UwsExecutor executor = factory.getExecutor();
		UwsScheduler scheduler = factory.getScheduler();
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		String listid = job.getListid();
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, listid);
		jobsListManager.addInMemoryJob(job.getJobId(), jobThread);
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
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, UwsConfiguration.ASYNC_LIST_ID);
		UwsJobThread jobThread = jobsListManager.getInMemoryJob(jobid);
		if(jobThread == null){
			UwsJob job = tryLoadJob(jobid, currentUser); //will raise a 'not found' exception if the job does not exists
			throw new UwsException("Job '"+jobid+"' cannot be started in phase: " + job.getPhase().name());
		}
		//check privileges
		UwsJobOwner owner = jobThread.getJob().getOwner();
		if(!UwsUtils.checkValidAccess(owner, currentUser)){
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
				//UwsScheduler scheduler = factory.getScheduler(jobThread.getJob());
				//return scheduler.enqueue(jobThread);
				return jobThread.scheduleThread();
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
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, UwsConfiguration.ASYNC_LIST_ID);
		UwsJobThread jobThread = jobsListManager.getInMemoryJob(jobid);
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
//		UwsScheduler scheduler = factory.getScheduler();
//		//Returns 'true' if the job was found in the scheduler queue
//		scheduler.abort(jobThread);
		jobThread.scheduleAbort();
		
		//Remove from 'ready to run/running' list
		jobsListManager.removeInMemoryJob(jobid);
		
		//Abort the thread if it is running
		//this method set the job to phase ABORT
		jobThread.abortJob(UwsJobThread.DO_NOT_FORCE_JOB_INTERRUPTION);
		
		//4. Update job status (phase)
		jobThread.getJob().setPhase(UwsJobPhase.ABORTED);

		//5. Update job error summary
		UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta("Job was aborted.", UwsErrorType.FATAL);
		jobThread.getJob().setErrorSummary(errorSummary);

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
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, UwsConfiguration.ASYNC_LIST_ID);
		UwsJobThread jobThread = jobsListManager.getInMemoryJob(jobid);
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
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, UwsConfiguration.ASYNC_LIST_ID);
		UwsJobThread jobThread = jobsListManager.getInMemoryJob(jobid);
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
	
//	/**
//	 * Returns a job list based on the jobName and currentUser (and session id, associated
//	 * to the user: {@link UwsJobOwner#getSession()}, if it is provided).
//	 * @param listName
//	 * @param currentUser
//	 * @return a jobs list. Can be null.
//	 * @throws UwsException
//	 */
//	public List<UwsJob> getJobsMetaList(String listName, UwsJobOwner currentUser) throws UwsException {
//		return getJobsMetaList(listName, currentUser, null, null, null);
//	}
	
//	/**
//	 * Returns a job list based on the jobName and currentUser (and session id, associated
//	 * to the user: {@link UwsJobOwner#getSession()}, if it is provided).
//	 * @param listName
//	 * @param currentUser
//	 * @param limit
//	 * @param offset
//	 * @return a jobs list. Can be null.
//	 * @throws UwsException
//	 */
//	public List<UwsJob> getJobsMetaList(String listName, UwsJobOwner currentUser, Integer limit, Integer offset, String order) throws UwsException {
//		List<UwsOwnerSessionFilter> filter = new ArrayList<UwsOwnerSessionFilter>();
//		String userid = currentUser.getId();
//		filter.add(new UwsOwnerSessionFilter(userid, currentUser.getSession()));
//		UwsStorage storage = getFactory().getStorageManager();
//		List<UwsJob> jobs = storage.getJobsMetaByList(listName, filter, appid, limit, offset, order);
//		return jobs;
//	}
	
	/**
	 * Returns a job list (only metadata) based on the filter
	 * @param listName
	 * @param filter
	 * @param limit
	 * @param offset
	 * @param order
	 * @param metadataOnly
	 * @return
	 * @throws UwsException
	 */
	public List<UwsJob> getJobsMetaList(String listName, UwsJobsFilter filter, Integer limit, Integer offset, String order, boolean metadataOnly) throws UwsException {
		UwsStorage storage = getFactory().getStorageManager();
		List<UwsJob> jobs = storage.getJobsByFilter(listName, filter, appid, limit, offset, order, metadataOnly);
		return jobs;
	}
	
	/**
	 * Returns the number of jobs after applying the filter.
	 * @param listName
	 * @param filter
	 * @return
	 */
	public int getJobsMetaListNumber(String listName, UwsJobsFilter filter) throws UwsException {
		UwsStorage storage = getFactory().getStorageManager();
		return storage.getJobsNumberByFilter(listName, filter, appid);
	}
	
	/**
	 * Returns the total number of jobs in a job list based on the jobName and currentUser (and session id, associated
	 * to the user: {@link UwsJobOwner#getSession()}, if it is provided).
	 * @param listName
	 * @param currentUser
	 * @return The number of jobs in the list.
	 * @throws UwsException
	 */
	public Integer getJobListTotal(String listName, UwsJobOwner currentUser) throws UwsException {
		List<UwsOwnerSessionFilter> filter = new ArrayList<UwsOwnerSessionFilter>();
		String userid = currentUser.getId();
		filter.add(new UwsOwnerSessionFilter(userid, currentUser.getSession()));
		UwsStorage storage = getFactory().getStorageManager();
		Integer numberOfResults = storage.getNumberOfJobsByList(listName, filter, appid);
		return numberOfResults;
	}
	
	/**
	 * Executes a sync job. Output data (result/error) is saved in job directory.
	 * Job directory is created! it must be removed once the data is served.
	 * @param job
	 * @throws UwsException
	 */
	public void executeSyncJob(UwsJob job) throws UwsException {
		//get list and verify max num sync jobs
		String listid = job.getListid();
		UwsJobsListManager jobsListManager = UwsJobsListManager.getInstance(appid, listid);
		if(!jobsListManager.canQueueJob()){
			throw new UwsException(UwsOutputResponseHandler.SERVICE_UNAVAILABLE, "Server overloaded: job rejected. Please, try again later.");
		}

		UwsJobStatusManager jobStatusManager = job.getStatusManager();
		jobStatusManager.setEnableUpdates(true);
		jobStatusManager.setUseStorage(false);
		UwsExecutor executor = factory.getExecutor();
		UwsScheduler scheduler = factory.getScheduler();
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);

		//Add job to the list
		jobsListManager.addInMemoryJob(job.getJobId(), jobThread);
		jobThread.scheduleThread();
		
		//jobThread.start();
		try {
			while(!jobThread.hasStarted()){
				Thread.sleep(100);
			}
			jobThread.join(0);
		} catch (InterruptedException e) {
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Job thread interrupted.", e);
		}
		if(job.getPhase().equals(UwsJobPhase.ERROR)){
			UwsJobErrorSummaryMeta error = job.getErrorSummary();
			if(error == null){
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unknow error");
			}else{
				throw new UwsException(error.getHttpErrorCode(), error.getMessage(), error.getExceptionOutputFormat());
				//throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, error.getMessage(),UwsExceptionOutputFormat.XML);
			}
		}
	}
	
	public String checkJobsRemovalProcedure() {
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		String olderThanTime = configuration.getProperty(UwsConfiguration.UWS_JOBS_DELTA_DESTRUCTION_TIME);
		
		StringBuilder sb = new StringBuilder();
		
		long currentTime = System.currentTimeMillis();
		long deltaDestructionTime = UwsUtils.parseDestructionTime(olderThanTime);

		UwsStorage storage = getFactory().getStorageManager();
		
		LOG.info("Jobs removal procedure starts... (appId = "+appid+") ");
		sb.append(UwsUtils.formatDate(new Date(System.currentTimeMillis()))).append('\n');
		sb.append("Jobs removal procedure starts...\n");

		
		// Use Set to ensure no duplicates are taken.
		Set<UwsJobDetails> jobsToRemove = new HashSet<UwsJobDetails>();
		
		/// Destroy jobs with destruction time set.
		try {
			List<UwsJobDetails> jobs = storage.getJobsToDestroy(appid, currentTime);
			for(UwsJobDetails v:jobs){
				jobsToRemove.add(v);
			}
		} catch (UwsException e) {
			LOG.severe("Cannot obtain jobs to destroy:" + e.getMessage());
			sb.append("Cannot obtain jobs to destroy:").append(e.getMessage()).append('\n');
			return sb.toString();
		}
		
		/// Destroy PENDING jobs with creation time older than current-delta
		try {
			List<UwsJobDetails> jobs = storage.getOlderJobs(appid, UwsJobPhase.PENDING, currentTime-deltaDestructionTime);
			for(UwsJobDetails v:jobs){
				jobsToRemove.add(v);
			}
		} catch (UwsException e) {
			LOG.severe("Cannot obtain old pending jobs:" + e.getMessage());
			sb.append("Cannot obtain old pending jobs:").append(e.getMessage()).append('\n');
			return sb.toString();
		}
		
		if (jobsToRemove != null && jobsToRemove.size() > 0) {
			LOG.info("Jobs removal procedure: number of 'jobs to remove': " + jobsToRemove.size());
			sb.append("Jobs removal procedure: number of 'jobs to remove': ").append(jobsToRemove.size()).append('\n');
			// Call removal functionality
			for (UwsJobDetails jobitem : jobsToRemove) {
				String jobid = jobitem.getJobid();
				String listid = jobitem.getListid();
				try {
					LOG.info("Removing job: " + jobitem);
					sb.append("Removing job: ").append(jobid).append('\n');
					removeJob(jobitem.getJobid(), listid);
				} catch (UwsException e) {
					LOG.severe("Cannot remove job " + jobid + " due to: " + e.getMessage());
					sb.append("Cannot remove job ").append(jobid).append(" due to: ").append(e.getMessage()).append('\n');
				}
			}
		}else{
			LOG.info("Jobs removal procedure: number of 'jobs to remove': None");
			sb.append("Jobs removal procedure: number of 'jobs to remove': None\n");
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
	public Map<String, Long> getNumberOfJobsInMemory(){
		return UwsJobsListManager.getNumberOfJobs();
	}
	
	/**
	 * Checks all managers are not null.
	 * If a manager is not set, an exception is raised.
	 */
	private void checkManagers() throws UwsException {
		boolean errorsFound = false;
		StringBuilder sb = new StringBuilder("The following managers are not set:\n");
		
		if(factory.getSecurityManager() == null){
			errorsFound = true;
			sb.append("\tSecurity Manager\n");
		}

		if(factory.getStorageManager() == null){
			errorsFound = true;
			sb.append("\tStorage Manager\n");
		}

		if(factory.getConfiguration() == null){
			errorsFound = true;
			sb.append("\tConfiguration Manager\n");
		}

		if(factory.getExecutor() == null){
			errorsFound = true;
			sb.append("\tExecutor Manager\n");
		}

		if(factory.getScheduler() == null){
			errorsFound = true;
			sb.append("\tScheduler Manager\n");
		}
		
		if(factory.getCreator() == null){
			errorsFound = true;
			sb.append("\tCreator Manager\n");
		}
		
		if(factory.getOutputHandler() == null){
			errorsFound = true;
			sb.append("\tOutput Manager\n");
		}
		
		if(factory.getJobsOwnersManager() == null){
			errorsFound = true;
			sb.append("\tJob Owners Manager\n");
		}
		
		if(factory.getShareManager() == null){
			errorsFound = true;
			sb.append("\tShare Manager\n");
		}
		
		if(factory.getEventsManager() == null){
			errorsFound = true;
			sb.append("\tEvents Manager\n");
		}
		
		if(factory.getNotificationsManager() == null){
			errorsFound = true;
			sb.append("\tNotifications Manager\n");
		}
		
		if(errorsFound){
			throw new UwsException(sb.toString());
		}
	}

	@Override
	public String toString(){
		return "UWS Manager for application '" + appid + "'\n" + factory.toString();
	}
	
}
