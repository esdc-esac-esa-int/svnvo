package esavo.uws.share;

import java.util.List;

public class UwsShareItemBase {
	
	private String resourceId;
	private int resourceType;
	private String ownerid;
	private String title;
	private String description;
	
	private List<UwsShareItem> shareToItems;
	
	public UwsShareItemBase(){
		
	}

	public UwsShareItemBase(String resourceId, int resourceType, String title, String description, String ownerid){
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.ownerid = ownerid;
		this.title = title;
		this.description = description;
	}

	/**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the resourceType
	 */
	public int getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the shareToItems
	 */
	public List<UwsShareItem> getShareToItems() {
		return shareToItems;
	}

	/**
	 * @param shareToItems the shareToItems to set
	 */
	public void setShareToItems(List<UwsShareItem> shareToItems) {
		this.shareToItems = shareToItems;
	}

}
