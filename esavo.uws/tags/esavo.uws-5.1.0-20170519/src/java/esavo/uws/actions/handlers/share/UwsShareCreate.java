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
package esavo.uws.actions.handlers.share;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;
import esavo.uws.share.UwsShareUser;
import esavo.uws.utils.UwsUtils;

/**
 * Handles <code>{share}?action=action&...</code></br>
 * POST: creates a share group, relation between user and group or share item.<br/>
 * POST: Response: 200 OK</br> 
 * 
 * <pre><tt>
 * Add group
 * 	-action=CreateGroup
 *  -title=title
 *  -description=description
 * 	-(from security) owner (ownerid)
 * 
 * Add user to group
 * 	-action=CreateUserGroup
 * 	-group_id=group
 * 	-(from security) owner (ownerid)
 * 	-user_id=user id
 * 
 * Add share item
 * 	-action=CreateItem
 * 	-resource_id=item id
 * 	-resource_type=item type (table, job)
 *  -title=
 *  -description=
 * 	-(from security) owner (ownerid)
 * 
 * Add share item relation
 * 	-action=CreateItemRelation
 * 	-resource_id=item id
 * 	-resource_type=item type (table, job)
 * 	-(from security) owner (ownerid)
 * 	-share_to_id (userid, groupid)
 * 	-share_type = Group/User
 * 	-share_mode = Read/Write
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsShareCreate extends UwsShareActionHandler implements UwsActionHandler {
	
	public static final String ID = "share_create";
	
	private static final Action VALID_ACTIONS[] = {
		Action.CreateOrUpdateGroup, 
		Action.CreateUserGroup, 
		Action.CreateOrUpdateItem, 
		Action.CreateItemRelation};

	
	class Parameters{
		String groupId;
		String resourceId;
		String userId;
		int resourceType;
		String shareToId;
		UwsShareType shareType;
		UwsShareMode shareMode;  
		String title;
		String description;
		List<String> usersList;
		List<UwsShareItem> shareItemsList;
	}
	
	public UwsShareCreate(){
		super(ID);
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isPost()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}
		
		//Raises an exception if the parameter 'action' is not found.
		//Raises an exception if the parameter 'action' is not found.
		Action action = getAction(actionRequest);
		
		if(!isValidAction(action)){
			return false;
		}

		return true;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		if(UwsUtils.isAnonymous(currentUser)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
					"User '" + currentUser.getId() + "' is not authorized to use 'share' capabilities");
		}
		
		Action action = getAction(actionRequest);
		Parameters parameters = getParameters(actionRequest, action);

		UwsShareManager shareManager = uwsManager.getFactory().getShareManager();
		String msg;
		switch(action){
		case CreateOrUpdateGroup:
			UwsShareGroup group = new UwsShareGroup(parameters.groupId, parameters.title, parameters.description, currentUser.getId());
			if(parameters.usersList != null && parameters.usersList.size() > 0){
				List<UwsShareUser> users = createUsers(parameters.usersList, shareManager);
				group.setUsers(users);
			}
			msg = shareManager.createOrUpdateGroup(group, currentUser);
			break;
		case CreateUserGroup:
			msg = shareManager.addUserToGroup(parameters.groupId, parameters.userId, currentUser);
			break;
		case CreateOrUpdateItem:
			UwsShareItemBase sib = new UwsShareItemBase(parameters.resourceId, parameters.resourceType, parameters.title, parameters.description, currentUser.getId());
			if(parameters.shareItemsList != null){
				sib.setShareToItems(parameters.shareItemsList);
				UwsUtils.updateSharedItemsIfRequired(sib);
			}
			msg = shareManager.createOrUpdateSharedItem(sib, currentUser);
			break;
		case CreateItemRelation:
			UwsShareItem si = new UwsShareItem(parameters.resourceId, parameters.resourceType, parameters.shareToId, parameters.shareType, parameters.shareMode);
			msg = shareManager.addSharedItemRelation(currentUser, si);
			break;
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
		case CreateOrUpdateGroup:
			//nothing to check
			parameters.groupId = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_GROUP_ID);
			parameters.title = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_TITLE);
			parameters.description = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_DESCRIPTION);
			parameters.usersList = UwsHandlersUtils.getList(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_USERS_LIST));
			break;
		case CreateUserGroup:
			//check userid exists
			parameters.groupId = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_GROUP_ID);
			parameters.userId = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_USER_ID);
			break;
		case CreateOrUpdateItem:
			parameters.resourceId = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_RESOURCE_ID);
			parameters.resourceType = getExistingResourceType(actionRequest);
			parameters.title = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_TITLE);
			parameters.description = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_DESCRIPTION);
			parameters.shareItemsList = createShareItemsListIfRequired(actionRequest);
			break;
		case CreateItemRelation:
			//check
			parameters.resourceId = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_RESOURCE_ID);
			parameters.resourceType = getExistingResourceType(actionRequest);
			parameters.shareToId = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_SHARE_TO_ID);
			parameters.shareType = getExistingShareType(actionRequest);
			parameters.shareMode = getExistingShareMode(actionRequest);
			break;
		default:
			//not valid case
		}
		return parameters;
	}
	
//	private List<String> getList(String param){
//		if(param == null){
//			return null;
//		}
//		List<String> l = new ArrayList<String>();
//		String[] items = param.split(",");
//		if(items == null){
//			return l;
//		}
//		for(String s: items){
//			l.add(s.trim());
//		}
//		return l;
//	}
	
	@Override
	public boolean isValidAction(Action action) {
		return checkValidAction(VALID_ACTIONS, action);
	}
	
	private List<UwsShareItem> createShareItemsListIfRequired(UwsActionRequest actionRequest){
		String sharedItemsListParam = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ITEMS_LIST);
		if(sharedItemsListParam == null || "".equals(sharedItemsListParam)){
			return null;
		}
		String[] items = sharedItemsListParam.split(",");
		if(items == null || items.length < 1){
			return null;
		}
		String[] sharedItemTmp;
		List<UwsShareItem> sharedItemsList = new ArrayList<UwsShareItem>();
		for(String item: items){
			sharedItemTmp = item.split("\\|");
			if(sharedItemTmp == null || sharedItemTmp.length != 3){
				continue;
			}
			UwsShareItem si = new UwsShareItem();
			si.setShareToId(sharedItemTmp[0]);
			si.setShareType(UwsShareType.valueOf(sharedItemTmp[1]));
			si.setShareMode(UwsShareMode.valueOf(sharedItemTmp[2]));
			sharedItemsList.add(si);
		}
		return sharedItemsList;
	}
	
	private List<UwsShareUser> createUsers(List<String> users, UwsShareManager shareManager) throws UwsException {
		if (users == null) {
			return null;
		} else {
			List<UwsShareUser> suList = new ArrayList<UwsShareUser>();
			for(String uid: users){
				UwsShareUser su = new UwsShareUser(uid);
				suList.add(su);
			}
			//get names from LDAP if available
			shareManager.updateUsers(suList);
			return suList;
		}
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
	

}
