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

import org.json.JSONException;
import org.json.JSONWriter;

import cds.savot.writer.SavotWriter;
import esavo.adql.db.DBColumn;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPTypes;
import esavo.uws.jobs.UwsJob;

public abstract class JSONFormat implements OutputFormat {

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;

	/** The {@link ServiceConnection} to use (for the log and to have some information about the service (particularly: name, description). */
	protected final TAPService service;


	public JSONFormat(final TAPService service){
		this(service, false);
	}

	public JSONFormat(final TAPService service, final boolean logFormatReport){
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	public String getMimeType() { return "application/json"; }

	public String getShortMimeType() { return "json"; }

	public String getDescription() { return null; }

	public String getFileExtension() { return "json"; }

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult, OutputStream output, TAPExecutionReport execReport) throws TAPException, InterruptedException {
		try{
			int nbRows = 0;
			
			long start = System.currentTimeMillis();

			PrintWriter writer = new PrintWriter(output);
			JSONWriter out = new JSONWriter(writer);

			out.object();

			out.key("metadata");
			DBColumn[] columns = writeMetadata(job, queryResult, out, execReport);
			if(job.isPhaseAborted()){
				return nbRows;
			}

			writer.flush();

			out.key("data");
			nbRows = writeData(job, queryResult, columns, out, execReport);
			if(job.isPhaseAborted()){
				return nbRows;
			}

			out.endObject();
			writer.flush();

			if (logFormatReport)
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in JSON ; "+nbRows+" rows ; "+columns.length+" columns) in "+(System.currentTimeMillis()-start)+" ms !");
			
			return nbRows;
		}catch(JSONException je){
			throw new TAPException("Error while writing a query result in JSON !", je);
		}catch(IOException ioe){
			throw new TAPException("Error while writing a query result in JSON !", ioe);
		}
	}

	protected abstract DBColumn[] writeMetadata(UwsJob job, ResultSet queryResult, JSONWriter out, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException, JSONException;

	/**
	 * <p>Formats in a VOTable field and writes the given {@link TAPColumn} in the given Writer.</p>
	 * 
	 * <p><i><u>Note:</u> If the VOTable datatype is <code>int</code>, <code>short</code> or <code>long</code> a NULL values is set by adding a node VALUES: &lt;VALUES null="..." /&gt;</i></p>
	 * 
	 * @param col				The column metadata to format into a VOTable field.
	 * @param out				The stream in which the formatted column metadata must be written.
	 * 
	 * @throws IOException		If there is an error while writing the field metadata.
	 * @throws TAPException		If there is any other error (by default: never happen).
	 */
	protected void writeFieldMeta(TAPColumn tapCol, JSONWriter out) throws IOException, TAPException, JSONException {
		out.object();

		out.key("name").value(tapCol.getName());

		if (tapCol.getDescription() != null && tapCol.getDescription().trim().length() > 0)
			out.key("description").value(tapCol.getDescription());

		out.key("datatype").value(tapCol.getVotType().datatype);

		int arraysize = tapCol.getVotType().arraysize;
		if (arraysize == TAPTypes.STAR_SIZE)
			out.key("arraysize").value("*");
		else if (arraysize > 0)
			out.key("arraysize").value(""+arraysize);
			//out.key("arraysize").value(arraysize);

		if (tapCol.getVotType().xtype != null)
			out.key("xtype").value(tapCol.getVotType().xtype);

		if (tapCol.getUnit() != null && tapCol.getUnit().length() > 0)
			out.key("unit").value(tapCol.getUnit());

		if (tapCol.getUcd() != null && tapCol.getUcd().length() > 0)
			out.key("ucd").value(tapCol.getUcd());

		if (tapCol.getUtype() != null && tapCol.getUtype().length() > 0)
			out.key("utype").value(tapCol.getUtype());

		out.endObject();
	}

	protected abstract int writeData(UwsJob job, ResultSet queryResult, DBColumn[] selectedColumns, JSONWriter out, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException, JSONException;

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
	protected void writeFieldValue(final Object value, final DBColumn column, final JSONWriter out) throws IOException, TAPException, JSONException {
		if (value instanceof Double && (((Double)value).isNaN() || ((Double)value).isInfinite()))
			out.value((Object)null);
		else if (value instanceof Float && (((Float)value).isNaN() || ((Float)value).isInfinite()))
			out.value((Object)null);
		else
			out.value(value);
	}
}
