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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.zip.GZIPOutputStream;

import uk.ac.starlink.fits.FitsTableWriter;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.votable.VOTableBuilder;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsStorage;

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
public class FitsFormatter implements OutputFormat {

	/** The {@link ServiceConnection} to use (for the log and to have some information about the service (particularly: name, description). */
	private final TAPService service;

	private boolean availableForSync = true;

	private String shortMimeType = "fits";
	private String mimeType = "application/fits";
	private String fileExtension = "fits.gz";

	/**
	 * Creates a FITS formatter.
	 * 
	 * @param service The service to use (for the log and to have some information about the service (particularly: name, description).
	 * 
	 * @throws NullPointerException	If the given service connection is <code>null</code>.
	 * 
	 */
	public FitsFormatter(final TAPService service) throws NullPointerException {
		if (service == null)
			throw new NullPointerException("The given service connection is NULL !");
		this.service = service;
	}

	public final String getShortMimeType() {
		return shortMimeType;
	}

	public String getDescription() {
		return null;
	}

	@Override
	public String getContentEncoding() {
		return null;
	};

	@Override
	public boolean availableForSync() {
		return availableForSync;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void setShortMimeType(String shortMimeType) {
		this.shortMimeType=shortMimeType;
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult,
			OutputStream output, TAPExecutionReport execReport)
			throws TAPException, InterruptedException, QuotaException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out)
			throws TAPException {
		
		GZIPOutputStream gzout = null;
		try {
			gzout = new GZIPOutputStream(out);
			UwsStorage storage = service.getFactory().getUwsManager().getFactory().getStorageManager();
			GzipVOTableStreamer streamer = new GzipVOTableStreamer( job, result.getId(), storage );
		    // new FitsTableWriter().writeStarTable( table, file );
		    new  FitsTableWriter().writeStarTable( streamer.createTable(), gzout );
		    // Finishing the GZIP output stream is needed to gracefully write the remaining data and close 
		    // the gzip file. Finish() closes the GZIP output stream but not the underlying stream.
		    gzout.finish();
		} catch (IOException e) {
			throw new TAPException(e);
		}finally{
			try {
				gzout.close();
			} catch (IOException e) {
				throw new TAPException("Unable to close output stream.",e);
			}
		}
	}

}
