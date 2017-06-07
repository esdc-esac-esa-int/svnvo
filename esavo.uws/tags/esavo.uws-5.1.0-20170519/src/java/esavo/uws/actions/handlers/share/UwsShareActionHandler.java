package esavo.uws.actions.handlers.share;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;

public abstract class UwsShareActionHandler implements UwsActionHandler {

//	public static final String PARAM_ACTION = "action";
//	public static final String PARAM_GROUP_ID = "group_id";
//	public static final String PARAM_RESOURCE_ID = "resource_id";
//	public static final String PARAM_RESOURCE_TYPE = "resource_type";
//	public static final String PARAM_SHARE_TYPE = "share_type";
//	public static final String PARAM_SHARE_TO_ID = "share_to_id";
//	public static final String PARAM_SHARE_MODE = "share_mode";
//	public static final String PARAM_USER_ID = "user_id";
//	public static final String PARAM_TITLE = "title";
//	public static final String PARAM_DESCRIPTION = "description";
//	public static final String PARAM_USERS_LIST = "users_list";
//	public static final String PARAM_ITEMS_LIST = "items_list";
	
	public static final String ACTION_NAME = "share";
	public static final boolean IS_JOB_ACTION = false;


	protected enum Action{
		CreateOrUpdateGroup,			//Create group
		CreateUserGroup,				//Create relation between user and group
		CreateOrUpdateItem,						//Create shared item
		CreateItemRelation,				//Create relation between item and user/group
		RemoveGroup,					//Remove group
		RemoveUserGroup,				//Remove relation between user and group
		RemoveItem,						//Remove shared item
		RemoveItemRelation,				//Remove relation between item and user/group
		GetGroupsDescriptionsOnly,		//Get groups (desc only) that belongs to the user
		GetGroupDescriptionOnly,		//Get group (desc only) that belong to the user.
		GetGroups,						//Get groups (desc+users) that belongs to the user.
		GetGroup,						//Get group (desc+users) that belong to the user.
		GetSharedItemsDescriptionsOnly,	//Get shared items descriptions only
		GetSharedItemDescriptionOnly,	//Get shared item description only
		GetSharedItems,					//Get shared items
		GetSharedItem,					//Get shared item
		GetUsers,						//Get users
		GetUserDetails,					//Get the user name from a userid
	}
	
	private String id;
	
	//private Action shareAction;
	
	public UwsShareActionHandler(String id) {
		this.id = id;
	}

	@Override
	public String getActionHandlerIdentifer() {
		return id;
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}
	
	@Override
	public boolean isJobAction() {
		return IS_JOB_ACTION;
	};

//	protected Action getShareAction(){
//		return shareAction;
//	}
	
	/**
	 * Returns 'true' if the parameters are valid.
	 * @param actionRequest
	 * @param action
	 * @return
	 * @throws UwsException
	 */
	public abstract Object getParameters(UwsActionRequest actionRequest, Action action) throws UwsException;
	
	/**
	 * Checks whether the handler can handle the action.
	 * @param action
	 * @return
	 */
	public abstract boolean isValidAction(Action action);
	
	protected Action getAction(UwsActionRequest actionRequest) throws UwsException {
		String shareAction = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_ACTION);
		try{
			return Action.valueOf(shareAction);
		}catch(IllegalArgumentException e){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+UwsHandlersUtils.PARAMETER_ACTION+"' value: " + shareAction + 
					". " + getValidValuesForAction());
		}
	}
	
//	/**
//	 * Returns the specified parameter or raises an exception if the parameter does not exist.
//	 * @param actionRequest
//	 * @param parameterName
//	 * @return
//	 * @throws UwsException
//	 */
//	protected String getExistingParam(UwsActionRequest actionRequest, String parameterName) throws UwsException {
//		if(!actionRequest.hasHttpParameter(parameterName)){
//			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Not found required parameter '"+parameterName+"'");
//		}
//		String value = actionRequest.getHttpParameter(parameterName);
//		if(value == null || "".equals(value)){
//			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Required parameter '"+parameterName+"' cannot be null.");
//		}
//		return value;
//	}
	
	protected int getExistingResourceType(UwsActionRequest actionRequest) throws UwsException {
		String value = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_RESOURCE_TYPE);
		try{
			return Integer.parseInt(value);
		}catch(NumberFormatException nfe){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+UwsHandlersUtils.PARAMETER_RESOURCE_TYPE+"' value: " + value + ". Expected an integer value.");
		}
	}
	
	protected UwsShareType getExistingShareType(UwsActionRequest actionRequest) throws UwsException {
		String value = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_SHARE_TYPE);
		UwsShareType shareType;
		try{
			shareType = UwsShareType.valueOf(value);
		}catch(IllegalArgumentException e){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+UwsHandlersUtils.PARAMETER_SHARE_TYPE+"' value: " + value + 
					". " + getValidValuesForShareType());
		}
		if(shareType == UwsShareType.All){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+UwsHandlersUtils.PARAMETER_SHARE_TYPE+"' value: " + shareType + 
					". " + getValidValuesForShareType());
		}
		return shareType;
	}
	
	protected UwsShareMode getExistingShareMode(UwsActionRequest actionRequest) throws UwsException {
		String value = UwsHandlersUtils.getExistingParam(actionRequest, UwsHandlersUtils.PARAMETER_SHARE_MODE);
		UwsShareMode shareMode;
		try{
			shareMode = UwsShareMode.valueOf(value);
		}catch(IllegalArgumentException e){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+UwsHandlersUtils.PARAMETER_SHARE_MODE+"' value: " + value + 
					". " + getValidValuesForShareMode());
		}
		return shareMode;
	}
	
	
	private String getValidValuesForShareMode(){
		return UwsShareMode.Read + " and " + UwsShareMode.Write;
	}
	
	private String getValidValuesForAction(){
		StringBuilder sb = new StringBuilder("Valid values are: ");
		boolean firstTime = true;
		for(Action a: Action.values()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append(a.name());
		}
		return sb.toString();
	}
	
	private String getValidValuesForShareType(){
		return "Valid values are: " + UwsShareType.Group.name() + ", and " + UwsShareType.User.name();
	}
	
	protected boolean checkValidAction(Action[] validActions, Action a) {
		for (Action va : validActions) {
			if (a == va) {
				return true;
			}
		}
		return false;

	}	

}
