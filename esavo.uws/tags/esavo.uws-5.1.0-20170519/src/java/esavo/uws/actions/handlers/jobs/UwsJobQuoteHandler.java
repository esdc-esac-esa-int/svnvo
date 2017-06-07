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
 * Rec. 1.0, section 2.1.6</br>
 * Handles <code>{job_list}/{job_id}/quote</code></br>
 * POST: Not allowed
 * GET: returns quote value in text/plain<br/>
 * GET: Response: text/plain destruction value<br/>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobQuoteHandler implements UwsActionHandler {
	
	public static final String ID = "job_quote";
	public static final String ACTION_NAME = "quote";
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
		//The method checks user permissions and will raise an exception if required.
		UwsJob job = uwsManager.tryLoadJob(actionRequest.getJobid(), currentUser);
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		long quote = job.getQuote();
//		if(d == null){
//			outputHandler.writeTextPlainResponse(response, UwsUtils.formatDate(d));
//		}else{
//			outputHandler.writeTextPlainResponse(response, "-1");
//		}
		outputHandler.writeTextPlainResponse(response, ""+quote);
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
