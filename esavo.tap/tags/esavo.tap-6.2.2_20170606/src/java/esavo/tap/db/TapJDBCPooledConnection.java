package esavo.tap.db;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import esavo.adql.query.ADQLQuery;
import esavo.tap.TAPException;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.TapUtils;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TAPTypes;
import esavo.tap.metadata.TapTableInfo;
import esavo.tap.publicgroup.PublicGroupItem;
import esavo.tap.sql.TapSql;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

public class TapJDBCPooledConnection implements TapJDBCPooledFunctions {

	public static final int RESULTS_FETCH_SIZE = 8192;
	
	private Connection dbConn = null;
	private long timeOut = -1;
	
	private Statement stmt = null;
	
	private TAPService service;

	public TapJDBCPooledConnection(TAPService service, Connection dbConn, long timeOut) throws TAPException {
		this.service = service;
		try {
			this.dbConn = dbConn;
			this.timeOut = timeOut;
			dbConn.createStatement().execute("SET statement_timeout TO " + timeOut);
		} catch (SQLException se) {
			throw new TAPException(
					"Impossible to establish a connection to the database "+se.getMessage(), se);
		}
	}
	
	private PreparedStatement createInsertIntoTapSchemaSchemasPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
		PreparedStatement insertIntoTapSchemaSchemas = dbConn.prepareStatement(
				"INSERT INTO " + tapSchemas + " (schema_name, description, utype) values (?,?,?)");
		return insertIntoTapSchemaSchemas;
	}
	
	private PreparedStatement createInsertIntoTapSchemaTablesPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		PreparedStatement insertIntoTapSchemaTables = dbConn.prepareStatement(
				"INSERT INTO "+tapTables+" (schema_name, table_name, table_type, description, utype, size, flags) values (?,?,?,?,?,?,?)");
		return insertIntoTapSchemaTables;
	}
	
	private PreparedStatement createInsertIntoTapSchemaColumnsPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns
		PreparedStatement insertIntoTapSchemaColumns = dbConn.prepareStatement(
				"INSERT INTO "+tapColumns+" (schema_name, table_name, column_name, description, unit, ucd, utype, datatype, size, principal, indexed, std, flags, pos) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		return insertIntoTapSchemaColumns;
	}

	private PreparedStatement createDeleteFromTapSchemaTablesPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		PreparedStatement deleteFromTapSchemaTables = dbConn.prepareStatement(
				"DELETE FROM "+tapTables+" WHERE schema_name=? AND table_name=?");
		return deleteFromTapSchemaTables;
	}
	private PreparedStatement createDeleteFromTapSchemaColumnsPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns
		PreparedStatement deleteFromTapSchemaColumns = dbConn.prepareStatement(
				"DELETE FROM "+tapColumns+" WHERE schema_name=? AND table_name=?");
		return deleteFromTapSchemaColumns;
	}
	private PreparedStatement createDeleteFromTapSchemaSchemasPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
		PreparedStatement deleteFromTapSchemaSchemas = dbConn.prepareStatement(
				"DELETE FROM "+tapSchemas+" WHERE schema_name=?");
		return deleteFromTapSchemaSchemas;
	}
	
	private PreparedStatement createUpdateSchemaStatusPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
		PreparedStatement updateSchemaStatus = dbConn.prepareStatement(
				"UPDATE "+tapSchemas+" SET public = ? WHERE schema_name = ?");
		return updateSchemaStatus;
	}
	private PreparedStatement createUpdateTableStatusPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		PreparedStatement updateTableStatus  = dbConn.prepareStatement(
				"UPDATE "+tapTables+" SET public = ? WHERE schema_name = ? AND table_name = ?");
		return updateTableStatus;
	}
	private PreparedStatement createUpdateColumnStatusPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns
		PreparedStatement updateColumnStatus = dbConn.prepareStatement(
				"UPDATE "+tapColumns+" SET public = ? WHERE schema_name = ? AND table_name = ?");
		return updateColumnStatus;
	}

	private PreparedStatement createGetSchemaInfoPS(TAPSchemaInfo tapSchemaInfo) throws SQLException{
		String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
		PreparedStatement getSchemaInfo = dbConn.prepareStatement(
				"SELECT description, utype, public, title, db_schema_name FROM "+tapSchemas+" WHERE schema_name = ?");
		return getSchemaInfo;
	}

	@Override
	public void startTransaction() throws DBException {
		try {
			dbConn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DBException("Impossible to start transaction, because: "
					+ e.getMessage());
		}
	}

	@Override
	public void cancelTransaction() throws DBException {
				
		try {
			if(stmt != null){
				stmt.cancel();
			}
			dbConn.rollback();
		} catch (SQLException e) {
			throw new DBException("Impossible to cancel transaction, because: "
					+ e.getMessage());
		}finally{
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new DBException("Impossible to reset autocommit to true, because: "
						+ e.getMessage());
			}
		}
	}

	@Override
	public void endTransaction() throws DBException {
		try {
			dbConn.commit();
		} catch (SQLException e) {
			throw new DBException("Impossible to commit transaction, because: "
					+ e.getMessage());
		}finally{
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new DBException("Impossible to reset autocommit to true, because: "
						+ e.getMessage());
			}
		}
	}
	
	@Override
	public boolean isTransactionFinished() throws DBException{
		try {
			return dbConn.getAutoCommit();
		} catch (SQLException e) {
			throw new DBException("Impossible to see transaction status, because: "
					+ e.getMessage());
		}
	}

	@Override
	public void close() throws DBException {
		try {
			//System.out.println("JDBCPooledConnection: Closing connection.");
			dbConn.close();
		} catch (SQLException e) {
			throw new DBException(
					"Impossible to close DB connection, because: "
							+ e.getMessage());
		}
	}

	@Override
	public ResultSet executeQuery(String sql, ADQLQuery query)
			throws DBException {
		try {
			stmt = dbConn.createStatement();
			if (!dbConn.getAutoCommit()) {
				stmt.setFetchSize(RESULTS_FETCH_SIZE);
			}
			return stmt.executeQuery(sql);
		} catch (SQLException se) {
			se.printStackTrace();
			throw new DBException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage());
		}
	}

	/* ************************************** */
	/* METHODS USED ONLY IF UPLOAD IS ENABLED */
	/* ************************************** */

	@Override
	public void createSchema(String arg0) throws DBException {
		try {
			dbConn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS "+arg0);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + "CREATE SCHEMA IF NOT EXISTS "+arg0
					+ "\n. Because: " + se.getMessage());
		}
		// TODO ONLY IF upload is enabled !
	}
	
	
	/**
	 * Add a schema to tap_schema. Create entries in tap_schema.schema.
	 * @param tapSchema
	 * @throws DBException
	 */
	@Override
	public void registerInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPSchema tapSchema) throws DBException {
		/// Test if schema exists
		try {
			PreparedStatement insertIntoTapSchemaSchemas = createInsertIntoTapSchemaSchemasPS(tapSchemaInfo);
			// If schema is not registered, register it.
			String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
			ResultSet result = dbConn.createStatement().executeQuery(
					"SELECT schema_name FROM " + tapSchemas +
					" WHERE schema_name = '" + tapSchema.getADQLName() + "'");
			if(!result.next()){
				insertIntoTapSchemaSchemas.setString(1, tapSchema.getADQLName());
				
				if(tapSchema.getDescription()!=null)
					insertIntoTapSchemaSchemas.setString(2, tapSchema.getDescription());
				else
					insertIntoTapSchemaSchemas.setNull(2, java.sql.Types.VARCHAR);
				
				if(tapSchema.getUtype()!=null)
					insertIntoTapSchemaSchemas.setString(3, tapSchema.getUtype());
				else
					insertIntoTapSchemaSchemas.setNull(3, java.sql.Types.VARCHAR);
				
				insertIntoTapSchemaSchemas.executeUpdate();
			}
			
		} catch (SQLException se) {
			throw new DBException("Can not register table in "+tapSchemaInfo.getTapSchemaName()+": \n" 
					+ "\n. Because: " + se.getMessage());
		}

	}

	/**
	 * Add a table to tap_schema. Create entries in tap_schema.schema, tap_schema.tables and tap_schema.columns.
	 * @param tapTable
	 * @throws DBException
	 */
	@Override
	public void registerInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable tapTable) throws DBException {
		registerInTapSchema(tapSchemaInfo, tapTable.getSchema());
		
		try {
			// Unregister table from tap_schema
			// Delete table information from tap_schema
			unregisterFromTapSchema(tapSchemaInfo, tapTable);
			
			PreparedStatement insertIntoTapSchemaTables = createInsertIntoTapSchemaTablesPS(tapSchemaInfo);
			
			// Insert table in tap_schema
			insertIntoTapSchemaTables.setObject(1, tapTable.getSchema().getADQLName(), java.sql.Types.VARCHAR);
			insertIntoTapSchemaTables.setObject(2, tapTable.getADQLName(), java.sql.Types.VARCHAR);
			insertIntoTapSchemaTables.setObject(3, tapTable.getType(), java.sql.Types.VARCHAR);
			insertIntoTapSchemaTables.setObject(4, tapTable.getDescription(), java.sql.Types.VARCHAR);
			insertIntoTapSchemaTables.setObject(5, tapTable.getUtype(), java.sql.Types.VARCHAR);
			insertIntoTapSchemaTables.setObject(6, 0, java.sql.Types.INTEGER);
			insertIntoTapSchemaTables.setObject(7, tapTable.getFlags(), java.sql.Types.INTEGER);
			insertIntoTapSchemaTables.execute();

			// Insert columns information in tap_schema
			
			// Add OID column
			TAPColumn oidColumn = new TAPColumn(tapTable.getName()+"_oid", "Object Identifier", "", "", "");
			oidColumn.setDatatype(TAPTypes.INTEGER,TAPTypes.NO_SIZE);
			oidColumn.setIndexed(true);
			oidColumn.setPrincipal(true);
			
			// Include OID column in original columns list
			List<TAPColumn> columns = tapTable.getColumnsList();
			columns.add(oidColumn);
			Iterator<TAPColumn> columnsIt = columns.iterator();
			
			PreparedStatement insertIntoTapSchemaColumns = createInsertIntoTapSchemaColumnsPS(tapSchemaInfo);
			
			while(columnsIt.hasNext()){
				TAPColumn col = columnsIt.next();
				insertIntoTapSchemaColumns.setObject(1, tapTable.getSchema().getADQLName(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(2, tapTable.getADQLName(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(3, col.getADQLName(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(4, col.getDescription(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(5, col.getUnit(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(6, col.getUcd(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(7, col.getUtype(), java.sql.Types.VARCHAR);
				insertIntoTapSchemaColumns.setObject(8, col.getDatatype(), java.sql.Types.VARCHAR);
				Integer arraySize = TAPTypes.getColumnArraySize(col.getDatatype(), col.getArraySize());
				insertIntoTapSchemaColumns.setObject(9, arraySize, java.sql.Types.INTEGER);
				
				Integer aux = (col.isPrincipal())?new Integer(1):new Integer(0);
				insertIntoTapSchemaColumns.setObject(10, aux, java.sql.Types.INTEGER);

				aux = (col.isIndexed())?new Integer(1):new Integer(0);
				insertIntoTapSchemaColumns.setObject(11, aux, java.sql.Types.INTEGER);
				
				aux = (col.isStd())?new Integer(1):new Integer(0);
				insertIntoTapSchemaColumns.setObject(12, aux, java.sql.Types.INTEGER);
				
				insertIntoTapSchemaColumns.setObject(13, col.getFlags(), java.sql.Types.INTEGER);
				
				insertIntoTapSchemaColumns.setObject(14, col.getPos(), java.sql.Types.INTEGER);
				
				insertIntoTapSchemaColumns.execute();
			}
			
			
			
		} catch (SQLException se) {
			throw new DBException("Can not register table in "+tapSchemaInfo.getTapSchemaName()+": \n" 
					+ "\n. Because: " + se.getMessage());
		}

	}
	

	/**
	 * Delete a table from tap_schema. Delete entries from tap_schema.schema, tap_schema.tables and tap_schema.columns.
	 * @param tapTable
	 * @throws DBException
	 */
	@Override
	public void unregisterFromTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable tapTable) throws DBException {
		/// Test if schema exists
		try {
			// Delete table information from tap_schema 
			PreparedStatement deleteFromTapSchemaColumns = createDeleteFromTapSchemaColumnsPS(tapSchemaInfo);
			
			deleteFromTapSchemaColumns.setString(1, tapTable.getSchema().getDBName());
			deleteFromTapSchemaColumns.setString(2, tapTable.getDBName());
			
			PreparedStatement deleteFromTapSchemaTables = createDeleteFromTapSchemaTablesPS(tapSchemaInfo);

			deleteFromTapSchemaTables.setString(1, tapTable.getSchema().getDBName());
			deleteFromTapSchemaTables.setString(2, tapTable.getDBName());
			
			deleteFromTapSchemaColumns.executeUpdate();
			deleteFromTapSchemaTables.executeUpdate();
			
		} catch (SQLException se) {
			throw new DBException("Can not register table in "+tapSchemaInfo.getTapSchemaName()+": \n" 
					+ "\n. Because: " + se.getMessage());
		}

	}

	@Override
	public void dropSchema(String arg0) throws DBException {
		try {
			dbConn.createStatement().execute("DROP SCHEMA IF EXISTS "+arg0+" CASCADE");
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + "DROP SCHEMA "+arg0
					+ "\n. Because: " + se.getMessage());
		}
	}

	@Override
	public void createTable(TAPTable table, String tableSpace) throws DBException {
		
		// TODO ONLY IF upload is enabled !
				
		String sql = "CREATE TABLE "+table.getDBSchemaName()+"."+table.getDBName()+" ( ";
		String join = "";
		
		Iterator<TAPColumn> columns = table.getColumns();
		while(columns.hasNext()){
			TAPColumn col = (TAPColumn)columns.next();
			sql+= join+col.getDBName()+" ";
//			if(TAPTypes.checkVarBinaryRequired(col.getDatatype(), col.getArraySize())){
//				//TODO function to obtain the array data type required
//				sql += "bytea";
//			}else{
//				sql += TAPTypes.getDBType(col.getDatatype());
//			}
			sql += TapUtils.getDbType(col);
			join=", ";
		}
		
		//Add OID column
		sql+=join+" "+table.getDBName()+"_oid SERIAL";
		sql+=" ) ";
		
		if(tableSpace != null && !"".equals(tableSpace.trim())){
			sql += "TABLESPACE " + tableSpace;
		}
		
		try {
			dbConn.createStatement().execute(sql);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage());
		}
	}
	
	
	/**
	 * Obtains the database size used by the given owner (in bytes)
	 * @param ownerid
	 * @return
	 * @throws SQLException
	 */
	@Override
	public long getDbSize(String ownerid) throws DBException {
		
		try{
			PreparedStatement statement = dbConn.prepareStatement("SELECT uws2_schema.db_user_usage(?)");
			statement.setString(1, ownerid);
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				long value = rs.getLong("db_user_usage");
				statement.close();
				return value;
			}else{
				statement.close();
				throw new DBException("Owner id '"+ownerid+"' not found");
			}
		}catch(SQLException e){
			throw new DBException(e);
		}

	}
	
	/**
	 * Obtains the database size used by the given table (in bytes)
	 * @param ownerid
	 * @return
	 * @throws SQLException
	 */
	@Override
	public long getTableSize(String schema, String table) throws DBException {
		
		try{
			PreparedStatement statement = dbConn.prepareStatement("SELECT uws2_schema.db_table_usage(?,?)");
			statement.setString(1, schema);
			statement.setString(2, table);
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				long value = rs.getLong("db_table_usage");
				statement.close();
				return value;
			}else{
				statement.close();
				throw new DBException("Table '"+schema+"."+table+"' not found");
			}
		}catch(SQLException e){
			throw new DBException(e);
		}

	}
	
	@Override
	public void dropTable(TAPTable table) throws DBException {
		dropTable(table, false);
	}


	@Override
	public void dropTable(TAPTable table, boolean forceRemoval) throws DBException {
		String sql = "DROP TABLE "+table.getDBSchemaName()+"."+table.getDBName();
		if(forceRemoval){
			sql += " CASCADE";
		}
		try {
			dbConn.createStatement().execute(sql);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage());
		}
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable) throws DBException, UwsException {
		return loadTableData(owner, table, starTable, -1);
	}

	@Override
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable, long taskid) throws DBException, UwsException {
		UwsQuotaSingleton quotaSingleton = UwsQuotaSingleton.getInstance(); 
		UwsQuota quota = quotaSingleton.createOrLoadQuota(owner);
		
		int sqlType;
		Iterator<TAPColumn> columns;
		
		int totalRows = (int)starTable.getRowCount();
		int nbRows = 0;
		int percent = 0;
		
		String sql = "INSERT INTO "+table.getDBSchemaName()+"."+table.getDBName()+" VALUES ( ";
		
		columns = table.getColumns();
		boolean firstTime = true;
		TAPColumn c;
		while(columns.hasNext()){
			c = columns.next();
			sqlType = TAPTypes.getEffectiveSQLType(c);
			if(firstTime){
				firstTime = false;
			}else{
				sql += ", ";
			}
			sql += getSuitablePreparedStatementArg(sqlType);
		}
		
		sql+=" ) ";

		PreparedStatement insertRowStatement=null;
		try {
			insertRowStatement = dbConn.prepareStatement(sql);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage());
		}
		
		
		
		RowSequence rSeq;
		String tableDbName = table.getDBName();
		String schemaName = table.getDBSchemaName().toLowerCase();
		try {
			rSeq = starTable.getRowSequence();
			
			long prevTableSize = 0;
			//Object oTmp;
			while ( rSeq.next() ) {
				Object[] row = rSeq.getRow();
				insertRowStatement.clearParameters();
				
				columns = table.getColumns();
				int i=0;
				while(columns.hasNext()){
					c = columns.next();
					sqlType = TAPTypes.getEffectiveSQLType(c);
					insertSuitableValue(i, row[i], sqlType, insertRowStatement);
					i++;
				}
				insertRowStatement.addBatch();
				
				if(nbRows%5000==0){
					insertRowStatement.executeBatch();
					insertRowStatement.clearBatch();
					
					// Check DB quota
					long newTableSize = getTableSize(schemaName, tableDbName);
					long deltaTableSize = newTableSize-prevTableSize;
					quota.addDbSize(deltaTableSize);
					prevTableSize = newTableSize;
					
					
					percent = (int)100*nbRows/totalRows;
					
					UwsStatusData statusIngestion = new UwsStatusData(UwsStatusData.TYPE_INGESTION, ""+percent);
					if(taskid >= 0){
						try{
							UwsStatusManager.getInstance().updateStatus(taskid, statusIngestion); 
						} catch (IllegalArgumentException iae){
							iae.printStackTrace();
						}
					}

				}
				nbRows++;
	        }
			insertRowStatement.executeBatch();
			insertRowStatement.clearBatch();
			
			// Check DB quota
			long newTableSize = getTableSize(schemaName, tableDbName);
			long deltaTableSize = newTableSize-prevTableSize;
			
			quota.addDbSize(deltaTableSize);
			
			prevTableSize = newTableSize;

			insertRowStatement.close();

			return nbRows;
		} catch (SQLException e) {
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new DBException("Error creating user table: "+e.getMessage(), e);
		} catch (QuotaException e) {
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new UwsException(e.getMessage(), e);
		} catch (IOException e) {
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new DBException("Error creating user table: "+e.getMessage(), e);
		}
	}
	
	private void restoreDbSize(UwsQuota quota, String schemaName, String tableDbName, Exception e) throws UwsException {
		long newTableSize = 0;
		try {
			newTableSize = getTableSize(schemaName, tableDbName);
		} catch (DBException ex) {
			throw new UwsException(e.getMessage() + "\nWARNING: Cannot restore db quota: " + ex.getMessage(), e);
		}
		quota.reduceDbSize(newTableSize);
	}
	
	private String getSuitablePreparedStatementArg(int sqlType){
		if(sqlType == java.sql.Types.TIMESTAMP){
			return "CAST(? AS timestamp)";
		}else{
			return "?";
		}
	}

	private void insertSuitableValue(int index, Object value, int sqlType, PreparedStatement st) throws SQLException{
		if(value == null){
			st.setNull(index+1, sqlType);
			return;
		}
		if(sqlType == java.sql.Types.TIMESTAMP){
			if(value == null || value.toString().isEmpty()){
				st.setNull(index+1, sqlType);
			}else{
				st.setString(index+1, value.toString());
			}
		}else if(sqlType == java.sql.Types.VARBINARY){
			//STIL returns an array of short's when they contains bytes actually...
			byte[] b = getBytesFromObject(value);
			st.setBytes(index+1, b);
		}else{
			st.setObject(index+1, value, sqlType);
		}
	}
	
	/**
	 * PATCH: for working with STIL with arrays. 
	 * STILT always returns a short[] array when (datatype='unsignedByte' arraysize='*') is found
	 * @param obj
	 * @return
	 * @throws SQLException
	 */
	private byte[] getBytesFromObject(Object obj) throws SQLException{
		if(obj instanceof short[]){
			short[] sArray = (short[])obj;
			byte[] bArray = new byte[sArray.length];
			short s;
			for(int i = 0; i < bArray.length; i++){
				s = sArray[i];
				bArray[i] = (byte)s;
			}
			return bArray;
		}else{
			throw new SQLException("Invalid value class. Expected short[], found: " + obj.getClass().getName());
		}
	}
	
	@Override
	public String createPkInTable(TAPSchemaInfo tapSchemaInfo, TAPTable table) throws DBException {
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns
		String key = table.getDBName()+"_oid";
		String query1 = "ALTER TABLE "+table.getDBSchemaName()+"."+table.getDBName()+
		" ADD CONSTRAINT "+table.getDBName()+"_pk PRIMARY KEY ("+key+") ";
		String query2 = "UPDATE "+tapColumns+" SET flags = " + TapUtils.TAP_COLUMN_TABLE_FLAG_PK + 
				" WHERE schema_name = '"+table.getDBSchemaName()+"' AND " +
				" table_name = '"+table.getDBName()+"' AND column_name = '"+key+"'";
		List<String> queries = new ArrayList<String>();
		queries.add(query1);
		queries.add(query2);
		for(String q: queries){
			try {
				dbConn.createStatement().execute(q);
			} catch (SQLException se) {
				throw new DBException("Can not execute the following SQL: \n" + q
					+ "\n. Because: " + se.getMessage());
			}
		}
		return key;
	}


	@Override
	public void createRaAndDecIndexes(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, String raCol, String decCol, int raDecFlag, String tableSpace) throws DBException {
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns

		String fullName = schemaName + "." + tableName;
		
		List<String> queries = new ArrayList<String>();
		
		String indexName = getRaDecIndexName(tableName);
		
		String tableSpaceCommand = null;
		if(tableSpace != null && !"".equals(tableSpace.trim())){
			tableSpaceCommand = " TABLESPACE " + tableSpace;
		}
		queries.add("CREATE INDEX " +  indexName + " ON "+ fullName + " USING btree (q3c_ang2ipix("+raCol+", "+decCol+"))" + 
				(tableSpaceCommand != null ? tableSpaceCommand: "") + ";");
		queries.add("ALTER TABLE " + fullName +	" ALTER COLUMN " + raCol + " SET NOT NULL");
		queries.add("ALTER TABLE " + fullName + " ALTER COLUMN " + decCol + " SET NOT NULL");		
		queries.add("ALTER TABLE " + fullName + " ADD CONSTRAINT "+tableName+"_ra_check_nan CHECK ( NOT "+raCol+" = 'NaN')");
		queries.add("ALTER TABLE " + fullName + " ADD CONSTRAINT "+tableName+"_dec_check_nan CHECK ( NOT "+decCol+" = 'NaN')");
		queries.add("UPDATE "+tapColumns+" SET indexed = 1 WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+
				"' AND (column_name = '"+raCol+"' OR column_name = '"+decCol+"')");
		queries.add("UPDATE "+tapTables+" SET flags = flags | " + raDecFlag + " WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"'");

		for(String q:queries){
			try {
				dbConn.createStatement().execute(q);
			} catch (SQLException se) {
				throw new DBException("Can not execute the following SQL: \n" + q
						+ "\n. Because: " + se.getMessage());
			}
		}
	}
	
	@Override
	public void removeRaAndDecIndexes(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, String raCol, String decCol, int raDecFlag) throws DBException {
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables

		String fullName = schemaName + "." + tableName;
		
		List<String> queries = new ArrayList<String>();
		
		String indexName = getRaDecIndexName(tableName);
	    queries.add("DROP INDEX IF EXISTS "+schemaName+"."+indexName); 
        queries.add("ALTER TABLE "+fullName+" DROP CONSTRAINT "+tableName+"_ra_check_nan"); 
        queries.add("ALTER TABLE "+fullName+" DROP CONSTRAINT "+tableName+"_dec_check_nan"); 
        queries.add("ALTER TABLE "+fullName+" ALTER COLUMN "+raCol+" DROP NOT NULL");
        queries.add("ALTER TABLE "+fullName+" ALTER COLUMN "+decCol+" DROP NOT NULL");
		
        queries.add("UPDATE "+tapTables+" SET flags = flags & (~" + raDecFlag + ") WHERE "+
				"schema_name = '"+schemaName+"' AND table_name = '"+tableName+"'");

		for(String q:queries){
			try {
				dbConn.createStatement().execute(q);
			} catch (SQLException se) {
				throw new DBException("Can not execute the following SQL: \n" + q
						+ "\n. Because: " + se.getMessage());
			}
		}
	}
	


	@Override
	public void updateTableSizeInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable table) throws DBException {
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		String sqlUpdate = "UPDATE "+tapTables+" "+
		" SET size = ( SELECT count(*) FROM "+table.getDBSchemaName()+"."+table.getDBName()+
		") WHERE table_name ILIKE '"+table.getDBName()+"' AND schema_name ILIKE '"+table.getDBSchemaName()+"'";
		
		
		try {
			dbConn.createStatement().execute(sqlUpdate);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sqlUpdate
					+ "\n. Because: " + se.getMessage());
		}
	
	}
	
	
	@Override
	public void vacuumAnalyze(String schemaName, String tableName) throws DBException {
		
		String sqlVacuum = "VACUUM ANALYZE "+ schemaName+"."+tableName;

		try {
			dbConn.createStatement().execute(sqlVacuum);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sqlVacuum
					+ "\n. Because: " + se.getMessage());
		}
	}
	
	@Override
	public void createTableColumnIndex(String schemaName, String tableName, String tableColumnName) throws DBException {
		String fullName = schemaName + "." + tableName;
		String indexName = getColIndexName(tableName, tableColumnName);
		String query = "CREATE INDEX " + indexName + " ON "+ fullName + " ("+tableColumnName+")";
		try {
			dbConn.createStatement().execute(query);
		} catch (SQLException e) {
			throw new DBException("Can not execute the following SQL: \n" + query
					+ "\n. Because: " + e.getMessage());
		}
	}

	@Override
	public void removeTableColumnIndex(String schemaName, String tableName, String tableColumnName) throws DBException {
		//NOTE TAP_SCHEMA.all_columns index is not updated here. It must be updated using 'updateUserTableData'
		String indexName = getColIndexName(tableName, tableColumnName);
		String query = "DROP INDEX IF EXISTS " + schemaName + "." + indexName;
		try {
			dbConn.createStatement().execute(query);
		} catch (SQLException e) {
			throw new DBException("Can not execute the following SQL: \n" + query
					+ "\n. Because: " + e.getMessage());
		}
	}

	@Override
	public void updateUserTableData(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName,
			String tableColumnName, String ucd, String uType, int flags, int indexed) throws DBException {
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns
		
		String query = "UPDATE "+tapColumns+" SET ucd = ?, utype = ?, flags = ?, indexed = ? WHERE "+
			"schema_name = ? AND table_name = ? AND column_name = ?";
		try {
			PreparedStatement statement = dbConn.prepareStatement(query);
			statement.setObject(1, ucd, java.sql.Types.VARCHAR);
			statement.setObject(2, uType, java.sql.Types.VARCHAR);
			statement.setObject(3, flags, java.sql.Types.INTEGER);
			statement.setObject(4, indexed, java.sql.Types.INTEGER);
			statement.setString(5, schemaName);
			statement.setString(6, tableName);
			statement.setString(7, tableColumnName);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DBException("Can not execute the following SQL: \n" + query
					+ "\n. Because: " + e.getMessage());
		}
	}

	@Override
	public void updateUserTableRaDecData(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, String raColumn, String decColumn, int flagsRa, int flagsDec) throws DBException {
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns

		String query = "UPDATE "+tapColumns+" SET flags = ?, indexed = ? WHERE "+
				"schema_name = ? AND table_name = ? AND column_name = ?";
			try {
				PreparedStatement statement = dbConn.prepareStatement(query);
				statement.setObject(1, flagsRa, java.sql.Types.INTEGER);
				statement.setObject(2, 1, java.sql.Types.INTEGER);
				statement.setString(3, schemaName);
				statement.setString(4, tableName);
				statement.setString(5, raColumn);
				statement.executeUpdate();

				statement.setObject(1, flagsDec, java.sql.Types.INTEGER);
				statement.setObject(2, 1, java.sql.Types.INTEGER);
				statement.setString(3, schemaName);
				statement.setString(4, tableName);
				statement.setString(5, decColumn);
				statement.executeUpdate();
			} catch (SQLException e) {
				throw new DBException("Can not execute the following SQL: \n" + query
						+ "\n. Because: " + e.getMessage());
			}
	}
	
	@Override
	public TapTableInfo fetchTapTableInfo(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException {
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable(); //tap_sceham.all_columns

		String query = "SELECT column_name, description, ucd, utype, datatype, unit, size, principal, std, indexed, flags FROM "+
			tapColumns+" WHERE schema_name = '"+schemaName+"' AND table_name='"+tableName+"' ORDER BY pos, column_name";
		
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			TapTableInfo t = null;
			String tableColumnName;
			while (result.next()) {
				if(t == null){
					t = TapTableInfo.createDefaultTapTableInfo(schemaName, tableName);
				}
				tableColumnName = result.getString(1);
				t.putColumn(tableColumnName, "description", result.getString(2));
				t.putColumn(tableColumnName, "ucd",         result.getString(3));
				t.putColumn(tableColumnName, "utype",       result.getString(4));
				t.putColumn(tableColumnName, "datatype",    result.getString(5));
				t.putColumn(tableColumnName, "unit",        result.getString(6));
				t.putColumn(tableColumnName, "size",        result.getInt(7));
				t.putColumn(tableColumnName, "principal",   result.getInt(8));
				t.putColumn(tableColumnName, "std",         result.getInt(9));
				t.putColumn(tableColumnName, "indexed",     result.getInt(10));
				t.putColumn(tableColumnName, "flags",       result.getInt(11));
			}
			return t;
		} catch (SQLException e) {
			throw new DBException("Cannot obtain table data for table: " + schemaName + "." + tableName, e);
		}
	}

	private String getRaDecIndexName(String tableName){
		return tableName + "_q3c";
	}
	
	private String getColIndexName(String tableName, String columnName){
		return tableName + "_" + columnName;
	}
	
	public static Timestamp getTimeStamp(String d) throws ParseException{
		Date date = UwsUtils.formatDate(d);
		return getTimeStamp(date);
	}
	
	public static Timestamp getTimeStamp(Date d){
		return new Timestamp(d.getTime());
	}

	@Override
	public void addAccessToPublicGroupTables(List<PublicGroupItem> items) throws DBException {
		try {
			startTransaction();
			PreparedStatement statement = dbConn.prepareStatement("INSERT INTO share_schema.accessible_public_group_tables "
					+ "(user_id, table_schema, table_name, table_owner) values (?,?,?,?)");
			for(PublicGroupItem item: items){
				statement.setString(1, item.getUser());
				statement.setString(2, item.getTableSchemaName());
				statement.setString(3, item.getTableName());
				statement.setString(4, item.getOwner());
				statement.executeUpdate();
			}
			endTransaction();
		} catch (SQLException e) {
			cancelTransaction();
			throw new DBException("Cannot add access to public group tables because: " + e.getMessage());
		} 
	}

	@Override
	public void removeAccessToPublicGroupTables(List<PublicGroupItem> items) throws DBException {
		try {
			startTransaction();
			PreparedStatement statement = dbConn.prepareStatement("DELETE FROM share_schema.accessible_public_group_tables "
					+ " WHERE user_id = ? AND table_schema = ? AND table_name = ? AND table_owner = ?");
			for(PublicGroupItem item: items){
				statement.setString(1, item.getUser());
				statement.setString(2, item.getTableSchemaName());
				statement.setString(3, item.getTableName());
				statement.setString(4, item.getOwner());
				statement.executeUpdate();
			}
			endTransaction();
		} catch (SQLException e) {
			cancelTransaction();
			throw new DBException("Cannot remove access from public group tables because: " + e.getMessage());
		} 
	}

	@Override
	public List<PublicGroupItem> getPublicGroupTables(String user) throws DBException {
		String query = "SELECT user_id, table_schema, table_name, table_owner FROM share_schema.accessible_public_group_tables";
		if(user != null && !"".equals(user)){
			query += " WHERE user_id = '"+user+"'";
		}
		try {
			ResultSet results = dbConn.createStatement().executeQuery(query);
			List<PublicGroupItem> items = new ArrayList<PublicGroupItem>();
			while(results.next()){
				PublicGroupItem item = new PublicGroupItem();
				item.setUser(results.getString("user_id"));
				item.setTableName(results.getString("table_name"));
				item.setTableSchemaName(results.getString("table_schema"));
				item.setOwner(results.getString("table_owner"));
				items.add(item);
			}
			return items;
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + query
					+ "\n. Because: " + se.getMessage());
		}
	}
	
	@Override
	public List<String> getPublicGroupSharedItems(List<PublicGroupItem> items, String groupid) throws DBException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.resource_id, s.title FROM share_schema.share s, share_schema.share_groups g WHERE ");
		sb.append("s.resource_id = g.resource_id AND s.resource_type = g.resource_type AND s.resource_type = ");
		sb.append(TAPMetadataLoader.SHARED_RESOURCE_TYPE_TABLE);
		sb.append(" AND g.group_id = '").append(groupid).append("' AND s.title IN (");
		boolean firstTime = true;
		for(PublicGroupItem item: items){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append('\'').append(item.getTableSchemaName()).append('.').append(item.getTableName()).append('\'');
		}
		sb.append(")"); 
		List<String> publicTables = new ArrayList<String>();
		try {
			ResultSet results = dbConn.createStatement().executeQuery(sb.toString());
			while(results.next()){
				publicTables.add(results.getString("title"));
			}
			return publicTables;
		} catch (SQLException se) {
			throw new DBException("Can not obtain non public tables\n. Because: " + se.getMessage());
		}
	}
	
	@Override
	public PreparedStatement createInsertRowStatement(TAPTable tableMetadata) throws DBException {
		String sql = "INSERT INTO "+tableMetadata.getDBSchemaName()+"."+tableMetadata.getDBName()+" VALUES ( ";
		
		Iterator<TAPColumn> columns = tableMetadata.getColumns();
		boolean firstTime = true;
		TAPColumn c;
		while(columns.hasNext()){
			c = columns.next();
			int sqlType = TAPTypes.getEffectiveSQLType(c);
			if(firstTime){
				firstTime = false;
			}else{
				sql += ", ";
			}
			sql += getSuitablePreparedStatementArg(sqlType);
		}
		
		sql+=" ) ";

		PreparedStatement insertRowStatement=null;
		try {
			insertRowStatement = dbConn.prepareStatement(sql);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage());
		}
		
		return insertRowStatement;
	}

	@Override
	public void loadTableDataBatch(UwsJobOwner owner, TAPTable table,
			Iterator<Object[]> rows, PreparedStatement statement)
			throws DBException, UwsException {

		if(!rows.hasNext()){
			return;
		}
		
		UwsQuotaSingleton quotaSingleton = UwsQuotaSingleton.getInstance(); 
		UwsQuota quota = quotaSingleton.createOrLoadQuota(owner);

		String tableDbName = table.getDBName();
		String schemaName = table.getDBSchemaName().toLowerCase();
		
		long prevTableSize = getTableSize(schemaName, tableDbName);

		try {
			

			while ( rows.hasNext() ) {
				Object[] row = rows.next();
				statement.clearParameters();
				
				int i=0;
				Iterator<TAPColumn> columns = table.getColumns();
				
				while(columns.hasNext()){
					TAPColumn c = columns.next();
					int sqlType = TAPTypes.getEffectiveSQLType(c);
					
					insertSuitableValue(i, row[i], sqlType, statement);
					i++;
				}
				if(i<row.length){
					throw new SQLException("Number of columns of the destination DB table ("+i+") does not match number of columns in the original table ("+row.length+").");
				}
				statement.addBatch();
				
	        }
			statement.executeBatch();
			statement.clearBatch();
			
			// Check DB quota
			long newTableSize = getTableSize(schemaName, tableDbName);
			long deltaTableSize = newTableSize-prevTableSize;
			
			quota.addDbSize(deltaTableSize);

		} catch(BatchUpdateException be){
			SQLException e = be.getNextException();
			if(e==null){
				e=be;
			}
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new DBException("Error creating user table: "+e.getMessage(), e);
		}catch (SQLException e) {
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new DBException("Error creating user table: "+e.getMessage(), e);
		} catch (QuotaException e) {
			restoreDbSize(quota, schemaName, tableDbName, e);
			throw new UwsException(e.getMessage(), e);
		}

	}

	@Override
	public void changeJobName(String jobid, String jobName) throws DBException {
		String query = "UPDATE uws2_schema.job_parameters SET string_representation = '" + jobName + 
				"' WHERE job_id = '" + jobid + "' and parameter_id = 'jobname'";
		try {
			dbConn.createStatement().execute(query);
		} catch (SQLException se) {
			throw new DBException("Can not execute the following SQL: \n" + query
				+ "\n. Because: " + se.getMessage());
		}

	}

	@Override
	public boolean isTablePublic(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException {
		try{
			String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
			
			PreparedStatement statement = dbConn.prepareStatement("SELECT public from "+tapTables+" where "
					+ "schema_name = ? and table_name = ?");
			statement.setString(1, schemaName);
			statement.setString(2, tableName);
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				boolean value = rs.getBoolean("public");
				statement.close();
				return value;
			}else{
				statement.close();
				throw new DBException("Table '"+schemaName.toLowerCase()+"."+tableName.toLowerCase()+"' not found");
			}
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	@Override
	public List<Boolean> isTablePublic(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableName) throws DBException {
		if(fullQualifiedTableName == null || fullQualifiedTableName.size() < 1){
			return null;
		}
		
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		StringBuilder sb = new StringBuilder("SELECT public from "+tapTables+" where ");
		String schemaName;
		String tableName;
		boolean firstTime = true;
		for(String fqtn: fullQualifiedTableName){
			schemaName = TapUtils.getSchemaNameOnly(fqtn);
			tableName = TapUtils.getTableNameOnly(fqtn);
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(" OR ");
			}
			sb.append("(schema_name = '").append(schemaName.toLowerCase()).append("' AND table_name = '").append(tableName.toLowerCase()).append("')");
		}
		try{
			List<Boolean> results = new ArrayList<Boolean>();
			Statement stmt = dbConn.createStatement();
			ResultSet rs = stmt.executeQuery(sb.toString());
			while(rs.next()){
				results.add(rs.getBoolean(1));
			}
			return results;
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	@Override
	public boolean isSchemaPublic(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException {
		try{
			String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas

			PreparedStatement statement = dbConn.prepareStatement(
					"SELECT public from "+tapSchemas+" where " + "schema_name = ?");
			statement.setString(1, schemaName.toLowerCase());
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				boolean value = rs.getBoolean("public");
				statement.close();
				return value;
			}else{
				statement.close();
				throw new DBException("Schema '" + schemaName + "' not found");
			}
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	@Override
	public List<String> getPublicSchemas(TAPSchemaInfo tapSchemaInfo) throws DBException {
		try{
			String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
			String query = "SELECT schema_name from "+tapSchemas+" where public = 'true'";
			Statement statement = dbConn.createStatement();
			ResultSet rs = statement.executeQuery(query);
			List<String> publicSchemas = new ArrayList<String>();
			String schema;
			while(rs.next()){
				schema = rs.getString(1);
				publicSchemas.add(schema);
			}
			return publicSchemas;
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	@Override
	public Map<String,String> publishTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean calculateArrayDims) throws DBException {
		if(fullQualifiedTableNames == null){
			return null;
		}
		try{
			startTransaction();
			PreparedStatement insertIntoTapSchemaSchemas = createInsertIntoTapSchemaSchemasPS(tapSchemaInfo);
			String schemaName;
			String tableName;
			Map<String,String> results = new HashMap<String,String>();
			boolean schemaCreated;
			boolean tableCreated;
			boolean arraysFound;
			String result;
			
			PreparedStatement insertIntoTapSchemaTables = createInsertIntoTapSchemaTablesPS(tapSchemaInfo);
			
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
				tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);
				schemaCreated = false;
				arraysFound = false;
				if(!checkSchemaExistInTapSchema(tapSchemaInfo, schemaName)){
					insertIntoTapSchemaSchemas.setString(1, schemaName);
					insertIntoTapSchemaSchemas.setString(2, null);
					insertIntoTapSchemaSchemas.setString(3, null);
					insertIntoTapSchemaSchemas.executeUpdate();
					schemaCreated = true;
				}

				tableCreated = false;
				if(!checkTableExistInTapTables(tapSchemaInfo, schemaName, tableName)){
					insertIntoTapSchemaTables.setString(1, schemaName);
					insertIntoTapSchemaTables.setString(2, tableName);
					insertIntoTapSchemaTables.setString(3, "table");
					insertIntoTapSchemaTables.setString(4, null); //description
					insertIntoTapSchemaTables.setString(5, null); //utype
					insertIntoTapSchemaTables.setInt(6, 0); //size
					insertIntoTapSchemaTables.setInt(7, 0); //flags
					insertIntoTapSchemaTables.executeUpdate();
					
					arraysFound = populateColumns(tapSchemaInfo, schemaName, tableName, calculateArrayDims);
					
					tableCreated = true;
				}
				if(schemaCreated){
					result = "Schema created";
				}else{
					result = "Schema already created";
				}
				if(tableCreated){
					result += ", Table created";
				}else{
					result += ", Table already created";
				}
				if(arraysFound){
					result += ", WARNING: arrays found (manual check could be required)";
				}
				results.put(fullQualifiedTableName, result);
			}
			endTransaction();
			return results;
		}catch(Exception e){
			cancelTransaction();
			throw new DBException(e);
		}
	}
	
	private boolean populateColumns(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, boolean calculateArrayDims) throws SQLException{
		String tapColumns = tapSchemaInfo.getFullQualifiedTapColumnsTable();
		PreparedStatement insertColumn = dbConn.prepareStatement(
				"INSERT INTO "+tapColumns+" (schema_name, table_name, column_name, datatype, array_type, array_dims) "
						+ "values ('"+schemaName+"','"+tableName+"',?,?,?,?)");
		String query = "SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE "
				+ "table_schema = '"+schemaName+"' and table_name = '"+tableName+"'";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query);
		String sqlDataType;
		String columnName;
		String udtName;
		String dataType;
		String arrayType;
		String arrayDims;
		boolean arraysFound = false;
		while(rs.next()){
			sqlDataType = rs.getString(2);
			columnName = rs.getString(1);
			dataType = getColumnDataType(sqlDataType);
			insertColumn.setString(1, columnName);
			insertColumn.setString(2, dataType);
			if("ARRAY".equalsIgnoreCase(sqlDataType)){
				arraysFound = true;
				udtName = rs.getString(3);
				arrayType = getColumnArrayType(udtName);
				insertColumn.setString(3, arrayType);
				if(calculateArrayDims){
					arrayDims = getColumnArrayDims(schemaName, tableName, columnName);
				}else{
					arrayDims = "*";
				}
				insertColumn.setString(4, arrayDims);
			}else{
				insertColumn.setNull(3, Types.VARCHAR);
				insertColumn.setNull(4, Types.VARCHAR);
			}
			insertColumn.execute();
		}
		return arraysFound;
	}
	
	private String getColumnArrayDims(String schemaName, String tableName, String columnName) throws SQLException {
		String query = "SELECT MAX(ARRAY_DIMS("+columnName+")) FROM " + schemaName + "." + tableName;
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query);
		if(rs.next()){
			String dims = rs.getString(1);
			//dims = '[1:3][1:9]' 2 dims, first 1..3, second 1..9 => [3][9] => 3x9
			String[] items = dims.split("\\]");
			StringBuilder sb = new StringBuilder();
			int p;
			int numItems = items.length;
			for(int i = 0; i < numItems; i++){
				if(i != 0){
					sb.append('x');
				}
				if(i == numItems - 1){
					//last item is always variable
					sb.append('*');
				}else{
					p = items[i].indexOf(':');
					sb.append(items[i].substring(p+1));
				}
			}
			return sb.toString();
		}else{
			return "";
		}
	}
	
	/**
	 * Returns the TAP column data type
	 * @param dataType
	 * @return
	 */
	private String getColumnDataType(String dataType){
		if(dataType == null){
			return "";
		}
		String value = dataType.toUpperCase();
		if(value.contains("SMALLINT")){
			return "SMALLINT";
		}
		if(value.contains("INTEGER")){
			return "INTEGER";
		}
		if(value.contains("BIGINT")){
			return "BIGINT";
		}
		if(value.contains("REAL")){
			return "REAL";
		}
		if(value.contains("DOUBLE PRECISION")){
			return "DOUBLE";
		}
		if(value.contains("BINARY")){
			return "BINARY";
		}
		if(value.contains("VARBINARY")){
			return "VARBINARY";
		}
		if(value.contains("CHAR")){
			return "CHAR";
		}
		if(value.contains("VARCHAR")){
			return "VARCHAR";
		}
		if(value.contains("BLOB")){
			return "BLOB";
		}
		if(value.contains("CLOB")){
			return "CLOB";
		}
		if(value.contains("TIMESTAMP")){
			return "TIMESTAMP";
		}
		if(value.contains("POINT")){
			return "POINT";
		}
		if(value.contains("REGION")){
			return "REGION";
		}
		if(value.contains("BOOLEAN")){
			return "BOOLEAN";
		}
		if(value.contains("ARRAY")){
			return "VARBINARY";
		}
		return dataType;
	}
	
	/**
	 * Returns the VOTable data type for arrays.
	 * @param columnSqlUdtName
	 * @return
	 */
	private String getColumnArrayType(String columnSqlUdtName){
		if(columnSqlUdtName == null){
			return "";
		}
		String value = columnSqlUdtName.trim().toLowerCase();
		if(value.contains("int2")){
			return "short";
		}
		if(value.contains("int4")){
			return "int";
		}
		if(value.contains("int8")){
			return "long";
		}
		if(value.contains("float4")){
			return "float";
		}
		if(value.contains("float8")){
			return "double";
		}
		if(value.contains("char")){
			return "char";
		}
		if(value.contains("bool")){
			return "boolean";
		}
		if(value.contains("bytea")){
			return "byte";
		}
		//unknown type for array for vo: return it.
		return columnSqlUdtName;
	}
	
	private boolean checkSchemaExistInTapSchema(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException {
		try{
			String tapSchemas = tapSchemaInfo.getFullQualifiedTapSchemasTable(); //tap_schema.all_schemas
			PreparedStatement statement = dbConn.prepareStatement("SELECT schema_name FROM "+tapSchemas+" WHERE "
					+ "schema_name = ?");
			statement.setString(1, schemaName);
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				statement.close();
				return true;
			}else{
				statement.close();
				return false;
			}
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
	
	private boolean checkTableExistInTapTables(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException {
		try{
			String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables			
			PreparedStatement statement = dbConn.prepareStatement("SELECT table_name FROM "+tapTables+" WHERE "
					+ "schema_name = ? and table_name = ?");
			statement.setString(1, schemaName);
			statement.setString(2, tableName);
			ResultSet rs = statement.executeQuery();
	
			if(rs.next()){
				statement.close();
				return true;
			}else{
				statement.close();
				return false;
			}
		}catch(SQLException e){
			throw new DBException(e);
		}
	}


	@Override
	public Map<String, String> setPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean isPublic) throws DBException {
		if(fullQualifiedTableNames == null){
			return null;
		}
		try{
			startTransaction();
			String schemaName;
			String tableName;
			long numPublicTablesAssociatedToSchema;
			Map<String,String> results = new HashMap<String, String>();
			String result;
			String status = (isPublic?"public":"private");
			int updateResult;
			PreparedStatement updateSchemaStatus = createUpdateSchemaStatusPS(tapSchemaInfo);
			PreparedStatement updateTableStatus = createUpdateTableStatusPS(tapSchemaInfo);
			PreparedStatement updateColumnStatus = createUpdateColumnStatusPS(tapSchemaInfo);
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
				tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);
				
				if(!isPublic){
					//set false to schema only if no more tables are public
					numPublicTablesAssociatedToSchema = getNumPublicTablesAssociatedToSchema(tapSchemaInfo, schemaName, tableName);
					if(numPublicTablesAssociatedToSchema < 1){
						updateSchemaStatus.setBoolean(1, false);
						updateSchemaStatus.setString(2, schemaName);
						updateResult = updateSchemaStatus.executeUpdate();
						if(updateResult == 1){
							result = "Schema set to private";
						}else{
							result = "Schema not found";
						}
					} else {
						result = "Schema keep status";
					}
				}else{
					updateSchemaStatus.setBoolean(1, true);
					updateSchemaStatus.setString(2, schemaName);
					updateResult = updateSchemaStatus.executeUpdate();
					if(updateResult == 1){
						result = "Schema set to public";
					}else{
						result = "Schema not found";
					}
				}
				
				updateTableStatus.setBoolean(1, isPublic);
				updateTableStatus.setString(2, schemaName);
				updateTableStatus.setString(3, tableName);
				updateResult = updateTableStatus.executeUpdate();
				if(updateResult == 1){
					result += ", Table set to " + status;
				}else{
					result += ", Table not found";
				}
				
				updateColumnStatus.setBoolean(1, isPublic);
				updateColumnStatus.setString(2, schemaName);
				updateColumnStatus.setString(3, tableName);
				updateColumnStatus.executeUpdate();
				result += ", Columns set to " + status;
				
				results.put(fullQualifiedTableName, result);
			}
			endTransaction();
			return results;
		}catch(Exception e){
			cancelTransaction();
			throw new DBException(e);
		}
	}

	@Override
	public Map<String, String> getPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException {
		if(fullQualifiedTableNames == null){
			return null;
		}
		try{
			startTransaction();
			String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
			PreparedStatement tableStatus = dbConn.prepareStatement("SELECT public FROM "+tapTables+" WHERE schema_name=? AND table_name=?");
			String schemaName;
			String tableName;
			Map<String,String> status = new HashMap<String, String>();
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
				tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);
				tableStatus.setString(1, schemaName);
				tableStatus.setString(2, tableName);
				ResultSet rs = tableStatus.executeQuery();
				if(rs.next()){
					status.put(fullQualifiedTableName, ""+rs.getBoolean(1));
				}else{
					status.put(fullQualifiedTableName, "Not found");
				}
			}
			endTransaction();
			return status;
		}catch(Exception e){
			cancelTransaction();
			throw new DBException(e);
		}
	}

	@Override
	public Map<String, String> removeTablesFromTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException {
		if(fullQualifiedTableNames == null){
			return null;
		}
		try{
			startTransaction();
			String schemaName;
			String tableName;
			long numTablesAssociatedToSchema;
			Map<String,String> results = new HashMap<String,String>();
			String result;
			int updateResult;
			PreparedStatement deleteFromTapSchemaTables = createDeleteFromTapSchemaTablesPS(tapSchemaInfo);
			PreparedStatement deleteFromTapSchemaColumns = createDeleteFromTapSchemaColumnsPS(tapSchemaInfo);
			PreparedStatement deleteFromTapSchemaSchemas = createDeleteFromTapSchemaSchemasPS(tapSchemaInfo);
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
				tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);
				deleteFromTapSchemaColumns.setString(1, schemaName);
				deleteFromTapSchemaColumns.setString(2, tableName);
				deleteFromTapSchemaColumns.executeUpdate();
				
				deleteFromTapSchemaTables.setString(1, schemaName);
				deleteFromTapSchemaTables.setString(2, tableName);
				updateResult = deleteFromTapSchemaTables.executeUpdate();
				
				if(updateResult == 1){
					result = "Table removed";
				}else{
					result = "Table not found";
				}
				
				//Remove schema if no more tables are associated to it
				numTablesAssociatedToSchema = getNumTablesAssociatedToSchema(tapSchemaInfo, schemaName, tableName);
				if(numTablesAssociatedToSchema < 1){
					deleteFromTapSchemaSchemas.setString(1, schemaName);
					updateResult = deleteFromTapSchemaSchemas.executeUpdate();
					if(updateResult == 1){
						result += ", Schema removed";
					}else{
						result += ", Schema not found";
					}
				}
				results.put(fullQualifiedTableName, result);
			}
			endTransaction();
			return results;
		}catch(Exception e){
			cancelTransaction();
			throw new DBException(e);
		}
	}
	
	private long getNumTablesAssociatedToSchema(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableToSkip) throws DBException {
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		String sql = null;
		if(tableToSkip != null){
			sql = "SELECT count(*) FROM "+tapTables+" WHERE schema_name = '"+schemaName+"' AND table_name <> '"+tableToSkip+"'";
		} else {
			sql = "SELECT count(*) FROM "+tapTables+" WHERE schema_name = '"+schemaName+"'";
		}
		try {
			ResultSet rs = dbConn.createStatement().executeQuery(sql);
			if(rs.next()){
				return rs.getLong(1);
			}else{
				return 0;
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	private long getNumPublicTablesAssociatedToSchema(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableToSkip) throws DBException {
		String tapTables = tapSchemaInfo.getFullQualifiedTapTablesTable(); //tap_schema.all_tables
		String sql = null;
		if(tableToSkip != null){
			sql = "SELECT count(*) FROM "+tapTables+" WHERE schema_name = '"+schemaName+"' AND table_name <> '"+tableToSkip+"' AND public is true";
		} else {
			sql = "SELECT count(*) FROM "+tapTables+" WHERE schema_name = '"+schemaName+"' AND public is true";
		}
		try {
			ResultSet rs = dbConn.createStatement().executeQuery(sql);
			if(rs.next()){
				return rs.getLong(1);
			}else{
				return 0;
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public TAPSchema loadSchema(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException {
		try {
			PreparedStatement getSchemaInfo = createGetSchemaInfoPS(tapSchemaInfo);
			getSchemaInfo.setString(1, schemaName.toLowerCase());
			ResultSet result = getSchemaInfo.executeQuery();
			if(result.next()){
				String description = result.getString("description");
				String utype = result.getString("utype");
				boolean isPublic = result.getBoolean("public");
				String title = result.getString("title");
				String dbSchemaName = result.getString("db_schema_name");
				TAPSchema tapSchema = new TAPSchema(schemaName, description, utype, title, isPublic);
				if(dbSchemaName != null && !dbSchemaName.isEmpty()){
					tapSchema.setDBName(dbSchemaName);
				}
				return tapSchema;
			}else{
				return null;
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public long getTimeOut() {
		return timeOut;
	}

	@Override
	public void enableSeqScan(boolean allowSeqScan) throws DBException {
		String value="on";
		
		if(!allowSeqScan){
			value="off";
		}
		
		try {
			dbConn.createStatement().execute("SET enable_seqscan = "+value);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public String getUserTapSchema(UwsJobOwner user) throws DBException {
		String sql = "select schema_name from tap_config.user_schema where user_id = '"+user.getId()+"'";
		try {
			ResultSet rs = dbConn.createStatement().executeQuery(sql);
			if(rs.next()){
				return rs.getString(1);
			}else{
				return null;
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public void createTapSchema(String tapSchemaName) throws DBException {
		String sql;
		try {
			sql = TapSql.getCreateTapSchemaSql(tapSchemaName);
		} catch (IOException e) {
			throw new DBException("Cannot create tap schema '"+tapSchemaName+"' because: " + e.getMessage());
		}
		try {
			startTransaction();
			dbConn.createStatement().executeUpdate(sql);
			endTransaction();
		} catch (SQLException e) {
			cancelTransaction();
			throw new DBException("Cannot create tap schema '"+tapSchemaName+"' because: " + e.getMessage());
		} 
	}

	@Override
	public void deleteTapSchema(String tapSchemaName) throws DBException {
		String sql;
		try {
			sql = TapSql.getDeleteTapSchemaSql(tapSchemaName);
		} catch (IOException e) {
			throw new DBException("Cannot delete tap schema '"+tapSchemaName+"' because: " + e.getMessage());
		}
		try {
			startTransaction();
			dbConn.createStatement().executeUpdate(sql);
			endTransaction();
		} catch (SQLException e) {
			cancelTransaction();
			throw new DBException("Cannot delete tap schema '"+tapSchemaName+"' because: " + e.getMessage());
		} 
	}

}
