package esavo.uws.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import esavo.uws.UwsException;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.events.UwsEventQueryHandler;
import esavo.uws.actions.handlers.jobs.DeleteMultipleJobsHandler;
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
import esavo.uws.actions.handlers.notifications.UwsNotifications;
import esavo.uws.actions.handlers.share.UwsShareCreate;
import esavo.uws.actions.handlers.share.UwsShareDelete;
import esavo.uws.actions.handlers.share.UwsShareList;
import esavo.uws.actions.handlers.stats.UwsStatsHandler;
import esavo.uws.actions.handlers.tasks.UwsTasksHandler;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpRequest;

public class ActionsProcedureTest {
	
	private static final String TEST_APP_ID = "__TEST__" + ActionsProcedureTest.class.getName(); 
	
	@Test
	public void testHandlers() throws UwsException{
		List<UwsActionHandler> handlers = new ArrayList<UwsActionHandler>();
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
		
		handlers.add(new UwsShareCreate()); //pending to test
		handlers.add(new UwsShareDelete()); //pending to test
		handlers.add(new UwsShareList()); //pending to test
		handlers.add(new UwsEventQueryHandler()); //pending to test
		handlers.add(new UwsNotifications()); //pending to test
		handlers.add(new UwsStatsHandler()); //pending to test
		handlers.add(new DeleteMultipleJobsHandler());
		handlers.add(new UwsTasksHandler()); //pending to test

		boolean[] result = new boolean[handlers.size()];
		boolean[] expected = new boolean[handlers.size()];
		UwsJobOwner user = new UwsJobOwner(UwsUtils.ANONYMOUS_USER, UwsJobOwner.ROLE_USER);

		DummyHttpRequest request;
		List<String> parametersToIgnore = null;
		
		String servletName = "tap";
		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
		String list = "async";
		String jobid = "12345";
		String url;
		UwsActionRequest actionRequest;
		List<String> noJobActions = UwsTestUtils.getNoJobActions();
		
		//Tests
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(TEST_APP_ID);
		
		//Destruction
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobDestructionHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[0] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobDestructionHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsHandlersUtils.PARAMETER_DESTRUCTION, UwsUtils.formatDate(new Date()));
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[0] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//Error
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobErrorHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[1] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobErrorHandler.ACTION_NAME + "/" + UwsJobErrorHandler.SUBACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[1] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		//Execduration
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobExecDurationHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[2] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobExecDurationHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsHandlersUtils.PARAMETER_EXEC_DURATION, "100");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[2] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//jobmeta
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[3] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//jobowner
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobOwnerHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[4] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		//jobparameters
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobParametersHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[5] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobParametersHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1","value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[5] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//phase
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobPhaseHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[6] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobPhaseHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsHandlersUtils.PARAMETER_PHASE, "RUN");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[6] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//quote
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobQuoteHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[7] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//results
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobResultsHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[8] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//list jobs
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[9] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//'jobs' handler list by order
		resetArray(result);
		resetArray(expected);
		url = urlBase + "jobs/" + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[10] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + "jobs/" + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[10] = false;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + "jobs";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		try{
			checkHandlers(handlers, user, actionRequest, result);
			Assert.fail("Exception expected: missing list name");
		}catch(Exception e){
			//expected
		}
		
		//delete
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("DELETE");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[11] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsHandlersUtils.PARAMETER_ACTION, UwsJobDeleteHandler.PARAMETER_ACTION_VALUE);
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[11] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//job create
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[12] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[12] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//share create
		//share delete
		//share list
		//events
		//notifications
		//stats
		
		//delete multiple jobs
		resetArray(result);
		resetArray(expected);
		url = urlBase + "deleteJobs";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[19] = false;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + "deleteJobs";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("JOB_IDS", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[19] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + "deleteJobs";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[19] = false;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + "deleteJobs";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("JOB_IDS", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		expected[19] = false;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		//task status
		
	}
	
	public void resetArray(boolean[] array){
		for(int i = 0; i < array.length; i++){
			array[i] = false;
		}
	}
	
	public void checkResult(boolean[] expected, boolean[] result, List<UwsActionHandler> handlers){
		for(int i = 0; i < expected.length; i++){
			if(expected[i] != result[i]){
				Assert.fail("Problem with handler " + handlers.get(i).getActionName() + ". Handling expected: " + expected[i] + ", found: " + result[i]);
			}
		}
	}
	
	private void checkHandlers (List<UwsActionHandler> handlers, UwsJobOwner currentUser, UwsActionRequest actionRequest, boolean[] result) throws UwsException{
		UwsActionHandler h;
		for(int i = 0; i < handlers.size(); i++){
			h = handlers.get(i);
			result[i] = h.canHandle(TEST_APP_ID, currentUser, actionRequest);
		}
	}
	
//	private int checkHandler(List<UwsActionHandler> handlers, UwsJobOwner currentUser, UwsActionRequest actionRequest) throws UwsException{
//		int i = 0;
//		for(UwsActionHandler h: handlers){
//			if(h.canHandle(TEST_APP_ID, currentUser, actionRequest)){
//				return i;
//			}
//			i++;
//		}
//		return -1;
//	}

}
