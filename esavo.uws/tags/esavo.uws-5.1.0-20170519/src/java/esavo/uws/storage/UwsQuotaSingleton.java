package esavo.uws.storage;

import java.util.HashMap;
import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.storage.UwsStorage;

/**
 * 
 * @author Raul Gutierrez-Sanchez
 *
 */
public class UwsQuotaSingleton {
	
	private Map<String, UwsQuota> users = new HashMap<String,UwsQuota>();
    
	private static UwsQuotaSingleton singleton = null;
    
    private UwsQuotaSingleton() {
    }
    
    public static synchronized UwsQuotaSingleton getInstance() {
    	if(singleton==null)	singleton = new UwsQuotaSingleton();
    	return singleton;
    }
    
    /**
     * Update the DB and File sizes used by the given owner.
     * @param owner
     * @param force True if update has to be done even if user quotas has been already updated for that user.
     * @throws UwsException
     * @throws IOException 
     * @throws UwsQuotaException 
     */
    /*
    synchronized public void updateUserQuotas(UwsJobOwner owner, boolean force) throws UwsException, IOException{
    	if(users.get(owner.getId())!=null){
    		if(!force) return;
    	}
		
		storage.updateOwner(owner);
    }
    */
    
    /**
     * Removes a user from the list of updated users. To be called on logout.
     * @param owner
     */
    public synchronized void removeUser(String userId){
    	users.remove(userId);
    }
    
    /**
     * Returns the quota of a user
     * @param owner
     * @return
     * @throws UwsException 
     * @throws QuotaException 
     */
    public synchronized UwsQuota createOrLoadQuota(UwsJobOwner owner, boolean force) throws UwsException{
    	if(users.get(owner.getId())!=null){
    		if(!force) return users.get(owner.getId());
    	}

    	UwsJobOwnerParameters ownerParameters = owner.getParameters();
		if(ownerParameters == null){
			throw new UwsException("Error: impossible to obtain quota information for user.");
		}

		long dbQuota = 0;
    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA)!=null){
    		dbQuota = owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA);
    	}else{
    		throw new UwsException("No DB quota available for user "+owner.getId());
    	}
    	
    	long fileQuota = 0;
    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA)!=null){
    		fileQuota = owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA);
    	}else{
    		throw new UwsException("No filesystem quota available for user "+owner.getId());
    	}

		UwsStorage storage = UwsManager.getInstance().getFactory().getStorageManager();
		long dbSize=0;
		try{
			dbSize = storage.calculateDbSize(owner.getId());
		}catch(UwsException e){
			throw new UwsException("Error: impossible to obtain DB used size: " + e.getMessage());
		}
		
		long fileSize=0;
		try{
			fileSize = storage.calculateFileSize(owner.getId());
		}catch(UwsException e){
			throw new UwsException("Error: impossible to obtain filesystem used size: " + e.getMessage());
		}
		
		long asyncMaxExecTime=-1;
		if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME)!=null){
			asyncMaxExecTime = owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME);
    	}else{
    		throw new UwsException("No max. async. exec. time for user "+owner.getId());
    	}

		long syncMaxExecTime=-1;
		if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME)!=null){
			syncMaxExecTime = owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME);
    	}else{
    		throw new UwsException("No max. sync. exec. time for user "+owner.getId());
    	}

		// Add user to updated users map
		UwsQuota userQuota = createQuota(owner.getId(),dbQuota, dbSize,fileQuota,fileSize,asyncMaxExecTime, syncMaxExecTime);
		users.put(owner.getId(), userQuota);

    	
    	if(userQuota==null) {
    		throw new UwsException("No quota information available for user "+owner);
    	}
    	
    	// Persist to DB
    	storage.updateOwner(owner);
    	
    	return userQuota;
    }
    
    public UwsQuota createOrLoadQuota(UwsJobOwner owner) throws UwsException{
    	return createOrLoadQuota(owner,false);
    }
    
    /*
    public synchronized void updateDbSize(UwsJobOwner owner) throws UwsException, QuotaException{
    	UwsStorage storage = UwsManager.getInstance().getFactory().getStorageManager();
		long sizeDB=0;
		try{
			sizeDB = storage.calculateDbSize(owner.getId());
			createOrLoadQuota(owner).setDbCurrentSize(sizeDB);
		}catch(UwsException e){
			throw new UwsException("Error: impossible to obtain DB used size.");
		}
    }
    */

    /*
    public synchronized void setDbSize(UwsJobOwner owner, long dbSize) throws UwsException, QuotaException{
		createOrLoadQuota(owner).setDbCurrentSize(dbSize);
    }
    */

    
    /**
     * Provides current quota state for the given user.
     * @param owner
     * @return
     * @throws QuotaException 
     * @throws UwsQuotaException
     */
    
    private static UwsQuota createQuota(String ownerId, long dbQuota, long dbSize, long fileQuota, long fileSize, long asyncMaxExecTime, long syncMaxExecTime) throws UwsException{
    	UwsQuota quota = new UwsQuota(ownerId, dbQuota, dbSize, fileQuota, fileSize, asyncMaxExecTime, syncMaxExecTime);
    	
    	/*
    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA)!=null){
    		quota.setDbQuota(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA));
    	}else{
    		throw new UwsException("No DB quota available for user "+owner.getId());
    	}
    	
    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE)!=null){
    		quota.setDbCurrentSize(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE));
    	}else{
    		throw new UwsException("No current DB size available for user "+owner.getId());
    	}

    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA)!=null){
    		quota.setFileQuota(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA));
    	}else{
    		throw new UwsException("No filesystem quota available for user "+owner.getId());
    	}

    	if(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE)!=null){
    		quota.setFileCurrentSize(owner.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE));
    	}else{
    		throw new UwsException("No current filesystem size available for user "+owner.getId());
    	}
    	*/
    	
    	return quota;

    }
    
    
    public void updateOwnerQuotaParameters(UwsJobOwner owner) throws UwsException{
    	UwsQuota quota = createOrLoadQuota(owner);
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA,quota.getDbQuota());
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE,quota.getDbCurrentSize());
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA,quota.getFileQuota());
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE,quota.getFileCurrentSize());
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME,quota.getAsyncMaxExecTime());
   		owner.getParameters().setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME,quota.getSyncMaxExecTime());
    }
    
}
