package esavo.tap.formatter;

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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOTableVersion;
import uk.ac.starlink.votable.VOTableWriter;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.QuotaException;

/**
 * Formats a {@link ResultSet} into a binary VOTable.
 * 
 * @author Raul Gutierrez
 * @version 05/2016
 *
 * @param <R>	Type of the result to format in Gzipped VOTable (i.e. {@link java.sql.ResultSet}).
 * 
 * @see ResultSet2VotableFormatter
 */
public abstract class AbstractBinary2VotableFormatter implements OutputFormat {

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;

	/** The {@link ServiceConnection} to use (for the log and to have some information about the service (particularly: name, description). */
	protected final TAPService service;

	protected VOTableVersion votTableVersion = VOTableVersion.V13;

	/**
	 * Creates a VOTable formatter without format report.
	 * 
	 * @param service				The service to use (for the log and to have some information about the service (particularly: name, description).
	 * 
	 * @throws NullPointerException	If the given service connection is <code>null</code>.
	 * 
	 * @see #VOTableFormat(ServiceConnection, boolean)
	 */
	public AbstractBinary2VotableFormatter(final TAPService service) throws NullPointerException {
		this(service, false);
	}

	/**
	 * Creates a VOTable formatter.
	 * 
	 * @param service				The service to use (for the log and to have some information about the service (particularly: name, description).
	 * @param logFormatReport		<code>true</code> to append a format report (start and end date/time) in the log output, <code>false</code> otherwise.
	 * 
	 * @throws NullPointerException	If the given service connection is <code>null</code>.
	 */
	public AbstractBinary2VotableFormatter(final TAPService service, final boolean logFormatReport) throws NullPointerException {
		if (service == null)
			throw new NullPointerException("The given service connection is NULL !");
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	public final String getMimeType() {
		return "application/x-votable+xml";
	}

	public abstract String getShortMimeType();

	public abstract String getDescription();

	public String getFileExtension() {
		return "vot";
	}

	@Override
	public abstract String getContentEncoding();

	@Override
	public long writeResult(final UwsJob job, final ResultSet queryResult, final OutputStream output, final TAPExecutionReport execReport) throws TAPException, InterruptedException, QuotaException {
		try{
			long start = System.currentTimeMillis();
			int  maxRec = execReport.parameters.getMaxRec();
			long nbRows = 0;

			TapWriter writer =
					new TapWriter(job, DataFormat.BINARY2, votTableVersion, maxRec, service);
			try {
				nbRows=writer.writeVOTable(execReport.resultingColumns, queryResult, output );
			}catch(SQLException e){
				throw new TAPException(e);
			}
			if (logFormatReport){
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in VOTable ; "+nbRows+" rows) in "+(System.currentTimeMillis()-start)+" ms !");
			}

			return nbRows;
		}catch(IOException ioe){
			if(ioe instanceof QuotaException){
				throw new QuotaException(ioe);
			}
			throw new TAPException("Error while writing a query result in VOTable !", ioe);
		}
	}

	/**
	 * @param shortMimeType the shortMimeType to set
	 */
	public abstract void setShortMimeType(String shortMimeType);
}
