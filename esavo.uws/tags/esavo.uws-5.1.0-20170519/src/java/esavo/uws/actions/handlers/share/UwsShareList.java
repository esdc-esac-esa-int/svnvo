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


import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.share.UwsShareCreate.Parameters;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareUser;

/**
 * Handles <code>{share}?action=action</code></br>
 * GET: gets the .<br/>
 * GET: Response: 200 OK</br> 
 * 
 * <pre><tt>
 * Get groups that belong to the user
 * 	-action=GetGroupsDescriptionsOnly
 * 	-(from security) owner (ownerid)
 * 
 * Get groups that belong to the user
 * 	-action=GetGroups
 * 	-(from security) owner (ownerid)
 * 
 * Get shared items descriptions only
 * 	-action=GetSharedItemsDescriptionsOnly
 * 	-(from security) owner (ownerid)
 * 
 * Get shared items
 * 	-action=GetSharedItems
 * 	-(from security) owner (ownerid)
 * 
 * Get users
 * 	-action=GetUsers
 * 
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsShareList extends UwsShareActionHandler implements UwsActionHandler {
	
	public static final String ID = "share_list";
	
	private static final Action VALID_ACTIONS[] = {
		Action.GetGroupsDescriptionsOnly,
		Action.GetGroups,
		Action.GetSharedItemsDescriptionsOnly,
		Action.GetSharedItems,
		Action.GetUsers};

	class Parameters{
		List<String> groupsList;
		boolean includeUsers;
	}
	
	public UwsShareList(){
		super(ID);
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
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
		Action action = getAction(actionRequest);
		Parameters parameters = getParameters(actionRequest, action);
		UwsShareManager shareManager = uwsManager.getFactory().getShareManager();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		String ownerid = currentUser.getId();
		switch(action){
		case GetGroups:
			List<UwsShareGroup> groups;
			if(parameters.groupsList == null || parameters.groupsList.size() < 1){
				groups = shareManager.getGroups(ownerid, parameters.includeUsers);
			}else{
				groups = shareManager.getGroupUsers(parameters.groupsList);
			}
			outputHandler.writeSharedGroupsResponse(response, groups, ownerid);
			break;
		case GetGroupsDescriptionsOnly:
			List<UwsShareGroup> groupsDescriptions = shareManager.getGroupsByOwner(ownerid, false);
			//shareManager.updateGroupUsers(groupsDescriptions);
			outputHandler.writeSharedGroupsResponse(response, groupsDescriptions, ownerid);
			break;
		case GetSharedItems:
			List<UwsShareItemBase> sharedItems = shareManager.getUserSharedItems(ownerid, true);
			outputHandler.writeSharedItemsResponse(response, sharedItems, ownerid);
			break;
		case GetSharedItemsDescriptionsOnly:
			List<UwsShareItemBase> sharedItemsBase = shareManager.getUserSharedItems(ownerid, false);
			outputHandler.writeSharedItemsResponse(response, sharedItemsBase, ownerid);
			break;
		case GetUsers:
			List<UwsShareUser> users = shareManager.getUsers();
			outputHandler.writeSharedUsers(response, users);
			break;
		default:
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Unknown action");
		}
	}
	
	@Override
	public Parameters getParameters(UwsActionRequest actionRequest, Action action) throws UwsException {
		Parameters parameters = new Parameters();
		parameters.includeUsers = true; //default behavior
		switch (action) {
		case GetGroups:
			parameters.groupsList = UwsHandlersUtils.getList(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_GROUPS_LIST));
			String includeUsersParam = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_INCLUDE_USERS);
			if(includeUsersParam != null){
				parameters.includeUsers = Boolean.parseBoolean(includeUsersParam);
			}
		default:
			//nothing to do
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
