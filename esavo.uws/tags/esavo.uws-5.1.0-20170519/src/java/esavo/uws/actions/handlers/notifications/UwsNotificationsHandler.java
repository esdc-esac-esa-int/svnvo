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
package esavo.uws.actions.handlers.notifications;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.notifications.UwsNotificationType;
import esavo.uws.notifications.UwsNotificationsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Handles <code>{notification}?action=action&...</code></br>
 * 
 * <pre><tt>
 * GetNotifications:
 * 	-all notifications are returned (plain text)
 * 		i.e:
 * 		notification_id1[type: type1,subtype1]=msg1
 * 		notification_id2[type: type2,subtype2]=msg2
 * 		...
 * 		notification_idn[type: typen,subtypen]=msgn
 * 
 * MarkNotificationAsRead:
 * -Input: a list of notification ids (comma separated)
 * 	OK/NOK
 * 
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsNotificationsHandler implements UwsActionHandler {
	
	public static final String ID = "notification_request";
	public static final String ACTION_NAME = "notification";
	private static final boolean IS_JOB_ACTION = false;

	
	public static final String PARAM_ACTION = "action";
	public static final String PARAM_NOTIFICATIONS_ID = "notifications_id";
	public static final String PARAM_SYSTEM_WARNING = "system_warning";
	
	protected enum Action{
		GetNotifications,			//Get the notifications associated to a user
		MarkNotificationsAsRead,	//Mark a notification as read
		CreateSystemWarningNotification, //Create system warning notification
		DeleteSystemWarningNotifications  //Delete a system warning notification
	}

	
	class Parameters{
		List<String> notificationIds;
		String systemWarningNotification;
	}
	
	public UwsNotificationsHandler(){
	}
	
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
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}

		//Raises an exception if the parameter 'action' is not found.
		//Raises an exception if the parameter 'action' is not found.
		getAction(actionRequest);
		
		return true;
	}
	
	protected Action getAction(UwsActionRequest actionRequest) throws UwsException {
		String shareAction = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_ACTION);
		try{
			return Action.valueOf(shareAction);
		}catch(IllegalArgumentException e){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+PARAM_ACTION+"' value: " + shareAction + 
					". " + getValidValuesForAction());
		}
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

	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		Action action = getAction(actionRequest);
		Parameters parameters = getParameters(actionRequest, action);
	
		UwsNotificationsManager notificationsManager = uwsManager.getFactory().getNotificationsManager();
		String msg;
		switch(action){
		case GetNotifications:
			List<UwsNotificationItem> notifications = notificationsManager.getNotificationsForUser(currentUser);
			msg = getOutput(notifications);
			break;
		case MarkNotificationsAsRead:
			notificationsManager.markNotificationAsRead(currentUser, parameters.notificationIds);
			msg = "OK";
			break;
		case CreateSystemWarningNotification:
			if(!currentUser.isAdmin()){
				throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
						"User '" + currentUser.getId() + "' is not authorized to create system warning notifications");
			}
			HashSet<String> allUsers = new HashSet<String>();
			allUsers.add(UwsJobOwner.ALL_USERS_OWNER.getId());
			UwsNotificationItem uwsNotificationItem = notificationsManager.createNotification(UwsNotificationType.SYSTEM_WARNING_NOTIFICATION, -1, parameters.systemWarningNotification, allUsers);
			msg = "SYSTEM WARNING NOTIFICATION CREATED: " + uwsNotificationItem.getId();
			break;
		case DeleteSystemWarningNotifications:
			if(!currentUser.isAdmin()){
				throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
						"User '" + currentUser.getId() + "' is not authorized to create system warning notifications");
			}
			int deleted = notificationsManager.deleteSystemWarningNotifications(parameters.notificationIds);
			msg = deleted+ " SYSTEM WARNING NOTIFICATIONS DELETED";
			break;
		default:
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Unknown action");
		}
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeTextPlainResponse(response, msg);
	}
	
	private String getOutput(List<UwsNotificationItem> items){
		if(items == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(UwsNotificationItem item: items){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append("\n");
			}
			sb.append(item.getId()).append("[type: ").append(item.getType()).append(',').append(item.getSubtype()).append("]=").append(item.getMsg());
		}
		return sb.toString();
	}
	
	
	private Parameters getParameters(UwsActionRequest actionRequest, Action action) throws UwsException {
		Parameters parameters = new Parameters();
		String allIds = null;
		switch(action){
		case GetNotifications:
			//nothing to get: user is retrieved from environment
			break;
		case MarkNotificationsAsRead: 
		case DeleteSystemWarningNotifications:
			//user is retrieved from environment
			allIds = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_NOTIFICATIONS_ID);
			parameters.notificationIds = UwsHandlersUtils.getList(allIds);
			break;
		case CreateSystemWarningNotification:
			//user is retrieved from environment
			String systemWarningNotification = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_SYSTEM_WARNING);
			try {
				parameters.systemWarningNotification = URLDecoder.decode(systemWarningNotification, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new UwsException(e);
			}
			break;
		default:
			//not valid case
		}
		return parameters;
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}


}
