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
package esavo.uws.actions.handlers.tasks;


import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

/**
 * Handles <code>{tasks}?TASKID=task_id&TASKTYPE=task_type</code></br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsTasksHandler implements UwsActionHandler {
	
	public static final String ID = "tasks";
	public static final String ACTION_NAME = "tasks";
	public static final boolean IS_JOB_ACTION = false;
	
	public static final String PARAM_TASKID = "TASKID";
	public static final String PARAM_TASKTYPE = "TASKTYPE";
	
	
	class Parameters{
		String taskId;
		String taskType;
	}
	
	public UwsTasksHandler(){
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
		
		return true;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		//will raise exception if required
		Parameters parameters = getParameters(actionRequest);
	
		long taskId;
		try{
			taskId = Long.parseLong(parameters.taskId);
		}catch(NumberFormatException nfe){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Wrong task identifier. Expected a long number. Found: '"+parameters.taskId+"'");
		}
	
		UwsStatusData data = null;
		String msg = null;
		String msgType = null;

		try{
			data = UwsStatusManager.getInstance().getStatus(taskId, parameters.taskType);
			if (data != null) {
				msg = data.getData();
			} else {
				msg = "";
			}
			msgType = "value";
		}catch(Exception e){
			msg = e.getMessage();
			msgType = "failed";
		}
			
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeTaskStatusResponse(response, parameters.taskId, parameters.taskType, msg, msgType);
	}
	
	private Parameters getParameters(UwsActionRequest actionRequest) throws UwsException {
		Parameters parameters = new Parameters();
		//will raise exception if required
		parameters.taskId = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_TASKID);
		parameters.taskType = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_TASKTYPE);
		return parameters;
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}


}
