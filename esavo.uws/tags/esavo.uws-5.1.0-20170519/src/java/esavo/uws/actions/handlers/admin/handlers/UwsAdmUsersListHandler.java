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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;

public class UwsAdmUsersListHandler implements UwsFunctionsHandler {
	
	public static final String ACTION = "users_list";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, outputHandler, response)){
			UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
			uwsOutput.writeUserList(response, null);
			return;
		}

		String userid = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_USER_ID);
		UwsJobsOwnersFilter filter = createFilter(userid);
		long offset = UwsHandlersUtils.getLongFromParameter(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_OFFSET), -1);
		long limit = UwsHandlersUtils.getLongFromParameter(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_LIMIT), -1);
		UwsStorage uwsStorage = uwsManager.getFactory().getStorageManager();
		List<UwsJobOwner> users = uwsStorage.retrieveOwners(filter, offset, limit);
		if (users != null) {
			for (UwsJobOwner owner : users) {
				// Get current user quota:
				UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(owner);
			}
		}
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		uwsOutput.writeUserList(response, users);
//
//		try {
//			String userid = actionRequest.getHttpParameter(AdminUtils.PARAM_USER_ID);
//			UwsJobsOwnersFilter filter = createFilter(userid);
//			long offset = AdminUtils.getLongFromParameter(actionRequest.getHttpParameter(AdminUtils.PARAM_QUERY_RESULTS_OFFSET), -1);
//			long limit = AdminUtils.getLongFromParameter(actionRequest.getHttpParameter(AdminUtils.PARAM_QUERY_RESULTS_LIMIT), -1);
//			List<UwsJobOwner> users = uwsStorage.retrieveOwners(filter, offset, limit);
//			if (users != null) {
//				for (UwsJobOwner owner : users) {
//					// Get current user quota:
//					UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(owner);
//				}
//			}
//			AdminUtils.writeUserList(response, UwsOutputResponseHandler.OK, users);
//		} catch (UwsException e) {
//			Utils.writeError(AdminUtils.GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error", e);
//		}
//		response.flushBuffer();
		return;
	}

	private UwsJobsOwnersFilter createFilter(String userid){
		UwsJobsOwnersFilter filter = new UwsJobsOwnersFilter();
		boolean filterFound = false;
		
		if(userid != null && !"".equals(userid)){
			filterFound = true;
			filter.setIdFilter(userid);
		}

		if(filterFound){
			return filter;
		}else{
			return null;
		}
	}

	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
