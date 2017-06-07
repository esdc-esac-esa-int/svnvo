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
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.storage.UwsStorage;

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
public class Binary2VotableFormatter extends AbstractBinary2VotableFormatter {

	private String shortMimeType = "votable_bin";

	/**
	 * Creates a VOTable formatter without format report.
	 * 
	 * @param service				The service to use (for the log and to have some information about the service (particularly: name, description).
	 * 
	 * @throws NullPointerException	If the given service connection is <code>null</code>.
	 * 
	 * @see #VOTableFormat(ServiceConnection, boolean)
	 */
	public Binary2VotableFormatter(final TAPService service) throws NullPointerException {
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
	public Binary2VotableFormatter(final TAPService service, final boolean logFormatReport) throws NullPointerException {
		super(service,logFormatReport);
//		if (service == null)
//			throw new NullPointerException("The given service connection is NULL !");
//		this.service = service;
//		this.logFormatReport = logFormatReport;
	}

	public final String getShortMimeType() {
		return shortMimeType;
	}

	@Override
	public String getContentEncoding() {
		return null;
	};

	public String getDescription() {
		return null;
	}

	public String getFileExtension() {
		return "xml";
	}

	/**
	 * @param shortMimeType the shortMimeType to set
	 */
	public void setShortMimeType(String shortMimeType) {
		this.shortMimeType = shortMimeType;
	}

	@Override
	public boolean availableForSync() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out)
			throws TAPException {
		try {
			UwsStorage storage = service.getFactory().getUwsManager().getFactory().getStorageManager();
			InputStream in = storage.getJobResultDataInputSource(job, result.getId());
			IOUtils.copy(new GZIPInputStream(in), out);
			in.close();
		} catch (IOException e) {
			throw new TAPException(e);
		} catch (UwsException e){
			throw new TAPException(e);
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				throw new TAPException("Unable to close output stream.",e);
			}
		}
	}

}
