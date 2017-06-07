package esavo.uws.creator;

import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsJobHttpParametersHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.owner.UwsJobOwner;

public interface UwsCreator {
	
	/**
	 * Creates a job.<br/>
	 * Call this method to create a new job.<br/>
	 * To restore a job from an storage, use {@link #createJob(String, String, UwsJobOwner, String, String, List, int)}
	 * @return a new job
	 * @throws UwsException
	 */
	public UwsJob createJob(UwsJobOwner owner, String listid, int priority) throws UwsException;
	
	/**
	 * Creates a job.<br/>
	 * Call this method when a job is rebuild from an storage.<br/>
	 * To create a new job, use {@link #createJob(UwsJobOwner, String, int)}
	 * @param jobid
	 * @param owner
	 * @param listid
	 * @param locationid
	 * @param results
	 * @param priority
	 * @param creationTime
	 * @throws UwsException
	 * @return
	 */
	public UwsJob createJob(String jobid, UwsJobOwner owner, String listid, String locationid, List<UwsJobResultMeta> results, int priority, long creationTime) throws UwsException;

	/**
	 * Returns a list of job http parameters handlers
	 * @return a list of job http parameters handlers
	 */
	public List<UwsJobHttpParametersHandler> getJobHttpParametersHandlers();
	
	/**
	 * Adds a job http parameters handler
	 * @param handler
	 */
	public void addJobHttpParametersHandler(UwsJobHttpParametersHandler handler);
}
