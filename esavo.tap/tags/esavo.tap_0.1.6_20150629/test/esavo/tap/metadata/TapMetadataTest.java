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
package esavo.tap.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.tap.TAPException;
import esavo.tap.TestUtils;
import esavo.tap.resource.TAP;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.test.database.DummyDatabaseConnection;
import esavo.tap.test.http.DummyHttpRequest;
import esavo.tap.test.http.DummyHttpResponse;
import esavo.tap.test.tap.DummyTapServiceConnection;
import esavo.tap.test.uws.DummyUwsFactory.StorageType;

public class TapMetadataTest {
	
	
//	Anonymous:
//		curl "http://localhost:8080/tap-local/tap/tables"
//		curl "http://localhost:8080/tap-local/tap/tables?only_schemas=true"
//		curl "http://localhost:8080/tap-local/tap/tables?only_tables=true"
//		curl "http://localhost:8080/tap-local/tap/tables?schemas=tap_schema"
//		curl "http://localhost:8080/tap-local/tap/tables?schemas=tap_schema&only_tables=true"
//
//		TABLE: curl "http://localhost:8080/tap-local/tap/tables?tables=tap_schema.columns"
//
//		TABLE: FAIL: curl "http://localhost:8080/tap-local/tap/tables?tables=user_jsegovia.table1"
//
//		curl "http://localhost:8080/tap-local/tap/tables?schemas=tap_schema,public"
//		curl "http://localhost:8080/tap-local/tap/tables?schemas=tap_schema,public&only_tables=true"
//
//		TABLE: curl "http://localhost:8080/tap-local/tap/tables?tables=tap_schema.columns,public.igsl_source"
//		TABLE: curl "http://localhost:8080/tap-local/tap/tables?tables=tap_schema.columns,tap_schema.keys,public.igsl_source"
//
//		TABLE: FAIL: curl "http://localhost:8080/tap-local/tap/tables?tables=tap_schema.columns,user_jsegovia.table1"
//
//  Authenticated:

	
	/**
	 * Unique id
	 */
	public static final String TEST_APP_ID = "__TEST__" + TapMetadataTest.class.getName();
	
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
	public void testMetadataLoader() throws IOException{
		TAP tap = service.getTap();
		UwsJobOwner user = UwsJobOwner.ANONYMOUS_OWNER;
		service.getFactory().getSecurityManager().setUser(user);

		DummyDatabaseConnection dbc = service.getDatabaseConnection();
		String query;
		String[] columnNames;
		String[][] queryResults;

		query = "SELECT table_name,description,size,flags FROM tap_schema.tables  WHERE schema_name='public'";
		columnNames = new String[]{"table_name","description","size","flags"};
		queryResults = new String[][] {
			{"igsl_source","desc columns","100","0"},
			{"gog_cataloguesource","desc keys","100","0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));

		query = "SELECT table_name,description,size,flags FROM tap_schema.tables  WHERE schema_name='tap_schema'";
		columnNames = new String[]{"table_name","description","size","flags"};
		queryResults = new String[][] {
			{"columns","desc columns","100","0"},
			{"keys","desc keys","100","0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT c.column_name,c.description,c.ucd,c.utype,c.datatype,c.unit,c.indexed,c.flags FROM "
				+ "tap_schema.columns c where c.schema_name= 'tap_schema' AND c.table_name='columns' ORDER BY c.indexed DESC,c.column_name ASC";
		columnNames = new String[]{"c.column_name","c.description","c.ucd","c.utype","c.datatype","c.unit","c.indexed","c.flags"};
		queryResults = new String[][] {
			{"column_a1","desc column 1","ucd1","utype1", "datatype1", "unit1", "0", "0"},
			{"column_a2","desc column 2","ucd2","utype2", "datatype2", "unit2", "0", "0"},
			{"column_a3","desc column 3","ucd3","utype3", "datatype3", "unit3", "0", "0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT c.column_name,c.description,c.ucd,c.utype,c.datatype,c.unit,c.indexed,c.flags FROM "
				+ "tap_schema.columns c where c.schema_name= 'tap_schema' AND c.table_name='keys' ORDER BY c.indexed DESC,c.column_name ASC";
		columnNames = new String[]{"c.column_name","c.description","c.ucd","c.utype","c.datatype","c.unit","c.indexed","c.flags"};
		queryResults = new String[][] {
			{"column_b1","desc column 1","ucd1","utype1", "datatype1", "unit1", "0", "0"},
			{"column_b2","desc column 2","ucd2","utype2", "datatype2", "unit2", "0", "0"},
			{"column_b3","desc column 3","ucd3","utype3", "datatype3", "unit3", "0", "0"},
			{"column_b4","desc column 4","ucd3","utype3", "datatype3", "unit3", "0", "0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT c.column_name,c.description,c.ucd,c.utype,c.datatype,c.unit,c.indexed,c.flags FROM "
				+ "tap_schema.columns c where c.schema_name= 'public' AND c.table_name='igsl_source' ORDER BY c.indexed DESC,c.column_name ASC";
		columnNames = new String[]{"c.column_name","c.description","c.ucd","c.utype","c.datatype","c.unit","c.indexed","c.flags"};
		queryResults = new String[][] {
			{"column_c1","desc column 1","ucd1","utype1", "datatype1", "unit1", "0", "0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT table_name,description,size,flags FROM tap_schema.tables  WHERE schema_name='tap_schema' AND table_name = 'columns'";
		columnNames = new String[]{"table_name","description","size","flags"};
		queryResults = new String[][] {
			{"columns","desc columns","1","0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT table_name,description,size,flags FROM tap_schema.tables  WHERE schema_name='public' AND table_name = 'igsl_source'";
		columnNames = new String[]{"table_name","description","size","flags"};
		queryResults = new String[][] {
			{"igsl_source","desc columns","1","0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		
		query = "SELECT table_name,description,size,flags FROM tap_schema.tables  WHERE schema_name='tap_schema' AND table_name = 'keys'";
		columnNames = new String[]{"table_name","description","size","flags"};
		queryResults = new String[][] {
			{"keys","desc columns","1","0"}};
		dbc.setDataForQuery(query, TestUtils.createDummyData(columnNames, queryResults));
		

		List<String> results;
		DummyHttpRequest request;
		DummyHttpResponse response;
		String txtResp;
		List<String> expected = new ArrayList<String>();

		//reqParams
		request = new DummyHttpRequest("http://localhost/tap-test/tap/tables", "tap");
		
		//only_schemas
		request.setParameter("only_schemas", "true");
		
		response = new DummyHttpResponse();
		response.clearOutput();
		expected.clear();
		
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("public");
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving schemas only.");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		expected.clear();
		TestUtils.checkList(expected, results, "Retrieving schemas only. No tables expected.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving schemas only. No columns expected.");

		//only tables
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("only_tables", "true");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("public");
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving table names only.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		expected.add("igsl_source");
		expected.add("gog_cataloguesource");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving table names only.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		TestUtils.checkList(expected, results, "Retrieving table names only. No columns expected.");
		
		//one schema
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("schemas", "tap_schema");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving one schema.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving one schema.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		expected.add("column_b1");
		expected.add("column_b2");
		expected.add("column_b3");
		expected.add("column_b4");
		TestUtils.checkList(expected, results, "Retrieving one schema. Columns expected.");

		//one schema, only tables
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("schemas", "tap_schema");
		request.setParameter("only_tables", "true");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving one schema, only tables.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving one schema, only tables.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		TestUtils.checkList(expected, results, "Retrieving one schema, only tables. Columns no expected.");
		
		//one table
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "tap_schema.columns");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving one table.");
		expected.clear();
		expected.add("columns");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving one table.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		TestUtils.checkList(expected, results, "Retrieving one table. Columns expected.");

		//one table of authenticated user from anonymous
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "user_test.table1");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		Assert.assertTrue("Expected error: access to user private area from anonymous", TestUtils.findErrorInHtml(txtResp));
		
		//2 schemas
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("schemas", "tap_schema,public");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("public");
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving 2 schemas.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		expected.add("igsl_source");
		expected.add("gog_cataloguesource");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving 2 schemas.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		expected.add("column_b1");
		expected.add("column_b2");
		expected.add("column_b3");
		expected.add("column_b4");
		expected.add("column_c1");
		TestUtils.checkList(expected, results, "Retrieving 2 schemas. Columns expected.");
		
		//2 schemas, only tables
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("schemas", "tap_schema,public");
		request.setParameter("only_tables", "true");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();

		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("public");
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving 2 schemas, tables only.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		expected.add("igsl_source");
		expected.add("gog_cataloguesource");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving 2 schemas, tables only.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		TestUtils.checkList(expected, results, "Retrieving 2 schemas, tables only. Columns expected.");
		
		//2 tables, 2 different schemas
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "tap_schema.columns,public.igsl_source");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		expected.add("public");
		TestUtils.checkList(expected, results, "Retrieving 2 tables different schemas.");
		expected.clear();
		expected.add("columns");
		expected.add("igsl_source");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving 2 tables different schemas.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		expected.add("column_c1");
		TestUtils.checkList(expected, results, "Retrieving 2 tables different schemas. Columns expected.");
		
		//2 tables, same schema
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "tap_schema.columns,tap_schema.keys");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();

		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		TestUtils.checkList(expected, results, "Retrieving 2 tables same schema.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving 2 tables shame schema.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		expected.add("column_b1");
		expected.add("column_b2");
		expected.add("column_b3");
		expected.add("column_b4");
		TestUtils.checkList(expected, results, "Retrieving 2 tables same schema. Columns expected.");
		
		//3 tables, 2 from same schema
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "tap_schema.columns,tap_schema.keys,public.igsl_source");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		results = TestUtils.getSchemasFromTapXmlResponse(txtResp);
		expected.add("tap_schema");
		expected.add("public");
		TestUtils.checkList(expected, results, "Retrieving 3 tables.");
		expected.clear();
		expected.add("columns");
		expected.add("keys");
		expected.add("igsl_source");
		results = TestUtils.getTablesFromTapXmlResponse(txtResp);
		TestUtils.checkList(expected, results, "Retrieving 3 tables.");
		results = TestUtils.getAllColumnsFromTapXmlResponse(txtResp);
		expected.clear();
		expected.add("column_a1");
		expected.add("column_a2");
		expected.add("column_a3");
		expected.add("column_b1");
		expected.add("column_b2");
		expected.add("column_b3");
		expected.add("column_b4");
		expected.add("column_c1");
		TestUtils.checkList(expected, results, "Retrieving 3 tables. Columns expected.");
		
		//2 tables: one private table
		request.clearParameters();
		response.clearOutput();
		expected.clear();
		
		request.setParameter("tables", "tap_schema.columns,user_test.table1");
		tap.executeRequest(request, response);
		txtResp = response.getOutputAsString();
		
		Assert.assertTrue("Expected error: access to user private area from anonymous", TestUtils.findErrorInHtml(txtResp));

	}
	
	@Test
	public void testSingleTableLoader(){
		
	}

}
