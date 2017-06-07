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
package esavo.uws.executor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
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
	
//	/**
//	 * Creates a single result (id {@link UwsJobResultMeta#DEFAULT_IDENTIFIER}} and associates the result to the job.<br/>
//	 * Probably, you want to create our own method because it is possible you do not now the number of rows before the 'source' stream
//	 * is analyzed.<br/>
//	 * You may use this method as a template.
//	 * @param job
//	 * @param mimeType
//	 * @param source
//	 * @param numRows
//	 * @return
//	 * @throws UwsException 
//	 */
//	public UwsJobResultMeta createSingleResult(UwsJob job, String mimeType, InputStream source, int numRows) throws UwsException{
//		UwsManager manager = UwsManager.getInstance(job.getAppid());
//		UwsStorage storage = manager.getFactory().getStorageManager();
//		UwsJobResultMeta result = new UwsJobResultMeta(UwsJobResultMeta.DEFAULT_IDENTIFIER);
//		OutputStream output = storage.getJobResultsDataOutputStream(job, result.getId());
//		try{
//			UwsOutputUtils.dumpToStream(source, output);
//		} catch (IOException ioe) {
//			throw new UwsException("Cannot save data results for job '"+job.getJobId()+"'", ioe);
//		} finally {
//			if(output != null){
//				try {
//					output.close();
//				} catch (IOException e) {
//					// ignore
//					e.printStackTrace();
//				}
//			}
//		}
//		long size = storage.getResultDataSize(job, result.getId());
//		result.setSize(size);
//		result.setRows(numRows);
//		result.setMimeType(mimeType);
//		
//		//Add result to job: this method notifies to managers.
//		job.addResult(result);
//		
//		return result;
//	}
	
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
		if(UwsManager.SYNC_LIST.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
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
			//UwsManager manager = UwsManager.getInstance(job.getAppid());
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

//	/**
//	 * Creates an error summary (with details).<br/>
//	 * InputStream is not closed.
//	 * @param job
//	 * @param message
//	 * @param errorType
//	 * @param detailsMimeType
//	 * @param errorDetails
//	 * @throws UwsException
//	 */
//	public void createError(UwsJob job, String message, UwsErrorType errorType, String detailsMimeType, InputStream errorDetails) throws UwsException{
//		UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta(message, errorType);
//		UwsManager manager = UwsManager.getInstance(job.getAppid());
//		if(UwsManager.SYNC_LIST.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
//			HttpServletResponse response = job.getArgs().getResponse();
//			UwsOutputResponseHandler outputHandler = manager.getFactory().getOutputHandler();
//			outputHandler.writeJobErrorResponse(response, errorSummary);
//		}else{
//			UwsStorage storage = manager.getFactory().getStorageManager();
//			OutputStream output = storage.getJobErrorDetailsDataOutputStream(job);
//			try{
//				UwsOutputUtils.dumpToStream(errorDetails, output);
//			} catch (IOException ioe) {
//				throw new UwsException("Cannot save data error details for job '"+job.getJobId()+"'", ioe);
//			} finally {
//				if(output != null){
//					try {
//						output.close();
//					} catch (IOException e) {
//						// ignore
//						e.printStackTrace();
//					}
//				}
//			}
//			long size = storage.getErrorDetailsDataSize(job);
//			errorSummary.setDetailsSize(size);
//			errorSummary.setDetailsMimeType(detailsMimeType);
//			
//			//Set error summary to the job: this method notifies listeners.
//			job.setErrorSummary(errorSummary);
//		}
//	}

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
		
		if(UwsManager.SYNC_LIST.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
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
