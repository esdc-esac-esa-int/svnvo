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
package esavo.uws.actions.handlers.scheduler.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsScheduler;
import esavo.uws.scheduler.UwsScheduler.SchedulerMode;

/**
 * Example: curl http://<server>/<tap-context>/tap/admin?ACTION=scheduler_set_mode&scheduler_mode=ALL
 * @author rgutierrez
 *
 */
public class UwsSchedulerModeHandler implements UwsFunctionsHandler {

	public static final String ACTION = "SchedulerMode";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException {
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		UwsScheduler uwsScheduler = uwsManager.getFactory().getScheduler();

		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, uwsOutput, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}

		String paramMode = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SET_SCHEDULER_MODE);
		SchedulerMode mode = uwsScheduler.getSchedulerMode();
		boolean newMode = false;

		if(paramMode!=null){
			try{
				mode = SchedulerMode.valueOf(paramMode);
				newMode=true;
			}catch(Exception e){
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid scheduler mode provided.");
			}
		}

		
		try {
			if(newMode){
				uwsScheduler.setSchedulerMode(mode);
			}
			uwsOutput.writeTextPlainResponse(response, "Scheduler mode: "+uwsScheduler.getSchedulerMode());
		} catch (UwsException e) {
			int code = e.getCode();
			if (code < 0){
				code = UwsOutputResponseHandler.INTERNAL_SERVER_ERROR;
			}
			throw new UwsException(code, "Cannot change scheduler mode: " + e.getMessage(), e);
		}
		return;
	}
	
	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
