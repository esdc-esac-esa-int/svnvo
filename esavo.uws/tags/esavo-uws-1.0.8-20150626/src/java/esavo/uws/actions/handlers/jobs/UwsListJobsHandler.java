package esavo.uws.actions.handlers.jobs;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Rec. 1.0, section 2.2.2.1</br>
 * Handles <code>{job_list}</code></br>
 * POST: not allowed<br/>
 * GET: returns the xml representation of the job list<br/>
 * GET: Response: 200. Job list (xml).</br>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsListJobsHandler implements UwsActionHandler {
	
	private static final String ID = "jobs_list";
	
	public static final String PARAMETER_SESSION = "session";

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
		if(actionRequest.hasHttpParameters() || actionRequest.isMultipartContent()){
			return false;
		}
		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		String session = actionRequest.getHttpParameter(PARAMETER_SESSION);
		if(session != null){
			//to filter by session
			currentUser.setSession(session);
		}
		String listName = actionRequest.getJobListName();
		//This method returns public (anonymous) jobs and the user jobs.
		List<UwsJob> jobs = uwsManager.getJobList(listName, currentUser);
		String baseUrl = actionRequest.getBaseUrl();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeJobListResponse(response, jobs, listName, baseUrl);
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
