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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.tabletool.TableToolHandler;
import esavo.sl.services.tabletool.TableToolUtils;
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

public class RaDecHandler implements TableToolHandler {

	private static final String ACTION = "radec";
	
	public static final String PARAM_TABLE = "TABLE_NAME";
	public static final String PARAM_RA = "RA";
	public static final String PARAM_DEC = "DEC";
	

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public void handle(Map<String, String> parameters, long taskIdentifier, UwsJobOwner user, HttpServletResponse response, TAPService service) throws Exception {
		check(parameters, user);
		execute(parameters, taskIdentifier, user, service);
	}
	
	void check(Map<String,String> parameters, UwsJobOwner user) throws IllegalArgumentException{
		// Common checks
		// taskid is not mandatory.
		
		if(parameters.get(PARAM_TABLE) == null){
			throw new IllegalArgumentException("Error: "+PARAM_TABLE+" parameter not provided.");
		}

		String schemaName = TAPMetadata.getUserSchema(user);
		String fullQualifiedTableName = parameters.get(PARAM_TABLE);
		String schema = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
		if(schema != null && !schemaName.equalsIgnoreCase(schema)){
			throw new IllegalArgumentException("Invalid schema for table '"+fullQualifiedTableName+"'");
		}

		String parameter = PARAM_RA;
		if(parameters.get(parameter) == null){
			throw new IllegalArgumentException("Error: "+parameter+" parameter not provided.");
		}
		if("".equals(parameter)){
			throw new IllegalArgumentException("Error: "+parameter+" parameter is empty.");
		}
		parameter = PARAM_DEC;
		if(parameters.get(parameter) == null){
			throw new IllegalArgumentException("Error: "+parameter+" parameter not provided.");
		}
		if("".equals(parameter)){
			throw new IllegalArgumentException("Error: "+parameter+" parameter is empty.");
		}
	}

	void execute(Map<String,String> parameters, long taskId, UwsJobOwner user, TAPService service) throws Exception {
		String schemaName = TAPMetadata.getUserSchema(user);
		String fullQualifiedTableName = parameters.get(PARAM_TABLE);
		String tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);

		TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
				"TableToolConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);

		// Begin a DB transaction:
		dbConn.startTransaction();
		
		TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(user);

		try{
			String tableSpace = service.getUserUploadTableSpace();
			boolean updated = executeTable(dbConn, tapSchemaInfo, parameters, taskId, schemaName, tableName, tableSpace);
			TableToolUtils.updateTaskStatus(1, 1, taskId);

			dbConn.endTransaction();

			if(updated){
				dbConn.vacuumAnalyze(schemaName, tableName);
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
	
	boolean executeTable(TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, Map<String,String> parameters, long taskId, String schemaName, String tableName, String tableSpace) throws Exception {
		TapTableInfo tapTableInfo = dbConn.fetchTapTableInfo(tapSchemaInfo, schemaName, tableName);
		if(tapTableInfo == null){
			throw new IllegalArgumentException("Table not found: " + schemaName + "." + tableName);
		}
		//get old ra/dec
		//if old ra/dec != new ra/dec => remove old ra/dec, create new ra/dec

		String newRa = parameters.get(PARAM_RA);
		String newDec = parameters.get(PARAM_DEC);
		
		String oldRa = TapUtils.findParameterByFlag(tapTableInfo, TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		String oldDec = TapUtils.findParameterByFlag(tapTableInfo, TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		
		if(newRa.equalsIgnoreCase(oldRa) && newDec.equalsIgnoreCase(oldDec)){
			return false;
		}
		
		TableToolUtils.removePrevRaDecIfExists(dbConn, tapSchemaInfo, tapTableInfo);
		
		//Get old flags of new ra/dec columns.
		//Keep flags but update ra/dec flags properly.
		int flagsRa = TapUtils.findFlagsFor(tapTableInfo, newRa);
		flagsRa |= TapUtils.TAP_COLUMN_TABLE_FLAG_RA;
		flagsRa &= (~TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		int flagsDec = TapUtils.findFlagsFor(tapTableInfo, newDec);
		flagsDec |= TapUtils.TAP_COLUMN_TABLE_FLAG_DEC;
		flagsDec &= (~TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		dbConn.updateUserTableRaDecData(tapSchemaInfo, schemaName, tableName, newRa, newDec, flagsRa, flagsDec);
		
		TableToolUtils.indexRaDec(dbConn, tapSchemaInfo, tapTableInfo, newRa, newDec, tableSpace);
		
		return true;
	}
}
