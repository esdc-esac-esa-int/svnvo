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
package esavo.uws.actions.handlers.jobs;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Handles multiple jobs DELETE <code>/deletejobs</code> or </br>
 * Handles POST <code>deletejobs?JOB_IDS=jobid1,jobid2...</code> or </br>
 * GET: not allowed<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class DeleteMultipleJobsHandler implements UwsActionHandler {

	public static final String ID = "deletejobs";
	public static final String ACTION_NAME = "deleteJobs";
	public static final boolean IS_JOB_ACTION = false;
	//public static final String PARAMETER_JOB_IDS = "JOB_IDS";

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
	public boolean canHandle(String appid, UwsJobOwner currentUser, UwsActionRequest actionRequest) {
		if(actionRequest.hasJobList()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}
		if(!actionRequest.isPost()){
			return false;
		}
		if(!hasJobIdsParameter(actionRequest)){
			return false;
		}
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		String jobids = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_JOB_IDS);
		List<String> jobidsList = UwsHandlersUtils.getList(jobids);
		//The method checks user permissions and will raise an exception if required.
		uwsManager.removeJobs(jobidsList, UwsConfiguration.ASYNC_LIST_ID, currentUser);

		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//outputHandler.redirectResponse(response, url, null);
		outputHandler.writeTextPlainResponse(response, "OK");

	}
	
	private boolean hasJobIdsParameter(UwsActionRequest actionRequest){
		if(actionRequest == null){
			return false;
		}
		if(!actionRequest.hasHttpParameter(UwsHandlersUtils.PARAMETER_JOB_IDS)){
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
