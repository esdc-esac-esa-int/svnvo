package esavo.uws.jobs.utils;

/**
 * This class contains a short description of a job
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobDetails implements Comparable<UwsJobDetails>{
	private String jobid;
	private String listid;
	private String jobName;
	private String ownerid;
	private String sessionid;
	private String phaseid;
	private long startTime;
	private long endTime;
	private String query;
	private String relativePath;
	private long creationTime;
	
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

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}
	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	/**
	 * @return the sessionid
	 */
	public String getSessionid() {
		return sessionid;
	}
	/**
	 * @param sessionid the sessionid to set
	 */
	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	/**
	 * @return the listid
	 */
	public String getListid() {
		return listid;
	}
	/**
	 * @param listid the listid to set
	 */
	public void setListid(String listid) {
		this.listid = listid;
	}
	
	@Override
	public String toString(){
		return "jobid: " + jobid + ", listid: " + listid +  ", ownerid: " + ownerid + ", session: " + sessionid + ", phaseid: " + phaseid + ", name: " + jobName +
				",\nCreation time: " + creationTime + ", Start time: " + startTime + ", End time: " + endTime + ",\nquery: " + query;
	}

	@Override
	public int compareTo(UwsJobDetails o) {
		return this.jobid.compareTo( o.jobid);
	}
}
