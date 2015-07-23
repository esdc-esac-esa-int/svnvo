package esavo.tap.metadata;

import esavo.tap.TAPSchemaInfo;
import esavo.tap.db.DBConnection;
import esavo.uws.owner.UwsJobOwner;

public class TAPSchemaLoaderArgs {
	
	private TAPMetadataLoaderArgs metadataArgs;
	private DBConnection dbConn;
	private TAPSchemaInfo tapSchemaInfo;
	private boolean authenticatedUser;
	private UwsJobOwner owner;
	
	
	public TAPSchemaLoaderArgs(DBConnection dbConn, TAPSchemaInfo tapSchemaInfo, UwsJobOwner owner, TAPMetadataLoaderArgs args) {
		this.dbConn = dbConn;
		this.metadataArgs = args;
		this.tapSchemaInfo = tapSchemaInfo;
		setOwner(owner);
	}

	/**
	 * @return the metadataArgs
	 */
	public TAPMetadataLoaderArgs getMetadataArgs() {
		return metadataArgs;
	}

	/**
	 * @return the dbConn
	 */
	public DBConnection getDbConn() {
		return dbConn;
	}

	/**
	 * @return the tapSchemaInfo
	 */
	public TAPSchemaInfo getTapSchemaInfo() {
		return tapSchemaInfo;
	}


	/**
	 * @return the owner
	 */
	public UwsJobOwner getOwner() {
		return owner;
	}


	/**
	 * @param owner the owner to set
	 */
	public void setOwner(UwsJobOwner owner) {
		this.owner = owner;
		this.authenticatedUser = false;
		if(owner != null && owner.getAuthUsername() != null){
			this.authenticatedUser = true;
		}
	}


	/**
	 * @return the authenticatedUser
	 */
	public boolean isAuthenticatedUser() {
		return authenticatedUser;
	}
	
	/**
	 * @return 'true' if the user is authenticated and include accessible shared items flag is 'true'.
	 */
	public boolean areAccessibleSharedItemsRequired(){
		return authenticatedUser && metadataArgs.isIncludeAccessibleSharedItems();
	}
	
	/**
	 * @return the owner identifier or null if no owner is available.
	 */
	public String getOwnerId() {
		if (owner == null) {
			return null;
		} else {
			return owner.getId();
		}
	}

}
