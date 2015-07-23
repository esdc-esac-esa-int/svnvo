package esavo.uws.jobs.utils;

public class UwsJobDetailsComparison extends UwsJobDetails {

	private long startTimeLimit;
	private long endTimeLimit;

	/**
	 * @return the startTimeLimit
	 */
	public long getStartTimeLimit() {
		return startTimeLimit;
	}

	/**
	 * @param startTimeLimit the startTimeLimit to set
	 */
	public void setStartTimeLimit(long startTimeLimit) {
		this.startTimeLimit = startTimeLimit;
	}

	/**
	 * @return the endTimeLimit
	 */
	public long getEndTimeLimit() {
		return endTimeLimit;
	}

	/**
	 * @param endTimeLimit the endTimeLimit to set
	 */
	public void setEndTimeLimit(long endTimeLimit) {
		this.endTimeLimit = endTimeLimit;
	}

	@Override
	public String toString() {
		return super.toString() + ",\nStart time limit: " + startTimeLimit + ", End time limit: " + endTimeLimit;
	}

}
