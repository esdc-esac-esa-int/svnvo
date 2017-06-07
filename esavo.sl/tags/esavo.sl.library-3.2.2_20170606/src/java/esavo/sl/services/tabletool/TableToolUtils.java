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

import esavo.sl.services.status.TaskTypes;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TapUtils;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TapTableInfo;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

public class TableToolUtils {

	/**
	 * package-private for test-harnesses<br/>
	 * Updates status manager
	 * @param currentTableIndex
	 * @param numTables
	 * @param taskid
	 */
	public static void updateTaskStatus(int currentTableIndex, int numTables, long taskid){
		if(taskid < 0){
			//to task id to update
			return;
		}
		int percentDone = (int) Math.round(100.00 * currentTableIndex / numTables);
		UwsStatusData status = new UwsStatusData(TaskTypes.TYPE_TABLE_EDIT, ""+percentDone);
		try{
			UwsStatusManager.getInstance().updateStatus(taskid, status); 
		} catch (IllegalArgumentException iae){
			iae.printStackTrace();
		}
	}

	
	/**
	 * Creates a ra/dec index.<br/>
	 * Checks whether ra/dec are already indexed.<br/>
	 * The previous ra/dec index is removed (if it is found).<br/>
	 * @param dbConn database connection.
	 * @param tapTableInfo current database information.
	 * @param tapSchemaInfo schema info.
	 * @param raColumn
	 * @param decColumn
	 * @param tableSpace
	 * @return 'true' if the field has been updated.
	 * @throws DBException
	 */
	public static boolean indexRaDec(TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, TapTableInfo tapTableInfo, String raColumn, String decColumn, String tableSpace) throws DBException{
		if(raColumn == null){
			throw new IllegalArgumentException("Ra column not found");
		}
		if(decColumn == null){
			throw new IllegalArgumentException("Dec column not found");
		}

		//If it is already indexed, do not index again.
		//Remove old Ra/Dec if they are not the same columns.
		boolean alreadyIndexed = TapUtils.areAlreadyIndexedRaDec(tapTableInfo, raColumn, decColumn);
		if(alreadyIndexed){
			//Nothing to do, ra/dec are already indexed on the same columns.
			return false;
		}
		
		//Not the same columns.
		//Remove previous ra/dec if they exists
		removePrevRaDecIfExists(dbConn, tapSchemaInfo, tapTableInfo);
		
		//Create new indexes
		dbConn.createRaAndDecIndexes(tapSchemaInfo, tapTableInfo.getSchemaName(), tapTableInfo.getTableName(), raColumn, decColumn, TapUtils.TAP_TABLE_TYPE_RADEC, tableSpace); 
		return true;
	}

	/**
	 * Removes previous ra/dec index, if it is found.
	 * @param dbConn database connection
	 * @param tapSchemaInfo tap schema info
	 * @param tapTableInfo current database information.
	 * @return 'true' if the field has been updated.
	 * @throws DBException
	 */
	public static boolean removePrevRaDecIfExists(TapJDBCPooledFunctions dbConn, TAPSchemaInfo tapSchemaInfo, TapTableInfo tapTableInfo) throws DBException{
		String raColumn = null;
		String decColumn = null;
		int flags = 0;
		for(String tableColumnName: tapTableInfo.getTableColumnNames()){
			//tapTableInfo contains database current info: flags is an integer in database (it can be null)
			flags = TapUtils.getFlagsFromTapTable(tapTableInfo, tableColumnName);
			if((flags & TapUtils.TAP_COLUMN_TABLE_FLAG_RA) > 0){
				raColumn = tableColumnName;
				continue;
			}
			if((flags & TapUtils.TAP_COLUMN_TABLE_FLAG_DEC) > 0){
				decColumn = tableColumnName;
				continue;
			}
		}
		if(raColumn == null || decColumn == null){
			//wrong ra/dec specification, no index created.
			return false;
		}
		
		//we have the previous ra/dec indexed columns, remove them
		dbConn.removeRaAndDecIndexes(tapSchemaInfo, tapTableInfo.getSchemaName(), tapTableInfo.getTableName(), raColumn, decColumn, TapUtils.TAP_TABLE_TYPE_RADEC);
		return true;
	}
	
}
