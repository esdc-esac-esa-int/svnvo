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

import java.io.OutputStream;
import java.sql.ResultSet;

import esavo.adql.db.DBColumn;
import esavo.adql.utils.AsciiTable;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;

public abstract class TextFormat implements OutputFormat {

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;

	protected final TAPService service;

	public TextFormat(final TAPService service){
		this(service, false);
	}

	public TextFormat(final TAPService service, final boolean logFormatReport){
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	public String getMimeType() { return "text/plain"; }

	public String getShortMimeType() { return "text"; }

	public String getDescription() { return null; }

	public String getFileExtension() { return "txt"; }

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult, OutputStream output, TAPExecutionReport execReport) throws TAPException, InterruptedException {
		try{
			AsciiTable asciiTable = new AsciiTable('|');

			final long startTime = System.currentTimeMillis();

			// Write header:
			String headerLine = getHeader(queryResult, execReport);
			asciiTable.addHeaderLine(headerLine);
			asciiTable.endHeaderLine();
			
			int nbRows = 0;
			
			if(job.isPhaseAborted()){
				return nbRows;
			}

			// Write data:
			nbRows = writeData(job, queryResult, asciiTable, execReport);
			
			if(job.isPhaseAborted()){
				return nbRows;
			}

			// Write all lines in the output stream:
			String[] lines = asciiTable.displayAligned(new int[]{AsciiTable.LEFT});
			for(String l : lines){
				output.write(l.getBytes());
				output.write('\n');
			}
			output.flush();

			if (logFormatReport)
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in text ; "+nbRows+" rows ; "+((execReport != null && execReport.resultingColumns != null)?"?":execReport.resultingColumns.length)+" columns) in "+(System.currentTimeMillis()-startTime)+" ms !");

			return nbRows;
		}catch(Exception ex){
			service.getFactory().getLogger().error("While formatting in text/plain !", ex);
			throw new TAPException(ex);
		}
	}

	protected abstract String getHeader(final ResultSet queryResult, final TAPExecutionReport execReport) throws TAPException;

	protected abstract int writeData(final UwsJob job, final ResultSet queryResult, final AsciiTable asciiTable, final TAPExecutionReport execReport) throws TAPException;

	protected void writeFieldValue(final Object value, final DBColumn tapCol, final StringBuffer line){
		Object obj = value;
		if (obj != null)
			line.append(obj.toString());
	}
}
