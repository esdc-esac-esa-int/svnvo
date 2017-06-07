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
package esavo.sl.services.tabletool.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.tabletool.IndexInfo;
import esavo.sl.services.tabletool.TableToolHandler;
import esavo.sl.services.tabletool.TableToolUtils;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.TapUtils;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TapTableInfo;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;

public class EditTableHandler implements TableToolHandler {
	
	public static final String PARAM_NUM_TABLES = "NUMTABLES";
	
	public static final String PARAM_PREFIX_TABLE = "TABLE";
	
	public static final String PARAM_SUFFIX_NUM_COLS = "_NUMCOLS";
	public static final String PARAM_SUFFIX_COLUMN = "_COL";
	public static final String PARAM_COL_SUFFIX_UCD = "_UCD";
	public static final String PARAM_COL_SUFFIX_UTYPE = "_UTYPE";
	public static final String PARAM_COL_SUFFIX_INDEXED = "_INDEXED";
	public static final String PARAM_COL_SUFFIX_FLAGS = "_FLAGS";

	private static final String ACTION = "edit"; 

	
	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public void handle(Map<String, String> parameters, long taskIdentifier, UwsJobOwner user, HttpServletResponse response, TAPService service) throws Exception {
		check(parameters);
		
//		int numTablesToProcess = -1;
//		String numCols = parameters.get(PARAM_NUM_TABLES);
//		try{
//			numTablesToProcess = Integer.parseInt(numCols);
//		}catch(NumberFormatException nfe){
//			service.getFactory().getOutputHandler().writeServerErrorResponse(
//					response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "TableTool", null, "Cannot get number of columns");
//			return;
//		}

		//This parameter is already checked by check method: it is a valid value.
		int numTablesToProcess = Integer.parseInt(parameters.get(PARAM_NUM_TABLES));

		execute(parameters, taskIdentifier, numTablesToProcess, user, service);
	}

	void check(Map<String,String> parameters) throws IllegalArgumentException{
		// Common checks
		// taskid is not mandatory.
		
		if(parameters.get(PARAM_NUM_TABLES) == null){
			throw new IllegalArgumentException("Error: number of tables not provided.");
		}
		//check all tables exists:
		int numTables;
		try{
			numTables = Integer.parseInt(parameters.get(PARAM_NUM_TABLES));
		}catch(NumberFormatException nfe){
			throw new IllegalArgumentException("Invalid number of tables: " + parameters.get(PARAM_NUM_TABLES));
		}
		String tableIndex;
		String numColsKey;
		String numCols;
		for(int i = 0; i < numTables; i++){
			//ensure param TABLEn exists
			tableIndex = PARAM_PREFIX_TABLE+i;
			if(!parameters.containsKey(tableIndex)){
				throw new IllegalArgumentException("Table parameter " + tableIndex + " not found.");
			}
			//ensure TABLEn_NUM_COLUMNS exists
			numColsKey = tableIndex + PARAM_SUFFIX_NUM_COLS;
			numCols = parameters.get(numColsKey);
			if(numCols == null){
				throw new IllegalArgumentException("Table number of columns parameter " + numColsKey + 
						" (table: "+parameters.get(tableIndex)+"): not found.");
			}
			//ensure TABLEn_NUM_COLUMNS is a valid number
			try{
				Integer.parseInt(numCols);
			}catch(NumberFormatException nfe){
				throw new IllegalArgumentException("Invalid number of columns parameter " + numColsKey + 
						" (table: "+parameters.get(tableIndex)+"): not a number: " + numCols);
			}
		}
	}


	public void execute(Map<String,String> parameters, long taskId, int numTables, UwsJobOwner user, TAPService service) throws Exception {
		String schemaName = TAPMetadata.getUserSchema(user);
		
		TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
				"TableToolConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
		
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(user);

		// Begin a DB transaction:
		dbConn.startTransaction();

		try{
			String tableSpace = service.getUserUploadTableSpace();
			Map<String, Boolean> allTables = new HashMap<String,Boolean>();
			String paramTablePrefix;
			String tableName;
			int numColumnsToProcess;
			boolean updated;
			for(int i = 0; i < numTables; i++){
				paramTablePrefix = PARAM_PREFIX_TABLE + i;
				tableName = TapUtils.getTableNameOnly(parameters.get(paramTablePrefix));
				String numCols = parameters.get(paramTablePrefix+PARAM_SUFFIX_NUM_COLS);
				numColumnsToProcess = Integer.parseInt(numCols);
				updated = executeTable(dbConn, tapSchemaInfo, parameters, taskId, numColumnsToProcess, schemaName, tableName, paramTablePrefix, tableSpace);
				TableToolUtils.updateTaskStatus(i+1, numTables, taskId);
				allTables.put(tableName, updated);
			}
			
			// Commit modifications:
			dbConn.endTransaction();
			
			boolean somethingUpdated = false;
			// Vacuum
			for(Entry<String, Boolean> e: allTables.entrySet()){
				if(e.getValue()){
					somethingUpdated = true;
					dbConn.vacuumAnalyze(schemaName, e.getKey());
				}
			}
			
			if(somethingUpdated){
				//generate event
				service.getFactory().getEventsManager().setEventTime(user, TAPFactory.TABLE_UPDATED_EVENT);
			}
				
		}catch(Exception e){
			dbConn.cancelTransaction();	// ROLLBACK
			throw e;
		}finally{
			try{
				dbConn.close();
			}catch(DBException ioe){
			}
		}
	}
	
	/**
	 * package-private for test-harnesses<br/>
	 * Updates tap columns table.
	 * @param dbConn
	 * @param tapSchemaInfo
	 * @param parameters request parameters.
	 * @param taskId task identifier (for progress information).
	 * @param numColumnsToProcess number of columns to be processed in the current request (COLn, where n = number of columns).
	 * @param paramTablePrefix table prefix
	 * @param tableSpace user table space
	 * @return 'true' if the table has been updated.
	 * @throws TAPException
	 */
	boolean executeTable(TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, Map<String,String> parameters, long taskId, int numColumnsToProcess, String schemaName, String tableName, String paramTablePrefix, String tableSpace) throws Exception {
		TapTableInfo tapTableInfo = dbConn.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);
		if(tapTableInfo == null){
			throw new IllegalArgumentException("Table not found: " + schemaName + "." + tableName);
		}
		boolean updated = false;
		IndexInfo indexInfo = new IndexInfo();
		for(int i = 0; i < numColumnsToProcess; i++){
			if(processField(i, dbConn, tapSchemaInfo, parameters, taskId, tapTableInfo, indexInfo, paramTablePrefix)){
				updated = true;
			}
		}
		
		if(indexInfo.isNewIndexRequested()){
			//A new ra/dec index creation will remove the previous ra/dec index if exists.
			if(indexRaDec(dbConn, tapSchemaInfo, tapTableInfo, numColumnsToProcess, parameters, paramTablePrefix, tableSpace)){
				updated = true;
			}
		}else{
			//No new ra/dec index has been requested.
			//Nevertheless, if an old ra/dec index removal was requested, it is necessary to remove them:
			if(indexInfo.isOldIndexRemovalRequested()){
				//old ra/dec index removal was requested.
				if(TableToolUtils.removePrevRaDecIfExists(dbConn, tapSchemaInfo, tapTableInfo)){
					updated = true;
				}
			}
		}
		return updated;
	}
	
	
	/**
	 * Updates a tap column.<br/>
	 * It checks whether the new values are different than the old ones.<br/>
	 * It updates the table and creates indexes if required (old indexes are removed if required also).<br/>
	 * In case that ra/dec index is requested, it is handled in a different procedure (returns 'true' in this case)<br/>
	 * ra/dec index cannot be executed here because this method handles column by column. ra/dec index involves two columns.
	 * @param index
	 * @param dbConn database connection.
	 * @param tapSchemaInfo tap schema info
	 * @param parameters request parameters.
	 * @param taskId task identifier (for progress information).
	 * @param tapTableInfo current database information.
	 * @param indexInfo (output) it contains information about ra/dec indexes.
	 * @param paramTablePrefix table prefix
	 * @return 'true' if the field has been updated.
	 * @throws DBException 
	 */
	private boolean processField(int index, TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, Map<String,String> parameters, long taskId, TapTableInfo tapTableInfo, IndexInfo indexInfo, String paramTablePrefix) throws DBException{
		String colIndex = paramTablePrefix + PARAM_SUFFIX_COLUMN + index;
		String tableColumnName = parameters.get(colIndex);
		String ucd = parameters.get(colIndex+PARAM_COL_SUFFIX_UCD);
		String uType = parameters.get(colIndex+PARAM_COL_SUFFIX_UTYPE);
		String sFlags = parameters.get(colIndex+PARAM_COL_SUFFIX_FLAGS);
		String sIndexed = parameters.get(colIndex+PARAM_COL_SUFFIX_INDEXED);
		
		//check input parameters
		if(tableColumnName == null){
			throw new IllegalArgumentException("Cannot process column " + index + ": column name not found");
		}
		if(ucd == null){
			throw new IllegalArgumentException("Cannot process column " + tableColumnName + ": ucd not found");
		}
		if(uType == null){
			throw new IllegalArgumentException("Cannot process column " + tableColumnName + ": utype not found");
		}
		if(sFlags == null){
			throw new IllegalArgumentException("Cannot process column " + tableColumnName + ": flags not found");
		}
		if(sIndexed == null){
			throw new IllegalArgumentException("Cannot process column " + tableColumnName + ": indexed not found");
		}
		
		//String flag is translated into integer
		int flags = TapUtils.convertTapTableFlag(sFlags);
		boolean bIndexedRequested = TapUtils.isTrueFromTapTableIndexed(sIndexed);
		int iIndexRequested = bIndexedRequested ? 1:0;
		String schemaName = tapTableInfo.getSchemaName();
		String tableName = tapTableInfo.getTableName();
		
		//Test whether we must update the table.
		
		boolean updated = false;

		//If any change in ucd, utype, flags or index is found, an update is required
		if(TapUtils.requireUpdate(tapTableInfo, tableColumnName, ucd, uType, flags, iIndexRequested)){
			dbConn.updateUserTableData(tapSchemaInfo, schemaName, tableName, tableColumnName, ucd, uType, flags, iIndexRequested);
			updated = true;
		}

		//Index:
		//In case the change is in index, if the index is removed, a remove request must be propagated to database.
		//Ra/Dec is an special case.

		if(bIndexedRequested){
			//want to create an index
			if(TapUtils.isRaOrDec(flags)){
				//special case: do not create index here
				indexInfo.setCreateNewIndex(true);
			} else {
				//Normal new index
				//It can be already indexed: normal and/or RaDec
				//if old index is not normal (i.e. Ra/Dec), the new normal index can be created (if not already created)
				if(!TapUtils.isNormalIndexed(tapTableInfo, tableColumnName)){
					//Remove index if exists
					dbConn.removeTableColumnIndex(schemaName, tableName, tableColumnName);
					//Create index
					dbConn.createTableColumnIndex(schemaName, tableName, tableColumnName);
					updated = true;
				}
				//new normal index was request. old ra/dec index is not requested
				//If it was ra/dec indexed, remove
				if(TapUtils.isRaDecIndexed(tapTableInfo, tableColumnName)){
					indexInfo.setRemoveOldIndex(true);
				}
			}
		} else {
			//want to remove index
			//If the column has a normal index => remove it
			if(TapUtils.isNormalIndexed(tapTableInfo, tableColumnName)){
				dbConn.removeTableColumnIndex(schemaName, tableName, tableColumnName);
				updated = true;
			}
			//If the column has a RaDec index => mark for further removal
			if(TapUtils.isRaDecIndexed(tapTableInfo, tableColumnName)){
				indexInfo.setRemoveOldIndex(true);
			}
		}
		
		return updated;
	}
	
	
	/**
	 * Creates a ra/dec index.<br/>
	 * Checks whether ra/dec are already indexed.<br/>
	 * The previous ra/dec index is removed (if it is found).<br/>
	 * It is expected that parameteres contains a 'flags' key that provides a single value: either 'ra' or 'dec'.
	 * If parameters does not contain columns for 'ra' and/or 'dec', an exception is raised.<br/>
	 * @param dbConn database connection.
	 * @param tapSchemaInfo tap schema info.
	 * @param tapTableInfo current database information.
	 * @param numColumnsToProcess number of columns to be processed in the current request (COLn, where n = number of columns).
	 * @param parameters request parameters.
	 * @param paramTablePrefix table prefix
	 * @param tableSpace user table space
	 * @return 'true' if the field has been updated.
	 * @throws DBException
	 */
	public boolean indexRaDec(TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, TapTableInfo tapTableInfo, int numColumnsToProcess, Map<String,String> parameters, String paramTablePrefix, String tableSpace) throws DBException{
		//Locate Ra
		//Locate Dec
		String raColumn = null;
		String decColumn = null;
		String colIndex;
		String flags;
		for(int i = 0; i < numColumnsToProcess; i++){
			colIndex = paramTablePrefix + PARAM_SUFFIX_COLUMN + i;
			flags = parameters.get(colIndex + PARAM_COL_SUFFIX_FLAGS);
			if(flags.equalsIgnoreCase(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA)){
				raColumn = parameters.get(colIndex);
				continue;
			}
			if(flags.equalsIgnoreCase(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC)){
				decColumn = parameters.get(colIndex);
				continue;
			}
		}
		
		//Manager ra/dec indexes
		return TableToolUtils.indexRaDec(dbConn, tapSchemaInfo, tapTableInfo, raColumn, decColumn, tableSpace);
	}
	
}
