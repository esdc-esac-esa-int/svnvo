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
package esavo.uws.actions.handlers.admin.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.event.UwsEventType;
import esavo.uws.jobs.UwsJob;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;

public class UwsAdmJobChangeNameHandler implements UwsFunctionsHandler {

	public static final String ACTION = "job_change_name";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException {
		String jobid = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_JOB_ID);
		if(jobid == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Job identifier not found.");
		}
		
		String jobName = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_JOB_NAME);
		if(jobName == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Job name not found.");
		}
		
		UwsStorage uwsStorage = uwsManager.getFactory().getStorageManager();
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		
		try {
			UwsJob job = uwsStorage.getJobMeta(jobid);
			if(!UwsHandlersUtils.checkValidUser(job, currentUser, uwsOutput, response)){
				throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
			}
			uwsStorage.changeJobName(jobid, jobName);
			//Generate job event
			uwsManager.getFactory().getEventsManager().setEventTime(currentUser, UwsEventType.JOB_UPDATED_EVENT);
			uwsOutput.writeTextPlainResponse(response, "OK");
		} catch (UwsException e) {
			int code = e.getCode();
			if (code < 0){
				code = UwsOutputResponseHandler.INTERNAL_SERVER_ERROR;
			}
			throw new UwsException(code, "Cannot change job name: " + e.getMessage(), e);
		}
		return;
	}
	
	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
