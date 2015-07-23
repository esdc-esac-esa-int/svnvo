package esavo.uws.owner;

import esavo.uws.UwsException;

public interface UwsJobsOwnersManager {
	
	public static final String OWNER_PARAMETER_DB_QUOTA = "db_quota";
	public static final String OWNER_PARAMETER_FILES_QUOTA = "files_quota";
	public static final String OWNER_PARAMETER_CURRENT_DB_SIZE = "db_current_size";
	public static final String OWNER_PARAMETER_CURRENT_FILES_SIZE = "files_current_size";
	
	public UwsJobOwner loadOrCreateOwner(String ownerid) throws UwsException;
	public boolean updateOwnerRoles(UwsJobOwner owner) throws UwsException;
	public boolean updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException;

}
