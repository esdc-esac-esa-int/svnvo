/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import esavo.uws.UwsException;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.jobs.UwsJobCreate;
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
import esavo.uws.actions.handlers.jobs.UwsListJobsHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.test.http.DummyHttpRequest;
import esavo.uws.utils.UwsUtils;

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
			handlers.add(new UwsJobDeleteHandler());
			handlers.add(new UwsJobCreate());

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
		
		//Tests
		
		//Destruction
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobDestructionHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[0] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobDestructionHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsJobDestructionHandler.PARAMETER_DESTRUCTION, UwsUtils.formatDate(new Date()));
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[0] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//Error
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobErrorHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[1] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobErrorHandler.ACTION_NAME + "/" + UwsJobErrorHandler.SUBACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[1] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		//Execduration
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobExecDurationHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[2] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobExecDurationHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsJobExecDurationHandler.PARAMETER_EXEC_DURATION, "100");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[2] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//jobmeta
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[3] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//jobowner
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobOwnerHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[4] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		//jobparameters
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobParametersHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[5] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobParametersHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1","value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[5] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//phase
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobPhaseHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[6] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobPhaseHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsJobPhaseHandler.PARAMETER_PHASE, "RUN");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[6] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//quote
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobQuoteHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[7] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//results
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid + "/" + UwsJobResultsHandler.ACTION_NAME;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[8] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//listjobs
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[9] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//delete
		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("DELETE");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[10] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		resetArray(result);
		resetArray(expected);
		url = urlBase + list + "/" + jobid;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter(UwsJobDeleteHandler.PARAMETER_ACTION, UwsJobDeleteHandler.PARAMETER_ACTION_VALUE);
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[10] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

		//job create
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[11] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);
		
		resetArray(result);
		resetArray(expected);
		url = urlBase + list;
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("POST");
		request.setParameter("param1", "value1");
		actionRequest = new UwsActionRequest(TEST_APP_ID, request, parametersToIgnore);
		expected[11] = true;
		checkHandlers(handlers, user, actionRequest, result);
		checkResult(expected, result, handlers);

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
