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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;

/**
 * Rec. 1.0, section 2.2.2.5</br>
 * Handles <code>{job_list}/{job_id}/error</code></br>
 * POST: not allowed<br/>
 * GET: NO ERROR or NO DETAILS: Response: 200. Error summary (xml).</br>
 * GET: ERROR WITH DETAILS: Response: 303 (see other).<br/> 
 * &nbsp;&nbsp;  Location header must point to error/details (extracted from {@link UwsJobErrorSummaryMeta#getUrlDetails()})<br/> 
 * &nbsp;&nbsp;  <code>{job_list}/{job_id}/error/{details}</code>.</br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobErrorHandler implements UwsActionHandler {
	
	private static final String ID = "job_error";
	public static final String ACTION_NAME = "error";
	public static final String SUBACTION_NAME = "details";
	private static final boolean IS_JOB_ACTION = true;


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
		if(!actionRequest.hasJobList()){
			return false;
		}
		if(!actionRequest.hasJobId()){
			return false;
		}
		if(!actionRequest.isAction(ACTION_NAME)){
			return false;
		}
		if(actionRequest.hasSubAction()){
			//asking for joblist/jobid/error/details: check sub-action is details
			if(!actionRequest.isSubAction(SUBACTION_NAME)){
				//error
				throw new UwsException(
						UwsOutputResponseHandler.BAD_REQUEST, 
						"Wrong error details specification. Expected '"+SUBACTION_NAME+"'. Found: '"+actionRequest.getSubActionName()+"'");
			}
		}
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		//This method will raise an exception if the user has no permissions.
		UwsJob job = uwsManager.tryLoadJob(actionRequest.getJobid(), currentUser);
		UwsJobErrorSummaryMeta errorSummary = job.getErrorSummary();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		if(actionRequest.hasSubAction()){
			//Error summary details data (dump error details data)
			dumpErrorSummary(job, errorSummary, uwsManager, response, outputHandler);
		} else {
			//Asking for details
			if(errorSummary == null){
				outputHandler.writeJobErrorResponse(response, null);
				//throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "No details available");
			} else {
				if(errorSummary.hasDetails()){
					//error summary with details: redirect
					String urlDetails = UwsOutputUtils.getErrorHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), actionRequest.getJobid());
					outputHandler.redirectResponse(response, urlDetails, null);
				}else{
					//error summary details
					outputHandler.writeJobErrorResponse(response, errorSummary);
				}
			}
//			if(errorSummary != null && errorSummary.hasDetails()){
//				//Has details: redirect
//				String urlDetails = UwsOutputUtils.getErrorHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), actionRequest.getJobid());
//				outputHandler.redirectResponse(response, urlDetails, null);
//			}else{
//				//No details: error
//				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "No details available");
//			}
		}
	}
	
	
	private void dumpErrorSummary(UwsJob job, UwsJobErrorSummaryMeta errorSummary, UwsManager uwsManager, 
			HttpServletResponse response, UwsOutputResponseHandler outputHandler) throws UwsException {
		UwsStorage storage = uwsManager.getFactory().getStorageManager();
		InputStream source = storage.getJobErrorDetailsDataInputSource(job);
		try{
			outputHandler.writeJobErrorDataResponse(response, job.getJobId(), errorSummary, source);
		}finally{
			if(source != null){
				try{
					source.close();
				}catch(IOException ioe){
					//TODO ?
				}
			}
		}
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
