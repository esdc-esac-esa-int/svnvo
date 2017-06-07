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
package esavo.tap.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.tap.TAPException;
import esavo.tap.TestUtils;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.test.database.DummyDatabaseConnection;
import esavo.tap.test.http.DummyHttpRequest;
import esavo.tap.test.http.DummyHttpResponse;
import esavo.tap.test.tap.DummyTapServiceConnection;
import esavo.tap.test.uws.DummyUwsFactory.StorageType;

public class TapTest {
	
	/**
	 * Unique id
	 */
	public static final String TEST_APP_ID = "__TEST__" + TapTest.class.getName();
	
	public static final String HTTP_TAP_REQUEST_BASE = "http://localhost/tap-test/";
	
	private static final String[] CAPABILITIES = {
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/tables\">tables</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/share\">share</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/functions\">functions</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/sync\">sync</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/capabilities\">capabilities</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/async\">async</a></li>",
		"<li><a href=\""+HTTP_TAP_REQUEST_BASE+"tap/availability\">availability</a></li>"
	};
	
	private static DummyTapServiceConnection service;
	
	@BeforeClass
	public static void beforeClass() throws UwsException, TAPException{
		service = new DummyTapServiceConnection(TEST_APP_ID, StorageType.database);
	}
	
	@AfterClass
	public static void afterClass(){
		service.clearStorage();
	}

	@Test
	public void testCapabilities() throws IOException{
		TAP tap = service.getTap();
		UwsJobOwner user = UwsJobOwner.ANONYMOUS_OWNER;
		service.getFactory().getSecurityManager().setUser(user);

		DummyDatabaseConnection dbc = service.getDatabaseConnection();
		String query;
		String[] columnNames;
		String[][] queryResults;

		List<String> results;
		DummyHttpRequest request;
		DummyHttpResponse response;
		String txtResp;
		
		query = "SET statement_timeout TO 1800000";
		dbc.setDataForQuery(query, TestUtils.createInsertOrUpdateData(0));

		query = "SELECT owner_id FROM uws2_schema.owners ORDER BY owner_id";
		columnNames = new String[]{"owner_id"};
		queryResults = new String[][] {};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT job_id FROM uws2_schema.jobs_meta WHERE phase_id IN ('EXECUTING','PENDING','QUEUED')";
		columnNames = new String[]{"job_id"};
		queryResults = new String[][] {};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		request = new DummyHttpRequest("http://localhost/tap-test/tap", "tap");

		response = new DummyHttpResponse();
		response.clearOutput();
		
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();

		//System.out.println(txtResp);
		TestUtils.findInHtml(txtResp, CAPABILITIES, "Checking capabilities.");
	}
	
//	@Test
//	public void testAddJobAndRun1() throws Exception {
//		//two steps: 1. add job, 2. run job
//		
////		DummyDatabaseConnection db = new DummyDatabaseConnection();
////		DummyTapDatabaseConnection dbconnection = new DummyTapDatabaseConnection(db);
////		DummyTapServiceConnection<ResultSet> serviceConnection = new DummyTapServiceConnection<ResultSet>("appid_test",dbconnection);
////		TAP tap = new TAP(tapService);
//		//boolean useDatabase = true;
//		//DummyTapServiceConnection tapServiceConnection = new DummyTapServiceConnection(TEST_APP_ID, useDatabase);
//		TAP tap = service.getTap();
//		
//		Map<String,String> reqParams = new HashMap<String, String>();
//
//		DummyHttpRequest request;
//		DummyHttpResponse response;
//		String txtResp;
//
//		//reqParams
//		request = TestUtils.createSimpleHttpGetRequest("tap", "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		checkCapabilitiesResponse(txtResp);
//
//		String jobid = "12345";
//
//		//Non authenticated user
//		request = TestUtils.createSimpleHttpGetRequest("tap/async/"+jobid, "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		Assert.assertTrue("Non authenticated user", TestUtils.findErrorInHtml(txtResp));
//		
//		//Get status for not launched job
//		reqParams.put("session", "SESSION1234");
//		request = TestUtils.createSimpleHttpGetRequest("tap/async/"+jobid, "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		Assert.assertTrue("Job does not exist", TestUtils.findErrorInHtml(txtResp));
//		
//		//REQUEST=doQuery -d LANG=ADQL -d QUERY='SELECT TOP 1 source_id, alpha, delta FROM public.g10_quasars' http://localhost:8080/tap-local/tap/async
//		reqParams.put("REQUEST", "doQuery");
//		reqParams.put("LANG", "ADQL");
//		reqParams.put("QUERY", "select * from table1");
//		request = TestUtils.createSimpleHttpPostRequest("tap/async", "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		Assert.assertTrue("Add job - Redirect", response.getStatus() == 303);
//		//System.out.println(response.getOutputAsString());
//		String location = response.getHeader("Location");
//		//System.out.println(location);
//		jobid = location.substring(location.lastIndexOf('/')+1);
//		//System.out.println(jobid);
//		
//		
//		String newReqContext = "tap/async/"+jobid;
//		
//		//Check job id exists
//		request = TestUtils.createSimpleHttpGetRequest(newReqContext, "tap");
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		Assert.assertFalse("Job must be found", TestUtils.findErrorInHtml(txtResp));
//		Assert.assertTrue("Pending phase", txtResp.indexOf("<uws:phase>PENDING</uws:phase>") >= 0);
//
//
//		//Run job
//		reqParams.clear();
//		reqParams.put("PHASE","run");
//		request = TestUtils.createSimpleHttpPostRequest(newReqContext+"/phase", "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		//System.out.println(response.getHeaders());
//		//System.out.println(response.getStatus());
//		Assert.assertTrue("Run job - Redirect", response.getStatus() == 303);
//		Assert.assertFalse("Job must be found", TestUtils.findErrorInHtml(txtResp));
//		
//		//Check status: must be executing or completed
//		request = TestUtils.createSimpleHttpGetRequest(newReqContext, "tap");
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		Assert.assertFalse("Job must be found", TestUtils.findErrorInHtml(txtResp));
//		Assert.assertFalse("Pending phase", txtResp.indexOf("<uws:phase>PENDING</uws:phase>") >= 0);
//		boolean status;
//		status = txtResp.indexOf("<uws:phase>COMPLETED</uws:phase>") >= 0;
//		if(!status){
//			status = txtResp.indexOf("<uws:phase>EXECUTING</uws:phase>") >= 0;
//			if(!status){
//				Assert.fail("Job not in execution/completed phase");
//			}
//		}
//
//	}
	
//	@Test
//	public void testAddJobAndRun2() throws Exception{
//		//one step: add job + run job
////		DummyDatabaseConnection db = new DummyDatabaseConnection();
////		DummyTapDatabaseConnection dbconnection = new DummyTapDatabaseConnection(db);
////		DummyTapServiceConnection<ResultSet> serviceConnection = new DummyTapServiceConnection<ResultSet>("appid_test",dbconnection);
////		DummyUserIdentifier userIdentifier = new DummyUserIdentifier(null); //set anonymous
////		serviceConnection.setUserIdentifier(userIdentifier);
////		TAP<ResultSet> tap = new TAP<ResultSet>(serviceConnection);
//		
//		//boolean useDatabase = true;
//		//DummyTapServiceConnection tapServiceConnection = new DummyTapServiceConnection(TEST_APP_ID, useDatabase);
//		TAP tap = service.getTap();
//
//		Map<String,String> reqParams = new HashMap<String, String>();
//
//		DummyHttpRequest request;
//		DummyHttpResponse response;
//		String txtResp;
//
//		//reqParams
//		request = TestUtils.createSimpleHttpGetRequest("tap", "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		checkCapabilitiesResponse(txtResp);
//
//		String jobid = "12345";
//
//		//Non authenticated user
//		request = TestUtils.createSimpleHttpGetRequest("tap/async/"+jobid, "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		Assert.assertTrue("Non authenticated user", TestUtils.findErrorInHtml(txtResp));
//		
//		//Get status for not launched job
//		reqParams.put("session", "SESSION1234");
//		request = TestUtils.createSimpleHttpGetRequest("tap/async/"+jobid, "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		//System.out.println(txtResp);
//		Assert.assertTrue("Job does not exist", TestUtils.findErrorInHtml(txtResp));
//		
//		//-d PHASE=run -d REQUEST=doQuery -d LANG=ADQL -d QUERY='SELECT TOP 1 source_id, alpha, delta FROM public.g10_quasars' http://localhost:8080/tap-local/tap/async
//		reqParams.put("REQUEST", "doQuery");
//		reqParams.put("LANG", "ADQL");
//		reqParams.put("QUERY", "select * from table1");
//		reqParams.put("PHASE", "run");
//		request = TestUtils.createSimpleHttpPostRequest("tap/async", "tap", reqParams);
//		response = TestUtils.createSimpleHttpResponse();
//		
//		tap.executeRequest(request, response);
//		Assert.assertTrue("Add job - Redirect", response.getStatus() == 303);
//		//System.out.println(response.getOutputAsString());
//		String location = response.getHeader("Location");
//		//System.out.println(location);
//		jobid = location.substring(location.lastIndexOf('/')+1);
//		//System.out.println(jobid);
//		
//		
//		String newReqContext = "tap/async/"+jobid;
//		
//		//Check job id exists
//		request = TestUtils.createSimpleHttpGetRequest(newReqContext, "tap");
//		response = TestUtils.createSimpleHttpResponse();
//		tap.executeRequest(request, response);
//		txtResp = response.getOutputAsString();
//		Assert.assertFalse("Job must be found", TestUtils.findErrorInHtml(txtResp));
//		Assert.assertFalse("Pending phase", txtResp.indexOf("<uws:phase>PENDING</uws:phase>") >= 0);
//		boolean status;
//		status = txtResp.indexOf("<uws:phase>COMPLETED</uws:phase>") >= 0;
//		if(!status){
//			status = txtResp.indexOf("<uws:phase>EXECUTING</uws:phase>") >= 0;
//			if(!status){
//				Assert.fail("Job not in execution/completed phase");
//			}
//		}
//
//	}
	
	private void checkCapabilitiesResponse(String response){
		for(String cap: CAPABILITIES){
			if(response.indexOf(cap) < 0){
				Assert.fail("Not found default capability: " + cap + "\nResponse:" + response);
			}
		}
	}

}
