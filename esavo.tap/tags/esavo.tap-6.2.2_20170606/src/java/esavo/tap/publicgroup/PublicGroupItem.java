package esavo.tap.publicgroup;

public class PublicGroupItem {

	/**
	 * The user that access to the table
	 */
	private String user;
	
	/**
	 * Table name
	 */
	private String tableName;
	
	/**
	 * Table schema name
	 */
	private String tableSchemaName;
	
	/**
	 * Table owner
	 */
	private String owner;
	
	public PublicGroupItem() {
		
	}


	/**
	 * Returns the user that access to the table
	 * @return the user that access to the table
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user that access to the table.
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * The table owner
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}


	/**
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}


	/**
	 * @param tableName the table name to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	/**
	 * @return the tableSchemaName
	 */
	public String getTableSchemaName() {
		return tableSchemaName;
	}


	/**
	 * @param tableSchemaName the tableSchemaName to set
	 */
	public void setTableSchemaName(String tableSchemaName) {
		this.tableSchemaName = tableSchemaName;
	}
}
