package esavo.uws.jobs.utils;

/**
 * This class contains a short description of a job
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobDetails {
	private String jobid;
	private String ownerid;
	private String phaseid;
	private long startTime;
	private long endTime;
	private String query;
	private String relativePath;
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
	 * @return the ownerid
	 */
	public String getOwnerid() {
		return ownerid;
	}
	/**
	 * @param ownerid the ownerid to set
	 */
	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
	}
	/**
	 * @return the phaseid
	 */
	public String getPhaseid() {
		return phaseid;
	}
	/**
	 * @param phaseid the phaseid to set
	 */
	public void setPhaseid(String phaseid) {
		this.phaseid = phaseid;
	}
	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}
	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public String toString(){
		return "jobid: " + jobid + ", ownerid: " + ownerid + ", phaseid: " + phaseid +
				",\nStart time: " + startTime + ", End time: " + endTime + ",\nquery: " + query;
	}

}
