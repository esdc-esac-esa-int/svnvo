package esavo.uws.executor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsErrorType;

public abstract class UwsAbstractExecutor implements UwsExecutor {
	
	private String appid;
	
	public UwsAbstractExecutor(String appid){
		this.appid = appid;
	}
	
	public String getAppId(){
		return appid;
	}
	
	/**
	 * Creates a single result (id {@link UwsJobResultMeta#DEFAULT_IDENTIFIER}} and associates the result to the job.<br/>
	 * @param job
	 * @param mimeType
	 * @param resultData
	 * @param numRows
	 * @return
	 * @throws UwsException
	 */
	public UwsJobResultMeta createSingleResult(UwsJob job, String mimeType, String resultData, int numRows) throws UwsException {
		if(UwsConfiguration.SYNC_LIST_ID.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
			HttpServletResponse response = job.getArgs().getResponse();
			response.setStatus(UwsOutputResponseHandler.OK);
			response.setContentType(mimeType);
			PrintWriter pw;
			try {
				pw = response.getWriter();
			} catch (IOException e) {
				throw new UwsException("Cannot get output writer: " + e.getMessage(), e);
			}
			pw.println(resultData);
			pw.flush();
			pw.close();
			return null;
		}else{
			UwsManager manager = UwsManager.getInstance();
			UwsStorage storage = manager.getFactory().getStorageManager();
			UwsJobResultMeta result = new UwsJobResultMeta(UwsJobResultMeta.DEFAULT_IDENTIFIER);
			result.setMimeType(mimeType);
			//no sync job: dump result to storage
			
			OutputStream output = storage.getJobResultsDataOutputStream(job, result.getId());
			try{
				output.write(resultData.getBytes());
			} catch (IOException ioe) {
				throw new UwsException("Cannot save data results for job '"+job.getJobId()+"'", ioe);
			} finally {
				if(output != null){
					try {
						output.close();
					} catch (IOException e) {
						// ignore
						e.printStackTrace();
					}
				}
			}
			long size = storage.getJobResultDataSize(job, result.getId());
			result.setSize(size);
			result.setRows(numRows);
			//Add result to job: this method notifies to managers.
			job.addResult(result);
			
			return result;
		}
		
	}

	/**
	 * Creates an error summary.
	 * @param job
	 * @param message
	 * @param errorType
	 * @param detailsMimeType
	 * @param errorDetails
	 * @throws UwsException
	 */
	public void createError(UwsJob job, String message, UwsErrorType errorType, String detailsMimeType, String errorDetails) throws UwsException{
		UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta(message, errorType);
		//UwsManager manager = UwsManager.getInstance(job.getAppid());
		UwsManager manager = UwsManager.getInstance();
		
		if(UwsConfiguration.SYNC_LIST_ID.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
			//sync
			HttpServletResponse response = job.getArgs().getResponse();
			UwsOutputResponseHandler outputHandler = manager.getFactory().getOutputHandler();
			outputHandler.writeJobErrorResponse(response, errorSummary);
		}else{
			//no sync
			UwsStorage storage = manager.getFactory().getStorageManager();
			OutputStream output = storage.getJobErrorDetailsDataOutputStream(job);
			try{
				output.write(errorDetails.getBytes());
			} catch (IOException ioe) {
				throw new UwsException("Cannot save data error details for job '"+job.getJobId()+"'", ioe);
			} finally {
				if(output != null){
					try {
						output.close();
					} catch (IOException e) {
						// ignore
						e.printStackTrace();
					}
				}
			}
			long size = storage.getJobErrorDetailsDataSize(job);
			errorSummary.setDetailsSize(size);
			errorSummary.setDetailsMimeType(detailsMimeType);
	
			//Set error summary to the job: this method notifies listeners.
			job.setErrorSummary(errorSummary);
		}
	}

	@Override
	public Object execute(UwsJob job) throws InterruptedException, UwsException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void cancel(UwsJob job) throws UwsException {
		throw new RuntimeException("Not implemented");
	}

}
