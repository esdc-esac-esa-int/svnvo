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
package esavo.sl.services.tabletool;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.sl.services.tabletool.handlers.EditTableHandler;
import esavo.sl.test.TestUtils;
import esavo.sl.test.tap.DummyGacsTapServiceConnection;
import esavo.sl.test.tap.DummyGacsTapDatabaseConnection;
import esavo.sl.test.tap.DummyGacsTapServiceFactory;
import esavo.tap.TAPException;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TapUtils;
import esavo.tap.metadata.TapTableInfo;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.database.DummyUwsDatabaseConnection;
import esavo.uws.utils.test.http.DummyHttpRequest;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class TableToolTest {
	
	private static final String TEST_APP_ID = "__TEST__" + TableToolTest.class.getName();
	
	private static DummyGacsTapServiceConnection service;
	
	@BeforeClass
	public static void beforeClass() throws UwsException, TAPException{
		service = new DummyGacsTapServiceConnection(TEST_APP_ID, StorageType.database);
	}
	
	@AfterClass
	public static void afterClass(){
		service.clearStorage();
	}
	
	@Test
	public void testRaDec() throws Exception{
		Map<String, String> params = null;
		String subcontext = "tableTool";
		String servletName = "tableTool";
		DummyHttpRequest request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		String outputResponse;
		
		TableTool tableTool = new TableTool(service);
		
		//User not authenticated.
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: user not authenticated");
		}

		UwsSecurity security = service.getFactory().getSecurityManager();
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dbConn = factory.getDatabaseConnection();
		DummyGacsTapDatabaseConnection database = factory.getDummyTapDatabaseConnection();
		database.setPublicSchema(true);
		database.setPublicTable(true);
		
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		UwsJobOwner user = new UwsJobOwner(TEST_APP_ID, UwsJobOwner.ROLE_USER);
		user.setAuthUsername(TEST_APP_ID);
		security.setUser(user);
		
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing action");
		}

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params = new HashMap<String, String>();
		params.put("ACTION", "xxxx");
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: invalid action");
		}

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put("ACTION", "radec");
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing table name");
		}

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		String schemaName = "user_" + TEST_APP_ID;
		String tableName = "table_test";
		
		params.put("TABLE_NAME", "table_test");

		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing ra");
		}
		
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put("RA", "col_0");
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing dec");
		}

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put("DEC", "col_1");
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: table not found in database");
		}
		
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		//Put in DB the required contents
		TapTableInfo tapTableInfo = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
		populateTapTableInfoDefault(tapTableInfo, 3);
		database.setTapTableInfo(tapTableInfo);

		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Should work");
		}
		
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_START_TRANSACTION, "");
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_FETCH_TAP_TABLE_INFO, "user_" + TEST_APP_ID + "." + tableName);
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_RADEC_USER_LOADED_TABLE, "user_" + TEST_APP_ID + "." + tableName +" - col_0/col_1 (1/2)");
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC, "user_" + TEST_APP_ID + "." + tableName +" (ra: col_0,dec: col_1, tableSpace: null)");
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_END_TRANSACTION, "");
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_VACUUM, "user_" + TEST_APP_ID + "." + tableName);
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_CLOSE_DB_CONNECTION,"");
	}
	
	
	
	@Test
	public void testEdit() throws Exception {
		Map<String, String> params = null;
		String subcontext = "tableTool";
		String servletName = "tableTool";
		DummyHttpRequest request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		TableTool tableTool = new TableTool(service);
		
		String schemaName = "user_" + TEST_APP_ID;
		String tableName = "table_test";


//		String schemaName = "schema";
//		String tableName = "table";
//
//		Map<String, String> params = null;
		params = new HashMap<String, String>();
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dbConn = factory.getDatabaseConnection();
		DummyGacsTapDatabaseConnection database = factory.getDummyTapDatabaseConnection();
		database.setPublicSchema(true);
		database.setPublicTable(true);
		
		String outputResponse;
		
		
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
//		String appid = "appid_test";
//		DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
//		TableTool<ResultSet> tableTool = new TableTool<ResultSet>(sc);
		
		UwsJobOwner user = new UwsJobOwner(TEST_APP_ID, UwsJobOwner.ROLE_USER);
		user.setAuthUsername(TEST_APP_ID);
		security.setUser(user);

		
//		DefaultJobOwner owner = new DefaultJobOwner("test", "test", "test");
//		tableTool.setOwner(owner);
		
//		TapTableInfo tti = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
//		dbConnection.setTapTableInfo(tti);
		
		
		//Put in DB the required contents
		TapTableInfo tapTableInfo = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
		populateTapTableInfoDefault(tapTableInfo, 3);
		database.setTapTableInfo(tapTableInfo);

		
		//Valid table info. Wrong specification (missing COLn, COLn_UCD, COLn_UTYPE, COLn_FLAGS, COLn_INDEXED)
		
		String tableColumnName = "col0";
		
//		long taskId = -1;
//		int numColumnsToProcess = 1;
//		
//		try{
//			tableTool.execute(params, taskId, numColumnsToProcess);
//			Assert.fail("Expected error: missing COL0");
//		}catch(Exception e){
//		}
//		dbConnection.clearFlags();
		
		//action not found
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing action");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put("ACTION", "edit");
		
		//no param num tables
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing num tables");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put(EditTableHandler.PARAM_NUM_TABLES, "a");
		
		//wrong number for num tables
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing num tables (wrong format)");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put(EditTableHandler.PARAM_NUM_TABLES, "1");
		
		//missing table0
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing table 0");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		
		String tablePrefix = EditTableHandler.PARAM_PREFIX_TABLE+"0";
		params.put(tablePrefix, tableName);
		
		//missing table num columns
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing table num columns");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_NUM_COLS, "a"); //one column.

		//table num cols wrong format
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing table num columns (wrong format)");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_NUM_COLS, "1"); //one column.
		
		//col0 not found
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing COL0");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN+"0", tableColumnName);
//		try{
//			tableTool.execute(params, taskId, numColumnsToProcess);
//			Assert.fail("Expected error: missing COL0_UCD");
//		}catch(Exception e){
//		}
//		dbConnection.clearFlags();
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing COL0_UCD");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN+"0"+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd0");
//		try{
//			tableTool.execute(params, taskId, numColumnsToProcess);
//			Assert.fail("Expected error: missing COL0_UTYPE");
//		}catch(Exception e){
//		}
//		dbConnection.clearFlags();
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing COL0_UTYPE");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN+"0"+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype0");
//		try{
//			tableTool.execute(params, taskId, numColumnsToProcess);
//			Assert.fail("Expected error: missing COL0_FLAGS");
//		}catch(Exception e){
//		}
//		dbConnection.clearFlags();
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing COL0_FLAGS");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN+"0"+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "flags0");
//		try{
//			tableTool.execute(params, taskId, numColumnsToProcess);
//			Assert.fail("Expected error: missing COL0_INDEXED");
//		}catch(Exception e){
//		}
//		dbConnection.clearFlags();
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		outputResponse = response.getOutputAsString();
		if(!UwsTestUtils.findErrorInHtml(outputResponse)){
			Assert.fail("Expected error: missing COL0_INDEXED");
		}
		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN+"0"+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "indexed0");
//		tableTool.execute(params, taskId, numColumnsToProcess);
//		Assert.assertTrue("Executed update", 
//				dbConnection.isActionLogged(
//						DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, schemaName+"."+tableName+"."+tableColumnName));
//		dbConnection.clearFlags();
		request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		tableTool.executeRequest(request, response);
		
		TestUtils.checkDbAction(database,DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, schemaName+"."+tableName+"."+tableColumnName);

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
	}
	
//	@Test
//	public void testCheckParameters() throws Exception {
//		String tableName = "table";
//		Map<String, String> params = new HashMap<String, String>();
//
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
//		String appid = "appid_test";
//		DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
//		TableTool<ResultSet> tableTool = new TableTool<ResultSet>(sc);
//		
//		//No table num
//		try{
//			tableTool.check(params);
//			Assert.fail("Exception expected. No table num.");
//		}catch(IllegalArgumentException iea){
//		}
//		
//		//Invalid number of tables
//		params.put(TableTool.PARAM_NUM_TABLES, "a");
//		try{
//			tableTool.check(params);
//			Assert.fail("Exception expected. Invalid table num.");
//		}catch(IllegalArgumentException iea){
//		}
//		
//		params.put(TableTool.PARAM_NUM_TABLES, "1");
//		
//		//No table
//		try{
//			tableTool.check(params);
//			Assert.fail("Exception expected. No table");
//		}catch(IllegalArgumentException iea){
//		}
//		
//		String tablePrefix = TableTool.PARAM_PREFIX_TABLE+"0";
//		params.put(tablePrefix, tableName);
//		
//		//No num cols for table 1
//		try{
//			tableTool.check(params);
//			Assert.fail("Exception expected. No table");
//		}catch(IllegalArgumentException iea){
//		}
//		
//		//Invalid number of columns for table 1
//		params.put(tablePrefix+TableTool.PARAM_NUM_COLS, "a");
//		try{
//			tableTool.check(params);
//			Assert.fail("Exception expected. Invalid number of columns.");
//		}catch(IllegalArgumentException iea){
//		}
//		
//		//OK
//		params.put(tablePrefix+TableTool.PARAM_NUM_COLS, "1");
//		tableTool.check(params);
//	}
	
	@Test
	public void testCheckCreateIndex() throws Exception {
//		String schemaName = "schema";
//		String tableName = "table";
//
//		Map<String, String> params = new HashMap<String, String>();
//
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
//		String appid = "appid_test";
//		DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
//		TableTool<ResultSet> tableTool = new TableTool<ResultSet>(sc);
//		
//		DefaultJobOwner owner = new DefaultJobOwner("test", "test", "test");
//		tableTool.setOwner(owner);
		
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		
		Map<String, String> params = null;
		String subcontext = "tableTool";
		String servletName = "tableTool";
		DummyHttpRequest request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		//TableTool tableTool = new TableTool(service);
		
		String schemaName = "user_" + TEST_APP_ID;
		String tableName = "table_test";

		params = new HashMap<String, String>();
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dbConn = factory.getDatabaseConnection();
		DummyGacsTapDatabaseConnection database = factory.getDummyTapDatabaseConnection();
		database.setPublicSchema(true);
		database.setPublicTable(true);
		
		UwsJobOwner user = new UwsJobOwner(TEST_APP_ID, UwsJobOwner.ROLE_USER);
		user.setAuthUsername(TEST_APP_ID);
		security.setUser(user);

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();
		

		TapTableInfo tti = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
		database.setTapTableInfo(tti);
		String tableColName0 = "colname0";
		String tableColName1 = "colname1";
		
		String oldDesc0 = "oldDesc0";
		String oldUcd0 = "oldUcd0";
		String oldUtype0 = "oldUtype0";
		String oldDataType0 = "oldDataType0";
		String oldUnit0 = "oldUnit0";
		int oldSize0 = 100;
		int oldPrincipal0 = 0;
		int oldStd0 = 1;
		int oldIndexed0 = 0;
		int oldFlags0 = TapUtils.TAP_COLUMN_TABLE_FLAG_RA;
		
		tti.putColumn(tableColName0, "description", oldDesc0);
		tti.putColumn(tableColName0, "ucd",         oldUcd0);
		tti.putColumn(tableColName0, "utype",       oldUtype0);
		tti.putColumn(tableColName0, "datatype",    oldDataType0);
		tti.putColumn(tableColName0, "unit",        oldUnit0);
		tti.putColumn(tableColName0, "size",        oldSize0);
		tti.putColumn(tableColName0, "principal",   oldPrincipal0);
		tti.putColumn(tableColName0, "std",         oldStd0);
		tti.putColumn(tableColName0, "indexed",     oldIndexed0);
		tti.putColumn(tableColName0, "flags",       oldFlags0);
		
		//Second column, 
		String oldDesc1 = "oldDesc1";
		String oldUcd1 = "oldUcd1";
		String oldUtype1 = "oldUtype1";
		String oldDataType1 = "oldDataType1";
		String oldUnit1 = "oldUnit1";
		int oldSize1 = 100;
		int oldPrincipal1 = 0;
		int oldStd1 = 1;
		int oldIndexed1 = 0;
		int oldFlags1 = TapUtils.TAP_COLUMN_TABLE_FLAG_DEC;
		
		tti.putColumn(tableColName1, "description", oldDesc1);
		tti.putColumn(tableColName1, "ucd",         oldUcd1);
		tti.putColumn(tableColName1, "utype",       oldUtype1);
		tti.putColumn(tableColName1, "datatype",    oldDataType1);
		tti.putColumn(tableColName1, "unit",        oldUnit1);
		tti.putColumn(tableColName1, "size",        oldSize1);
		tti.putColumn(tableColName1, "principal",   oldPrincipal1);
		tti.putColumn(tableColName1, "std",         oldStd1);
		tti.putColumn(tableColName1, "indexed",     oldIndexed1);
		tti.putColumn(tableColName1, "flags",       oldFlags1);
		
		int numColumnsToProcess = 2;
		String tablePrefix = EditTableHandler.PARAM_PREFIX_TABLE+"0";
		
		params = new HashMap<String, String>();
		params.put(EditTableHandler.PARAM_NUM_TABLES, "1");
		params.put(tablePrefix, tableName);
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_NUM_COLS, ""+numColumnsToProcess); //one column.
		
		String key0 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "0";
		params.put(key0, tableColName0);
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UCD, oldUcd0);
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, oldUtype0);
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+oldFlags0); //flags does not contain ra/dec
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");

		String key1 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "1";
		params.put(key1, tableColName1);
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UCD, oldUcd1);
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, oldUtype1);
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+oldFlags1); //flags does not contain ra/dec
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");
		
		
		EditTableHandler editTableHandler = new EditTableHandler();

		//No ra/ no dec => illegalargumentexception
		try{
			editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, null);
			Assert.fail("Ra should not be found.");
		}catch(IllegalArgumentException e){
		}

		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA); //flags does not contain dec
		try{
			editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, null);
			Assert.fail("Dec should not be found.");
		}catch(IllegalArgumentException e){
		}

		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC);

		//Already indexed (oldflag0=Ra, oldFlag1=Dec)
		editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, null);
		
		String msg = schemaName + "." + tableName + " (ra: " + tableColName0 + ",dec: "+tableColName1+",tableSpace: )";
		Assert.assertFalse(database.isActionLogged(DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC, msg));
		
		database.clear();
		
		//Already indexed (oldflag0=Ra, oldFlag1=Dec) + tablespace
		String tableSpace = "dummy_user_tablespace";
		editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, tableSpace);
		
		msg = schemaName + "." + tableName + " (ra: " + tableColName0 + ",dec: "+tableColName1+", tableSpace: "+tableSpace+")";
		Assert.assertFalse(database.isActionLogged(DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC, msg));
		
		database.clear();
		
		//no already indexed:
		oldFlags0 = 0;
		oldFlags1 = 0;
		tti.putColumn(tableColName0, "flags", oldFlags0);
		tti.putColumn(tableColName1, "flags", oldFlags1);
		editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, tableSpace);
		Assert.assertTrue(database.isActionLogged(DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC, msg));
		
		database.clear();
		
		//Ra/Dec in other fields
		numColumnsToProcess = 4;
		String tableColName2 = "colName2";
		tti.putColumn(tableColName2, "description", "desc2");
		tti.putColumn(tableColName2, "ucd",         "ucd2");
		tti.putColumn(tableColName2, "utype",       "utype2");
		tti.putColumn(tableColName2, "datatype",    "datatype2");
		tti.putColumn(tableColName2, "unit",        "unit2");
		tti.putColumn(tableColName2, "size",        100);
		tti.putColumn(tableColName2, "principal",   0);
		tti.putColumn(tableColName2, "std",         0);
		tti.putColumn(tableColName2, "indexed",     0);
		tti.putColumn(tableColName2, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_RA);

		String key2 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "2";
		params.put(key2, tableColName2);
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""); //flags does not contain ra/dec
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0");

		String tableColName3 = "colName3";
		tti.putColumn(tableColName3, "description", "desc2");
		tti.putColumn(tableColName3, "ucd",         "ucd2");
		tti.putColumn(tableColName3, "utype",       "utype2");
		tti.putColumn(tableColName3, "datatype",    "datatype2");
		tti.putColumn(tableColName3, "unit",        "unit2");
		tti.putColumn(tableColName3, "size",        100);
		tti.putColumn(tableColName3, "principal",   0);
		tti.putColumn(tableColName3, "std",         0);
		tti.putColumn(tableColName3, "indexed",     0);
		tti.putColumn(tableColName3, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);

		String key3 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "3";
		params.put(key3, tableColName3);
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd3");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype3");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "mag"); //flags does not contain ra/dec
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");

		editTableHandler.indexRaDec(database, tapSchemaInfo, tti, numColumnsToProcess, params, tablePrefix, tableSpace);
		Assert.assertTrue(database.isActionLogged(DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC, msg));
		msg = schemaName + "." + tableName + " (ra: " + tableColName2 + ",dec: "+tableColName3+")";
		Assert.assertTrue(database.isActionLogged(DummyGacsTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_RA_DEC, msg));
	}
	
	
	@Test
	public void testFlow1() throws Exception {
//		String schemaName = "schema";
//		String tableName = "table";
//
//		Map<String, String> params = new HashMap<String, String>();
//
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
//		String appid = "appid_test";
//		DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
//		TableTool<ResultSet> tableTool = new TableTool<ResultSet>(sc);
//		
//		DefaultJobOwner owner = new DefaultJobOwner("test", "test", "test");
//		tableTool.setOwner(owner);
		
		Map<String, String> params = null;
		String subcontext = "tableTool";
		String servletName = "tableTool";
		DummyHttpRequest request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		//TableTool tableTool = new TableTool(service);
		
		String schemaName = "user_" + TEST_APP_ID;
		String tableName = "table_test";

		params = new HashMap<String, String>();
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dbConn = factory.getDatabaseConnection();
		DummyGacsTapDatabaseConnection database = factory.getDummyTapDatabaseConnection();
		database.setPublicSchema(true);
		database.setPublicTable(true);

		UwsJobOwner user = new UwsJobOwner(TEST_APP_ID, UwsJobOwner.ROLE_USER);
		user.setAuthUsername(TEST_APP_ID);
		security.setUser(user);

		TapTableInfo tti = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
		database.setTapTableInfo(tti);
		int numTablesToProcess = 1;
		int numColsTable1 = 5;
		String tablePrefix = EditTableHandler.PARAM_PREFIX_TABLE+"0";
		params.put(tablePrefix, tableName);
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_NUM_COLS, ""+numColsTable1);
		
		//1 table
		//5 columns
		//col0 and col1 new ra/dec index (new flags 'ra' and 'dec' also)
		//col2 and col3 old ra/dec index (old flags 'ra' and 'dec' also).
		//col3 new normal index requested, new flags 'mag' requested
		//col4 old normal index. No new flags no new index.

		//col0
		//New ra index requested
		//New flags ra requested
		//No old index
		//No old flags
		String tableColName0 = "colname0";
		tti.putColumn(tableColName0, "description", "desc0");
		tti.putColumn(tableColName0, "ucd",         "ucd0");
		tti.putColumn(tableColName0, "utype",       "utype0");
		tti.putColumn(tableColName0, "datatype",    "datatype0");
		tti.putColumn(tableColName0, "unit",        "unit0");
		tti.putColumn(tableColName0, "size",        100);
		tti.putColumn(tableColName0, "principal",   0);
		tti.putColumn(tableColName0, "std",         0);
		tti.putColumn(tableColName0, "indexed",     0);
		tti.putColumn(tableColName0, "flags",       0);
		
		String key0 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "0";
		params.put(key0, tableColName0);
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd0");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype0");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA);

		//col1
		//New dec index requested
		//New flags dec requested
		//No old index
		//No old flags
		String tableColName1 = "colname1";
		tti.putColumn(tableColName1, "description", "desc1");
		tti.putColumn(tableColName1, "ucd",         "ucd1");
		tti.putColumn(tableColName1, "utype",       "utype1");
		tti.putColumn(tableColName1, "datatype",    "datatype1");
		tti.putColumn(tableColName1, "unit",        "unit1");
		tti.putColumn(tableColName1, "size",        100);
		tti.putColumn(tableColName1, "principal",   0);
		tti.putColumn(tableColName1, "std",         0);
		tti.putColumn(tableColName1, "indexed",     0);
		tti.putColumn(tableColName1, "flags",       0);
		
		String key1 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "1";
		params.put(key1, tableColName1);
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd0");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype0");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""+TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC);

		//col2
		//Old ra index
		//no index requested
		//no flags requested
		String tableColName2 = "colname2";
		tti.putColumn(tableColName2, "description", "desc2");
		tti.putColumn(tableColName2, "ucd",         "ucd2");
		tti.putColumn(tableColName2, "utype",       "utype2");
		tti.putColumn(tableColName2, "datatype",    "datatype2");
		tti.putColumn(tableColName2, "unit",        "unit2");
		tti.putColumn(tableColName2, "size",        100);
		tti.putColumn(tableColName2, "principal",   0);
		tti.putColumn(tableColName2, "std",         0);
		tti.putColumn(tableColName2, "indexed",     1);
		tti.putColumn(tableColName2, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_RA);

		String key2 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "2";
		params.put(key2, tableColName2);
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""); //flags does not contain ra/dec
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0");

		//col3
		//Old dec index
		//new normal index requested
		//new mag flags requested
		String tableColName3 = "colname3";
		tti.putColumn(tableColName3, "description", "desc3");
		tti.putColumn(tableColName3, "ucd",         "ucd3");
		tti.putColumn(tableColName3, "utype",       "utype3");
		tti.putColumn(tableColName3, "datatype",    "datatype3");
		tti.putColumn(tableColName3, "unit",        "unit3");
		tti.putColumn(tableColName3, "size",        100);
		tti.putColumn(tableColName3, "principal",   0);
		tti.putColumn(tableColName3, "std",         0);
		tti.putColumn(tableColName3, "indexed",     1);
		tti.putColumn(tableColName3, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		
		String key3 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "3";
		params.put(key3, tableColName3);
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd3");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype3");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "mag"); //flags does not contain ra/dec
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1"); //it was not indexed, now it is.
		
		//col4
		//Old normal index
		//No new index requested
		//No new flags requested
		String tableColName4 = "colname4";
		tti.putColumn(tableColName4, "description", "desc4");
		tti.putColumn(tableColName4, "ucd",         "ucd4");
		tti.putColumn(tableColName4, "utype",       "utype4");
		tti.putColumn(tableColName4, "datatype",    "datatype4");
		tti.putColumn(tableColName4, "unit",        "unit4");
		tti.putColumn(tableColName4, "size",        100);
		tti.putColumn(tableColName4, "principal",   0);
		tti.putColumn(tableColName4, "std",         0);
		tti.putColumn(tableColName4, "indexed",     1);
		tti.putColumn(tableColName4, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX);
		
		String key4 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "4";
		params.put(key4, tableColName4);
		params.put(key4+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd4");
		params.put(key4+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype4");
		params.put(key4+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, ""); //flags does not contain ra/dec
		params.put(key4+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0"); //now it is not indexed (remove index)
		
		long taskId = -1;
		EditTableHandler editTableHandler = new EditTableHandler();
		//EditTableHandler.execute(params, taskId, numTablesToProcess);
		editTableHandler.execute(params, taskId, numTablesToProcess, user, service);
		
//		List<String[]> msgs = new ArrayList<String[]>();
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname0"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname1"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname2"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname3"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_INDEX_TABLE_COLUMN, "schema.table.colname3"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname4"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_TABLE_COLUMN, "schema.table.colname4"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_RA_DEC, "schema.table (ra: colname2,dec: colname3)"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC,"schema.table (ra: colname0,dec: colname1)"});
//		
//		for(String[] s: msgs){
//			Assert.assertTrue("Not found action requested: " + s[0] + ": " + s[1], dbConnection.isActionLogged(s[0], s[1]));
//		}

		
		String[] actions = new String[] {
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_INDEX_TABLE_COLUMN,
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_TABLE_COLUMN,
			DummyGacsTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_RA_DEC,
			DummyGacsTapDatabaseConnection.ACTION_CMD_REQ_INDEX_RA_DEC
		};

		String[] msgs = new String[] {
				schemaName+"."+tableName+".colname0",
				schemaName+"."+tableName+".colname1",
				schemaName+"."+tableName+".colname2",
				schemaName+"."+tableName+".colname3",
				schemaName+"."+tableName+".colname3",
				schemaName+"."+tableName+".colname4",
				schemaName+"."+tableName+".colname4",
				schemaName+"."+tableName+" (ra: colname2,dec: colname3)",
				schemaName+"."+tableName+" (ra: colname0,dec: colname1, tableSpace: null)"
		};

		TestUtils.checkDbActions(database, actions, msgs);
	}
	
	@Test
	public void testFlow2() throws Exception {
//		String schemaName = "schema";
//		String tableName = "table";
//
//		Map<String, String> params = new HashMap<String, String>();
//
//		DummyDatabaseConnection uwsDbConnection = new DummyDatabaseConnection();
//		DummyTapDatabaseConnection dbConnection = new DummyTapDatabaseConnection(uwsDbConnection);
//		String appid = "appid_test";
//		DummyTapServiceConnection<ResultSet> sc = new DummyTapServiceConnection<ResultSet>(appid, dbConnection);
//		TableTool<ResultSet> tableTool = new TableTool<ResultSet>(sc);
//		
//		DefaultJobOwner owner = new DefaultJobOwner("test", "test", "test");
//		tableTool.setOwner(owner);
		
		Map<String, String> params = null;
		String subcontext = "tableTool";
		String servletName = "tableTool";
		DummyHttpRequest request = TestUtils.createSimpleHttpGetRequest(subcontext, servletName, params);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		//TableTool tableTool = new TableTool(service);
		
		String schemaName = "user_" + TEST_APP_ID;
		String tableName = "table_test";

		params = new HashMap<String, String>();
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dbConn = factory.getDatabaseConnection();
		DummyGacsTapDatabaseConnection database = factory.getDummyTapDatabaseConnection();
		database.setPublicSchema(true);
		database.setPublicTable(true);
		
		UwsJobOwner user = new UwsJobOwner(TEST_APP_ID, UwsJobOwner.ROLE_USER);
		user.setAuthUsername(TEST_APP_ID);
		security.setUser(user);

		database.clear();
		response.clearOutput();
		dbConn.clearExecutedQueries();

		
		TapTableInfo tti = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
		database.setTapTableInfo(tti);
		int numTablesToProcess = 1;
		int numColsTable1 = 4;
		String tablePrefix = EditTableHandler.PARAM_PREFIX_TABLE+"0";
		
		params.put(tablePrefix, tableName);
		params.put(tablePrefix+EditTableHandler.PARAM_SUFFIX_NUM_COLS, ""+numColsTable1);
		
		//1 table
		//4 columns
		//col0 and col1 old ra/dec index (new flags 'ra' and 'dec' also)
		//remove old indexes on col0 and col1
		//col2 no changes (to check no changes flow) no indexes/no flags
		//col3 no changes (nomal indexe + flags)

		//col0
		//old ra index requested
		//old flags ra requested
		//No new index
		//No new flags
		String tableColName0 = "colname0";
		tti.putColumn(tableColName0, "description", "desc0");
		tti.putColumn(tableColName0, "ucd",         "ucd0");
		tti.putColumn(tableColName0, "utype",       "utype0");
		tti.putColumn(tableColName0, "datatype",    "datatype0");
		tti.putColumn(tableColName0, "unit",        "unit0");
		tti.putColumn(tableColName0, "size",        100);
		tti.putColumn(tableColName0, "principal",   0);
		tti.putColumn(tableColName0, "std",         0);
		tti.putColumn(tableColName0, "indexed",     1);
		tti.putColumn(tableColName0, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		
		String key0 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "0";
		params.put(key0, tableColName0);
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd0");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype0");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0");
		params.put(key0+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "");

		//col1
		//old dec index requested
		//old flags dec requested
		//No new index
		//No new flags
		String tableColName1 = "colname1";
		tti.putColumn(tableColName1, "description", "desc1");
		tti.putColumn(tableColName1, "ucd",         "ucd1");
		tti.putColumn(tableColName1, "utype",       "utype1");
		tti.putColumn(tableColName1, "datatype",    "datatype1");
		tti.putColumn(tableColName1, "unit",        "unit1");
		tti.putColumn(tableColName1, "size",        100);
		tti.putColumn(tableColName1, "principal",   0);
		tti.putColumn(tableColName1, "std",         0);
		tti.putColumn(tableColName1, "indexed",     1);
		tti.putColumn(tableColName1, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		
		String key1 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "1";
		params.put(key1, tableColName1);
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd1");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype1");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0");
		params.put(key1+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "");

		//col2
		//no changes
		//no old flags
		//no old index
		String tableColName2 = "colname2";
		tti.putColumn(tableColName2, "description", "desc2");
		tti.putColumn(tableColName2, "ucd",         "ucd2");
		tti.putColumn(tableColName2, "utype",       "utype2");
		tti.putColumn(tableColName2, "datatype",    "datatype2");
		tti.putColumn(tableColName2, "unit",        "unit2");
		tti.putColumn(tableColName2, "size",        100);
		tti.putColumn(tableColName2, "principal",   0);
		tti.putColumn(tableColName2, "std",         0);
		tti.putColumn(tableColName2, "indexed",     0);
		tti.putColumn(tableColName2, "flags",       0);
		
		String key2 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "2";
		params.put(key2, tableColName2);
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype2");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "0");
		params.put(key2+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, "");

		//col3
		//no changes
		//old flags
		//old index
		String tableColName3 = "colname3";
		tti.putColumn(tableColName3, "description", "desc2");
		tti.putColumn(tableColName3, "ucd",         "ucd2");
		tti.putColumn(tableColName3, "utype",       "utype2");
		tti.putColumn(tableColName3, "datatype",    "datatype2");
		tti.putColumn(tableColName3, "unit",        "unit2");
		tti.putColumn(tableColName3, "size",        100);
		tti.putColumn(tableColName3, "principal",   0);
		tti.putColumn(tableColName3, "std",         0);
		tti.putColumn(tableColName3, "indexed",     1);
		tti.putColumn(tableColName3, "flags",       TapUtils.TAP_COLUMN_TABLE_FLAG_MAG);
		
		String key3 = tablePrefix+EditTableHandler.PARAM_SUFFIX_COLUMN + "3";
		params.put(key3, tableColName3);
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UCD, "ucd2");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_UTYPE, "utype2");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_INDEXED, "1");
		params.put(key3+EditTableHandler.PARAM_COL_SUFFIX_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_ID_MAG);
		
		long taskId = -1;
		EditTableHandler editTableHandler = new EditTableHandler();
		//editTableHandler.execute(params, taskId, numTablesToProcess);
		editTableHandler.execute(params, taskId, numTablesToProcess, user, service);

		
//		List<String[]> msgs = new ArrayList<String[]>();
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname0"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE, "schema.table.colname1"});
//		msgs.add(new String[]{DummyTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_RA_DEC, "schema.table (ra: colname0,dec: colname1)"});
//		
//		for(String[] s: msgs){
//			Assert.assertTrue("Not found action requested: " + s[0] + ": " + s[1], database.isActionLogged(s[0], s[1]));
//		}
		
		String[] actions = new String[]{
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_UPDATE_USER_LOADED_TABLE,
			DummyGacsTapDatabaseConnection.ACTION_CMD_REMOVE_INDEX_RA_DEC
		};
		
		String[] msgs = new String[] {
			schemaName+"."+tableName+".colname0",
			schemaName+"."+tableName+".colname1",
			schemaName+"."+tableName+" (ra: colname0,dec: colname1)"
		};
		
		TestUtils.checkDbActions(database, actions, msgs);
	}
	
	private void populateTapTableInfoDefault(TapTableInfo tapTableInfo, int numCols){
		String column;
		Object[] data;
		for(int i = 0; i < numCols; i++){
			column = "col_" + i;
			data = createDataForTapTableInfo(column, i);
			populateTapTableInfo(tapTableInfo, column, data);
		}
	}
	
	private Object[] createDataForTapTableInfo(String columnName, int index){
//		t.addColumnDataType("description", String.class);
//		t.addColumnDataType("ucd",         String.class);
//		t.addColumnDataType("utype",       String.class);
//		t.addColumnDataType("datatype",    String.class);
//		t.addColumnDataType("unit",        String.class);
//		t.addColumnDataType("size",        Integer.class);
//		t.addColumnDataType("principal",   Integer.class);
//		t.addColumnDataType("std",         Integer.class);
//		t.addColumnDataType("indexed",     Integer.class);
//		t.addColumnDataType("flags",       Integer.class);
		return new Object[] {
				"Desc_" + index,
				"Ucd_" + index,
				"Utype_" + index,
				"Datatype_" + index,
				"Unit_" + index,
				new Integer(0),
				new Integer(0),
				new Integer(0),
				new Integer(0),
				new Integer(0)
		};
	}
	
	private void populateTapTableInfo(TapTableInfo tapTableInfo, String columnName, Object[] data){
//		t.addColumnDataType("description", String.class);
//		t.addColumnDataType("ucd",         String.class);
//		t.addColumnDataType("utype",       String.class);
//		t.addColumnDataType("datatype",    String.class);
//		t.addColumnDataType("unit",        String.class);
//		t.addColumnDataType("size",        Integer.class);
//		t.addColumnDataType("principal",   Integer.class);
//		t.addColumnDataType("std",         Integer.class);
//		t.addColumnDataType("indexed",     Integer.class);
//		t.addColumnDataType("flags",       Integer.class);
		tapTableInfo.putColumn(columnName, "description", data[0]);
		tapTableInfo.putColumn(columnName, "ucd",         data[1]);
		tapTableInfo.putColumn(columnName, "utype",       data[2]);
		tapTableInfo.putColumn(columnName, "datatype",    data[3]);
		tapTableInfo.putColumn(columnName, "unit",        data[4]);
		tapTableInfo.putColumn(columnName, "size",        data[5]);
		tapTableInfo.putColumn(columnName, "principal",   data[6]);
		tapTableInfo.putColumn(columnName, "std",         data[7]);
		tapTableInfo.putColumn(columnName, "indexed",     data[8]);
		tapTableInfo.putColumn(columnName, "flags",       data[9]);
	}
}
