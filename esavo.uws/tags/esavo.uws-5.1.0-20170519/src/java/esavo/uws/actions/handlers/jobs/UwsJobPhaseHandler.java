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

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.jobs.UwsJob;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, sections 2.2.3.5 and 2.2.36</br>
 * Handles <code>{job_list}/{job_id}/phase</code></br>
 * POST: Requires HTTP parameter <code>PHASE=phase</code> valid phases: 'RUN' and 'ABORT'</br>
 * POST: Response: 303 (see other). Location header must point to job: i.e. <code>{job_list}/{job_id}</code></br> 
 * GET: returns the current job phase value in text/plain<br/>
 * GET: Response: text/plain job phase value<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobPhaseHandler implements UwsActionHandler {
	
	public static final String ID = "job_phase";
	public static final String ACTION_NAME = "phase";
	private static final boolean IS_JOB_ACTION = true;

	//public static final String PARAMETER_PHASE = "PHASE";
	
//	public static final String PARAMETER_VALUE_PHASE_RUN = "RUN";
//	public static final String PARAMETER_VALUE_PHASE_ABORT = "ABORT";
	
	private enum AllowedPhases {
		RUN, ABORT
	}
	
	//private AllowedPhases newPhaseValue;
	class Parameters {
		AllowedPhases newPhaseValue;
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
		if(!actionRequest.hasJobList()){
			return false;
		}
		if(!actionRequest.hasJobId()){
			return false;
		}
		if(!actionRequest.isAction(ACTION_NAME)){
			return false;
		}
		return true;
	}
	
	private Parameters getParameters(UwsActionRequest actionRequest) throws UwsException {
		Parameters actionParameters = new Parameters();
		if(actionRequest.isPost()){
			//Posting value: parameter required
			if(!actionRequest.hasHttpParameter(UwsHandlersUtils.PARAMETER_PHASE)){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Missing expected '"+UwsHandlersUtils.PARAMETER_PHASE+"' parameter for '"+ACTION_NAME+"' handler");
			}
//			//Allowed values are 'RUN' or 'ABORT'
			String sNewPhaseValue = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_PHASE);
			try{
				actionParameters.newPhaseValue = AllowedPhases.valueOf(sNewPhaseValue.toUpperCase());
			}catch(Exception e){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
						"Invalid phase value '"+sNewPhaseValue+"'. Only '"+AllowedPhases.RUN.name()+"' and '"+AllowedPhases.ABORT.name()+"' are allowed.");
			}
		}
		return actionParameters;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		Parameters actionParameters = getParameters(actionRequest);
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		if(actionRequest.isPost()){
			String jobid = actionRequest.getJobid();
			//setting phase: return 303
			//returns 500 if phase cannot be changed
			try{
				switch(actionParameters.newPhaseValue){
				case RUN:
					//The method checks user permissions and will raise an exception if required.
					uwsManager.startJob(jobid, currentUser);
					break;
				case ABORT:
					//The method checks user permissions and will raise an exception if required.
					uwsManager.abortJob(jobid, currentUser);
					break;
				}
				String urlLocation = UwsOutputUtils.getJobHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), jobid);
				outputHandler.redirectResponse(response, urlLocation, null);
			}catch(UwsException e){
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Cannot change phase to '"+actionParameters.newPhaseValue+"'. Job '"+jobid+"'", e);
			}
		} else {
			//return text/plain phase value
			//The method checks user permissions and will raise an exception if required.
			UwsJob job = uwsManager.tryLoadJob(actionRequest.getJobid(), currentUser);
			outputHandler.writeTextPlainResponse(response, job.getPhase().name());
		}
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
