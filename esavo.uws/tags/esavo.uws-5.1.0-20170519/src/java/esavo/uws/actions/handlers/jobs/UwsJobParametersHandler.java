package esavo.uws.actions.handlers.jobs;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, sections 2.1.11 and 2.2.2.4</br>
 * Handles <code>{job_list}/{job_id}/parameters</code></br>
 * POST: a list of parameter_name=parameter_value should be provided (if not provided, no updated action is performed).
 * POST: Response: 303 (see other). Location header must point to job: i.e. <code>{job_list}/{job_id}</code></br> 
 * GET: returns a parameter list in XML<br/>
 * GET: Response: 200, parameter list in XML<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobParametersHandler implements UwsActionHandler {
	
	public static final String ID = "job_parameters";
	public static final String ACTION_NAME = "parameters";
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
		if(!actionRequest.isGet() && !actionRequest.isPost()){
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
		if(actionRequest.isPost()){
			//UwsJobParameters newParameters = actionRequest.createParameters();
			UwsJobParameters newParameters = actionRequest.createJobParametersAsStrings();
			uwsManager.tryUpdateJobParameters(job.getJobId(), currentUser, newParameters.getParameters());
			String urlLocation = UwsOutputUtils.getJobHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), job.getJobId());
			outputHandler.redirectResponse(response, urlLocation, null);
		} else {
			//GET: 200, xml with parameters
			outputHandler.writeJobParametersResponse(response, job.getParameters());
		}
		
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
