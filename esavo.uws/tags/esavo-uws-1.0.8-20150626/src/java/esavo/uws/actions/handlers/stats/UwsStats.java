package esavo.uws.actions.handlers.stats;


import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

/**
 * Handles <code>{stats}?action=action&...</code></br>
 * 
 * <pre><tt>
 * GetStats:
 * 	-all statistics are returned (plain text)
 * 
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStats implements UwsActionHandler {
	
	public static final String ID = "stats";
	public static final String ACTION_NAME = "stats";
	
	public static final String PARAM_ACTION = "action";
	
	protected enum Action{
		GetStats			//Get all statistics
	}
	
	class Parameters{
		
	}
	
	public UwsStats(){
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
		if(!actionRequest.isGet()){
			return false;
		}
		if(!actionRequest.hasStats()){
			return false;
		}

		return true;
	}
	
	protected Action getAction(UwsActionRequest actionRequest) throws UwsException {
		String action = UwsHandlersUtils.getExistingParam(actionRequest, PARAM_ACTION);
		try{
			return Action.valueOf(action);
		}catch(IllegalArgumentException e){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, 
					"Invalid required parameter '"+PARAM_ACTION+"' value: " + action + 
					". " + getValidValuesForAction());
		}
	}

	private String getValidValuesForAction(){
		StringBuilder sb = new StringBuilder("Valid values are: ");
		boolean firstTime = true;
		for(Action a: Action.values()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append(a.name());
		}
		return sb.toString();
	}

	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		if(currentUser.getId().equals(UwsUtils.ANONYMOUS_USER)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
					"User '" + currentUser.getId() + "' is not authorized to use statistics capabilities");
		}
		if(!currentUser.isAdmin()){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
					"User '" + currentUser.getId() + "' is not authorized to use statistics capabilities (admin. role is required)");
		}
		
		Action action;
		if(actionRequest.hasHttpParameter(PARAM_ACTION)){
			action = getAction(actionRequest);	
		}else{
			//by default
			action = Action.GetStats;
		}
		
		Parameters parameters = getParameters(actionRequest, action);
	
		String msg;
		switch(action){
		case GetStats:
			int numberOfJobsInMemory = uwsManager.getNumberOfJobsInMemory();
			
			msg = "Number of jobs in memory: " + numberOfJobsInMemory;
			
			UwsStorage uwsStorage = uwsManager.getFactory().getStorageManager();
			UwsJobsFilter filter = new UwsJobsFilter();
			
			filter.setFilterByPhaseId(UwsJobPhase.EXECUTING.toString(), false);
			List<UwsJobDetails> jobsExecuting = uwsStorage.retrieveJobsByFilter(filter, -1, -1);
			
			filter.setFilterByPhaseId(UwsJobPhase.QUEUED.toString(), false);
			List<UwsJobDetails> jobsQueued = uwsStorage.retrieveJobsByFilter(filter, -1, -1);

			msg += "\nExecuting: "+jobsExecuting.size();
			msg += "\nQueued: "+jobsQueued.size();
			
			break;
		default:
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Unknown action");
		}
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		outputHandler.writeTextPlainResponse(response, msg);
	}
	
	private Parameters getParameters(UwsActionRequest actionRequest, Action action) throws UwsException {
		Parameters parameters = new Parameters();
		switch(action){
		case GetStats:
			//nothing to get: user is retrieved from environment
			break;
		default:
			//not valid case
		}
		return parameters;
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}


}
