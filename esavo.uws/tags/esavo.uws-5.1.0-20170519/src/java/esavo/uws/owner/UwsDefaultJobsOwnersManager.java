package esavo.uws.owner;

import esavo.uws.UwsException;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareUser;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class UwsDefaultJobsOwnersManager implements UwsJobsOwnersManager {
	
	public static final int DEFAULT_QUOTA_DB = 100000000; //Bytes
	public static final int DEFAULT_QUOTA_FILE = 1000000000; //Bytes
	public static final int DEFAULT_ASYNC_MAX_EXEC_TIME = 1800; //Seconds
	public static final int DEFAULT_SYNC_MAX_EXEC_TIME = 60; //Seconds

	private String appid;
	private UwsStorage storage;
	private UwsShareManager shareManager;
	
	public UwsDefaultJobsOwnersManager(String appid, UwsStorage storage, UwsShareManager shareManager){
		this.appid = appid;
		this.storage = storage;
		this.shareManager = shareManager;
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public synchronized UwsJobOwner loadOrCreateOwner(String ownerid) throws UwsException {
		//getOwnerIfAvailable returns 'null' if the user is not found (no exception is raised)
		UwsJobOwner owner = storage.getOwnerIfAvailable(ownerid);
		if(owner == null){
			owner = createDefaultOwner(ownerid, appid);
			storage.addOwner(owner);
		} else {
			UwsShareUser shareUser = shareManager.getSharedUser(ownerid);
			if(shareUser != null){
				owner.setName(shareUser.getName());
			}
		}
		return owner;
	}

	@Override
	public synchronized boolean updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException {
		storage.updateOwnerParameter(owner, parameterName);
		return true;
	}

	@Override
	public boolean updateOwnerRoles(UwsJobOwner owner) throws UwsException {
		storage.updateOwnerRoles(owner);
		return true;
	}
	
	public UwsJobOwner createDefaultOwner(String ownerid, String appid) throws UwsException{
		return UwsUtils.createDefaultOwner(ownerid, appid);
	}


	@Override
	public String toString(){
		return "Default Owners Manager for application '"+appid+"'";
	}

}
