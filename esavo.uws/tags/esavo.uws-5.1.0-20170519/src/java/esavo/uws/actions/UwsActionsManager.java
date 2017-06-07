package esavo.uws.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.UwsServlet;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.admin.AdminHandler;
import esavo.uws.actions.handlers.events.UwsEventQueryHandler;
import esavo.uws.actions.handlers.jobs.UwsJobsMultipleDeleteHandler;
import esavo.uws.actions.handlers.jobs.UwsJobCreateHandler;
import esavo.uws.actions.handlers.jobs.UwsJobDeleteHandler;
import esavo.uws.actions.handlers.jobs.UwsJobDestructionHandler;
import esavo.uws.actions.handlers.jobs.UwsJobErrorHandler;
import esavo.uws.actions.handlers.jobs.UwsJobExecDurationHandler;
import esavo.uws.actions.handlers.jobs.UwsJobMetaHandler;
import esavo.uws.actions.handlers.jobs.UwsJobOwnerHandler;
import esavo.uws.actions.handlers.jobs.UwsJobParametersHandler;
import esavo.uws.actions.handlers.jobs.UwsJobPhaseHandler;
import esavo.uws.actions.handlers.jobs.UwsJobQuoteHandler;
import esavo.uws.actions.handlers.jobs.UwsJobResultsHandler;
import esavo.uws.actions.handlers.jobs.UwsJobsHandler;
import esavo.uws.actions.handlers.jobs.UwsListJobsHandler;
import esavo.uws.actions.handlers.notifications.UwsNotificationsHandler;
import esavo.uws.actions.handlers.scheduler.UwsSchedulerHandler;
import esavo.uws.actions.handlers.share.UwsShareCreate;
import esavo.uws.actions.handlers.share.UwsShareDelete;
import esavo.uws.actions.handlers.share.UwsShareList;
import esavo.uws.actions.handlers.stats.UwsStatsHandler;
import esavo.uws.actions.handlers.tasks.UwsTasksHandler;
import esavo.uws.actions.handlers.users.UwsGetUsersHandler;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * Performs an action based on a {@link HttpServletRequest} parameters.
 * <p>To use this class from a servlet see {@link UwsServlet}.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsActionsManager {
	
	private static final Logger LOG = Logger.getLogger(UwsActionsManager.class.getName());
	
	private static List<UwsActionHandler> handlers = new ArrayList<UwsActionHandler>();
	private static List<String> noJobActions = new ArrayList<String>();

	static{
		handlers.add(new UwsJobDestructionHandler());
		handlers.add(new UwsJobErrorHandler());
		handlers.add(new UwsJobExecDurationHandler());
		handlers.add(new UwsJobMetaHandler());
		handlers.add(new UwsJobOwnerHandler());
		handlers.add(new UwsJobParametersHandler());
		handlers.add(new UwsJobPhaseHandler());
		handlers.add(new UwsJobQuoteHandler());
		handlers.add(new UwsJobResultsHandler());
		handlers.add(new UwsListJobsHandler());
		handlers.add(new UwsJobsHandler());
		handlers.add(new UwsJobDeleteHandler());
		handlers.add(new UwsJobCreateHandler());
		handlers.add(new UwsShareCreate());
		handlers.add(new UwsShareDelete());
		handlers.add(new UwsShareList());
		handlers.add(new UwsEventQueryHandler());
		handlers.add(new UwsNotificationsHandler());
		handlers.add(new UwsStatsHandler());
		handlers.add(new UwsJobsMultipleDeleteHandler());
		handlers.add(new UwsTasksHandler());
		//handlers.add(new UwsJobsSearchHandler());
		handlers.add(new AdminHandler());
		handlers.add(new UwsSchedulerHandler());
		handlers.add(new UwsGetUsersHandler());
		
		HashMap<String,String> map = new HashMap<String,String>();
		for(UwsActionHandler handler: handlers){
			if(!handler.isJobAction()){
				map.put(handler.getActionName(),null);
			}
		}
		noJobActions = new ArrayList<String>(map.keySet());
	}
	
	
	private static UwsActionsManager manager;
	
	private UwsActionsManager(){
		
	}
	
	public static synchronized UwsActionsManager getInstance(){
		if(manager == null){
			manager = new UwsActionsManager();
		}
		return manager;
	}
	
	/**
	 * Performs an action based on the request.
	 * @param appid application identifier
	 * @param owner user accessing to the service
	 * @param request
	 * @param response
	 * @param parametersToIgnore
	 */
	public void executeRequest(UwsManager manager, UwsJobOwner owner, HttpServletRequest request, HttpServletResponse response, List<String> parametersToIgnore){
		UwsOutputResponseHandler outputHandler = manager.getFactory().getOutputHandler();
		UwsActionRequest actionRequest;
		String appid = manager.getAppId();
		UwsConfiguration configuration = manager.getFactory().getConfiguration();
		try {
			actionRequest = new UwsActionRequest(appid, configuration, request, noJobActions, parametersToIgnore);
		} catch (IllegalArgumentException e) {
			try {
				outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Getting request information", e, UwsExceptionOutputFormat.HTML);
			} catch (UwsException e1) {
				LOG.severe("Cannot write output for request '"+request.getRequestURI()+"' : " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e1));
			}
			return;
		}
		
		//System.out.println("UwsActionsManager - Request for: " + owner + ": " + request.getRequestURL());
		
		//Register Http Parameters handlers (by default, actionRequest manages post multipart and post url-encoded
		List<UwsJobHttpParametersHandler> httpJobParameterHandlers = manager.getFactory().getCreator().getJobHttpParametersHandlers();
		actionRequest.registerJobHttpParametersHandler(httpJobParameterHandlers);

		//NOTE: user permissions are checked by each handler if required.
		
		boolean canHandle;
		for(UwsActionHandler handler: handlers){
			try {
				canHandle = handler.canHandle(appid, owner, actionRequest);
			} catch (UwsException e) {
				//the handler is found, but some parameters are missing.
				try {
					outputHandler.writeServerErrorResponse(response, "Executing handlers", e);
				} catch (UwsException e1) {
					LOG.severe("Cannot write output for request '"+request.getRequestURI()+"' : " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e1));
				}
				return;
			}
			if(canHandle){
				try {
					handler.handle(manager, owner, actionRequest, response);
				} catch (UwsException e) {
					//problems handling the request
					LOG.severe("Error executing: " + handler + "for request '"+request.getRequestURI()+"': " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
					try {
						outputHandler.writeServerErrorResponse(response, "Handling request", e);
					} catch (UwsException e1) {
						LOG.severe("Cannot write output for request '"+request.getRequestURI()+"' : " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e1));
					}
				}
				return;
			}
		}

		//action not found:
		try {
			UwsConfiguration uwsConfiguration = manager.getFactory().getConfiguration();
			outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "UWS " + uwsConfiguration.getUwsVersion(), "No action handler found", "Cannot find a suitable handler for " + request.getPathInfo());
		} catch (UwsException e) {
			LOG.severe("Cannot write output for request '"+request.getRequestURI()+"': " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
		}
	}
	

}
