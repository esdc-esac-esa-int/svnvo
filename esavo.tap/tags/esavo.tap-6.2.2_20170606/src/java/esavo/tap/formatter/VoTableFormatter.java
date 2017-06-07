package esavo.tap.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.GZIPOutputStream;

import nom.tam.util.DataTable;
import uk.ac.starlink.fits.FitsTableWriter;
import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOTableVersion;
import uk.ac.starlink.votable.VOTableWriter;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsStorage;

public class VoTableFormatter implements OutputFormat {

	protected VOTableVersion votTableVersion = VOTableVersion.V13;

	private String shortMimeType = "votable_plain";

	/** Indicates whether a format report (start and end date/time) must be printed in the log output.  */
	private boolean logFormatReport;
	
	private TAPService service;
	
	public VoTableFormatter(TAPService service){
		this(service, false);
	}
	
	public VoTableFormatter(TAPService service, boolean logFormatReport){
		this.service = service;
		this.logFormatReport = logFormatReport;
	}

	@Override
	public boolean availableForSync() {
		return true;
	}

	@Override
	public String getContentEncoding() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getFileExtension() {
		return "vot";
	}

	@Override
	public long writeResult(UwsJob job, ResultSet queryResult,
			OutputStream output, TAPExecutionReport execReport)
			throws TAPException, InterruptedException, QuotaException {
		try{
			long start = System.currentTimeMillis();
			int  maxRec = execReport.parameters.getMaxRec();
			long nbRows = 0;

			TapWriter writer =
					new TapWriter(job, DataFormat.TABLEDATA, votTableVersion, maxRec, service);
			try {
				nbRows=writer.writeVOTable(execReport.resultingColumns, queryResult, output );
			}catch(SQLException e){
				throw new TAPException(e);
			}
			if (logFormatReport)
				service.getFactory().getLogger().info("JOB "+execReport.jobID+" WRITTEN\tResult formatted (in VOTable ; "+nbRows+" rows) in "+(System.currentTimeMillis()-start)+" ms !");

			return nbRows;
		}catch(IOException ioe){
			if(ioe instanceof QuotaException){
				throw new QuotaException(ioe);
			}
			throw new TAPException("Error while writing a query result in VOTable !", ioe);
		}
	}

	@Override
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out) throws TAPException {
		try {
			UwsStorage storage = service.getFactory().getUwsManager().getFactory().getStorageManager();
			GzipVOTableStreamer streamer = new GzipVOTableStreamer( job, result.getId(), storage );
			new VOTableWriter(DataFormat.TABLEDATA, false, votTableVersion).writeStarTable(streamer.createTable(), out);
		} catch (IOException e) {
			throw new TAPException(e);
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				throw new TAPException("Unable to close output stream.",e);
			}
		}
	}

	@Override
	public String getMimeType() {
		return "application/x-votable+xml";
	}

	@Override
	public String getShortMimeType() {
		return shortMimeType;
	}

	@Override
	public void setShortMimeType(String shortMimeType) {
		this.shortMimeType = shortMimeType;
	}

}
