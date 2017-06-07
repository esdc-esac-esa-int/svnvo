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

import java.io.OutputStream;
import java.sql.ResultSet;

import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;

public class DirectFormat implements OutputFormat {
	
	public static final String DIRECT_FORMAT = "direct";

	private boolean availableForSync = true;
	
	/** The {@link ServiceConnection} to use (for the log and to have some information about the service (particularly: name, description). */
	protected final TAPService service;

	private String shortMimeType = DIRECT_FORMAT;

	public DirectFormat(final TAPService service){
		this.service = service;
	}

	public String getMimeType() {
		return "text/plain";
	}

	public String getShortMimeType() {
		return shortMimeType;
	}

	public String getDescription() {
		return null;
	}

	public String getFileExtension() {
		return ".txt";
	}

	public String getContentEncoding() {
		return null;
	}

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult, OutputStream output, TAPExecutionReport execReport) throws TAPException, InterruptedException {
		throw new TAPException("Not allowed");
	}
	
	/**
	 * @param shortMimeType the shortMimeType to set
	 */
	public void setShortMimeType(String shortMimeType) {
		this.shortMimeType = shortMimeType;
	}

	@Override
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out) throws TAPException {
		throw new TAPException("Not allowed");
	}

	@Override
	public boolean availableForSync() {
		return availableForSync;
	}

}
