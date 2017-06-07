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
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Rec. 1.0, section 2.2.2.1</br>
 * Handles <code>{job_list}</code></br>
 * POST: not allowed<br/>
 * GET: returns the xml representation of the job list<br/>
 * GET: Response: 200. Job list (xml).</br>
 * 
 * <p>To allow sorting, see {@link UwsJobsHandler}
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsListJobsHandler implements UwsActionHandler {
	
	private static final String ID = "jobs_list";
	private static final boolean IS_JOB_ACTION = true;
	
	//public static final String PARAMETER_SESSION = "session";

	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		//No action name
		return null;
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
		if(!actionRequest.hasJobList()){
			return false;
		}
		if(actionRequest.hasJobId()){
			return false;
		}
		if(actionRequest.hasAction()){
			return false;
		}
		
////		if(actionRequest.getHttpParameter(PARAMETER_LIMIT)!=null || actionRequest.getHttpParameter(PARAMETER_OFFSET)!=null || actionRequest.getHttpParameter(PARAMETER_ORDER)!=null){
////			return true;
////		}
//		
////		if(actionRequest.hasHttpParameters() || actionRequest.isMultipartContent()){
////			return false;
////		}
//		
//		if(actionRequest.isMultipartContent()){
//			return false;
//		}
		
		if(actionRequest.hasHttpParameters() || actionRequest.isMultipartContent()){
			return false;
		}


		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
//		String session = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SESSION);
//		if(session != null){
//			//to filter by session
//			currentUser.setSession(session);
//		}
//		String session = null;
//		if(UwsUtils.isAnonymous(currentUser)){
//			//Only anonymous requries to filter by session id when retrieving jobs
//			//for anonymous,
//			session = currentUser.getSession();
//		}

		String session = UwsUtils.getSessionFilterForUser(actionRequest, currentUser);

		UwsJobsFilter filter = UwsHandlersUtils.createFilter(actionRequest, currentUser.getId(), session);
		String listName = actionRequest.getJobListName();
		//This method returns public (anonymous) jobs and the user jobs.
		//List<UwsJob> jobs = uwsManager.getJobsMetaList(listName, currentUser);
		List<UwsJob> jobs = uwsManager.getJobsMetaList(listName, filter, null, null, null, true);
		String baseUrl = actionRequest.getBaseUrl();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeJobListResponse(response, baseUrl, listName, jobs);
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
