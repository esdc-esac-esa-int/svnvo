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
package esavo.tap.log;

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

import esavo.tap.TAPExecutionReport;
import esavo.tap.db.DBConnection;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPTable;

/**
 * Lets logging any kind of message about a TAP service.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2012
 */
public interface TAPLog {

	public void queryFinished(final TAPExecutionReport report);

	public void dbInfo(final String message);

	public void dbError(final String message, final Throwable t);

	public void tapMetadataFetched(final TAPMetadata metadata);

	public void tapMetadataLoaded(final TAPMetadata metadata);

	public void connectionOpened(final DBConnection connection, final String dbName);

	public void connectionClosed(final DBConnection connection);

	public void transactionStarted(final DBConnection connection);

	public void transactionCancelled(final DBConnection connection);

	public void transactionEnded(final DBConnection connection);

	public void schemaCreated(final DBConnection connection, final String schema);

	public void schemaDropped(final DBConnection connection, final String schema);

	public void tableCreated(final DBConnection connection, final TAPTable table);

	public void tableDropped(final DBConnection connection, final TAPTable table);

	public void rowsInserted(final DBConnection connection, final TAPTable table, final int nbInsertedRows);

	public void sqlQueryExecuting(final DBConnection connection, final String sql);

	public void sqlQueryError(final DBConnection connection, final String sql, final Throwable t);

	public void sqlQueryExecuted(final DBConnection connection, final String sql);
	
	public void warning(final String message);

	public void info(final String message);
	
	public void error(Throwable t);

	public void error(String message);

	public void error(String message, Throwable t);

}
