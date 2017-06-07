/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.actions.handlers.admin.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;

public class UwsAdmUserUpdateHandler implements UwsFunctionsHandler {

	public static final String ACTION = "user_update";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException  {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, outputHandler, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}
		
		String userid = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_USER_ID);
		if(userid == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "User identifier not found.");
		}else{
            long newQuotaDB;
            try{
            	newQuotaDB = Long.parseLong(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_QUOTA_DB));
            }catch(NumberFormatException nfe){
    			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid db quota number: " + actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_QUOTA_DB));
            }

            long newQuotaFiles;
            try{
            	newQuotaFiles = Long.parseLong(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_QUOTA_FILES));
            }catch(NumberFormatException nfe){
    			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid files quota number: " + actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_QUOTA_FILES));
            }

            long newAsyncMaxExecTime;
            try{
            	newAsyncMaxExecTime = Long.parseLong(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ASYNC_MAX_EXEC_TIME));
            }catch(NumberFormatException nfe){
    			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid max async time number: " + actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ASYNC_MAX_EXEC_TIME));
            }

            long newSyncMaxExecTime;
            try{
            	newSyncMaxExecTime = Long.parseLong(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SYNC_MAX_EXEC_TIME));
            }catch(NumberFormatException nfe){
    			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid max sync time number: " + actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SYNC_MAX_EXEC_TIME));
            }

            int roles;
            try{
                roles = Integer.parseInt(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ROLES));
            }catch(NumberFormatException nfe){
    			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid roles identifier: " + actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ROLES));
            }

            UwsStorage uwsStorage = uwsManager.getFactory().getStorageManager();
			UwsJobOwner owner = uwsStorage.getOwner(userid);
            UwsJobOwnerParameters ownerParameters = owner.getParameters();
            if(ownerParameters == null){
                ownerParameters = new UwsJobOwnerParameters();
                owner.setParameters(ownerParameters);
            }
            
            UwsQuota newQuota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner,true);
            newQuota.setDbQuota(newQuotaDB);
            newQuota.setFileQuota(newQuotaFiles);
            newQuota.setAsyncMaxExecTime(newAsyncMaxExecTime);
            newQuota.setSyncMaxExecTime(newSyncMaxExecTime);
            
			UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(owner);

            owner.setRoles(roles);

            uwsStorage.updateOwner(owner);
            
            UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
            uwsOutput.writeSimpleJsonResponse(response, "id", "User " + userid + " updated.");
        }
        return; 
	}

	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
