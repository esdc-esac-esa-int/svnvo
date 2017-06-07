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
package esavo.tap.db;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.starlink.table.StarTable;
import esavo.adql.query.ADQLQuery;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

/**
 * TODO
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2012
 * 
 * @param <R>	Result type of the execution of a query (see {@link #executeQuery(String, ADQLQuery)}.
 */
public interface DBConnection {

	public String getID();

	public void enableSeqScan(boolean allowSeqScan) throws DBException;
	
	public void startTransaction() throws DBException;

	public void cancelTransaction() throws DBException;

	public void endTransaction() throws DBException;
	
	public boolean isTransactionFinished() throws DBException;
	
	public long getTimeOut();
	
	public ResultSet executeQuery(String sqlQuery, ADQLQuery adqlQuery) throws DBException;

	public void createSchema(String schemaName) throws DBException;

	public void dropSchema(String schemaName) throws DBException;
	
	public TAPSchema loadSchema(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException;
	
	public String getUserTapSchema(UwsJobOwner user) throws DBException;

	/**
	 * 
	 * @param table
	 * @param tableSpace can be null
	 * @throws DBException
	 */
	public void createTable(final TAPTable table, final String tableSpace) throws DBException;

	public void registerInTapSchema(final TAPSchemaInfo tapSchemaInfo, final TAPSchema schema) throws DBException;
	
	public void registerInTapSchema(final TAPSchemaInfo tapSchemaInfo, final TAPTable table) throws DBException;
	public void unregisterFromTapSchema(final TAPSchemaInfo tapSchemaInfo, final TAPTable table) throws DBException;

	
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable) throws DBException, UwsException;
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable, long taskid) throws DBException, UwsException;
	public PreparedStatement createInsertRowStatement(TAPTable tableMetadata) throws DBException;
	public void loadTableDataBatch(UwsJobOwner owner, TAPTable table, Iterator<Object[]> rows, PreparedStatement statement) throws DBException, UwsException;
	
	public void dropTable(final TAPTable table) throws DBException;
	public void dropTable(final TAPTable table, final boolean forceRemoval) throws DBException;

	public void close() throws DBException;
	
	long getDbSize(String ownerid) throws DBException;
	long getTableSize(String schema, String table) throws DBException;
	
	public boolean isTablePublic(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException;
	public List<Boolean> isTablePublic(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableName) throws DBException;
	public boolean isSchemaPublic(TAPSchemaInfo tapSchemaInfo, String schemaName) throws DBException;
	public List<String> getPublicSchemas(TAPSchemaInfo tapSchemaInfo) throws DBException;
	
	/**
	 * Publish a table in tab_schema. If the schema does not exist, the schema is created too.
	 * If the schema exists, only the table and columns are created.
	 * @param tapSchemaInfo
	 * @param fullQualifiedTableNames
	 * @param calculateArrayDims
	 * @throws DBException
	 */
	public Map<String,String> publishTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean calculateArrayDims) throws DBException;
	/**
	 * If 'isPublic' is false (private) the schema will be public if any table associated to the schema is public.
	 * @param tapSchemaInfo
	 * @param fullQualifiedTableNames
	 * @param isPublic
	 * @throws DBException
	 */
	public Map<String,String> setPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames, boolean isPublic) throws DBException;
	/**
	 * Returns the status of the tables
	 * @param tapSchemaInfo
	 * @param fullQualifiedTableNames
	 * @return
	 * @throws DBException
	 */
	public Map<String,String> getPublicStatusForTablesInTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException;
	/**
	 * Removes entries from tap_schema. If the schema contains more tables, the schema is kept.
	 * If no more tables are associated to this schema, the schema is removed too.
	 * @param tapSchemaInfo
	 * @param fullQualifiedTableNames
	 * @throws DBException
	 */
	public Map<String,String> removeTablesFromTapSchema(TAPSchemaInfo tapSchemaInfo, List<String> fullQualifiedTableNames) throws DBException;
	

}
