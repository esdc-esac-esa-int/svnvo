package esavo.uws.share;

public class UwsShareItem {

	/**
	 * Resource identifier
	 */
	private String resourceId;

	/**
	 * TABLE, JOB, etc.
	 */
	private int resourceType;
	
	/**
	 * Owner identifier.
	 */
	private String ownerId;
	
	/**
	 * User or group identifier
	 */
	private String shareToId;
	
	/**
	 * User or Group
	 */
	private UwsShareType shareType;
	
	/**
	 * Read or Write.
	 */
	private UwsShareMode shareMode;
	
	/**
	 * Constructor
	 */
	public UwsShareItem(){
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param resourceId
	 * @param resourceType
	 * @param shareToId
	 * @param shareType
	 * @param shareMode
	 */
	public UwsShareItem(String resourceId, int resourceType,
			String shareToId, UwsShareType shareType, UwsShareMode shareMode) {
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.shareToId = shareToId;
		this.shareType = shareType;
		this.shareMode = shareMode;
	}

	/**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @return the resourceType
	 */
	public int getResourceType() {
		return resourceType;
	}

	/**
	 * @return the shareToId
	 */
	public String getShareToId() {
		return shareToId;
	}

	/**
	 * @return the shareType
	 */
	public UwsShareType getShareType() {
		return shareType;
	}

	/**
	 * @return the shareMode
	 */
	public UwsShareMode getShareMode() {
		return shareMode;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * @param shareToId the shareToId to set
	 */
	public void setShareToId(String shareToId) {
		this.shareToId = shareToId;
	}

	/**
	 * @param shareType the shareType to set
	 */
	public void setShareType(UwsShareType shareType) {
		this.shareType = shareType;
	}

	/**
	 * @param shareMode the shareMode to set
	 */
	public void setShareMode(UwsShareMode shareMode) {
		this.shareMode = shareMode;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the ownerId
	 */
	public String getOwnerid() {
		return ownerId;
	}

	/**
	 * @param ownerId the ownerId to set
	 */
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	
	/**
	 * Returns resurceid + '#' + resourceType
	 * @return resurceid + '#' + resourceType
	 */
	public String getShareItemFullId(){
		return getSharedItemFullId(resourceId, resourceType);
	}
	
	/**
	 * Returns resurceid + '#' + resourceType
	 * @return resurceid + '#' + resourceType
	 */
	public static String getSharedItemFullId(String resourceid, int resourceType){
		return resourceid + "#" + resourceType;
	}

	@Override
	public String toString(){
		return resourceId + ", type: " + resourceType + ", owner: " + ownerId + 
				", share to: " + shareToId + " (type: "+shareType+"), mode: " + shareMode;
	}
	
}
