package esavo.uws.actions.handlers.scheduler.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsScheduler;
import esavo.uws.scheduler.UwsScheduler.SchedulerMode;

/**
 * Example: curl http://<server>/<tap-context>/tap/admin?ACTION=scheduler_set_mode&scheduler_mode=ALL
 * @author rgutierrez
 *
 */
public class UwsSchedulerModeHandler implements UwsFunctionsHandler {

	public static final String ACTION = "SchedulerMode";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException {
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		UwsScheduler uwsScheduler = uwsManager.getFactory().getScheduler();

		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, uwsOutput, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}

		String paramMode = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SET_SCHEDULER_MODE);
		SchedulerMode mode = uwsScheduler.getSchedulerMode();
		boolean newMode = false;

		if(paramMode!=null){
			try{
				mode = SchedulerMode.valueOf(paramMode);
				newMode=true;
			}catch(Exception e){
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid scheduler mode provided.");
			}
		}

		
		try {
			if(newMode){
				uwsScheduler.setSchedulerMode(mode);
			}
			uwsOutput.writeTextPlainResponse(response, "Scheduler mode: "+uwsScheduler.getSchedulerMode());
		} catch (UwsException e) {
			int code = e.getCode();
			if (code < 0){
				code = UwsOutputResponseHandler.INTERNAL_SERVER_ERROR;
			}
			throw new UwsException(code, "Cannot change scheduler mode: " + e.getMessage(), e);
		}
		return;
	}
	
	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
