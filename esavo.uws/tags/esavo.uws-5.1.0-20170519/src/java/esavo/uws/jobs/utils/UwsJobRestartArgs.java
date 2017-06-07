package esavo.uws.jobs.utils;

import java.util.List;

import esavo.uws.jobs.UwsJobResultMeta;

public class UwsJobRestartArgs extends UwsJobInitArgs {
	
	private String jobid;
	private String locationid;
	private long creationTime;
	private List<UwsJobResultMeta> results;

	public UwsJobRestartArgs() {

	}

	/**
	 * @return the jobid
	 */
	public String getJobid() {
		return jobid;
	}

	/**
	 * @param jobid the jobid to set
	 */
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	/**
	 * @return the locationid
	 */
	public String getLocationid() {
		return locationid;
	}

	/**
	 * @param locationid the locationid to set
	 */
	public void setLocationid(String locationid) {
		this.locationid = locationid;
	}

	/**
	 * @return the results
	 */
	public List<UwsJobResultMeta> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(List<UwsJobResultMeta> results) {
		this.results = results;
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

}
