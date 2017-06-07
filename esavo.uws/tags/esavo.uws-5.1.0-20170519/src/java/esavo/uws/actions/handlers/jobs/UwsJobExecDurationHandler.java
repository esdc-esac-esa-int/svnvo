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
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, section 2.2.3.4</br>
 * Handles <code>{job_list}/{job_id}/executionduration</code></br>
 * POST: Requires HTTP parameter <code>EXECUTIONDURATION=exec_duration_in_seconds</code> (in seconds)</br>
 * POST: Response: 303 (see other). Location header must point to job: i.e. <code>{job_list}/{job_id}</code></br>
 * GET: returns destruction value in text/plain<br/>
 * GET: Response: text/plain execution duration value<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobExecDurationHandler implements UwsActionHandler {

	public static final String ID = "job_exec_duration";
	public static final String ACTION_NAME = "executionduration";
	private static final boolean IS_JOB_ACTION = true;

	//public static final String PARAMETER_EXEC_DURATION = "EXECUTIONDURATION";
	
	//public long execDuration;
	class Parameters{
		long execDuration;
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
			if(!actionRequest.hasHttpParameter(UwsHandlersUtils.PARAMETER_EXEC_DURATION)){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
						"Missing expected '"+UwsHandlersUtils.PARAMETER_EXEC_DURATION+"' parameter for '"+ACTION_NAME+"' handler");
			}
			String sExecDuration = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_EXEC_DURATION);
			try{
				actionParameters.execDuration = Long.parseLong(sExecDuration);
			}catch(NumberFormatException nfe){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Invalid long format for 'execuctionDuration' parameter: " + sExecDuration);
			}
		}
		return actionParameters;
	}


	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		String jobid = actionRequest.getJobid();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		if(actionRequest.isPost()){
			//This method will raise a UwsException if the job cannot be updated.
			//This method will raise an exception if the user has no permissions.
			Parameters actionParameters = getParameters(actionRequest);
			
			uwsManager.tryUpdateJobAttribute(jobid, currentUser, UwsJobAttribute.ExecutionDuration, new Long(actionParameters.execDuration));
			String url = UwsOutputUtils.getJobHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), jobid);
			outputHandler.redirectResponse(response, url, null);
		}else{
			//get value
			//This method will raise an exception if the user has no permissions.
			UwsJob job = uwsManager.tryLoadJob(jobid, currentUser);
			//String dateAsString = UwsUtils.formatDate(job.getDestructionTime());
			//outputHandler.writeTextPlainResponse(response, dateAsString);
			String execDurationAsString = "" + job.getExecutionDuration();
			outputHandler.writeTextPlainResponse(response, execDurationAsString);
		}
		
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
