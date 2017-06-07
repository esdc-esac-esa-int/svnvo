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
