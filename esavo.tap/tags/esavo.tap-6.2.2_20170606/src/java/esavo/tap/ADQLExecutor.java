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
package esavo.tap;

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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsUploadResource;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputStream;
import esavo.uws.output.UwsOutputStreamListener;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsErrorType;
import esavo.uws.utils.UwsUtils;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.TAPLog;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.parameters.TAPParameters;

/**
 * Class to handle ADQL queries
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class ADQLExecutor extends AbstractADQLExecutor implements UwsExecutor {
	
	private static final Logger LOG4J = Logger.getLogger(ADQLExecutor.class.getName());

	public ADQLExecutor(final TAPService service, String appid, TAPLog logger){
		super(service, appid, logger);
	}
	
	@Override
	public Object execute(UwsJob job) throws InterruptedException, UwsException {
		
		job.getParameters().setParameter(TAPParameters.PARAM_TAP_LIB_VERSION,service.getTapVersion());
		
		TAPParameters tapParams = new TAPParameters(job.getParameters());
		TAPSchema uploadSchemaAndTables = null;
		DBConnection dbConn = null;
		UwsJobResultMeta result = null;
		
		long start = System.currentTimeMillis();
		long dbQuotaToRestore = -1;
		
		TAPExecutionReport report = new TAPExecutionReport(job.getJobId(), job.getListid(), job.getName(), tapParams);
		
		try{
			//Assign output format if required
			assignOutputFormatIfRequried(job);
			
			checkParameters(tapParams);
			
			//set MaxRec if not already set
			if(!tapParams.containsParameter(TAPParameters.PARAM_MAX_REC)){
				int maxRec = service.getOutputLimit()[0];
				tapParams.setParameter(TAPParameters.PARAM_MAX_REC, new Integer(maxRec));
			}
			
			dbConn=getDbConnection(job);

			//sets a handler for the query execution so it can be cancelled if necessary
			job.getExecutorJobHandler().setHandler(dbConn);
			
			dbConn.startTransaction();
			
			UwsUploadResource[] uploaders = job.getArgs().getUploadResources();
			
			// Upload tables if needed:
			if (tapParams != null && uploaders != null && uploaders.length > 0){
				setExecutionProgression(ExecutionProgression.UPLOADING);
				tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());
				uploadSchemaAndTables = uploadTables(job.getOwner(), tapParams, report, dbConn, uploaders);
			}
			
			if(job.isPhaseAborted()){
				logJobFinished(job,null,null, null,tapParams.getQuery());
				report.success = false;
				return report;
			}

			UwsJobOwner owner = job.getOwner();
			ResultSet queryResult = executeADQL(owner, tapParams, report, uploadSchemaAndTables, dbConn);

			if(job.isPhaseAborted()){
				logJobFinished(job,null,null, null,tapParams.getQuery());
				report.success = false;
				return report;
			}
			
			UwsEventsManager eventsManager = UwsManager.getInstance().getFactory().getEventsManager();
			
			//IF needed update db quota
			if(isQuotaUpdateNeeded(tapParams)){
				queryResult.next();
				dbQuotaToRestore = queryResult.getLong(1);
				UwsQuotaSingleton.getInstance().createOrLoadQuota(job.getOwner()).addDbSize(dbQuotaToRestore);
				eventsManager.setEventTime(job.getOwner(), UwsEventType.QUOTA_DB_UPDATED_EVENT);
			}
			if(isTableCreated(tapParams)){
				eventsManager.setEventTime(job.getOwner(), TAPFactory.TABLE_CREATED_EVENT);
			}
			
			if(job.isPhaseAborted()){
				if(!dbConn.isTransactionFinished()){
					dbConn.endTransaction();
				}
				logJobFinished(job,null,null, null,tapParams.getQuery());
				report.success = false;
				return report;
			}

			// Write the result:
			setExecutionProgression(ExecutionProgression.WRITING_RESULT);
			tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());
			
			long writeResultsStart = System.currentTimeMillis();
			result = writeResult(queryResult, job, tapParams, report);
			logger.info("JOB "+job.getJobId()+" - SAVED RESULTS IN "+(System.currentTimeMillis()-writeResultsStart)+" MS !");
			
			//End transaction must be performed after 'writeResult'.
			//writeResult uses a resultSet that handles fetch size.
			//To enable fetch size in a resultSet, the transaction must be finished at the end of the process
			if(!dbConn.isTransactionFinished()){
				dbConn.endTransaction();
			}
			
			logger.info("Raising event for finised job for user: " + job.getOwner());
			eventsManager.setEventTime(job.getOwner(), UwsEventType.QUOTA_FILE_UPDATED_EVENT);
			
			job.addResult(result);

			if(job.isPhaseAborted()){
				logJobFinished(job,null,result.getRows(), result.getSize(),tapParams.getQuery());
				report.success = false;
				return report;
			}
		}catch(Exception e){
			int httpErrorCode = -1;
			if(e instanceof UwsException){
				httpErrorCode = ((UwsException)e).getCode();
			}
			try {
				if(dbConn != null){
					dbConn.cancelTransaction();
					restoreDbQuota(job.getOwner(), dbQuotaToRestore, e);
				}
			} catch (DBException e1) {
				throw new UwsException("Error executing job: " + job.getJobId(), e1, UwsExceptionOutputFormat.VOTABLE);
			} catch (TAPException e1) {
				throw new UwsException("Error executing job: " + job.getJobId(), e1, UwsExceptionOutputFormat.VOTABLE);
			}finally{
				String query = "Unknown";
				if(tapParams != null){
					query = tapParams.getQuery();
				}
				String msg = "Error executing query '"+query+"' for job '"+job.getJobId()+"': " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e);
				logger.error(msg);
				UwsStorage storage = service.getFactory().getStorageManager();
				UwsOutputUtils.writeError(job, storage, e, "Executing ADQL", null);
				if(job.getErrorSummary()==null){
					UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta(msg, UwsErrorType.FATAL);
					job.setErrorSummary(errorSummary);
				}
				job.getErrorSummary().setHttpErrorCode(httpErrorCode);
				if(httpErrorCode==400){
					job.getErrorSummary().setExceptionOutputFormat(UwsExceptionOutputFormat.VOTABLE_XML);
				}else{
					job.getErrorSummary().setExceptionOutputFormat(UwsExceptionOutputFormat.VOTABLE);
				}
				
				logJobFinished(job,"ERROR",null, null, tapParams.getQuery());

			}
			throw new UwsException(httpErrorCode, "Error executing job: " + job.getJobId(), e, UwsExceptionOutputFormat.VOTABLE);
		} finally {
			//remove uploaded tables
			removeUploadedTables(dbConn, uploadSchemaAndTables, job);
			try {
				closeDBConnection(dbConn);
			} catch (TAPException e) { 
				logger.error("JOB "+report.jobID+"\tCan not close the DB connection !", e); 
			}
			report.setTotalDuration(System.currentTimeMillis()-start);
			logger.queryFinished(report);
		}

		logger.info("JOB "+report.jobID+" COMPLETED");
		setExecutionProgression(ExecutionProgression.FINISHED);
		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());

		report.success = true;
		if(result != null){
			report.setNumberOfResults(result.getRows());
		}
		
		logJobFinished(job,"FINISHED",result.getRows(),result.getSize(),tapParams.getQuery());
		
		return report;
	}
	
	protected boolean isQuotaUpdateNeeded(TAPParameters tapParams){
		return isTableCreated(tapParams);
	}
	
	protected boolean isTableCreated(TAPParameters tapParams){
		return false;
	}
	
	protected UwsJobResultMeta writeResult(ResultSet queryResult, final UwsJob job, TAPParameters tapParams, TAPExecutionReport report) throws InterruptedException, TAPException, UwsException {
		UwsJobResultMeta result = new UwsJobResultMeta(UwsJobResultMeta.DEFAULT_IDENTIFIER);

		long numberOfRows;
		if(UwsConfiguration.SYNC_LIST_ID.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
			//sync
			long start = System.currentTimeMillis();
			try{
				HttpServletResponse response = job.getArgs().getResponse();
				
				OutputFormat formatter = getFormatter(tapParams);

				String mimeType = formatter.getMimeType();
		        String contentEncoding = formatter.getContentEncoding();
				String suitableFileName = UwsOutputUtils.getSuitableContentDispositionFileName(job, result);
		        String fileExtension = null; //use default system extension
		        long size = -1; //undefined
				
				UwsOutputUtils.writeDataResponseHeader(response, suitableFileName, mimeType, fileExtension, contentEncoding, size);

				
				ServletOutputStream sos = response.getOutputStream();
				esavo.uws.utils.UwsOutputStreamWrapper os = new esavo.uws.utils.UwsOutputStreamWrapper(sos);
				numberOfRows = formatter.writeResult(job, queryResult, os, report);
			} catch (IOException e) {
				throw new UwsException("Cannot get response output stream: " + e.getMessage(), e);
			}finally{
				report.setDuration(ExecutionProgression.WRITING_RESULT, System.currentTimeMillis()-start);
			}
		}else{
			//async
			
			// IN ASYNC JOBS FORCE COMMON FORMAT
			OutputFormat formatter = getCommonFormatter();
			result.setMimeType(formatter.getMimeType());
			result.setFileExtension(formatter.getFileExtension());
			result.setContentEncoding(formatter.getContentEncoding());
			
			long start = System.currentTimeMillis();
			UwsOutputStream output = null;
			try{
				UwsManager manager = UwsManager.getInstance();
				UwsStorage storage = manager.getFactory().getStorageManager();
				output = new UwsOutputStream(job, storage.getJobResultsDataOutputStream(job, result.getId()));
				output.addListener(new UwsOutputStreamListener() {
					@Override
					public void notifyWrite(long bytes) throws QuotaException,UwsException {
						if(job.isPhaseAborted()){
							return;
						}
						UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(job.getOwner());
						quota.addFileSize(bytes);
					}
				});
				try{
					numberOfRows = formatter.writeResult(job, queryResult, output, report);
				}catch(QuotaException e){
					storage.removeJobOutputData(job);
					throw new UwsException(e.getMessage());
				}

				long size = storage.getJobResultDataSize(job, result.getId());
				result.setSize(size);
			}finally{
				report.setDuration(ExecutionProgression.WRITING_RESULT, System.currentTimeMillis()-start);
				if(output != null){
					try{
						output.flush();
						output.close();
					}catch(Exception e){
						logger.error("Cannot close output stream when writing error details for job "+job.getJobId(), e); 
					}
				}
			}
		}
		long suitableNumberOfRows = getSuitableNumberOfRows(numberOfRows, tapParams, queryResult);
		result.setRows(suitableNumberOfRows);
		return result;
	}

	protected long getSuitableNumberOfRows(long numberOfRows, TAPParameters tapParams, ResultSet queryResult){
		return numberOfRows;
	}
	
	private void logJobFinished(UwsJob job, String status, Long rows, Long bytes, String query){
		String rowsStr = "";
		String bytesStr = "";
		String showStatus = job.getPhase().name();
		Date showDateStart = job.getStartTime();
		Date showDateEnd = new Date();
		
		SimpleDateFormat formatter;
		

		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS", Locale.getDefault());
		
		if(status!=null){
			showStatus = status;
		}
		
		if(rows!=null){
			rowsStr = rows.toString();
		}
		
		if(bytes!=null){
			bytesStr = bytes.toString();
		}

		try{
			StringBuilder sb = new StringBuilder();
			sb.append(job.getListid()+"|");
			sb.append(job.getJobId()+"|");
			sb.append(job.getOwner().getId()+"|");
			sb.append(showDateStart.getTime()+"|");
			sb.append(formatter.format(showDateStart)+"|");
			sb.append(showDateEnd.getTime()+"|");
			sb.append(showDateEnd.getTime()-showDateStart.getTime()+"|");
			sb.append(formatter.format(showDateEnd)+"|");
			sb.append(showStatus+"|");
			sb.append(rowsStr+"|");
			sb.append(bytesStr+"|");
			sb.append(properLogSyntax(query));
			
			LOG4J.info(sb);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String properLogSyntax(String input){
		if(input==null){
			return null;
		}
		
		String output = input.replaceAll("\\|", "\\\\|");
		output = output.replaceAll("\\s+", " ");
		
		return output;
	}

}
