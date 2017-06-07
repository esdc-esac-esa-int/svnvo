package esavo.uws.actions.handlers.admin.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;

public class UwsAdmUserDetailsHandler implements UwsFunctionsHandler {

	public static final String ACTION = "user_details";

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
		if(userid == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "User identifier not found.");
		}else{
			UwsStorage uwsStorage = uwsManager.getFactory().getStorageManager();
			UwsJobOwner userDetails = uwsStorage.getOwner(userid);
			UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(userDetails);
			List<UwsJobOwner> users = new ArrayList<UwsJobOwner>();
			users.add(userDetails);
			UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
			uwsOutput.writeUserList(response, users);
//			try {
//				UwsJobOwner userDetails = uwsStorage.getOwner(userid);
//				List<UwsJobOwner> users = new ArrayList<UwsJobOwner>();
//
//				// Get current user quota:
//				UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(userDetails);
//				
//				users.add(userDetails);
//				AdminUtils.writeUserList(response, UwsOutputResponseHandler.OK, users);
//			} catch (UwsException e) {
//				Utils.writeError(AdminUtils.GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error", e);
//			}
		}
//		response.flushBuffer();
		return;
	}

	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
