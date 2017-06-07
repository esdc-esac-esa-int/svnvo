package esavo.uws.actions.handlers.jobs;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, (not fully specified yet)</br>
 * Handles <code>{job_list}/{job_id}/owner</code></br>
 * POST: not allowed<br/>
 * GET: returns the xml representation of a job owner<br/>
 * GET: Response: 200. Job owner (xml).</br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobOwnerHandler implements UwsActionHandler {
	
	private static final String ID = "job_owner";
	public static final String ACTION_NAME = "owner";
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
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		//This method will raise an exception if the user has no permissions.
		UwsJob job = uwsManager.tryLoadJob(actionRequest.getJobid(), currentUser);
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeTextPlainResponse(response, job.getOwner().getId());
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
