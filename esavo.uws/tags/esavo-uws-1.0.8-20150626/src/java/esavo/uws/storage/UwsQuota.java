package esavo.uws.storage;

import esavo.uws.utils.UwsUtils;

public class UwsQuota {
	private String ownerId;
	private long dbQuota = 0; 
	private long dbCurrentSize = 0; 
	private long fileQuota = 0; 
	private long fileCurrentSize = 0;
	
	public UwsQuota(String ownerId, long dbQuota, long dbCurrentSize, long fileQuota, long fileCurrentSize){
		this.ownerId=ownerId;
		this.dbQuota=dbQuota;
		this.dbCurrentSize=dbCurrentSize;
		this.fileQuota=fileQuota;
		this.fileCurrentSize=fileCurrentSize;
	}
	
	public String getOwnerId() {
		return ownerId;
	}

	/*
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	*/



	public long getDbQuota() {
		return dbQuota;
	}

	public void setDbQuota(long dbQuota) {
		this.dbQuota = dbQuota;
	}

	public long getDbCurrentSize() {
		return dbCurrentSize;
	}

	public long getFileQuota() {
		return fileQuota;
	}

	public void setFileQuota(long fileQuota) {
		this.fileQuota = fileQuota;
	}

	public long getFileCurrentSize() {
		return fileCurrentSize;
	}

	public synchronized void addDbSize(long bytes) throws QuotaException{
		dbCurrentSize+=bytes;
		checkQuota();
	}

	public synchronized void addFileSize(long bytes) throws QuotaException{
		fileCurrentSize+=bytes;
		checkQuota();
	}

	public synchronized void reduceDbSize(long bytes){
		dbCurrentSize-=bytes;
		if(dbCurrentSize<0) dbCurrentSize=0;
	}

	public synchronized void reduceFileSize(long bytes){
		fileCurrentSize-=bytes;
		if(fileCurrentSize<0) fileCurrentSize=0;
	}
	
	public synchronized void checkQuota() throws QuotaException{
		boolean quotaExceeded = false;
		String message = "";
		if(dbQuota>=0 && dbCurrentSize>dbQuota){
			message = "DB quota exceeded";
			quotaExceeded=true;
		}else if(fileQuota>=0 && fileCurrentSize>fileQuota){
			message = "Filesystem quota exceeded";
			quotaExceeded=true;
		}
		
		if(ownerId.equals(UwsUtils.ANONYMOUS_USER)){
			message+="for user "+ownerId+". Registered users have an increased quota, please log in.";
		}
		
		if(quotaExceeded) {
			throw new QuotaException(message);
		}
		
	}
	
	/**
	 * Returns the minimum file space available.<br/>
	 * This method is necessary because some functionalities work with integers instead of longs.
	 * Checks whether quota is enabled, if it is enabled, the minimum value between the current file space and the provided
	 * hardAvaiableSpace is returned. If quota is not enabled, the hardAvailableSpace is returned.
	 * @param hardAvailableSpace (-1 to disable hard limit).
	 * @return the free space amount is returned. 0 means no space. -1 means check disabled.
	 */
	public int getMinFileQuotaAvailable(int hardAvailableSpace){
		return (int)getMinFileQuotaAvailable((long)hardAvailableSpace);
	}

	
	/**
	 * Returns the file space available. -1 means quota check disabled.<br/>
	 * If file quota is disabled, returns hardAvailableSpace.<br/>
	 * If file quota is enabled:
	 * <ul>
	 * <li>For authenticated user: returns the amount of free space available (quota). 0 means no space.
	 * <li>For anonymous user: returns the minimum between hard limit and the amount of free space available (quota).
	 * </ul>
	 * @param hardAvailableSpace (-1 to disable hard limit).
	 * @return the free space amount is returned. 0 means no space. -1 means check disabled.
	 */
	public long getMinFileQuotaAvailable(long hardAvailableSpace){
		//Check whether quota file is disabled
		if(fileQuota < 0){
			//disabled: return hard limit
			return hardAvailableSpace;
		}else{
			//return min between available file quota space and hardAvaialabeSpace
			long available = fileQuota - fileCurrentSize;
			if(available < 0){
				//At this point, fileQuota is not disabled.
				//If available < 0 it means a normal dec. operation is in progress and temporary the value is < 0: fix to 0
				//(because -1 means disabled)
				available = 0;
			}
			if(UwsUtils.isAnonymous(ownerId)){
				//if user is anonymous, do not use global file quota.
				//apply minimum between hard limit and quota (to limit the max amount of file by operation.
				if (hardAvailableSpace < 0) {
					// hard limit disabled.
					return available;
				} else {
					// quota is not disabled (!= -1)
					// hard limit is not disabled (!= -1)
					// return minimum value
					return Math.min(available, hardAvailableSpace);
				}
			}else{
				return available;
			}
		}
	}
	
	@Override
	public String toString(){
		return "Quota info for user: " + ownerId + ": db quota: " + dbQuota + ", current db size: " + dbCurrentSize +
				", file quota: " + fileQuota + ", current file size: " + fileCurrentSize;
	}
}
