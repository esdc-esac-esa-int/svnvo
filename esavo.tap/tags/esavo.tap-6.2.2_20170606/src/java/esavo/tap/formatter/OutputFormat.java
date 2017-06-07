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

import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.storage.QuotaException;


/**
 * Describes an output format and formats a given query result into this format.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 *
 * @param <R>	The type of raw query result (i.e. {@link java.sql.ResultSet}).
 * 
 * @version 06/2012
 * 
 * @see VOTableFormat
 */
public interface OutputFormat {

	/**
	 * Indicates if this formatter should be available for synchronous interface.
	 * @return boolean
	 */
	public boolean availableForSync();
	
	/**
	 * Gets the MIME type corresponding to this format.
	 * 
	 * @return	Its MIME type (MUST BE DIFFERENT FROM NULL).
	 */
	public String getMimeType();

	/**
	 * Gets the Content Encoding corresponding to this format.
	 * 
	 * @return	Its Content Encoding
	 */
	public String getContentEncoding();

	/**
	 * Gets a short expression of its MIME type.
	 * 
	 * @return	Its short MIME type.
	 */
	public String getShortMimeType();

	/**
	 * Specifies the short mime type (in case of clashes)
	 * @param shortMimeType
	 */
	public void setShortMimeType(String shortMimeType);

	/**
	 * Gets a description of this format.
	 * 
	 * @return	Its description.
	 */
	public String getDescription();

	/**
	 * Gets a file extension for this format.
	 * 
	 * @return	Its file extension.
	 */
	public String getFileExtension();

	/**
	 * Formats the given query result and writes it in the given output stream.
	 * 
	 * @param job				The job.
	 * @param queryResult		The raw result to format (i.e. a {@link java.sql.ResultSet}).
	 * @param output			The output stream (a ServletOutputStream or a stream on a file) in which the formatted result must be written.
	 * @param execReport		The report of the execution of the TAP query whose the result must be now written.
	 * 
	 * @throws TAPException		If there is an error while formatting/writing the query result.
	 */
	public long writeResult(final UwsJob job, final ResultSet queryResult, final OutputStream output, final TAPExecutionReport execReport) throws TAPException, InterruptedException, QuotaException;

//	/**
//	 * Formats the given query result and writes it in the given output stream.
//	 * 
//	 * @param queryResult		The raw result to format (i.e. a {@link java.sql.ResultSet}).
//	 * @param output			The output stream (a ServletOutputStream or a stream on a file) in which the formatted result must be written.
//	 * @param execReport		The report of the execution of the TAP query whose the result must be now written.
//	 * @param thread			The thread which has asked the result writting.
//	 * 
//	 * @throws TAPException		If there is an error while formatting/writing the query result.
//	 */
//	public long writeResult(final R queryResult, final OutputStream output, final TAPExecutionReport execReport, final Thread thread) throws TAPException, InterruptedException;

	/*
	 * Formats the given query result and writes it in some way accessible through the returned {@link Result}.
	 * 
	 * @param queryResult		The raw result to format (i.e. a {@link java.sql.ResultSet}).
	 * @param job				The job which processed the query.
	 * 
	 * @return					The {@link Result} which provides an access to the formatted query result.
	 * 
	 * @throws TAPException		If there is an error while formatting/writing the query result.
	 *
	public Result writeResult(final R queryResult, final TAPJob job) throws TAPException;*/
	
	
	/**
	 * Translates the input stream.
	 * @param in input stream in common format.
	 * @param out output stream in formatter's own format.
	 * @throws IOException
	 */
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out) throws TAPException;

}
