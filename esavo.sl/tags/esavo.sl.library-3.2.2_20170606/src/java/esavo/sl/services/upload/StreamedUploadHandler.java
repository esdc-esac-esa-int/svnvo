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
package esavo.sl.services.upload;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import esavo.sl.services.util.Utils;
import esavo.tap.TAPException;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.tap.TapUtils;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;

public class StreamedUploadHandler implements TableSink {

	private TAPService service;
	private TapJDBCPooledFunctions dbConn;
	private UwsJobOwner owner;
	private TAPSchema schema;
	private String tableName;
	private String tableDesc;
	private String raCol;
	private String deCol;
	
	private String tableSpace;
	
	private TAPTable tapTable;
	PreparedStatement insertStatement;
	
	private StringBuilder message;
	
	private ArrayList<Object[]> batchRows = new ArrayList<Object[]>();
	
	private TAPSchemaInfo tapSchemaInfo;
	
	public StreamedUploadHandler(TAPService service, TapJDBCPooledFunctions dbConn, UwsJobOwner owner, 
								TAPSchema schema, String tableName, String tableDesc, String raCol, String deCol,
								String tableSpace) throws TAPException {
		this.service = service;
		this.dbConn = dbConn;
		this.owner = owner;
		this.schema = schema;
		this.tableName = tableName;
		this.tableDesc = tableDesc;
		this.raCol = raCol;
		this.deCol = deCol;
		this.tableSpace = tableSpace;
		this.message=new StringBuilder();
		this.tapSchemaInfo = service.getTapSchemaInfo(owner);
	}
	
	@Override
	public void acceptMetadata(StarTable metadata) throws TableFormatException {
		
		try {
			// 1st STEP: Convert the VOTable metadata into DBTable:
			tapTable = fetchTableMeta(tableName, tableDesc, metadata, raCol, deCol);
			schema.addTable(tapTable);

			// 2nd STEP: Create the corresponding table in the database:
			dbConn.createTable(tapTable, tableSpace);
			
			dbConn.registerInTapSchema(tapSchemaInfo, tapTable);
			
			insertStatement = dbConn.createInsertRowStatement(tapTable);
		} catch (DBException e) {
			try {
				if(insertStatement!=null) insertStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new TableFormatException(e.getMessage(),e);
		}
	}

	@Override
	public void acceptRow(Object[] row) throws IOException {
		try {
			batchRows.add(row);
			if(batchRows.size()>0 && batchRows.size()%5000==0){
				dbConn.loadTableDataBatch(owner, tapTable, batchRows.iterator(), insertStatement);
				batchRows.clear();
			}
			
		} catch (DBException e) {
			try {
				insertStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new UploadDBException(e);
		} catch (UwsException e) {
			try {
				insertStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new UploadUwsException(e);
		}
	}

	@Override
	public void endRows() throws IOException {
		try {
			dbConn.loadTableDataBatch(owner, tapTable, batchRows.iterator(), insertStatement);
			batchRows.clear();
		
			// 5th STEP: Update table size in TAP_SCHEMA
			dbConn.updateTableSizeInTapSchema(tapSchemaInfo, tapTable);
				
			// 6th Create PK
			dbConn.createPkInTable(tapSchemaInfo, tapTable);
			
			// 7th Create Indexes if ra/dec are available
			if(	raCol!=null && raCol.length()>0  
					&& deCol!=null && deCol.length()>0){
					dbConn.createRaAndDecIndexes(tapSchemaInfo, tapTable.getDBSchemaName(), tableName,  
							raCol, deCol, TapUtils.TAP_TABLE_TYPE_RADEC, tableSpace);
			}

		} catch (DBException e) {
			throw new UploadDBException(e);
		} catch (UwsException e) {
			throw new UploadUwsException(e);
		} finally{
			try {
				insertStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	
	
	/**
	 * Fetches table metadata into a TAPTable object
	 * @param tableName
	 * @param tableDesc
	 * @param votable
	 * @param ra_column Name of the ICRS RA column (to overwrite its UTYPE)
	 * @param dec_column Name of the ICRS DEC column (to overwrite its UTYPE)
	 * @return
	 */
	private TAPTable fetchTableMeta(final String tableName, final String tableDesc, final StarTable tableMetadata, final String ra_column, final String dec_column){
		//Table upload is available only for authenticated users.
		//All table users are private
		TAPTable tapTable = new TAPTable(tableName, TAPTable.PRIVATE_TABLE);
		
		if(tableDesc!=null && !tableDesc.trim().isEmpty()){
			tapTable.setDescription(tableDesc);
		}
		tapTable.setDBName(tableName);

		boolean foundRa = false;
		boolean foundDec = false;
		int flags;
		
		Map<String,Integer> columnNameCount = new HashMap<String,Integer>();
		
		for(int col=0 ; col<tableMetadata.getColumnCount(); col++){
			ColumnInfo field = tableMetadata.getColumnInfo(col);
			//fieldNameLowerCase = field.getName().toLowerCase();
			
			int arraysize = 0;
			try{
				arraysize = TapUtils.getArraySize(field);
			}catch(IllegalArgumentException iae){
				service.getFactory().getLogger().warning("Invalid array-size in the uploaded table \""+tableName+"\": "+iae.getMessage()+". It will be considered as \"*\" !");
			}
			
			flags = 0;
			
			if(ra_column!=null && !ra_column.trim().isEmpty() && !foundRa && field.getName().equalsIgnoreCase(ra_column)){
				foundRa = true;
				flags |= TapUtils.TAP_COLUMN_TABLE_FLAG_RA;
			}
			
			if(dec_column!=null && !dec_column.trim().isEmpty() && !foundDec && field.getName().equalsIgnoreCase(dec_column)){
				foundDec = true;
				flags |= TapUtils.TAP_COLUMN_TABLE_FLAG_DEC;
			}
			
			// Correct column name to be accepted by postgres
			String originalFieldName = field.getName().toLowerCase();
			field.setName(Utils.getAdqlPostgresProperColumnName(field.getName()));
			if(!originalFieldName.equals(field.getName())){
				message.append("Invalid column name ").append(originalFieldName).append(" renamed to ").append(field.getName()).append(";");
			}
			
			// If column already exists, add trailing numeral.
			Integer colCount = columnNameCount.get(field.getName());
			if(colCount==null){
				columnNameCount.put(field.getName(), 1);
			}else{
				colCount++;
				columnNameCount.put(field.getName(), colCount);
				originalFieldName = field.getName();
				field.setName(field.getName()+"_"+colCount);
				message.append("Duplicated column ").append(originalFieldName).append(" renamed to ").append(field.getName()).append(";");
			}
			
			TapUtils.addColumnToTable(tapTable, field, arraysize, flags, col);

		}
		
		if(ra_column!=null && !ra_column.trim().isEmpty() && !foundRa){
			service.getFactory().getLogger().error("RA Column "+ra_column+" not found in table");
		}
		if(dec_column!=null && !dec_column.trim().isEmpty() && !foundDec){
			service.getFactory().getLogger().error("DEC Column "+dec_column+" not found in table");
		}

		return tapTable;
	}
	
	public TAPTable getTAPTable(){
		return tapTable;
	}

	public String getMessage(){
		return message.toString();
	}

}
