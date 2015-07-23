package esavo.uws.share;

import java.util.List;

public class UwsShareGroup {
	
	private String id;
	private String title;
	private String description;
	private String creator;
	
	private List<UwsShareUser> users;
	
	/**
	 * Constructor
	 */
	public UwsShareGroup(){
		users = null;
	}
	
	/**
	 * Constructor
	 * @param id
	 * @param title
	 * @param description
	 * @param creator
	 */
	public UwsShareGroup(String id, String title, String description, String creator){
		this.id = id;
		this.title = title;
		this.description = description;
		this.creator = creator;
		this.users = null;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the users
	 */
	public List<UwsShareUser> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(List<UwsShareUser> users) {
		this.users = users;
	}

}
