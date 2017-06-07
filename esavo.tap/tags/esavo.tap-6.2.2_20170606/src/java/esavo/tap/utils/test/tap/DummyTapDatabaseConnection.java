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
package esavo.tap.utils.test.tap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.starlink.table.StarTable;
import esavo.adql.query.ADQLQuery;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.test.database.DummyUwsDatabaseConnection;

public class DummyTapDatabaseConnection implements DBConnection {
	
	private DummyUwsDatabaseConnection dbConn;
	
	public static final String ACTION_CMD_START_TRANSACTION = "START_TRANSACTION";
	public static final String ACTION_CMD_END_TRANSACTION = "END_TRANSACTION";
	public static final String ACTION_CMD_TRANSACTION_ROLLBACK = "TRANSACTION_ROLLBACK";
	public static final String ACTION_CMD_CLOSE_DB_CONNECTION = "CLOSE_DB_CONNECTION";
	public static final String ACTION_CMD_EXECUTE_QUERY = "EXECUTE_QUERY";
	public static final String ACTION_CMD_CREATE_SCHEMA = "CREATE_SCHEMA";
	public static final String ACTION_CMD_DROP_SCHEMA = "DROP_SCHEMA";
	public static final String ACTION_CMD_CREATE_TABLE = "CREATE_TABLE";
	public static final String ACTION_CMD_INSERT_ROW = "INSERT_ROW";
	public static final String ACTION_CMD_DROP_TABLE = "DROP_TABLE";
	public static final String ACTION_CMD_GET_ID = "GET_ID";
	public static final String ACTION_CMD_GET_TIMEOUT = "GET_TIMEOUT";
	public static final String ACTION_CMD_REGISTER_IN_TAP = "REGISTER_IN_TAP";
	public static final String ACTION_CMD_UNREGISTER_FROM_TAP = "UNREGISTER_FROM_TAP";
	public static final String ACTION_CMD_LOAD_TABLE_DATA = "LOAD_TABLE_DATA";
	public static final String ACTION_CMD_INDEX_TABLE_COLUMN = "INDEX_TABLE_COLUMN";
	public static final String ACTION_CMD_UPDATE_USER_LOADED_TABLE = "UPDATE_USER_LOADED_TABLE";
	public static final String ACTION_CMD_FETCH_TAP_TABLE_INFO = "FETCH_TAP_TABLE_INFO";
	public static final String ACTION_CMD_REMOVE_INDEX_TABLE_COLUMN = "REMOVE_INDEX_TABLE_COLUMN";
	public static final String ACTION_CMD_LOAD_USER_DETAILS = "LOAD_USER_DETAILS";
	public static final String ACTION_CMD_CREATE_USER = "CREATE_USER";
	public static final String ACTION_CMD_RETRIEVE_USERS_BY_FILTER = "RETRIEVE_ALL_USERS";
	public static final String ACTION_CMD_LOAD_JOB_DETAILS = "LOAD_JOB_DETAILS";
	public static final String ACTION_CMD_RETRIEVE_JOBS_BY_FILTER = "RETRIEVE_JOBS_BY_FILTER";
	public static final String ACTION_CMD_UPDATE_USER_DETAILS = "UPDATE_USER_DETAILS";
	public static final String ACTION_CMD_CHECK_IS_PUBLIC_TABLE = "CHECK_IS_PUBLIC_TABLE";
	public static final String ACTION_CMD_CHECK_ARE_PUBLIC_TABLES = "CHECK_ARE_PUBLIC_TABLES";
	public static final String ACTION_CMD_CHECK_IS_PUBLIC_SCHEMA = "CHECK_IS_PUBLIC_SCHEMA";
	public static final String ACTION_CMD_GET_PUBLIC_SCHEMAS = "GET_PUBLIC_SCHEMAS";
	public static final String ACTION_CMD_PUBLISH_TABLES_IN_TAP_SCHEMA = "PUBLISH_TABLES_IN_TAP_SCHEMA";
	public static final String ACTION_CMD_SET_PUBLISH_STATUS_IN_TAP_SCHEMA = "SET_PUBLISH_STATUS_IN_TAP_SCHEMA";
	public static final String ACTION_CMD_GET_PUBLISH_STATUS_IN_TAP_SCHEMA = "GET_PUBLISH_STATUS_IN_TAP_SCHEMA";
	public static final String ACTION_CMD_REMOVE_TABLES_FROM_TAP_SCHEMA = "REMOVE_TABLES_FROM_TAP_SCHEMA";
	public static final String ACTION_CMD_LOAD_SCHEMA = "LOAD_SCHEMA";
	public static final String ACTION_CMD_ENABLE_SEQSCAN = "ENABLE_SEQSCAN";
	public static final String ACTION_CMD_GET_TAP_SCHEMA_INFO = "GET_TAP_SCHEMA_INFO";

	
	private List<String> actions;
	
	private boolean raiseException;
	private int raiseExceptionByCounter;
	
	private long dbSize;
	private long tableSize;
	private boolean publicTable;
	private boolean publicSchema;
	private List<Boolean> publicTables;
	private List<String> publicSchemaNames;
	private List<String> fullQualifiedTableNames;
	private Map<String,String> fullQualifiedTableNamesStatus;
	private boolean tapPublishStatus;
	private Map<String,TAPSchema> tapSchemas;
	private TAPTable table;
	private String tapSchemaInfo;
	private boolean transactionFinished = false;
	
	public DummyTapDatabaseConnection(DummyUwsDatabaseConnection dbconnection){
		dbConn = dbconnection;
		actions = new ArrayList<String>();
		tapSchemas = new HashMap<String,TAPSchema>();
		clearFlags();
	}
	
	protected String createLogEntry(String action, String msg){
		return action + ": " + msg;
	}
	
	public void setRaiseException(boolean raiseException){
		this.raiseException = raiseException;
	}
	
	/**
	 * 0 disabled. 1: next query will raise the exception. n: n-th query will raise the exception  
	 * @param raiseExceptionByCounter
	 */
	public void setRaiseExceptionByCounter(int raiseExceptionByCounter){
		this.raiseExceptionByCounter = raiseExceptionByCounter;
	}
	
	protected void logAction(String action, String msg) throws DBException{
		if(raiseException){
			throw new DBException("Exception requested");
		}
		if(raiseExceptionByCounter > 0){
			raiseExceptionByCounter--;
			if(raiseExceptionByCounter == 0){
				throw new DBException("Exception requested");
			}
		}
		String logEntry = createLogEntry(action, msg);
		System.out.println("DB action: " + logEntry);
		actions.add(logEntry);
	}
	
	public boolean isActionLogged(String action, String msg){
		return actions.contains(createLogEntry(action, msg));
	}
	
	public String findSimilarAction(String action){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(String a: actions){
			if(a.startsWith(action)){
				if(firstTime){
					firstTime = false;
				}else{
					sb.append("\n");
				}
				sb.append("\t'").append(a).append('\'');
			}
		}
		return sb.toString();
	}
	
	public String getRegisteredActions(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(String a: actions){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append("\n");
			}
			sb.append("\t'").append(a).append('\'');
		}
		return sb.toString();
	}
	
	public void clear(){
		clearFlags();
		clearSchemas();
	}

	public void clearFlags(){
		actions.clear();
		raiseException = false;
		raiseExceptionByCounter = 0;
	}
	
	public void clearSchemas(){
		tapSchemas.clear();
	}

	//---------------------------------------------------------
	@Override
	public void startTransaction() throws DBException {
		try {
			dbConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			transactionFinished=false;
			logAction(ACTION_CMD_START_TRANSACTION,"");
		} catch (SQLException e) {
			throw new DBException("Impossible to start transaction, because: "+e.getMessage());
		}
	}

	@Override
	public void cancelTransaction() throws DBException {
		try{
			dbConn.rollback();
			transactionFinished=true;
			logAction(ACTION_CMD_TRANSACTION_ROLLBACK,"");
		} catch (SQLException e) {
			throw new DBException("Impossible to cancel transaction, because: "+e.getMessage());
		}
	}

	@Override
	public void endTransaction() throws DBException {
		try{
			dbConn.commit();
			transactionFinished=true;
			logAction(ACTION_CMD_END_TRANSACTION,"");
		} catch (SQLException e) {
			throw new DBException("Impossible to commit transaction, because: "+e.getMessage());
		}
	}

	@Override
	public void close() throws DBException {
		try{
			dbConn.close();
			logAction(ACTION_CMD_CLOSE_DB_CONNECTION,"");
		} catch (SQLException e) {
			throw new DBException("Impossible to close DB connection, because: "+e.getMessage());
		}
	}

	@Override
	public ResultSet executeQuery(String sql, ADQLQuery query) throws DBException {
		try{
			Statement stmt = dbConn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			logAction(ACTION_CMD_EXECUTE_QUERY, sql);
			return rs;
		}catch(SQLException se){
			throw new DBException("Can not execute the following SQL: \n"+sql+"\n. Because: "+se.getMessage());
		}
	}


	/* ************************************** */
	/* METHODS USED ONLY IF UPLOAD IS ENABLED */
	/* ************************************** */

	@Override
	public void createSchema(String arg0) throws DBException {
		logAction(ACTION_CMD_CREATE_SCHEMA, arg0);
	}

	@Override
	public void dropSchema(String arg0) throws DBException {
		logAction(ACTION_CMD_DROP_SCHEMA, arg0);
	}

	@Override
	public void createTable(TAPTable table, String tableSpace) throws DBException {
		logAction(ACTION_CMD_CREATE_TABLE, table.getFullName() + ", tableSpace: '"+tableSpace+"'");
		this.table=table;
	}
	
	@Override
	public void dropTable(TAPTable table) throws DBException {
		//System.out.println("TapDBConnection: Entering dropTable("+table.getDBName()+")");
		logAction(ACTION_CMD_DROP_TABLE, table.getFullName());
		this.table=table;
	}
	
	@Override
	public void dropTable(TAPTable table, boolean forceRemoval)	throws DBException {
		logAction(ACTION_CMD_DROP_TABLE, table.getFullName() + "-forceRemoval="+forceRemoval);
		this.table=table;
	}

	@Override
	public String getID() {
		try {
			logAction(ACTION_CMD_GET_ID, "");
		} catch (DBException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getTimeOut() {
		try {
			logAction(ACTION_CMD_GET_TIMEOUT, "");
		} catch (DBException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void registerInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPSchema arg0) throws DBException {
		logAction(ACTION_CMD_REGISTER_IN_TAP, arg0.getName());
		
	}

	@Override
	public void registerInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable arg0) throws DBException {
		logAction(ACTION_CMD_REGISTER_IN_TAP, arg0.getFullName());
		this.table=arg0;
	}

	@Override
	public void unregisterFromTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable arg0) throws DBException {
		logAction(ACTION_CMD_UNREGISTER_FROM_TAP, arg0.getFullName());
		this.table=arg0;
	}

	@Override
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable) throws DBException {
		return loadTableData(owner, table, starTable, -1);
	}

	@Override
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable, long taskid) throws DBException {
		this.table=table;
		logAction(ACTION_CMD_LOAD_TABLE_DATA, table.getFullName());
		return 0;
	}
	
	@Override
	public PreparedStatement createInsertRowStatement(TAPTable tableMetadata)
			throws DBException {
				this.table = tableMetadata;

		try {
			return dbConn.prepareStatement("");
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public void loadTableDataBatch(UwsJobOwner owner, TAPTable table,
			Iterator<Object[]> rows, PreparedStatement statement)
			throws DBException, UwsException {
		logAction(ACTION_CMD_LOAD_TABLE_DATA, table.getFullName());
		this.table = table;
	}

	@Override
	public long getDbSize(String ownerid) throws DBException {
		return dbSize;
	}

	@Override
	public long getTableSize(String schema, String table) throws DBException {
		return tableSize;
	}
	
	public void setDbSize(long dbSize){
		this.dbSize = dbSize;
	}
	
	public void setTableSize(long tableSize){
		this.tableSize = tableSize;
	}

	@Override
	public boolean isTablePublic(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException {
		logAction(ACTION_CMD_CHECK_IS_PUBLIC_TABLE, schemaName+" - "+tableName);
		return publicTable;
	}

	@Override
	public List<Boolean> isTablePublic(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableName)	throws DBException {
		logAction(ACTION_CMD_CHECK_ARE_PUBLIC_TABLES, ""+fullQualifiedTableName.size());
		return publicTables;
	}

	@Override
	public boolean isSchemaPublic(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException {
		logAction(ACTION_CMD_CHECK_IS_PUBLIC_SCHEMA, schemaName);
		return publicSchema;
	}

	/**
	 * @return the publicTable
	 */
	public boolean isPublicTable() {
		return publicTable;
	}

	/**
	 * @param publicTable the publicTable to set
	 */
	public void setPublicTable(boolean publicTable) {
		this.publicTable = publicTable;
	}

	/**
	 * @return the publicSchema
	 */
	public boolean isPublicSchema() {
		return publicSchema;
	}

	/**
	 * @param publicSchema the publicSchema to set
	 */
	public void setPublicSchema(boolean publicSchema) {
		this.publicSchema = publicSchema;
	}

	/**
	 * @return the publicTables
	 */
	public List<Boolean> getPublicTables() {
		return publicTables;
	}

	/**
	 * @param publicTables the publicTables to set
	 */
	public void setPublicTables(List<Boolean> publicTables) {
		this.publicTables = publicTables;
	}

	@Override
	public List<String> getPublicSchemas(TAPSchemaInfo tapSchemaInfo) throws DBException {
		logAction(ACTION_CMD_GET_PUBLIC_SCHEMAS,"");
		return publicSchemaNames;
	}
	
	public void setPublicSchemaNames(List<String> publicSchemaNames){
		this.publicSchemaNames = publicSchemaNames;
	}
	@Override
	public Map<String,String> publishTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean calculateArrayDims) throws DBException {
		logAction(ACTION_CMD_PUBLISH_TABLES_IN_TAP_SCHEMA, "");// = "PUBLISH_TABLES_IN_TAP_SCHEMA";
		this.fullQualifiedTableNames = fullQualifiedTableNames;
		return this.fullQualifiedTableNamesStatus;
	}

	@Override
	public Map<String,String> setPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean isPublic)	throws DBException {
		logAction(ACTION_CMD_SET_PUBLISH_STATUS_IN_TAP_SCHEMA, "");// = "ACTION_CMD_SET_PUBLISH_STATUS_IN_TAP_SCHEMA";
		this.fullQualifiedTableNames = fullQualifiedTableNames;
		this.tapPublishStatus = isPublic;
		return this.fullQualifiedTableNamesStatus;
	}

	@Override
	public Map<String, String> getPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException {
		logAction(ACTION_CMD_GET_PUBLISH_STATUS_IN_TAP_SCHEMA,"");// = "GET_PUBLISH_STATUS_IN_TAP_SCHEMA";
		this.fullQualifiedTableNames = fullQualifiedTableNames;
		return this.fullQualifiedTableNamesStatus;
	}

	@Override
	public Map<String,String> removeTablesFromTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException {
		logAction(ACTION_CMD_REMOVE_TABLES_FROM_TAP_SCHEMA,"");// = "ACTION_CMD_REMOVE_TABLES_FROM_TAP_SCHEMA";
		this.fullQualifiedTableNames = fullQualifiedTableNames;
		return this.fullQualifiedTableNamesStatus;
	}

	/**
	 * @return the fullQualifiedTableNames
	 */
	public List<String> getFullQualifiedTableNames() {
		return fullQualifiedTableNames;
	}

	/**
	 * @param fullQualifiedTableNames the fullQualifiedTableNames to set
	 */
	public void setFullQualifiedTableNames(List<String> fullQualifiedTableNames) {
		this.fullQualifiedTableNames = fullQualifiedTableNames;
	}

	/**
	 * @return the tapPublishStatus
	 */
	public boolean isTapPublishStatus() {
		return tapPublishStatus;
	}

	/**
	 * @param tapPublishStatus the tapPublishStatus to set
	 */
	public void setTapPublishStatus(boolean tapPublishStatus) {
		this.tapPublishStatus = tapPublishStatus;
	}

	/**
	 * @return the fullQualifiedTableNamesStatus
	 */
	public Map<String, String> getFullQualifiedTableNamesStatus() {
		return fullQualifiedTableNamesStatus;
	}

	/**
	 * @param fullQualifiedTableNamesStatus the fullQualifiedTableNamesStatus to set
	 */
	public void setFullQualifiedTableNamesStatus(
			Map<String, String> fullQualifiedTableNamesStatus) {
		this.fullQualifiedTableNamesStatus = fullQualifiedTableNamesStatus;
	}

	@Override
	public TAPSchema loadSchema(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException {
		logAction(ACTION_CMD_LOAD_SCHEMA,schemaName);
		return tapSchemas.get(schemaName);
	}

	/**
	 * @param tapSchema the tapSchema to set
	 */
	public void setTapSchema(TAPSchema tapSchema) {
		this.tapSchemas.put(tapSchema.getName(), tapSchema);
	}

	@Override
	public void enableSeqScan(boolean allowSeqScan) throws DBException {
		logAction(ACTION_CMD_ENABLE_SEQSCAN,"enable_seqscan="+allowSeqScan);
	}

	public TAPTable getTable() {
		return table;
	}

	@Override
	public String getUserTapSchema(UwsJobOwner user) throws DBException {
		logAction(ACTION_CMD_GET_TAP_SCHEMA_INFO,"user="+user);
		return tapSchemaInfo;
	}

	/**
	 * @param tapSchemaInfo the tapSchemaInfo to set
	 */
	public void setTapSchemaInfo(String tapSchemaInfo) {
		this.tapSchemaInfo = tapSchemaInfo;
	}

	@Override
	public boolean isTransactionFinished() throws DBException {
		return transactionFinished;
	}
}
