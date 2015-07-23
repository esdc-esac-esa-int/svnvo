package esavo.uws.actions.handlers.jobs;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, section 2.2.3.2</br>
 * Handles DELETE <code>{job_list}/{job_id}</code> or </br>
 * Handles POST <code>{job_list}/{job_id}?ACTION=DELETE</code> or </br>
 * DELETE: Response: 303 (see other). Location header must point to the job list: i.e. <code>{job_list}</code></br>
 * POST: Requires HTTP parameter <code>ACTION=DELETE</code> </br>
 * POST: Response: 303 (see other). Location header must point to the job list: i.e. <code>{job_list}</code></br> 
 * GET: not allowed<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobDeleteHandler implements UwsActionHandler {

	public static final String ID = "job_delete";
	public static final String ACTION_NAME = "delete";
	public static final String PARAMETER_ACTION = "ACTION";
	public static final String PARAMETER_ACTION_VALUE = "DELETE";

	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner currentUser, UwsActionRequest actionRequest) {
		if(!actionRequest.hasJobList()){
			return false;
		}
		if(!actionRequest.hasJobId()){
			return false;
		}
		if(actionRequest.isPost()){
			//Posting value: parameter required
			if(!hasDeleteParameter(actionRequest)){
				return false;
			}
		}else{
			if(!actionRequest.isDelete()){
				return false;
			}
		}
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		String jobid = actionRequest.getJobid();
		//The method checks user permissions and will raise an exception if required.
		uwsManager.removeJob(jobid, currentUser);
		String url = UwsOutputUtils.getJobListHref(actionRequest.getBaseUrl(), actionRequest.getJobListName());
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.redirectResponse(response, url, null);
	}
	
	private boolean hasDeleteParameter(UwsActionRequest actionRequest){
		if(actionRequest == null){
			return false;
		}
		if(!actionRequest.hasHttpParameter(PARAMETER_ACTION)){
			return false;
		}
		if(!actionRequest.getHttpParameter(PARAMETER_ACTION).equalsIgnoreCase(PARAMETER_ACTION_VALUE)){
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
