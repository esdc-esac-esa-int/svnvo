package esavo.uws.actions.handlers.users;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareUser;
import esavo.uws.utils.UwsUtils;

/**
 * GET tap/users?[user=pattern]
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsGetUsersHandler implements UwsActionHandler {

	//public static final String ACTION = "get_users";
	public static final String ID = "get_users";
	public static final String ACTION_NAME = "users";
	public static final boolean IS_JOB_ACTION = false;

	
	public static final int MAX_USERS_RESULT = 1000;
	
	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	@Override
	public boolean isJobAction() {
		return IS_JOB_ACTION;
	};

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
			return false;
		}
		if(actionRequest.hasJobList()){
			return false;
		}
		if(actionRequest.hasAction()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}
		return true;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		if(UwsUtils.isAnonymous(currentUser)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
					"User '" + currentUser.getId() + "' is not authorized.");
		}
		
		String userPattern = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_USER);
		UwsShareManager shareManager = uwsManager.getFactory().getShareManager();
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		List<UwsShareUser> users = shareManager.getUsers(userPattern, MAX_USERS_RESULT);
		uwsOutput.writeShareUserList(response, users);
	}

	
//	@Override
//	public boolean canHandle(String action) {
//		return ACTION.equalsIgnoreCase(action);
//	}
//
//	@Override
//	public void handle(Map<String, String> parameters, HttpServletResponse response, UwsShareManager shareManager) throws IOException  {
//		String userPattern = parameters.get(Share.PARAM_USER_PATTERN);
//		List<UwsShareUser> users;
//		try {
////			if (userPattern == null) {
////				ShareUtils.writeError(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error", "User pattern not found.");
////			}
//			users = shareManager.getUsers(userPattern, Share.MAX_USERS_RESULT);
//			ShareUtils.writeUsers(response, UwsOutputResponseHandler.OK, users);
//		} catch (UwsException e) {
//			int code = e.getCode();
//			if (code < 0) {
//				code = UwsOutputResponseHandler.INTERNAL_SERVER_ERROR;
//			}
//			Utils.writeError(ShareUtils.GENERIC_ERROR_MSG, response, code, "Error", e);
//		}
//		response.flushBuffer();
//		return;
//	}
//
//	@Override
//	public String getActionIdentifier() {
//		return ACTION;
//	}
}
