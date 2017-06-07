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
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;

/**
 * Rec. 1.0, section 2.2.2.3</br>
 * Handles <code>{job_list}/{job_id}/results</code></br>
 * POST: not allowed<br/>
 * GET: returns the xml representation of a results list associated to a job<br/>
 * GET: Response: 200. Results list (xml).</br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobResultsHandler implements UwsActionHandler {
	
	public static final String ID = "job_result";
	public static final String ACTION_NAME = "results";
	private static final boolean IS_JOB_ACTION = true;
	private static final String PARAMETER_OUTPUT_FORMAT = "format";

	//public static final String SUBACTION_NAME = "result";

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
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) {
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
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		//The method checks user permissions and will raise an exception if required.
		UwsJob job = uwsManager.tryLoadJob(actionRequest.getJobid(), currentUser);
		List<UwsJobResultMeta> results = job.getResults();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		if(actionRequest.hasSubAction()){
			String resultId = actionRequest.getSubActionName();
			for(UwsJobResultMeta result: results){
				if(resultId.equals(result.getId())){
					// 
					String outputFormat = actionRequest.getHttpParameter(PARAMETER_OUTPUT_FORMAT);
					//found
					dumpResult(job, result, outputFormat, uwsManager, response, outputHandler);
					return;
				}
			}
			//Not found: error result not found
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Result identifier '"+resultId+"' not found in job '"+actionRequest.getJobid()+"'");
		} else {
			//results list
			outputHandler.writeJobResultListResponse(response, actionRequest.getBaseUrl(), job);
		}
	}
	
	private void dumpResult(UwsJob job, UwsJobResultMeta result, String outputFormat, UwsManager uwsManager, 
			HttpServletResponse response, UwsOutputResponseHandler outputHandler) throws UwsException {
		UwsStorage storage = uwsManager.getFactory().getStorageManager();
		InputStream source = storage.getJobResultDataInputSource(job, result.getId());
		try{
			outputHandler.writeJobResultDataResponse(response, job, result, outputFormat, source);
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
