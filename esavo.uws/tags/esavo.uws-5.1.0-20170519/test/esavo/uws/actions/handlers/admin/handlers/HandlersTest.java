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
package esavo.uws.actions.handlers.admin.handlers;

import java.io.File;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpRequest;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsExecutor;
import esavo.uws.utils.test.uws.DummyUwsFactory;
import esavo.uws.utils.test.uws.DummyUwsScheduler;
import esavo.uws.utils.test.uws.DummyUwsStorageManager;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class HandlersTest {
	
	private static final String TEST_ID = "__TEST__" + HandlersTest.class.getName();
	
	private static File fStorageDir;
	private static String appid = TEST_ID;
	private static UwsManager manager;
	private static DummyUwsFactory factory;
	private static UwsConfiguration configuration;
	private static DummyUwsStorageManager storage;
	//private static UwsJobsListManager listManager;
	private static DummyUwsExecutor executor;
	private static DummyUwsScheduler scheduler;
	
	@BeforeClass
	public static void beforeClass() throws UwsException{
		fStorageDir = new File(".", TEST_ID);
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
	public void dummyTest(){
		
	}

//	@Test
//	public void testJobsListHandler() throws Exception{
//		TestUtils.initDirectory(fStorageDir);
//		factory.clear();
//
//		UwsJobsHandler h = new UwsJobsHandler();
//		//JobsListHandler h = new JobsListHandler();
//		Assert.assertEquals("jobs_list", h.getActionIdentifier());
//		Assert.assertFalse(h.canHandle(JobsListHandler.ACTION+"x"));
//		Assert.assertTrue(h.canHandle(JobsListHandler.ACTION));
//		
//		DummyHttpResponse response = new DummyHttpResponse();
//
//		String servletName = "tap";
//		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
//		String url = urlBase + "admin?"+UwsHandlersUtils.PARAMETER_ACTION+"=jobs_list";
//
//		DummyHttpRequest request = new DummyHttpRequest(url, servletName);
//		UwsActionRequest actionRequest = new UwsActionRequest(TEST_ID, configuration, request, null);
//		
//		try{
//			h.handle(actionRequest, response, manager, null);
//			org.junit.Assert.fail("Exception expected");
//		}catch(UwsException uws){
//			//expected
//		}
//		
//	}

	@Test
	public void testUsersListHandler() throws Exception{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();

		
		UwsAdmUsersListHandler h = new UwsAdmUsersListHandler();
		Assert.assertEquals("users_list", h.getActionIdentifier());
		Assert.assertFalse(h.canHandle(UwsAdmUsersListHandler.ACTION+"x"));
		Assert.assertTrue(h.canHandle(UwsAdmUsersListHandler.ACTION));
		
		DummyHttpResponse response = new DummyHttpResponse();

		String servletName = "tap";
		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
		String url = urlBase + "admin?"+UwsHandlersUtils.PARAMETER_ACTION+"=users_list";

		DummyHttpRequest request = new DummyHttpRequest(url, servletName);
		UwsActionRequest actionRequest = new UwsActionRequest(TEST_ID, configuration, request, UwsTestUtils.getNoJobActions(), null);
		
		h.handle(actionRequest, response, manager, null);

//		
//		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
//		
//		Map<String,String> params = new HashMap<String, String>();
//		
//		UwsSecurity security = service.getFactory().getSecurityManager();
//		
//		UwsStorage uwsStorage = service.getFactory().getStorageManager();
//		DummyTapServiceFactory factory = (DummyTapServiceFactory)service.getFactory();
//		DummyDatabaseConnection dbConn = factory.getDatabaseConnection();
//		
//		DummyTapDatabaseConnection database = factory.getDummyDatabaseConnection();
//		
//		String outputResponse;
//		
//		String queryExecuted = "SELECT owner_id FROM uws2_schema.owners ORDER BY owner_id";
//		DummyData data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		UwsJobOwner owner = new UwsJobOwner("test", UwsJobOwner.ROLE_ADMIN);
//		
//		h.handle(params, response, uwsStorage, owner, service);
//		String result = response.getOutputAsString();
//		Assert.assertEquals("[\n]\n", result);
//		TestUtils.checkDbQuery(dbConn, queryExecuted);
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		//filters
//		String userid = "user1";
//		long offset = 100;
//		long limit = 10;
//		params.put(Manager.PARAM_USER_ID, userid);
//		params.put(Manager.PARAM_QUERY_RESULTS_OFFSET, ""+offset);
//		params.put(Manager.PARAM_QUERY_RESULTS_LIMIT, ""+limit);
//		
//		queryExecuted = "SELECT owner_id FROM uws2_schema.owners WHERE owner_id ILIKE '%user1%' ORDER BY owner_id OFFSET 100 LIMIT 10";
//		data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		h.handle(params, response, uwsStorage, owner, service);
//		result = response.getOutputAsString();
//		Assert.assertEquals("[\n]\n", result);
//		TestUtils.checkDbQuery(dbConn, queryExecuted);
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		params.put(Manager.PARAM_USER_ID, "");
//		params.put(Manager.PARAM_QUERY_RESULTS_OFFSET, ""+offset);
//		params.put(Manager.PARAM_QUERY_RESULTS_LIMIT, ""+limit);
//		
//		queryExecuted = "SELECT owner_id FROM uws2_schema.owners ORDER BY owner_id OFFSET 100 LIMIT 10";
//		data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		h.handle(params, response, uwsStorage, owner, service);
//		result = response.getOutputAsString();
//		Assert.assertEquals("[\n]\n", result);
//		TestUtils.checkDbQuery(dbConn, queryExecuted);		
//
//		//Generate exception
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		dbConn.enableGenerateExceptionRequested(queryExecuted);
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: exception requested.");
//		}
	}

	@Test
	public void testUserDetailsHandler() throws Exception{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();

		
		UwsAdmUserDetailsHandler h = new UwsAdmUserDetailsHandler();
		Assert.assertEquals("user_details", h.getActionIdentifier());
		Assert.assertFalse(h.canHandle(UwsAdmUserDetailsHandler.ACTION+"x"));
		Assert.assertTrue(h.canHandle(UwsAdmUserDetailsHandler.ACTION));
		
//		Map<String,String> parameters = new HashMap<String, String>();
//		DummyHttpResponse response = new DummyHttpResponse();
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);

		DummyHttpResponse response = new DummyHttpResponse();

		String servletName = "tap";
		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
		String url = urlBase + "admin?"+UwsHandlersUtils.PARAMETER_ACTION+"=user_details";

		DummyHttpRequest request = new DummyHttpRequest(url, servletName);
		UwsActionRequest actionRequest = new UwsActionRequest(TEST_ID, configuration, request, UwsTestUtils.getNoJobActions(), null);
		
		h.handle(actionRequest, response, manager, null);

//		
//		
//		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
//		
//		Map<String,String> params = new HashMap<String, String>();
//		
//		UwsSecurity security = service.getFactory().getSecurityManager();
//		
//		UwsStorage uwsStorage = service.getFactory().getStorageManager();
//		DummyTapServiceFactory factory = (DummyTapServiceFactory)service.getFactory();
//		DummyDatabaseConnection dbConn = factory.getDatabaseConnection();
//		
//		DummyTapDatabaseConnection database = factory.getDummyDatabaseConnection();
//		
//		String outputResponse;
//
//		UwsJobOwner owner = new UwsJobOwner("test", UwsJobOwner.ROLE_ADMIN);
//		
//		//No userid: error
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no userid parameter.");
//		}
//
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		String userid = "user1";
//		String role = "1";
//		params.put(Manager.PARAM_USER_ID, userid);
//
//		String queryExecuted = "SELECT auth_name, pseudo, roles FROM uws2_schema.owners WHERE owner_id = 'user1'";
//		DummyData data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no user found.");
//		}
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//
//		data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), new String[][]{{userid,userid,role}});
//		dbConn.setDataForQuery(queryExecuted, data);
//		
//		TestUtils.setUserData(dbConn, userid, role);
//		
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		Assert.assertEquals("[\n{\"id\": \"user1\", \"roles\": \"1\", \"quota_db\": \"1000\", \"curent_size_db\": \"105\", \"quota_files\": \"2000\", \"current_size_files\": \"0\"}]\n", outputResponse);
//		TestUtils.checkDbQuery(dbConn, queryExecuted);
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//
//		//DB Exception
//		dbConn.enableGenerateExceptionRequested(queryExecuted);
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: exception requested.");
//		}
	}
	
	

	@Test
	public void testUserUpdateHandler() throws Exception{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();

		
		UwsAdmUserUpdateHandler h = new UwsAdmUserUpdateHandler();
		Assert.assertEquals("user_update", h.getActionIdentifier());
		Assert.assertFalse(h.canHandle(UwsAdmUserUpdateHandler.ACTION+"x"));
		Assert.assertTrue(h.canHandle(UwsAdmUserUpdateHandler.ACTION));
		
//		Map<String,String> parameters = new HashMap<String, String>();
//		DummyHttpResponse response = new DummyHttpResponse();
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);

		DummyHttpResponse response = new DummyHttpResponse();

		String servletName = "tap";
		String urlBase = "http://localhost:8080/tap-local/" + servletName + "/";
		String url = urlBase + "admin?"+UwsHandlersUtils.PARAMETER_ACTION+"=user_update";

		DummyHttpRequest request = new DummyHttpRequest(url, servletName);
		UwsActionRequest actionRequest = new UwsActionRequest(TEST_ID, configuration, request, UwsTestUtils.getNoJobActions(), null);
	
		try{
			h.handle(actionRequest, response, manager, null);
			Assert.fail("Exception expected: null user");
		}catch(UwsException e){
			//expected
		}

//		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
//		
//		Map<String,String> params = new HashMap<String, String>();
//		
//		UwsSecurity security = service.getFactory().getSecurityManager();
//		
//		UwsStorage uwsStorage = service.getFactory().getStorageManager();
//		DummyTapServiceFactory factory = (DummyTapServiceFactory)service.getFactory();
//		DummyDatabaseConnection dbConn = factory.getDatabaseConnection();
//		
//		DummyTapDatabaseConnection database = factory.getDummyDatabaseConnection();
//		
//		String outputResponse;
//		
//		UwsJobOwner owner = new UwsJobOwner("test", UwsJobOwner.ROLE_ADMIN);
//
//		//No userid: error
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no userid parameter.");
//		}
//
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		String userid = "user1";
//		params.put(Manager.PARAM_USER_ID, userid);
//		
//		String queryExecuted = "SELECT auth_name, pseudo, roles FROM uws2_schema.owners WHERE owner_id = 'user1'";
//		DummyData data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no user found.");
//		}
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//
//		String role = "1";
//		data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), new String[][]{{userid,userid,role}});
//		dbConn.setDataForQuery(queryExecuted, data);
//		
//		TestUtils.setUserData(dbConn, userid, role);
//
//		//No DB quota
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no quota db.");
//		}
//		
//		database.clearFlags();
//		//dbConn.clear();
//		response.clearOutput();
//		
//		long quotadb = 100;
//		params.put(Manager.PARAM_QUOTA_DB, ""+quotadb);
//
//		//No files quota
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no quota files.");
//		}
//		
//		database.clearFlags();
//		//dbConn.clear();
//		response.clearOutput();
//		
//		long quotaFiles = 500;
//		params.put(Manager.PARAM_QUOTA_FILES, ""+quotaFiles);
//		
//		//No roles
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no quota files.");
//		}
//		
//		database.clearFlags();
//		//dbConn.clear();
//		response.clearOutput();
//		
//		int roles = 1;
//		params.put(Manager.PARAM_ROLES, ""+roles);
//		
//		dbConn.setDataForQuery(
//				"UPDATE uws2_schema.owner_parameters SET data_type = 'Long', string_representation = '105' WHERE parameter_id = 'db_current_size' AND owner_id = 'user1'",
//				DatabaseUtils.createInsertOrUpdateData(1));
//		
//		dbConn.setDataForQuery(
//				"UPDATE uws2_schema.owner_parameters SET data_type = 'Long', string_representation = '500' WHERE parameter_id = 'files_quota' AND owner_id = 'user1'", 
//				DatabaseUtils.createInsertOrUpdateData(1));
//
//		dbConn.setDataForQuery(
//				"UPDATE uws2_schema.owner_parameters SET data_type = 'Long', string_representation = '0' WHERE parameter_id = 'files_current_size' AND owner_id = 'user1'", 
//				DatabaseUtils.createInsertOrUpdateData(1));
//
//		dbConn.setDataForQuery(
//				"UPDATE uws2_schema.owner_parameters SET data_type = 'Long', string_representation = '100' WHERE parameter_id = 'db_quota' AND owner_id = 'user1'", 
//				DatabaseUtils.createInsertOrUpdateData(1));
//
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		//{ "id": "User user1 updated." }
//		String expected = Templates.JSON_SIMPLE_MSG.
//				replaceAll("\\{0\\}", "\"id\": \"User "+userid+" updated.\"").
//				replaceAll("'","") + "\n";
//		Assert.assertEquals(expected, outputResponse);
////		TestUtils.checkDbAction(dbConnection, 
////				DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_DETAILS, 
////				userid+","+roles+","+quotadb+","+quotaFiles);
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//
//		//DB Exception
//		dbConn.enableGenerateExceptionRequested(queryExecuted);
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: exception requested.");
//		}
	}

//	@Test
//	public void testJobDetailsHandler() throws IOException{
//		TestUtils.initDirectory(fStorageDir);
//		factory.clear();
//
//		JobDetailsHandler h = new JobDetailsHandler();
//		Assert.assertEquals("job_details", h.getActionIdentifier());
//		Assert.assertFalse(h.canHandle(JobDetailsHandler.ACTION+"x"));
//		Assert.assertTrue(h.canHandle(JobDetailsHandler.ACTION));
//		
//		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
//		
//		Map<String,String> params = new HashMap<String, String>();
//		
//		UwsSecurity security = service.getFactory().getSecurityManager();
//		
//		UwsStorage uwsStorage = service.getFactory().getStorageManager();
//		DummyTapServiceFactory factory = (DummyTapServiceFactory)service.getFactory();
//		DummyDatabaseConnection dbConn = factory.getDatabaseConnection();
//		
//		DummyTapDatabaseConnection database = factory.getDummyDatabaseConnection();
//		
//		String outputResponse;
//		
//		UwsJobOwner owner = new UwsJobOwner("test", UwsJobOwner.ROLE_ADMIN);
//
//		//No userid: error
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no jobid parameter.");
//		}
//
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//		
//		String jobid = "job";
//		params.put(Manager.PARAM_JOB_ID, jobid);
//		
//		String queryExecuted = "SELECT owner_id, session_id, phase_id, quote, start_time, end_time, destruction_time, execution_duration, relative_path, list_id, priority, creation_time FROM uws2_schema.jobs_meta WHERE job_id = 'job'";
//		DummyData data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), (String[][])null);
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: no jobid found.");
//		}
//		
//		database.clearFlags();
//		//dbConn.clear();
//		response.clearOutput();
//		
//		String ownerid = "user1";
//		String sessionid = "session";
//		String phaseid = "PENDING";
//		String quote = "1";
//		String startTime = "1432785600000";
//		String endTime = "1432785700000";
//		String destructionTime = "1432785800000";
//		String executionDuration = "10";
//		String relativePath = "path";
//		String listid = "ASYNC";
//		String priority = "1";
//		String creationTime = "1432785800000";
//		
//		data = DatabaseUtils.createDummyData(DatabaseUtils.getColumnNamesFromSelectQuery(queryExecuted), new String[][]{
//			{ownerid, sessionid, phaseid, quote, startTime, endTime, destructionTime, executionDuration, relativePath, listid, priority, creationTime}
//		});
//		dbConn.setDataForQuery(queryExecuted, data);
//
//		TestUtils.setUserDataComplete(dbConn, "user1", "1");
//
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Unexpected error: job must be created.");
//		}
//		
//		String expected = "{\"meta\": \n" + 
//				"{\"job_id\": \"job\", \"owner_id\": \"user1\", \"phase_id\": \"PENDING\", \"start_time\": \"1432785600000\", \"end_time\": \"1432785700000\" , \"query\": \"\", \"relative_path\": \"path\"},\n"+
//				"\"parameters\": [],\n"+
//				"\"error_summary\": {\"message\": \"\", \"type\": \"\", \"details\": \"\"},\n"+
//				"\"results\": []}\n";
//
//		Assert.assertEquals(expected, outputResponse);
//		
//		database.clearFlags();
//		dbConn.clear();
//		response.clearOutput();
//
//		//DB Exception
//		dbConn.enableGenerateExceptionRequested(queryExecuted);
//		h.handle(params, response, uwsStorage, owner, service);
//		outputResponse = response.getOutputAsString();
//		if(!TestUtils.findErrorInHtml(outputResponse)){
//			Assert.fail("Expected error: exception requested.");
//		}
//	}
	

}
