package esavo.uws.actions.handlers.jobs;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
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
	public static final String PARAMETER_PHASE = "PHASE";
	
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
			if(!actionRequest.hasHttpParameter(PARAMETER_PHASE)){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Missing expected '"+PARAMETER_PHASE+"' parameter for '"+ACTION_NAME+"' handler");
			}
//			//Allowed values are 'RUN' or 'ABORT'
			String sNewPhaseValue = actionRequest.getHttpParameter(PARAMETER_PHASE);
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
