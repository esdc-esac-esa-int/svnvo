package esavo.sl.tap.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.sl.test.DatabaseUtils;
import esavo.sl.test.savot.DummyRowSequence;
import esavo.sl.test.savot.DummyStarTable;
import esavo.sl.test.tap.DummyGacsTapServiceConnection;
import esavo.sl.test.tap.DummyGacsTapServiceFactory;
import esavo.tap.TAPException;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledConnection;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TapTableInfo;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.jdbc.UwsJdbcManager;
import esavo.uws.utils.test.database.DummyUwsData;
import esavo.uws.utils.test.database.DummyUwsDatabaseConnection;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class JDBCPooledConnectionTest {
	
	public static final String TEST_APP_ID = "__TEST__" + JDBCPooledConnectionTest.class.getName();
	
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
	public void testGeneric() throws TAPException, SQLException, UwsException{
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		//TapJDBCPoolSingleton.setConnection(dbconnection);
		
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		
		long timeout = 1800000;
		String timeOutQuery = "SET statement_timeout TO " + timeout;
		dbconnection.enableGenerateExceptionRequested(timeOutQuery);
		
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		//service.setTapSchemaInfo(tapSchemaInfo);
		
		try{
			new TapJDBCPooledConnection(service, dbconnection, timeout);
			Assert.fail("Exception expected");
		}catch(TAPException e){
			
		}
		
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		dbconnection.setGenerateNonQueryException();
		try{
			jpc.startTransaction();
			Assert.fail("Exception expected: startTransaction");
		}catch(DBException e){
		}
		dbconnection.setGenerateNonQueryException();
		try{
			jpc.cancelTransaction();
			Assert.fail("Exception expected: cancelTransaction");
		}catch(DBException e){
		}
		dbconnection.setGenerateNonQueryException();
		try{
			jpc.endTransaction();
			Assert.fail("Exception expected: endTransaction");
		}catch(DBException e){
		}
		dbconnection.setGenerateNonQueryException();
		try{
			jpc.close();
			Assert.fail("Exception expected: close");
		}catch(DBException e){
		}
		
		//No more generic exceptions
		//dbconnection.setGenerateNonQueryException(false);

		//Test close behavior
		Assert.assertFalse("Closed", dbconnection.isClosed());
		jpc.close();
		Assert.assertTrue("Closed", dbconnection.isClosed());
		dbconnection.setClosed(false);
		
		//Test commit behavior
		Assert.assertTrue("autocommit by default", dbconnection.getAutoCommit());
		Assert.assertFalse("commit called", dbconnection.getCommitCalled());
		
		jpc.startTransaction();
		Assert.assertFalse("autocommit disabled", dbconnection.getAutoCommit());
		Assert.assertFalse("commit called", dbconnection.getCommitCalled());
		
		dbconnection.setCommitCalled(false);
		jpc.endTransaction();
		Assert.assertTrue("autocommit by default", dbconnection.getAutoCommit());
		Assert.assertTrue("commit called", dbconnection.getCommitCalled());
		
		Assert.assertNull("id", jpc.getID());
	}
	
	@Test
	public void testExecuteQuery() throws Exception{
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;

		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		
		//service.setTapSchemaInfo(tapSchemaInfo);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);

		dbconnection.setGenerateNonQueryException();
		try {
			jpc.executeQuery(null, null);
			Assert.fail("Exception expected: executeQuery");
		} catch (DBException e) {
		}

		//dbconnection.setGenerateNonQueryException(false);
		ResultSet rs = jpc.executeQuery(null, null);
		Assert.assertFalse("No query specified", rs.next());
	}
	
	@Test
	public void testCreateSchema() throws Exception{
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		//service.setTapSchemaInfo(tapSchemaInfo);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);

		dbconnection.setGenerateNonQueryException();
		try {
			jpc.createSchema(null);
			Assert.fail("Exception expected: createSchema");
		} catch (DBException e) {
		}

		//dbconnection.setGenerateNonQueryException(false);
		
		dbconnection.setGenerateNonQueryException(1);
		
		String schemaName = "schema";
		try {
			jpc.createSchema(schemaName);
			Assert.fail("Exception expected: createSchema");
		} catch (DBException e) {
		}
		
		jpc.createSchema(schemaName);
		

	}
	
	@Test
	public void testRegisterInTapSchema() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		dbconnection.setGenerateNonQueryException();
		try {
			jpc.registerInTapSchema(tapSchemaInfo, (TAPSchema)null);
			Assert.fail("Exception expected: registerInTapSchema");
		} catch (DBException e) {
		}

		//dbconnection.setGenerateNonQueryException();
		try {
			jpc.registerInTapSchema(tapSchemaInfo, (TAPTable)null);
			Assert.fail("Exception expected: registerInTapSchema");
		} catch (NullPointerException e) {
		}
		
		String schemaName = "schema";
		String tableName = "table";
		String query = "DELETE FROM tap_schema.all_tables WHERE schema_name='"+schemaName+"' AND table_name='"+tableName+"'";
		
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		
		dbconnection.enableGenerateExceptionRequested(query, 1);
		
		try {
			jpc.registerInTapSchema(tapSchemaInfo, table);
			Assert.fail("Exception expected: registerInTapSchema");
		} catch (DBException e) {
		}
		
		String executedQuery = "INSERT INTO tap_schema.all_columns "+
				"(schema_name, table_name, column_name, description, unit, ucd, utype, datatype, size, principal, indexed, std, flags, pos) values "+
				"('schema','table','table_oid','Object Identifier','','','','INTEGER',-1,1,1,0,0,-1)";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(executedQuery, insertData);
		jpc.registerInTapSchema(tapSchemaInfo, table);
		Assert.assertTrue("Executed insert", dbconnection.isExecutedQuery(executedQuery));
	}
	
	@Test
	public void testDropSchema() throws Exception{
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.dropSchema(null);
			Assert.fail("Exception expected: dropSchema");
		} catch (DBException e) {
		}
		
		String schema = "schema";
		String query = "DROP SCHEMA IF EXISTS "+schema+" CASCADE";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		jpc.dropSchema(schema);
		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}
	
	@Test
	public void testCreateTable() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		dbconnection.setGenerateNonQueryException();
		
		String tableName = "table";
		String schemaName = "schema";
		String columnName = "col";
		TAPColumn column = new TAPColumn(columnName);
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		table.addColumn(column);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		try {
			jpc.createTable(table, null);
			Assert.fail("Exception expected: createTable");
		} catch (DBException e) {
		}
		
		String query = "CREATE TABLE schema.table ( "+columnName+" VARCHAR,  table_oid SERIAL ) ";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);
		
		jpc.createTable(table, null);
		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));

		String userUploadTableSpace = "dummy_table_space";
		query = "CREATE TABLE schema.table ( "+columnName+" VARCHAR,  table_oid SERIAL ) TABLESPACE " + userUploadTableSpace;
		insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);
		
		jpc.createTable(table, userUploadTableSpace);
		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}
	
//	@Test
//	public void testInsertRow() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		dbconnection.setGenerateNonQueryException();
//		
//		String tableName = "table";
//		String schemaName = "schema";
//		String columnName = "col";
//		TAPColumn column = new TAPColumn(columnName);
//		TAPTable table = new TAPTable(tableName);
//		table.addColumn(column);
//		TAPSchema schema = new TAPSchema(schemaName);
//		schema.addTable(table);
//		
//		try {
//			jpc.insertRow(null, table);
//			Assert.fail("Exception expected: insertRow");
//		} catch (DBException e) {
//		}
//		
//		String vala = "a";
//		String valb = "b";
//		String query = "INSERT INTO schema.table VALUES (  '"+vala+"'  ) ";
//		DummyData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, insertData);
//		
//		SavotTR row = new SavotTR();
//		SavotTD td1 = new SavotTD();
//		td1.setContent(vala);
//		SavotTD td2 = new SavotTD();
//		td2.setContent(valb);
//		TDSet tdSet = new TDSet();
//		tdSet.addItem(td1);
//		tdSet.addItem(td2);
//		row.setTDs(tdSet);
//		jpc.insertRow(row, table);
//
//		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
//	}

	@Test
	public void testDropTable() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);

		String tableName = "table";
		String schemaName = "schema";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);

		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.dropTable(table, false);
			Assert.fail("Exception expected: dropTable");
		} catch (DBException e) {
		}
		
		String query = "DROP TABLE schema.table";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		jpc.dropTable(table, false);
		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}
	
	@Test
	public void testLoadTableData() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		
		DummyGacsTapServiceFactory factory = (DummyGacsTapServiceFactory)service.getFactory();
		DummyUwsDatabaseConnection dummydbconn = factory.getDatabaseConnection();
		
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		
		String tableName = "table";
		String schemaName = "schema";
		String columnName = "col";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPColumn column = new TAPColumn(columnName);
		table.addColumn(column);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);

		UwsJobOwner owner = UwsUtils.createDefaultOwner(UwsUtils.ANONYMOUS_USER, TEST_APP_ID);
		
		DatabaseUtils.createOwnerUpdateQueries(new DummyUwsDatabaseConnection[]{dbconnection, dummydbconn}, owner, schemaName, tableName);

		DummyStarTable starTable = new DummyStarTable();
		starTable.setRowCount(1);
		DummyRowSequence sequence = new DummyRowSequence();
		starTable.setRowSequence(sequence);

		//exception requested
		dbconnection.setGenerateNonQueryException();
		
		
		try {
			jpc.loadTableData(owner, table, starTable);
			Assert.fail("Exception expected: loadTableData");
		} catch (DBException e) {
			//expected OK
		}
		
		//no exception expected, no data found
		int r;
		r = jpc.loadTableData(owner, table, starTable);
		Assert.assertEquals(0,r);
		
		//1 row expected
		sequence.setData(new Object[][]{{1}});
		//jpc.setTaskId(1);
		r = jpc.loadTableData(owner, table, starTable, 1);
		Assert.assertEquals(1,r);
	}
	
	@Test
	public void testIndexAndPk() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		dbconnection.setGenerateNonQueryException();
		
		String paramRaCol = "ra";
		String paramDecCol = "dec";
//		try {
//			jpc.indexAndPk(table, paramRaCol, paramDecCol);
//			Assert.fail("Exception expected: indexAndPk");
//		} catch (DBException e) {
//		}
		try {
			jpc.createPkInTable(tapSchemaInfo, table);
			Assert.fail("Exception expected: indexAndPk");
		} catch (DBException e) {
		}
		
		
		List<String> queries = new ArrayList<String>();
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_pk PRIMARY KEY (table_oid) "); 
		queries.add("CREATE INDEX table_q3c ON schema.table USING btree (q3c_ang2ipix(ra, dec));");
		queries.add("ALTER TABLE schema.table ALTER COLUMN ra SET NOT NULL");
		queries.add("ALTER TABLE schema.table ALTER COLUMN dec SET NOT NULL");
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_ra_check_nan CHECK ( NOT ra = 'NaN')");
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_dec_check_nan CHECK ( NOT dec = 'NaN')");
		queries.add("UPDATE tap_schema.all_columns SET indexed = 1 WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+
				"' AND (column_name = 'ra' OR column_name = 'dec')");
		queries.add("UPDATE tap_schema.all_tables SET flags = flags | 0 WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"'");

		for(String q: queries){
			DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
			dbconnection.setDataForQuery(q, insertData);
		}

		//jpc.indexAndPk(table, paramRaCol, paramDecCol);
		jpc.createPkInTable(tapSchemaInfo, table);
		jpc.createRaAndDecIndexes(tapSchemaInfo, table.getDBSchemaName(), table.getDBName(), paramRaCol, paramDecCol, 0, null);

		for(String q: queries){
			Assert.assertTrue("Executed query: " + q, dbconnection.isExecutedQuery(q));
		}
		
		dbconnection.clear();
		
		//with tablespace
		String tableSpace = "dummy_user_tablespace";
		queries.clear();
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_pk PRIMARY KEY (table_oid) "); 
		queries.add("CREATE INDEX table_q3c ON schema.table USING btree (q3c_ang2ipix(ra, dec)) TABLESPACE "+tableSpace+";");
		queries.add("ALTER TABLE schema.table ALTER COLUMN ra SET NOT NULL");
		queries.add("ALTER TABLE schema.table ALTER COLUMN dec SET NOT NULL");
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_ra_check_nan CHECK ( NOT ra = 'NaN')");
		queries.add("ALTER TABLE schema.table ADD CONSTRAINT table_dec_check_nan CHECK ( NOT dec = 'NaN')");
		queries.add("UPDATE tap_schema.all_columns SET indexed = 1 WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+
				"' AND (column_name = 'ra' OR column_name = 'dec')");
		queries.add("UPDATE tap_schema.all_tables SET flags = flags | 0 WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"'");

		for(String q: queries){
			DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
			dbconnection.setDataForQuery(q, insertData);
		}

		//jpc.indexAndPk(table, paramRaCol, paramDecCol);
		jpc.createPkInTable(tapSchemaInfo, table);
		jpc.createRaAndDecIndexes(tapSchemaInfo, table.getDBSchemaName(), table.getDBName(), paramRaCol, paramDecCol, 0, tableSpace);

		for(String q: queries){
			Assert.assertTrue("Executed query: " + q, dbconnection.isExecutedQuery(q));
		}
		

	}
	
	@Test
	public void testRemoveRaAndDecIndexes() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		dbconnection.setGenerateNonQueryException();
		
		String paramRaCol = "ra";
		String paramDecCol = "dec";
		int raDecFlag = 0;
		
		try {
			jpc.removeRaAndDecIndexes(tapSchemaInfo, schemaName, tableName, paramRaCol, paramDecCol, raDecFlag);
			Assert.fail("Exception expected: testRemoveRaAndDecIndexes");
		} catch (DBException e) {
		}
		
		String fullName = schemaName + "." + tableName;
		String indexName = tableName + "_q3c";
		
		List<String> queries = new ArrayList<String>();
		
	    queries.add("DROP INDEX IF EXISTS "+schemaName+"."+indexName); 
        queries.add("ALTER TABLE "+fullName+" DROP CONSTRAINT "+tableName+"_ra_check_nan"); 
        queries.add("ALTER TABLE "+fullName+" DROP CONSTRAINT "+tableName+"_dec_check_nan"); 
        queries.add("ALTER TABLE "+fullName+" ALTER COLUMN "+paramRaCol+" DROP NOT NULL");
        queries.add("ALTER TABLE "+fullName+" ALTER COLUMN "+paramDecCol+" DROP NOT NULL");
        queries.add("UPDATE tap_schema.all_tables SET flags = flags & (~" + raDecFlag + ") WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"'");

		for(String q: queries){
			DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
			dbconnection.setDataForQuery(q, insertData);
		}

		//jpc.indexAndPk(table, paramRaCol, paramDecCol);
		jpc.removeRaAndDecIndexes(tapSchemaInfo, schemaName, tableName, paramRaCol, paramDecCol, raDecFlag);
		
		for(String q: queries){
			Assert.assertTrue("Executed query: " + q, dbconnection.isExecutedQuery(q));
		}

	}
	
	@Test
	public void testUpdateTableSize() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.updateTableSizeInTapSchema(tapSchemaInfo, table);
			Assert.fail("Exception expected: updateTableSize");
		} catch (DBException e) {
		}
		
		String query = "UPDATE tap_schema.all_tables  SET size = ( SELECT count(*) FROM schema.table) WHERE table_name ILIKE 'table' AND schema_name ILIKE 'schema'";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);
		
		jpc.updateTableSizeInTapSchema(tapSchemaInfo, table);

		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}
	
	@Test
	public void testVacuumAnalyze() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		TAPTable table = new TAPTable(tableName, TAPTable.PUBLIC_TABLE);
		TAPSchema schema = new TAPSchema(schemaName, null, null, null, true);
		schema.addTable(table);
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.vacuumAnalyze(table.getDBSchemaName(), table.getDBName());
			Assert.fail("Exception expected: vacuumAnalyze");
		} catch (DBException e) {
		}
		
		String query = "VACUUM ANALYZE schema.table";
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);
		
		jpc.vacuumAnalyze(table.getDBSchemaName(), table.getDBName());

		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}

//	@Test
//	public void testGetOldJobs() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		String jobsManagerIdentifier = "T";
//		String ownerid = "ownerid";
//		long olderThan = 1;
//		dbconnection.setGenerateNonQueryException();
//		
//		try {
//			jpc.getOldJobs(jobsManagerIdentifier, ownerid, olderThan);
//			Assert.fail("Exception expected: getOldJobs");
//		} catch (DBException e) {
//		}
//		
//		String query = MessageFormat.format(DatabaseUtils.QUERY_JOB_OLDER_THAN, new Object[]{ownerid, olderThan, jobsManagerIdentifier});
//		
//		DummyData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, insertData);
//		List<String> jobs = jpc.getOldJobs(jobsManagerIdentifier, ownerid, olderThan);
//		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("No data", 0, jobs.size());
//
//		//Put data
//		List<List<String>> results = new ArrayList<List<String>>();
//		DatabaseUtils.appendRowData(results, new String[]{"job1"});
//		insertData = DatabaseUtils.createDummyData(DatabaseUtils.COLUMN_NAMES_QUERY_JOB_OLDER_THAN , results);
//		dbconnection.setDataForQuery(query, insertData);
//		
//		jobs = jpc.getOldJobs(jobsManagerIdentifier, ownerid, olderThan);
//		Assert.assertEquals("Number of results: 1", 1, jobs.size());
//	}

	@Test
	public void testCreateTableColumnIndex() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		String colName = "col1";
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.createTableColumnIndex(schemaName, tableName, colName);
			Assert.fail("Exception expected: createTableColumnIndex");
		} catch (DBException e) {
		}
		
		
		String indexName = tableName + "_" + colName;
		String query = "CREATE INDEX " + indexName + " ON "+ schemaName+"."+tableName + " ("+colName+")"; 

		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		jpc.createTableColumnIndex(schemaName, tableName, colName);

		Assert.assertTrue("Executed query: " +  query, dbconnection.isExecutedQuery(query));
	}

	@Test
	public void testRemoveTableColumnIndex() throws Exception {
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		String colName = "col1";
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.removeTableColumnIndex(schemaName, tableName, colName);
			Assert.fail("Exception expected: removeTableColumnIndex");
		} catch (DBException e) {
		}
		
		
		String indexName = tableName + "_" + colName;
		String query = "DROP INDEX IF EXISTS " + schemaName + "." + indexName; 

		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		jpc.removeTableColumnIndex(schemaName, tableName, colName);

		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}
	
	@Test
	public void testUpdateUserTableData() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		String colName = "col1";
		String ucd = "ucd";
		String uType = "uType";
		int flags = 0;
		int indexed = 0;
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.updateUserTableData(tapSchemaInfo, schemaName, tableName, colName, ucd, uType, flags, indexed);
			Assert.fail("Exception expected: updateUserTableData");
		} catch (DBException e) {
		}
		
		String query = "UPDATE tap_schema.all_columns SET ucd = '"+ucd+"', utype = '"+uType+"', flags = "+flags+", indexed = "+indexed+" WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"' AND column_name = '"+colName+"'"; 

		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		jpc.updateUserTableData(tapSchemaInfo, schemaName, tableName, colName, ucd, uType, flags, indexed);

		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
	}

	@Test
	public void testFetchTapTableInfo() throws Exception {
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(null);
		DummyUwsDatabaseConnection dbconnection = new DummyUwsDatabaseConnection();
		UwsJdbcManager.setDummyConnection(TEST_APP_ID, TEST_APP_ID, dbconnection);
		long timeout = 1800000;
		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
		//String appid = "appid_test";
		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
		//service.setTapSchemaInfo(tapSchemaInfo);
		//DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
		TapJDBCPooledConnection jpc = new TapJDBCPooledConnection(service, dbconnection, timeout);
		
		String tableName = "table";
		String schemaName = "schema";
		dbconnection.setGenerateNonQueryException();
		
		try {
			jpc.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);
			Assert.fail("Exception expected: updateUserTableData");
		} catch (DBException e) {
		}
		
		
		String query = "SELECT column_name, description, ucd, utype, datatype, unit, size, principal, std, indexed, flags FROM "+
				"tap_schema.all_columns WHERE schema_name = '"+schemaName+"' AND table_name='"+tableName+"' ORDER BY pos, column_name"; 
		
		DummyUwsData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
		dbconnection.setDataForQuery(query, insertData);

		TapTableInfo tti = jpc.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);

		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
		Assert.assertNull("Empty data", tti);
		
		//Put data
		List<List<String>> results = new ArrayList<List<String>>();
		DatabaseUtils.appendRowData(results, new String[]{"col1", "desc1", "ucd1", "utype1", "INTGER", "", "100", "0", "1", "2", "3"});
		insertData = DatabaseUtils.createTapMetaInfoData(results);
		dbconnection.setDataForQuery(query, insertData);
		
		tti = jpc.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);
		Assert.assertEquals("Number of results: 1", 1, tti.getNumTableColumnNames());

		DatabaseUtils.appendRowData(results, new String[]{"col2", "desc2", "ucd2", "utype2", "DOUBLE", "", "101", "4", "5", "6", "7"});
		dbconnection.setDataForQuery(query, insertData);

		tti = jpc.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);
		Assert.assertEquals("Number of results: 2", 2, tti.getNumTableColumnNames());

	}

//	@Test
//	public void testLoadUserDetails() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		String userid = "userid";
//		dbconnection.setGenerateNonQueryException();
//		
//		try {
//			jpc.loadUserDetails(userid);
//			Assert.fail("Exception expected: testLoadUserDetails");
//		} catch (DBException e) {
//		}
//		
//		String query = "SELECT roles, quota_db, current_size_db, quota_file, current_size_file FROM admin.users WHERE id = '"+userid+"'";
//		DummyData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, insertData);
//
//		UserDetails userDetails = jpc.loadUserDetails(userid);
//		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
//		Assert.assertNull("No data", userDetails);
//		
//		//Put data
//		List<String> columnNames = new ArrayList<String>(Arrays.asList(new String[]{"roles", "quota_db", "current_size_db", "quota_file", "current_size_file"}));
//		String[] rowData = new String[]{"1", "100", "10", "200", "20"};
//		insertData = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(query, insertData);
//
//		userDetails = jpc.loadUserDetails(userid);
//		Assert.assertEquals("Roles", 1, userDetails.getRoles());
//		Assert.assertEquals("Quota db", 100, userDetails.getQuotaDb());
//		Assert.assertEquals("Current size db", 10, userDetails.getCurrentSizeDb());
//		Assert.assertEquals("Quota files", 200, userDetails.getQuotaFile());
//		Assert.assertEquals("Current size files", 20, userDetails.getCurrentSizeFile());
//	}
	
//	@Test
//	public void testCreatUser() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		UserDetails userDetails = new UserDetails();
//		String userid = "userid";
//		int role = 1;
//		long quotaDb = 111;
//		long quotaFiles = 222;
//		long currentSizeDb = 33;
//		long currentSizeFiles = 44;
//		userDetails.setId(userid);
//		userDetails.setRoles(role);
//		userDetails.setQuotaDb(quotaDb);
//		userDetails.setCurrentSizeDb(currentSizeDb);
//		userDetails.setQuotaFile(quotaFiles);
//		userDetails.setCurrentSizeFile(currentSizeFiles);
//		
//		dbconnection.setGenerateNonQueryException();
//		
//		try {
//			jpc.createUser(userDetails);
//			Assert.fail("Exception expected: testCreatUser");
//		} catch (DBException e) {
//		}
//		
//		String query = "INSERT INTO admin.users (id, roles, quota_db, current_size_db, quota_file, current_size_file) VALUES ('"+userid+
//				"',"+role+","+quotaDb+","+currentSizeDb+","+quotaFiles+","+currentSizeFiles+")";
//		
//		DummyData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, insertData);
//
//		jpc.createUser(userDetails);
//		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
//	}

//	@Test
//	public void testRetrieveUsersByFilter() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		List<UserDetails> users;
//		long offset = -1;
//		long limit = -1;
//		UsersFilter filter = new UsersFilter();
//		
//		String userid = "userid";
//		int role = 1;
//		long quotaDb = 111;
//		long quotaFiles = 222;
//		long currentSizeDb = 33;
//		long currentSizeFiles = 44;
//		
//		dbconnection.setGenerateNonQueryException();
//
//		try {
//			users = jpc.retrieveUsersByFilter(null, -1, -1);
//			Assert.fail("Exception expected: testRetrieveUsersByFilter");
//		} catch (DBException e) {
//		}
//		
//		String queryBase = "SELECT id, roles, quota_db, current_size_db, quota_file, current_size_file FROM admin.users";
//		String query = queryBase + " ORDER BY id";
//
//		List<String> columnNames = new ArrayList<String>(Arrays.asList(new String[]{"id", "roles", "quota_db", "current_size_db", "quota_file", "current_size_file"}));
//		String[] rowData = new String[]{userid, ""+role, ""+quotaDb, ""+currentSizeDb, ""+quotaFiles, ""+currentSizeFiles};
//		DummyData insertData = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(query, insertData);
//
//		users = jpc.retrieveUsersByFilter(null, -1, -1);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("No data", 1, users.size());
//		
//		UserDetails ud = users.get(0);
//		Assert.assertEquals("id", userid, ud.getId());
//		Assert.assertEquals("roles", role, ud.getRoles());
//		Assert.assertEquals("quota db", quotaDb, ud.getQuotaDb());
//		Assert.assertEquals("quota files", quotaFiles, ud.getQuotaFile());
//		Assert.assertEquals("current size db", currentSizeDb, ud.getCurrentSizeDb());
//		Assert.assertEquals("current size files", currentSizeFiles, ud.getCurrentSizeFile());
//		
//		
//		offset = 5;
//		limit = 15;
//		
//		query = queryBase + " ORDER BY id OFFSET "+offset+" LIMIT "+limit;
//		dbconnection.setDataForQuery(query, insertData);
//
//		users = jpc.retrieveUsersByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("No data", 1, users.size());
//		
//		ud = users.get(0);
//		Assert.assertEquals("id", userid, ud.getId());
//		Assert.assertEquals("roles", role, ud.getRoles());
//		Assert.assertEquals("quota db", quotaDb, ud.getQuotaDb());
//		Assert.assertEquals("quota files", quotaFiles, ud.getQuotaFile());
//		Assert.assertEquals("current size db", currentSizeDb, ud.getCurrentSizeDb());
//		Assert.assertEquals("current size files", currentSizeFiles, ud.getCurrentSizeFile());
//		
//		filter.setFilterById(userid);
//		query = queryBase + " WHERE (id ILIKE '%"+userid+"%') ORDER BY id OFFSET "+offset+" LIMIT "+limit;
//		
//		dbconnection.setDataForQuery(query, insertData);
//
//		users = jpc.retrieveUsersByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("No data", 1, users.size());
//		
//		ud = users.get(0);
//		Assert.assertEquals("id", userid, ud.getId());
//		Assert.assertEquals("roles", role, ud.getRoles());
//		Assert.assertEquals("quota db", quotaDb, ud.getQuotaDb());
//		Assert.assertEquals("quota files", quotaFiles, ud.getQuotaFile());
//		Assert.assertEquals("current size db", currentSizeDb, ud.getCurrentSizeDb());
//		Assert.assertEquals("current size files", currentSizeFiles, ud.getCurrentSizeFile());
//	}
	
	
//	@Test
//	public void testRetrieveJobsByFilter() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		List<JobDetails> jobs;
//		long offset = -1;
//		long limit = -1;
//		JobsFilter filter = new JobsFilter();
//		
//		String jobid = "jobid";
//		String ownerid = "onwerid";
//		String phaseid = "phaseid";
//		String executedQuery = "query";
//		String relativePath = "path";
//		long startTime = 111;
//		long endTime = 222;
//		
//		dbconnection.setGenerateNonQueryException();
//
//		try {
//			jobs = jpc.retrieveJobsByFilter(null, -1, -1);
//			Assert.fail("Exception expected: testRetrieveJobsByFilter");
//		} catch (DBException e) {
//		}
//		
//		String queryBase = "SELECT " +
//				"j.job_id, j.owner_id, j.phase_id, j.start_time, j.end_time, j.relative_path, p.string_representation FROM " +
//				"uws_schema.jobs AS j, uws_schema.job_parameters p WHERE " +
//				"(j.job_id = p.job_id) AND (p.parameter_id = 'query')";
//		
//		String query = queryBase + " ORDER BY j.owner_id";
//
//		List<String> columnNames = new ArrayList<String>(Arrays.asList(new String[]{
//				"j.job_id", "j.owner_id", "j.phase_id", "j.start_time", "j.end_time", "j.relative_path", "p.string_representation"}));
//		String[] rowData = new String[]{jobid, ownerid, phaseid, ""+startTime, ""+endTime, relativePath, executedQuery};
//		DummyData insertData = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(null, -1, -1);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		JobDetails jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//		
//		
//		offset = 5;
//		limit = 15;
//		
//		query = queryBase + " ORDER BY j.owner_id OFFSET "+offset+" LIMIT "+limit;
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//		
//		filter.setFilterByJobId(jobid, false);
//		query = queryBase + " AND (j.job_id = '"+jobid+"') ORDER BY j.owner_id OFFSET "+offset+" LIMIT "+limit;
//		
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//		
//		filter.setFilterByJobId(jobid, true);
//		filter.setFilterByOwnerId(ownerid, true);
//		filter.setFilterByPhaseId(phaseid, false);
//		filter.setFilterByQuery(executedQuery, true);
//		filter.setFilterByStartTime(startTime);
//		filter.setFilterByEndTime(endTime);
//		
//		query = queryBase + " AND (j.job_id ILIKE '%"+jobid+"%') " +
//				"AND (j.owner_id ILIKE '%" + ownerid + "%') " +
//				"AND (j.phase_id = '" + phaseid + "') " +
//				"AND (p.string_representation ILIKE '%" + executedQuery + "%') " +
//				"AND (j.start_time >= " + startTime + ") " +
//				"AND (j.end_time >= " + endTime + ") " +
//				"ORDER BY j.owner_id OFFSET "+offset+" LIMIT "+limit;
//		
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//		
//		filter = new JobsFilter();
//		filter.setFilterByStartTimeLimit(startTime);
//		filter.setFilterByEndTimeLimit(endTime);
//		query = queryBase + " " +
//				"AND (j.start_time <= " + startTime + ") " +
//				"AND (j.end_time <= " + endTime + ") " +
//				"ORDER BY j.owner_id OFFSET "+offset+" LIMIT "+limit;
//		
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//		
//		filter = new JobsFilter();
//		filter.setFilterByJobId(jobid, true);
//		filter.setFilterByOwnerId(ownerid, true);
//		filter.setFilterByPhaseId(phaseid, false);
//		filter.setFilterByQuery(executedQuery, true);
//		filter.setFilterByStartTime(startTime);
//		filter.setFilterByEndTime(endTime);
//		filter.setFilterByStartTimeLimit(startTime);
//		filter.setFilterByEndTimeLimit(endTime);
//		query = queryBase + " AND (j.job_id ILIKE '%"+jobid+"%') " +
//				"AND (j.owner_id ILIKE '%" + ownerid + "%') " +
//				"AND (j.phase_id = '" + phaseid + "') " +
//				"AND (p.string_representation ILIKE '%" + executedQuery + "%') " +
//				"AND (j.start_time >= " + startTime + " AND j.start_time <= " + startTime + ") " +
//				"AND (j.end_time >= " + endTime + " AND j.end_time <= " + endTime + ") " +
//				"ORDER BY j.owner_id OFFSET "+offset+" LIMIT "+limit;
//		
//		dbconnection.setDataForQuery(query, insertData);
//
//		jobs = jpc.retrieveJobsByFilter(filter, offset, limit);
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		Assert.assertEquals("One row", 1, jobs.size());
//		
//		jd = jobs.get(0);
//		Assert.assertEquals("job id", jobid, jd.getJobid());
//		Assert.assertEquals("owner id", ownerid, jd.getOwnerid());
//		Assert.assertEquals("phase id", phaseid, jd.getPhaseid());
//		Assert.assertEquals("query", executedQuery, jd.getQuery());
//		Assert.assertEquals("relative path", relativePath, jd.getRelativePath());
//		Assert.assertEquals("start time", startTime, jd.getStartTime());
//		Assert.assertEquals("end time", endTime, jd.getEndTime());
//	}

//	@Test
//	public void testLoadJobDetails() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		String jobid = "jobid";
//		String ownerid = "ownerid";
//		String phaseid = "phaseid";
//		long startTime = 11;
//		long endTime = 22;
//		String relativePath = "path";
//		
//		dbconnection.setGenerateNonQueryException();
//		
//		try {
//			jpc.loadJobDetails(jobid);
//			Assert.fail("Exception expected: testLoadJobDetails");
//		} catch (DBException e) {
//		}
//		
//		String query = "SELECT job_id, owner_id, phase_id, start_time, end_time, relative_path FROM uws_schema.jobs WHERE job_id = '" + jobid + "'";
//		DummyData data = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, data);
//
//		try{
//			jpc.loadJobDetails(jobid);
//			Assert.fail("Excepction expected: no job found.");
//		}catch (DBException e){
//		}
//		
//		Assert.assertTrue("Executed query: '" + query + "'", dbconnection.isExecutedQuery(query));
//		
//		//Put data
//		List<String> columnNames = new ArrayList<String>(Arrays.asList(new String[]{"job_id", "owner_id", "phase_id", "start_time", "end_time", "relative_path"}));
//		String[] rowData = new String[]{jobid, ownerid, phaseid, ""+startTime, ""+endTime, relativePath};
//		data = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(query, data);
//
//		List<String> queries = new ArrayList<String>();
//		queries.add(query);
//		String queryParam = "SELECT parameter_id, parameter_type, data_type, string_representation FROM uws_schema.job_parameters WHERE job_id = '"	+ jobid + "'";
//		queries.add(queryParam);
//		dbconnection.setDataForQuery(queryParam, new DummyData());
//		String queryResults = "SELECT result_id, href, type, mime_type, size, rows FROM uws_schema.results WHERE job_id = '" + jobid + "'";
//		queries.add(queryResults);
//		dbconnection.setDataForQuery(queryResults, new DummyData());
//		String queryError = "SELECT message, type, details FROM uws_schema.error_summary WHERE job_id = '" + jobid + "'";
//		queries.add(queryError);
//		dbconnection.setDataForQuery(queryError, new DummyData());
//
//		
//		JobDetails jobDetails = jpc.loadJobDetails(jobid);
//		for(String q: queries){
//			Assert.assertTrue("Executed query: '" + q + "'", dbconnection.isExecutedQuery(query));
//		}
//		Assert.assertEquals("Job", jobid, jobDetails.getJobid());
//		Assert.assertEquals("Owner", ownerid, jobDetails.getOwnerid());
//		Assert.assertEquals("Phase", phaseid, jobDetails.getPhaseid());
//		Assert.assertEquals("Start time", startTime, jobDetails.getStartTime());
//		Assert.assertEquals("End time", endTime, jobDetails.getEndTime());
//		Assert.assertEquals("Relative path", relativePath, jobDetails.getRelativePath());
//		Assert.assertNull("Parameters", jobDetails.getParameters());
//		Assert.assertNull("Results", jobDetails.getResults());
//		Assert.assertNull("Errors", jobDetails.getError());
//		
//		String paramId = "pid";
//		String paramType = "ptype";
//		String paramDataType = "pdtpye";
//		String paramStringRep = "psr";
//		columnNames = new ArrayList<String>(Arrays.asList(new String[]{"parameter_id", "parameter_type", "data_type", "string_representation"}));
//		rowData = new String[]{paramId, paramType, paramDataType, paramStringRep};
//		data = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(queryParam, data);
//		
//		String resultId = "rid";
//		String resultHref = "rhref";
//		String resultType = "rtype";
//		String resultMimeType = "rmt";
//		long resultSize = 100;
//		long resultRows = 1000;
//		columnNames = new ArrayList<String>(Arrays.asList(new String[]{"result_id", "href", "type", "mime_type", "size", "rows"}));
//		rowData = new String[]{resultId, resultHref, resultType, resultMimeType, ""+resultSize, ""+resultRows};
//		data = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(queryResults, data);
//		
//		String errorMsg = "emsg";
//		String errorDetails = "edetails";
//		String errorType = "etype";
//		columnNames = new ArrayList<String>(Arrays.asList(new String[]{"message", "type", "details"}));
//		rowData = new String[]{errorMsg, errorType, errorDetails};
//		data = DatabaseUtils.createSingleRowDummyData(columnNames, rowData);
//		dbconnection.setDataForQuery(queryError, data);
//		
//		
//		jobDetails = jpc.loadJobDetails(jobid);
//		for(String q: queries){
//			Assert.assertTrue("Executed query: '" + q + "'", dbconnection.isExecutedQuery(query));
//		}
//		Assert.assertEquals("Job", jobid, jobDetails.getJobid());
//		Assert.assertEquals("Owner", ownerid, jobDetails.getOwnerid());
//		Assert.assertEquals("Phase", phaseid, jobDetails.getPhaseid());
//		Assert.assertEquals("Start time", startTime, jobDetails.getStartTime());
//		Assert.assertEquals("End time", endTime, jobDetails.getEndTime());
//		Assert.assertEquals("Relative path", relativePath, jobDetails.getRelativePath());
//		
//		List<JobDetailsParameter> parameters = jobDetails.getParameters();
//		Assert.assertEquals("Num params", 1, parameters.size());
//		JobDetailsParameter jdp = parameters.get(0);
//		Assert.assertEquals("Param id", paramId, jdp.getId());
//		Assert.assertEquals("Param type", paramType, jdp.getParameterType());
//		Assert.assertEquals("Param data type", paramDataType, jdp.getDataType());
//		Assert.assertEquals("Param string rep", paramStringRep, jdp.getStringRepresentation());
//		
//		List<JobDetailsResults> results = jobDetails.getResults();
//		Assert.assertEquals("Num results", 1, results.size());
//		JobDetailsResults jdr = results.get(0);
//		Assert.assertEquals("Result id", resultId, jdr.getId());
//		Assert.assertEquals("Result href", resultHref, jdr.getHref());
//		Assert.assertEquals("Result type", resultType, jdr.getType());
//		Assert.assertEquals("Result mime type", resultMimeType, jdr.getMimeType());
//		Assert.assertEquals("Result size", resultSize, jdr.getSize());
//		Assert.assertEquals("Result rows", resultRows, jdr.getRows());
//		
//		JobDetailsError jde = jobDetails.getError();
//		Assert.assertEquals("Error msg", errorMsg, jde.getErrorSummaryMessage());
//		Assert.assertEquals("Error details", errorDetails, jde.getErrorSummaryDetails());
//		Assert.assertEquals("Error type", errorType, jde.getErrorSummaryType());
//	}
	
//	@Test
//	public void testUpdateUserDetails() throws Exception {
//		DummyDatabaseConnection dbconnection = new DummyDatabaseConnection();
//		JDBCPoolSingleton.setConnection(dbconnection);
//		//TAPSchemaInfo tapSchemaInfo = new TAPSchemaInfo();
//		//String appid = "appid_test";
//		//DummyTapDatabaseConnection tapdbconnection = new DummyTapDatabaseConnection(dbconnection);
//		//DummyTapServiceConnection<ResultSet> service = new DummyTapServiceConnection<ResultSet>(appid, tapdbconnection);
//		//service.setTapSchemaInfo(tapSchemaInfo);
//		DummyTapServiceConnection service = new DummyTapServiceConnection(TEST_APP_ID, true);
//		JDBCPooledConnection jpc = new JDBCPooledConnection(service);
//		
//		String userid = "userid";
//		int roles = 1;
//		long quotaDb = 111;
//		long quotaFiles = 222;
//		
//		dbconnection.setGenerateNonQueryException();
//		
//		try {
//			jpc.updateUserDetails(userid, roles, quotaDb, quotaFiles);
//			Assert.fail("Exception expected: testUpdateUserDetails");
//		} catch (DBException e) {
//		}
//		
//		String query = "UPDATE admin.users SET roles = "+roles+", quota_db = "+quotaDb+", quota_file = "+quotaFiles+" WHERE id = '"+userid+"'";
//		
//		DummyData insertData = DatabaseUtils.createSingleInsertOrUpdateData();
//		dbconnection.setDataForQuery(query, insertData);
//
//		jpc.updateUserDetails(userid, roles, quotaDb, quotaFiles);
//		Assert.assertTrue("Executed query: " + query, dbconnection.isExecutedQuery(query));
//	}

}
