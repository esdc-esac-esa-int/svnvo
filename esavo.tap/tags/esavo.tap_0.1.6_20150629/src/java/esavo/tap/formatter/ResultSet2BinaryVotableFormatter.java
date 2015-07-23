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

import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOTableVersion;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.QuotaException;

/**
 * Formats a {@link ResultSet} into a binary VOTable.
 * 
 * @author Raul Gutierrez
 * @version 01/2014
 *
 * @param <R>	Type of the result to format in VOTable (i.e. {@link java.sql.ResultSet}).
 * 
 * @see ResultSet2VotableFormatter
 */
public class ResultSet2BinaryVotableFormatter implements OutputFormat {

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;

	/** The {@link ServiceConnection} to use (for the log and to have some information about the service (particularly: name, description). */
	protected final TAPService service;

	protected String votTableVersion = "1.2";
	protected String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
	protected String xsiSchemaLocation = "http://www.ivoa.net/xml/VOTable/v1.2";
	protected String xsiNoNamespaceSchemaLocation = null;
	protected String xmlns = "http://www.ivoa.net/xml/VOTable/v1.2";

	/**
	 * Creates a VOTable formatter without format report.
	 * 
	 * @param service				The service to use (for the log and to have some information about the service (particularly: name, description).
	 * 
	 * @throws NullPointerException	If the given service connection is <code>null</code>.
	 * 
	 * @see #VOTableFormat(ServiceConnection, boolean)
	 */
	public ResultSet2BinaryVotableFormatter(final TAPService service) throws NullPointerException {
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
	public ResultSet2BinaryVotableFormatter(final TAPService service, final boolean logFormatReport) throws NullPointerException {
		if (service == null)
			throw new NullPointerException("The given service connection is NULL !");
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	public final String getMimeType() { return "application/x-votable+xml"; }

	public final String getShortMimeType() { return "votable"; }

	public String getDescription() { return null; }

	public String getFileExtension() { return "xml"; }

	@Override
	public final long writeResult(final UwsJob job, final ResultSet queryResult, final OutputStream output, final TAPExecutionReport execReport) throws TAPException, InterruptedException {
		try{
			long start = System.currentTimeMillis();
			int  maxRec = execReport.parameters.getMaxRec();
			long nbRows = 0;

			TapWriter writer =
					new TapWriter(job, DataFormat.BINARY, VOTableVersion.V12, maxRec );
			try {
				nbRows=writer.writeVOTable(execReport.resultingColumns, queryResult, output );
			}catch(SQLException e){
				throw new TAPException(e);
			}catch(IOException e){
				if(e instanceof QuotaException){
					throw new QuotaException(e);
				}
			}
			if (logFormatReport)
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in VOTable ; "+nbRows+" rows) in "+(System.currentTimeMillis()-start)+" ms !");

			return nbRows;
		}catch(IOException ioe){
			throw new TAPException("Error while writing a query result in VOTable !", ioe);
		}
	}

}
