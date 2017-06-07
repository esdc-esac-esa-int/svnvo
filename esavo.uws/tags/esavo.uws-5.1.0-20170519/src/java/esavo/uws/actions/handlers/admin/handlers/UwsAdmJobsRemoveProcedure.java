package esavo.uws.actions.handlers.admin.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

public class UwsAdmJobsRemoveProcedure implements UwsFunctionsHandler {

	public static final String ACTION = "jobs_remove_procedure";

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException  {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, outputHandler, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}
		
		String report = uwsManager.checkJobsRemovalProcedure();
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		uwsOutput.writeJobsRemovalReport(response, report);
		return;
	}

	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
}
