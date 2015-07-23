package esavo.uws.actions.handlers.share;


import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
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
public class UwsShareList extends UwsShareAction implements UwsActionHandler {
	
	public static final String ID = "share_list";
	
	private static final Action VALID_ACTIONS[] = {
		Action.GetGroupsDescriptionsOnly,
		Action.GetGroups,
		Action.GetSharedItemsDescriptionsOnly,
		Action.GetSharedItems,
		Action.GetUsers};

	public UwsShareList(){
		super(ID);
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
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
		Action action = getAction(actionRequest);
		UwsShareManager shareManager = uwsManager.getFactory().getShareManager();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		String ownerid = currentUser.getId();
		switch(action){
		case GetGroups:
			List<UwsShareGroup> groups = shareManager.getGroups(ownerid, true);
			outputHandler.writeSharedGroupsResponse(response, groups, ownerid);
			break;
		case GetGroupsDescriptionsOnly:
			List<UwsShareGroup> groupsDescriptions = shareManager.getGroupsByOwner(ownerid, false);
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
	public Object getParameters(UwsActionRequest actionRequest, Action action) throws UwsException {
		// nothing to do
		return null;
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
