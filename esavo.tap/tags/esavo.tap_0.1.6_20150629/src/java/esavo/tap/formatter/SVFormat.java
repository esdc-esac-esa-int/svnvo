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
import java.io.PrintWriter;
import java.sql.ResultSet;

import cds.savot.writer.SavotWriter;
import esavo.adql.db.DBColumn;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;

public abstract class SVFormat implements OutputFormat {

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;

	public static final char COMMA_SEPARATOR = ',';
	public static final char SEMI_COLON_SEPARATOR = ';';
	public static final char TAB_SEPARATOR = '\t';

	protected final TAPService service;

	protected final String separator;
	protected final boolean delimitStr;

	public SVFormat(final TAPService service, char colSeparator){
		this(service, colSeparator, true);
	}

	public SVFormat(final TAPService service, char colSeparator, boolean delimitStrings){
		this(service, colSeparator, delimitStrings, false);
	}

	public SVFormat(final TAPService service, char colSeparator, boolean delimitStrings, final boolean logFormatReport){
		separator = ""+colSeparator;
		delimitStr = delimitStrings;
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	public SVFormat(final TAPService service, String colSeparator){
		this(service, colSeparator, true);
	}

	public SVFormat(final TAPService service, String colSeparator, boolean delimitStrings){
		separator = (colSeparator==null)?(""+COMMA_SEPARATOR):colSeparator;
		delimitStr = delimitStrings;
		this.service = service;
	}

	public String getMimeType() {
		if(separator.length()==0) return "text/plain";
		switch(separator.charAt(0)){
		case COMMA_SEPARATOR:
		case SEMI_COLON_SEPARATOR:
			return "text/csv";
		case TAB_SEPARATOR:
			return "text/tsv";
		default:
			return "text/plain";
		}
	}

	public String getShortMimeType() {
		if(separator.length()==0) return "text";
		switch(separator.charAt(0)){
		case COMMA_SEPARATOR:
		case SEMI_COLON_SEPARATOR:
			return "csv";
		case TAB_SEPARATOR:
			return "tsv";
		default:
			return "text";
		}
	}

	public String getDescription() { return null; }

	public String getFileExtension() {
		if(separator.length()==0) return "txt";
		switch(separator.charAt(0)){
		case COMMA_SEPARATOR:
		case SEMI_COLON_SEPARATOR:
			return "csv";
		case TAB_SEPARATOR:
			return "tsv";
		default:
			return "txt";
		}
	}

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult, OutputStream output, TAPExecutionReport execReport) throws TAPException, InterruptedException {
		try{
			int nbRows = 0;
			
			final long startTime = System.currentTimeMillis();

			final PrintWriter writer = new PrintWriter(output);

			// Write header:
			DBColumn[] columns = writeMetadata(job, queryResult, writer, execReport);
			if(job.isPhaseAborted()){
				return nbRows;
			}

			// Write data:
			nbRows = writeData(job, queryResult, columns, writer, execReport);
			if(job.isPhaseAborted()){
				return nbRows;
			}

			writer.flush();

			if (logFormatReport)
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in SV["+delimitStr+"] ; "+nbRows+" rows ; "+columns.length+" columns) in "+(System.currentTimeMillis()-startTime)+" ms !");
			
			return nbRows;
		}catch(Exception ex){
			service.getFactory().getLogger().error("While formatting in (T/C)SV !", ex);
			throw new TAPException(ex);
		}
	}

	protected abstract DBColumn[] writeMetadata(UwsJob job, ResultSet queryResult, PrintWriter writer, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException;

	protected abstract int writeData(UwsJob job, ResultSet queryResult, DBColumn[] selectedColumns, PrintWriter writer, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException;

	/**
	 * <p>Writes the given field value in the given OutputStream.</p>
	 * 
	 * <p>
	 * 	The given value will be encoded as an XML element (see {@link SavotWriter#encodeElement(String)}.
	 * 	Besides, if the given value is <code>null</code> and if the column datatype is <code>int</code>,
	 * 	<code>short</code> or <code>long</code>, the NULL values declared in the field metadata will be written.</p>
	 * 
	 * @param value				The value to write.
	 * @param column			The corresponding column metadata.
	 * @param out				The stream in which the field value must be written.
	 * 
	 * @throws IOException		If there is an error while writing the given field value in the given stream.
	 * @throws TAPException		If there is any other error (by default: never happen).
	 */
	protected void writeFieldValue(final Object value, final DBColumn column, final PrintWriter writer) throws IOException, TAPException {
		if (value != null){
			if ((delimitStr && value instanceof String) || value.toString().contains(separator)){
				writer.print('"');
				writer.print(value.toString().replaceAll("\"", "'"));
				writer.print('"');
			}else
				writer.print(value.toString());
		}
	}
}
