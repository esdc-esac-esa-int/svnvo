package esavo.uws.actions.handlers.jobs;

import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Rec. 1.0, section 2.2.3.3</br>
 * Handles <code>{job_list}/{job_id}/destruction</code></br>
 * POST: Requires HTTP parameter <code>DESTRUCTION=destruction_time</code> format ISO8601</br>
 * POST: Response: 303 (see other). Location header must point to job: i.e. <code>{job_list}/{job_id}</code></br> 
 * GET: returns destruction value in text/plain<br/>
 * GET: Response: text/plain destruction value<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobDestructionHandler implements UwsActionHandler {
	
	public static final String ID = "job_destruction";
	public static final String ACTION_NAME = "destruction";
	public static final String PARAMETER_DESTRUCTION = "DESTRUCTION";
	
//	/**
//	 * This attibute is populated in {@link #canHandle(String, UwsJobOwner, UwsActionRequest)}.
//	 */
//	private Date dateToSet = null;
	
	class Parameters{
		Date dateToSet = null;
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
			if(!actionRequest.hasHttpParameter(PARAMETER_DESTRUCTION)){
				throw new UwsException(
						UwsOutputResponseHandler.BAD_REQUEST, 
						"Missing expected '"+PARAMETER_DESTRUCTION+"' parameter for '"+ACTION_NAME+"' handler");
			}
			//check format
			String date = actionRequest.getHttpParameter(PARAMETER_DESTRUCTION);
			try {
				actionParameters.dateToSet = UwsUtils.formatDate(date);
			} catch (ParseException e) {
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Invalid date-time format for 'destruction' parameter: " + date);
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
			uwsManager.tryUpdateJobAttribute(jobid, currentUser, UwsJobAttribute.DestructionTime, actionParameters.dateToSet);
			String url = UwsOutputUtils.getJobHref(actionRequest.getBaseUrl(), actionRequest.getJobListName(), jobid);
			outputHandler.redirectResponse(response, url, null);
		}else{
			//get value
			//This method will raise an exception if the user has no permissions.
			UwsJob job = uwsManager.tryLoadJob(jobid, currentUser);
			String dateAsString = UwsUtils.formatDate(job.getDestructionTime());
			if(dateAsString == null){
				dateAsString = "null";
			}
			outputHandler.writeTextPlainResponse(response, dateAsString);
		}
		
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
