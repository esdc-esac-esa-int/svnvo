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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.UwsManager;
import esavo.uws.actions.handlers.jobs.UwsJobDestructionHandler;
import esavo.uws.actions.handlers.tasks.UwsTasksHandler;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;
import esavo.uws.utils.status.UwsUserInfo;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpRequest;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsExecutor;
import esavo.uws.utils.test.uws.DummyUwsFactory;
import esavo.uws.utils.test.uws.DummyUwsScheduler;
import esavo.uws.utils.test.uws.DummyUwsStorageManager;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class TaskStatusTest {
	
	public static final String TEST_APP_ID = "__TEST__" + TaskStatusTest.class.getName();
	
	private static File fStorageDir;
	private static String appid = TEST_APP_ID;
	private static UwsManager manager;
	private static DummyUwsFactory factory;
	private static UwsConfiguration configuration;
	private static DummyUwsStorageManager storage;
	//private static UwsJobsListManager listManager;
	private static DummyUwsExecutor executor;
	private static DummyUwsScheduler scheduler;
	
	@BeforeClass
	public static void beforeClass() throws UwsException{
		fStorageDir = new File(".", TEST_APP_ID);
		fStorageDir.mkdirs();
		configuration = UwsConfigurationManager.getConfiguration(appid);
		factory = new DummyUwsFactory(appid, fStorageDir, configuration, StorageType.fake); 
		manager = UwsManager.getInstance();  //the manager is created by the factory when a factory is created.
		storage = (DummyUwsStorageManager)factory.getStorageManager();
		executor = (DummyUwsExecutor)factory.getExecutor();
		scheduler = (DummyUwsScheduler)factory.getScheduler();
		//listManager = UwsJobsListManager.getInstance(appid);
	}
	
	@AfterClass
	public static void afterClass(){
		UwsTestUtils.removeDirectory(fStorageDir);
	}

	@Test
	public void test1() throws Exception{
		UwsStatusManager.enableCheckThread(false);
		
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();

		String ownerid = "test";
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();
		DummyHttpRequest request;
		List<String> noJobActions = UwsTestUtils.getNoJobActions();
		List<String> parametersToIgnore = null;

		//DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
		//DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
		//String appid = "appid_test";
		//DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
		//TaskStatus ts = new TaskStatus<ResultSet>(sc);
		
		//boolean useDatabase = true;
		//DummyTapServiceConnection sc = new DummyTapServiceConnection(TEST_APP_ID, useDatabase);
		//TaskStatus ts = new TaskStatus(service);
		

		
		String servletName = "tap";
		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
		String taskId;
		String taskType;
		String url;
		UwsActionRequest actionRequest;
		
		//Tests
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(TEST_APP_ID);
		
		UwsTasksHandler taskHandler = new UwsTasksHandler();
		
		//missing parameters
		
		url = urlBase + "/tasks";
		request = new DummyHttpRequest(url, servletName);
		request.setMethod("GET");
		actionRequest = new UwsActionRequest(TEST_APP_ID, configuration, request, noJobActions, parametersToIgnore);
		
		try {
			taskHandler.handle(manager, currentUser, actionRequest, response);
			Assert.fail("task status without parameters: exception expected.");
		} catch (Exception e){
			//expected
		}
		
		response.clearOutput();
		
		//missing task type
		
		taskId = "12345";
		taskType = "";
		url = urlBase + "/tasts?TASK_ID=" + taskId;

		try {
			taskHandler.handle(manager, currentUser, actionRequest, response);
			Assert.fail("task status without TASK_TYPE parameter: exception expected.");
		} catch (Exception e){
			//expected
		}

		response.clearOutput();
		
		//missing task id
		
		taskId = "";
		taskType = "UPLOAD";

		url = urlBase + "/tasts?TASK_TYPE=" + taskType;

		try {
			taskHandler.handle(manager, currentUser, actionRequest, response);
			Assert.fail("task status without TASK_ID parameter: exception expected.");
		} catch (Exception e){
			//expected
		}

		response.clearOutput();
		
		//task id not a number
		
		taskId = "A";
		taskType = "UPLOAD";
		
		url = urlBase + "/tasts?TASK_TYPE=" + taskType;

		try {
			taskHandler.handle(manager, currentUser, actionRequest, response);
			Assert.fail("Wrong TASK_ID parameter format (not a number): exception expected.");
		} catch (Exception e){
			//expected
		}

		response.clearOutput();
		
		//expected failed because the task was not created

		taskId = "12345";
		taskType = "UPLOAD";

		taskHandler.handle(manager, currentUser, actionRequest, response);
		


//
//
//		Map<String,String> reqParams = new HashMap<String, String>();
//		//reqParams
//		DummyHttpRequest request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
//		ts.executeRequest(request, response);
//		
//		if(!TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Expected error: no parameter " + TaskStatus.PARAM_TASKID);
//		}
//		
//
//		//param task id provided (missing task type)
//		reqParams.put(TaskStatus.PARAM_TASKID, "NotANumber");
//		request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		response.clearOutput();
//		ts.executeRequest(request, response);
//		
//		if(!TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Expected error: no parameter " + TaskStatus.PARAM_TASKTYPE);
//		}
//		
//		//Invalid task type
//		reqParams.put(TaskStatus.PARAM_TASKTYPE, "xxxInvalidType");
//		request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		response.clearOutput();
//		ts.executeRequest(request, response);
//
//		if(!TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Expected error: invalid task type");
//		}
//		
//		//Valid task type, taskid is not a number
//		reqParams.put(TaskStatus.PARAM_TASKTYPE, "UPLOAD");
//		request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		response.clearOutput();
//		ts.executeRequest(request, response);
//
//		if(!TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Expected error: invalid task id (expected a number)");
//		}
//		
//		//Valid task type, task id is a number. StatusManager does not contain info about task id '1' => error
//		long id = 1;
//		reqParams.put(TaskStatus.PARAM_TASKID, ""+id);
//		request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		response.clearOutput();
//		ts.executeRequest(request, response);
//		
//		if(!TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Expected error: invalid task id (expected a number)");
//		}
//
//		//Update StatusManager with a valid task id
//		UwsStatusManager sm = UwsStatusManager.getInstance();
//		
//		//DefaultJobOwner owner = TapUtils.createJobOwner("test");
//		UwsJobOwner owner = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
//		UwsUserInfo userInfo = new UwsUserInfo(owner);
//		id = sm.createUserIdentifier(userInfo);
//
//		reqParams.put(TaskStatus.PARAM_TASKID, ""+id);
//		request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
//		response.clearOutput();
//		ts.executeRequest(request, response);
//		
//		if(TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Unexpected error");
//		}
//		
//		if(!TestUtils.findValueInJSon(response.getOutputAsString(), "")){
//			Assert.fail("Expected error: invalid task id (expected a number)");
//		}
//		
//		//Put a status for the task
//		String value = "50";
//		//StatusData sd = StatusDataFactory.createStatusData(TaskType.UPLOAD, value);
//		UwsStatusData sd = new UwsStatusData("UPLOAD", value);
//		sm.updateStatus(id, sd);
//
//		response.clearOutput();
//		ts.executeRequest(request, response);
//
//		System.out.println(response.getOutputAsString());
//		if(TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Unexpected error");
//		}
//		
//		if(!TestUtils.findValueInJSon(response.getOutputAsString(), value)){
//			Assert.fail("Expected error: invalid task id (expected a number)");
//		}

	}

}
