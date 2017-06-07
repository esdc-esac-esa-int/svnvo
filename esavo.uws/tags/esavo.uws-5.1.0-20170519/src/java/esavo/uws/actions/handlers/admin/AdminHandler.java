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
package esavo.uws.actions.handlers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmEventsHandler;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmJobChangeNameHandler;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmJobsRemoveProcedure;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmStatistics;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmUserDetailsHandler;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmUserUpdateHandler;
import esavo.uws.actions.handlers.admin.handlers.UwsAdmUsersListHandler;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

/**
 * Handles <code>{admin}?action=action</code></br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class AdminHandler  implements UwsActionHandler{

	public static final String ID = "admin";
	public static final String ACTION_NAME = "admin";
	public static final boolean IS_JOB_ACTION = false;

	
	private static final List<UwsFunctionsHandler> handlers = new ArrayList<UwsFunctionsHandler>();
	static{
		handlers.add(new UwsAdmUsersListHandler());
		handlers.add(new UwsAdmUserDetailsHandler());
		//handlers.add(new JobsListHandler());
		//handlers.add(new JobDetailsHandler());
		//handlers.add(new HandleJobHandler());
		handlers.add(new UwsAdmUserUpdateHandler());
		handlers.add(new UwsAdmJobsRemoveProcedure());
		handlers.add(new UwsAdmEventsHandler());
		handlers.add(new UwsAdmJobChangeNameHandler());
		handlers.add(new UwsAdmStatistics());
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
	public boolean isJobAction(){
		return IS_JOB_ACTION;
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner currentUser, UwsActionRequest actionRequest) throws UwsException {
		return actionRequest.hasHandlerAction(ACTION_NAME);
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		UwsSecurity security = uwsManager.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			HttpServletRequest request = actionRequest.getRequest();
			user = security.getUser(request);
		} catch (UwsException e) {
			throw new UwsException("Cannot obtain current user: " + e.getMessage(), e);
		}
		
		if(user == null){
			throw new UwsException("Cannot obtain current user");
		}
		
		String session = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SESSION);
		if(session != null){
			//to filter by session
			currentUser.setSession(session);
		}
		
		String action = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ACTION);
		for(UwsFunctionsHandler handler: handlers){
			if(handler.canHandle(action)){
				try {
					handler.handle(actionRequest, response, uwsManager, currentUser);
				} catch (UwsException e) {
					throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error handling action '"+action+"'" + e.getMessage(), e);
				}
				return;
			}
		}
		
		throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid action '"+action+"'\n" + getAvailableActions());
	}

	private String getAvailableActions(){
		StringBuilder sb = new StringBuilder();
		sb.append("<p>Available actions:\n");
		sb.append("<ul>\n");
		for(UwsFunctionsHandler handler: handlers){
			sb.append("<li>").append(handler.getActionIdentifier()).append("</li>\n");
		}
		sb.append("</ul>\n");
		sb.append("</p>\n");
		return sb.toString();
	}
	
}
