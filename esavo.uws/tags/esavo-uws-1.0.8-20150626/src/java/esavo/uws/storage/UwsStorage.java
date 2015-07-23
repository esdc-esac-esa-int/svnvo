package esavo.uws.storage;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.utils.UwsUtils;

public interface UwsStorage {
	
	/**
	 * Returns the application identifier.
	 * @return the application identifier.
	 */
	public String getAppId();
	
	
	/**
	 * Returns a job from the storage.
	 * @param jobid
	 * @return
	 * @throws UwsException
	 */
	public UwsJob getJobMeta(String jobid) throws UwsException;
	
	/**
	 * Tests whether a jobid exists;
	 * @param jobid
	 * @return
	 * @throws UwsException
	 */
	public boolean checkJobExists(String jobid) throws UwsException;
	
	/**
	 * Returns an owner from the storage. If the user is not found, an exception is raised.
	 * To test whether a user exists or not, use {@link #getOwnerIfAvailable(String)} instead.
	 * @param ownerid
	 * @return
	 * @throws UwsException raised if the owner is not found.
	 */
	public UwsJobOwner getOwner(String ownerid) throws UwsException;
	
	/**
	 * Returns an owner from the storage. If the owner is not found, 'null' is returned.
	 * This method will not raise an exception if the user is not found.
	 * To raise an exception, use {@link #getOwner(String)}
	 * @param ownerid
	 * @return
	 * @throws UwsException
	 */
	public UwsJobOwner getOwnerIfAvailable(String ownerid) throws UwsException;
	
	/**
	 * Adds an owner to the storage.
	 * @param owner
	 * @throws UwsException
	 */
	public void addOwner(UwsJobOwner owner) throws UwsException;
	
	/**
	 * This method checks whether the job already exists (useful when restoring jobs from database: the job already exists)
	 * @param job
	 * @throws SQLException
	 */
	public boolean addNewJobMetaIfNeeded(UwsJob job) throws UwsException;
	
	
	/**
	 * Searches for jobs with an specific value in a specific parameter.<br/>
	 * 'value' can be null.
	 * @param parameterName
	 * @param value
	 * @return
	 * @throws SQLException
	 * @throws UWSException
	 */
	public List<UwsJob> searchByParameter(String parameterName, String value) throws UwsException;
	
	/**
	 * Creates a new parameter. If the parameter already exists, an exception is raised.
	 * @param job
	 * @param parameterid
	 * @return
	 * @throws UwsException
	 */
	public boolean createParameter(UwsJob job, String parameterid) throws UwsException;
	
	/**
	 * Creates a parameter or updates it if it already exists.
	 * @param job
	 * @param parameterid
	 * @return
	 * @throws UwsException
	 */
	public boolean createOrUpdateParameter(UwsJob job, String parameterid) throws UwsException;
	
	/**
	 * Updates a job parameter. If the parameter does not exist, the update is ignored.
	 * @param job
	 * @param parameterid
	 * @return
	 * @throws SQLException
	 */
	public boolean updateParameter(UwsJob job, String parameterid) throws UwsException;
	
	/**
	 * Updates all job parameters.<br/>
	 * It removes all parameters from the storage that do not appear in the current job parameters list.<br/>
	 * New parameters (that are not stored) are created.<br/>
	 * Parameters already stored are updated.<br/>
	 * @param job
	 * @throws UwsException
	 */
	public void updateAllParameters(UwsJob job) throws UwsException;
	
	/**
	 * Returns true if the parameter is removed.
	 * @param jobid
	 * @param parameterid
	 * @return
	 * @throws SQLException
	 */
	public boolean removeParameter(UwsJob job, String parameterid) throws UwsException;
	
	/**
	 * Job attributes are mandatory parameters.<br/>
	 * Optional parameters can be handled using the following methods:<br/>
	 * <ul>
	 * <li>{@link #createParameter(UwsJob, String)}</li>
	 * <li>{@link #createOrUpdateParameter(UwsJob, String)}</li>
	 * <li>{@link #updateParameter(UwsJob, String)}</li>
	 * <li>{@link #updateAllParameters(UwsJob)}</li>
	 * <li>{@link #removeParameter(UwsJob, String)}</li>
	 * </ul>
	 * Attributes are Job class attributes and are persisted when the job is saved.<br/>
	 * There is no need to create these attributes.<br/>
	 * Attributes removal is not allowed.<br/> 
	 * @param job
	 * @param attributeid
	 * @return
	 * @throws UwsException
	 */
	public boolean updateJobAttribute(UwsJob job, UwsJobAttribute attributeid) throws UwsException;
	
	/**
	 * Returns 'true' if the job result is added.
	 * @param job
	 * @param res
	 * @return
	 * @throws SQLException 
	 */
	public boolean addJobResultMeta(String jobid, UwsJobResultMeta res) throws UwsException;
	
	/**
	 * Returns 'true' if the error summary is added.
	 * @param jobid
	 * @param errorSummary
	 * @return
	 * @throws SQLException
	 */
	public boolean addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws UwsException;
	
	/**
	 * Returns the number of job currently stored.
	 * @return the number of job currently stored.
	 * @throws UwsException
	 */
	public int getNumJobs() throws UwsException;
	
	/**
	 * Returns the number of job owners currently stored.
	 * @return the number of job owners currently stored.
	 * @throws UwsException
	 */
	public int getNumOwners() throws UwsException;
	
	/**
	 * Returns a list of jobs that belong to the specified owner.
	 * @param ownerid owner identifier.
	 * @return
	 * @throws UwsException
	 */
	public List<UwsJob> getJobsByOwner(String ownerid) throws UwsException;

	/**
	 * Returns a list of jobs that belong to the provided listName and ownerid/session
	 * @param listName
	 * @param ownersFilter list of valid onwers/sessions
	 * @return
	 */
	public List<UwsJob> getJobsByList(String listName, List<UwsOwnerSessionFilter> ownersFilter, String appid) throws UwsException;

	/**
	 * Returns the pending jobs stored.<br/>
	 * A job is pending when its phase is one of the following: {@link UwsManager#TO_BE_RELAUNCHED_JOBS_PHASES}<br/>
	 * @return
	 * @throws UwsException
	 */
	public List<UwsJob> getPendingJobs() throws UwsException;
	
	/**
	 * Removes job output data.<br/>
	 * Removes:
	 * <ul>
	 * <li>job results data</li>
	 * <li>job results meta</li>
	 * <li>job error summary data</li>
	 * <li>job error summary meta</li>
	 * <li>job data directory</li>
	 * </ul>
	 * Job metadata are not removed (job attributes).<br/>
	 * Job parameters are not removed.<br/>
	 * This method is useful for restarting a job.<br/>
	 * To remove the entire job use: {@link #removeJobMetaDataAndOutputData(UwsJob)}
	 * @param job
	 * @return
	 * @throws SQLException
	 */
	public boolean removeJobOutputData (UwsJob job) throws UwsException;
	
	/**
	 * Removes a job: 
	 * <ul>
	 * <li>job results data</li>
	 * <li>job results meta</li>
	 * <li>job error summary data</li>
	 * <li>job error summary meta</li>
	 * <li>job data directory</li>
	 * <li>job metadata (attributes)</li>
	 * <li>job parameters</li>
	 * <li>any other job directory/resource</li>
	 * </ul>
	 * To remove output data only (a job restart), use {@link #removeJobOutputData(UwsJob)}
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public boolean removeJobMetaDataAndOutputData(UwsJob job) throws UwsException;

	/**
	 * Returns the storage directory.
	 * @return
	 */
	public File getStorageDir();	
	
	/**
	 * Returns the real job directory.
	 * @param joblocation
	 * @return
	 */
	public File getRealJobDir(String joblocation);

	/**
	 * Creates a Job output data directory if it does not exist already.
	 * @param fJobDir
	 * @return 'true' if the directory has been created (or it has been already created).
	 */
	public boolean createJobOutputDataDirIfNecessary(File fJobDir);
	
	/**
	 * Returns the OutputStream to save the data associated to a result.<br/>
	 * Caller must close the OutputStream.
	 * @param job
	 * @param resultid
	 * @throws UwsException
	 */
	public OutputStream getJobResultsDataOutputStream(UwsJob job, String resultid) throws UwsException;
	
	/**
	 * Returns the OutputStream to save the data associated to the job error summary details.<br/>
	 * Caller must close the OutputStream.
	 * @param job
	 * @throws UwsException
	 */
	public OutputStream getJobErrorDetailsDataOutputStream(UwsJob job) throws UwsException;

	/**
	 * Returns an input stream to the result data.
	 * @param job
	 * @param resultid
	 * @return
	 * @throws UwsException
	 */
	public InputStream getJobResultDataInputSource(UwsJob job, String resultid) throws UwsException;
	
	/**
	 * Returns an input stream to the error details data.
	 * @param job
	 * @return
	 * @throws UwsException
	 */
	public InputStream getJobErrorDetailsDataInputSource(UwsJob job) throws UwsException;
	
	/**
	 * Returns the size (e.g. file size) of the specified result.
	 * @param job
	 * @param resultid
	 * @return
	 * @throws UwsException
	 */
	public long getJobResultDataSize(UwsJob job, String resultid) throws UwsException;

	/**
	 * Returns the size (e.g. file size) of the job error.
	 * @param job
	 * @return
	 * @throws UwsException
	 */
	public long getJobErrorDetailsDataSize(UwsJob job) throws UwsException;
	
	/**
	 * Returns the file of the specified result.
	 * @param job
	 * @param resultid
	 * @return
	 * @throws UwsException
	 */
	public File getJobResultDataFile(UwsJob job, String resultid) throws UwsException;

	/**
	 * Returns the file of the details of the job error.
	 * @param job
	 * @return
	 * @throws UwsException
	 */
	public File getJobErrorDetailsFile(UwsJob job) throws UwsException;
	
	/**
	 * Returns the upload base directory
	 * @param owner
	 * @return
	 */
	public File getUploadDir(UwsJobOwner owner);
	
	
	/**
	 * Returns the owner directory
	 * @param owner
	 * @return
	 */
	public File getOwnerDir(UwsJobOwner owner);
	
	/**
	 * Updates an owner
	 * @param owner
	 * @throws UwsException
	 */
	public void updateOwner(UwsJobOwner owner) throws UwsException;
	
	/**
	 * Updates an owner parameter
	 * @param owner
	 * @param parameterName
	 * @throws UwsException
	 */
	public void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException;
	
	/**
	 * 
	 * @param owner
	 * @throws UwsException
	 */
	public void updateOwnerRoles(UwsJobOwner owner) throws UwsException;

	/**
	 * Returns the owners by filter
	 * @param filter
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UwsException
	 */
	public List<UwsJobOwner> retrieveOwners(UwsJobsOwnersFilter filter, long offset, long limit) throws UwsException;

	/**
	 * Returns jobs by filter
	 * @param filter
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UwsException
	 */
	public List<UwsJobDetails> retrieveJobsByFilter(UwsJobsFilter filter, long offset, long limit) throws UwsException;
	
	/**
	 * Returns the jobs older than the specified 'currentTime'. It checks job 'destructiontime' attribute.
	 * If job 'destructiontime' is lower or equals than 0, it is ignored and it is not returned.
	 * @param appid
	 * @param currentTime
	 * @return
	 * @throws UwsException
	 */
	public List<String> getOldJobs(String appid, long currentTime) throws UwsException;
	
	
	/**
	 * Returns the amount of DB space used by an owner (in MB). If the user is not found, an exception is raised.
	 * To test whether a user exists or not, use {@link #getOwnerIfAvailable(String)} instead.
	 * @param ownerid
	 * @return
	 * @throws UwsException raised if the owner is not found.
	 */
	public long calculateDbSize(String ownerid) throws UwsException;
	
	/**
	 * Returns the amount of filesystem space used by an owner (in MB). If the user is not found, an exception is raised.
	 * To test whether a user exists or not, use {@link #getOwnerIfAvailable(String)} instead.
	 * @param ownerid
	 * @return
	 * @throws UwsException raised if the owner is not found.
	 */
	public long calculateFileSize(String ownerid) throws UwsException;
	
	
	
	/**
	 * Creates a notification and the relations between the notification and the users
	 * @param type
	 * @param msg
	 * @return
	 * @throws UwsException
	 */
	public void createNotification(UwsNotificationItem uwsNotificationItem) throws UwsException;

	/**
	 * Retrieves the notifications for the specified user.
	 * In this case, the list of users is empty.
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public List<UwsNotificationItem> getNotificationsForUser(String userid) throws UwsException;

	/**
	 * Removes the specified notifications associated to the provided user.
	 * @param userid
	 * @param notificationid
	 * @throws UwsException
	 */
	public void deleteNotificationRelation(String userid, List<String> notificationid) throws UwsException;
	
	/**
	 * Removes all notifications (notifications and user-notifications relations) older than the specified delta time.
	 * @param currentTime
	 * @param deltaDestructionTime
	 * @return
	 * @throws UwsException
	 */
	public int deleteNotifications(long currentTime, long deltaDestructionTime) throws UwsException;


	
}
