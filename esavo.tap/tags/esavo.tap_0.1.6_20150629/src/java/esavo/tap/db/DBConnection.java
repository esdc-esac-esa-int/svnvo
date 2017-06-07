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

import java.sql.ResultSet;

import uk.ac.starlink.table.StarTable;
import esavo.adql.query.ADQLQuery;
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

	public void startTransaction() throws DBException;

	public void cancelTransaction() throws DBException;

	public void endTransaction() throws DBException;

	public ResultSet executeQuery(final String sqlQuery, final ADQLQuery adqlQuery) throws DBException;

	public void createSchema(final String schemaName) throws DBException;

	public void dropSchema(final String schemaName) throws DBException;

	public void createTable(final TAPTable table) throws DBException;

	public void registerInTapSchema(final TAPTable table) throws DBException;
	public void unregisterFromTapSchema(final TAPTable table) throws DBException;
	
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable) throws DBException, UwsException;
	public int loadTableData(UwsJobOwner owner, TAPTable table, StarTable starTable, long taskid) throws DBException, UwsException;
	
	public void dropTable(final TAPTable table) throws DBException;
	public void dropTable(final TAPTable table, final boolean forceRemoval) throws DBException;

	public void close() throws DBException;
	
	long getDbSize(String ownerid) throws DBException;
	long getTableSize(String schema, String table) throws DBException;
	

}
