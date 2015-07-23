package esavo.uws.actions.handlers.share;


import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareType;
import esavo.uws.utils.UwsUtils;

/**
 * Handles <code>{share}?action=action&...</code></br>
 * POST: creates a share group, relation between user and group or share item.<br/>
 * POST: Response: 200 OK</br> 
 * 
 * <pre><tt>
 * Delete group
 * 	-action=RemoveGroup
 * 	-group_id=group id
 * 	-(from security) owner (ownerid)
 * 
 * Delete user from group
 * 	-action=RemoveUserGroup
 * 	-group_id=group
 * 	-(from security) owner (ownerid)
 * 	-user_id=user id
 * 
 * Delete shared item
 * 	-action=RemoveItem
 * 	-resource_id=item id
 * 	-resource_type=item type (table, job)
 * 	-(from security) owner (ownerid)
 * 
 * Delete shared item relation
 * 	-action=RemoveItemRelations
 * 	-resource_id=item id
 * 	-resource_type=item type (table, job)
 * 	-(from security) owner (ownerid)
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsShareDelete extends UwsShareAction implements UwsActionHandler {
	
	public static final String ID = "share_delete";
	
	private static final Action VALID_ACTIONS[] = {
		Action.RemoveGroup, 
		Action.RemoveUserGroup, 
		Action.RemoveItem, 
		Action.RemoveItemRelation};
	
	
	class Parameters {
		String groupId;
		String resourceId;
		String userId;
		int resourceType;
		String shareToId;
		UwsShareType shareType;
	}
	
	
	public UwsShareDelete(){
		super(ID);
	}
	
	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isPost()){
			return false;
		}
		if(!actionRequest.hasShare()){
			return false;
		}

		//Raises an exception if the parameter 'action' is not found.
		Action action = getAction(actionRequest);
		
		if(!isValidAction(action)){
			return false;
		}

		return true;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		if(currentUser.getId().equals(UwsUtils.ANONYMOUS_USER)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
					"User '" + currentUser.getId() + "' is not authorized to use 'share' capabilities");
		}
		
		//valid share action: check required parameters:
		Action action = getAction(actionRequest);
		Parameters parameters = getParameters(actionRequest, action);
		
		UwsShareManager shareManager = uwsManager.getFactory().getShareManager();
		String msg;
		switch(action){
		case RemoveGroup:
			msg = shareManager.removeGroup(parameters.groupId, currentUser);
			break;
		case RemoveUserGroup:
			msg = shareManager.removeGroupUser(parameters.groupId, parameters.userId, currentUser);
			break;
		case RemoveItem:
			msg = shareManager.removeSharedItem(parameters.resourceId, parameters.resourceType, currentUser);
			break;
		case RemoveItemRelation:
			UwsShareItem si = new UwsShareItem(parameters.resourceId, parameters.resourceType, parameters.shareToId, parameters.shareType, UwsShareManager.UNSPECIFIED_SHARE_MODE);
			//msg = shareManager.removeSharedItemRelation(parameters.resourceId, parameters.resourceType, parameters.shareToId, parameters.shareType, currentUser);
			msg = shareManager.removeSharedItemRelation(currentUser, si);
		default:
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Unknown action");
		}
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//TODO a different output...?
		outputHandler.writeTextPlainResponse(response, "OK\n"+msg);
	}
	
	
	public Parameters getParameters(UwsActionRequest actionRequest, Action action) throws UwsException {
		Parameters parameters = new Parameters();
		switch(action){
		case RemoveGroup:
			parameters.groupId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_GROUP_ID);
			break;
		case RemoveUserGroup:
			parameters.groupId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_GROUP_ID);
			parameters.userId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_USER_ID);
			break;
		case RemoveItem:
			parameters.resourceId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_RESOURCE_ID);
			parameters.resourceType = getExistingResourceType(actionRequest);
			break;
		case RemoveItemRelation:
			parameters.resourceId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_RESOURCE_ID);
			parameters.resourceType = getExistingResourceType(actionRequest);
			parameters.shareType = getExistingShareType(actionRequest);
			parameters.shareToId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_SHARE_TO_ID);
			break;
		default:
			//not valid case
		}
		return parameters;
	}
	
	@Override
	public boolean isValidAction(Action action) {
		return checkValidAction(VALID_ACTIONS, action);
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}

}
