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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsUploadResourceLoader;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.executor.UwsAbstractExecutor;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.executor.UwsExecutorJobHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobParameters;
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
import esavo.adql.parser.ADQLParser;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.ParseException;
import esavo.adql.parser.QueryChecker;
import esavo.adql.query.ADQLQuery;
import esavo.adql.translator.ADQLTranslator;
import esavo.adql.translator.TranslationException;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.TAPLog;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.tap.parameters.TAPParameters;
import esavo.tap.upload.Uploader;

public class ADQLExecutor extends UwsAbstractExecutor implements UwsExecutor {
	
	private static final Logger LOG = Logger.getLogger(ADQLExecutor.class.getName());

	protected final TAPService service;
	protected final TAPLog logger;

	public ADQLExecutor(final TAPService service, String appid, TAPLog logger){
		super(appid);
		this.service = service;
		this.logger = logger;
	}
	
	@Override
	public Object execute(UwsJob job) throws UwsException {
		//Assign output format if requited
		assignOutputFormatIfRequried(job);
		
		TAPParameters tapParams = new TAPParameters(job.getParameters());
		
		checkParameters(tapParams);
		
		//set MaxRec if not already set
		if(!tapParams.containsParameter(TAPParameters.PARAM_MAX_REC)){
			//UwsConfiguration config = UwsConfigurationManager.getConfiguration(getAppId());
			//String sMaxRec = config.getProperty(TapServiceConnection.TAP_OUTPUT_LIMIT_ROWS);
			//int maxRec = UwsUtils.getInt(sMaxRec);
			int maxRec = service.getOutputLimit()[0];
			tapParams.setParameter(TAPParameters.PARAM_MAX_REC, new Integer(maxRec));
		}
		
		TAPExecutionReport report = new TAPExecutionReport(job.getJobId(), false, tapParams);
		
		TAPSchema uploadSchemaAndTables = null;
		DBConnection dbConn = null;
		UwsJobResultMeta result = null;
		
		long start = System.currentTimeMillis();
		long dbQuotaToRestore = -1;
		try{
			dbConn=service.getFactory().createDBConnection(job.getJobId());
			
			//sets a handler for the query execution so it can be cancelled if necessary
			UwsExecutorJobHandler ejh = new UwsExecutorJobHandler(dbConn);
			job.setExecutorJobHandler(ejh);
			
			dbConn.startTransaction();
			
			UwsUploadResourceLoader[] uploaders = job.getArgs().getUploadResources();
			
			// Upload tables if needed:
			if (tapParams != null && uploaders != null && uploaders.length > 0){
				tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.UPLOADING);
				uploadSchemaAndTables = uploadTables(job.getOwner(), tapParams, report, dbConn, uploaders);
			}
			
			if(job.isPhaseAborted()){
				report.success = false;
				return report;
			}

			UwsJobOwner owner = job.getOwner();
			ResultSet queryResult = executeADQL(owner, tapParams, report, uploadSchemaAndTables, dbConn);

			if(job.isPhaseAborted()){
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
			
			//dbConn.endTransaction();
			
			if(job.isPhaseAborted()){
				dbConn.endTransaction();
				report.success = false;
				return report;
			}

			// Write the result:
			tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.WRITING_RESULT);
			
			long writeResultsStart = System.currentTimeMillis();
			result = writeResult(queryResult, job, tapParams, report);
			logger.info("JOB "+job.getJobId()+" - SAVED RESULTS IN "+(System.currentTimeMillis()-writeResultsStart)+" MS !");
			
			//End transaction must be performed after 'writeResult'.
			//writeResult uses a resultSet that handles fetch size.
			//To enable fetch size in a resultSet, the transaction must be finished at the end of the process
			dbConn.endTransaction();
			
			eventsManager.setEventTime(job.getOwner(), UwsEventType.QUOTA_FILE_UPDATED_EVENT);
			
			job.addResult(result);

			if(job.isPhaseAborted()){
				report.success = false;
				return report;
			}

		}catch(Exception e){
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
				String msg = "Error executing query '"+query+"' for job '"+job.getJobId()+"': " + e.getMessage();
				LOG.severe(msg);
				UwsStorage storage = service.getFactory().getStorageManager();
				UwsOutputUtils.writeError(job, storage, e, "Executing ADQL", null);
				//writeError(job, e);
				if(job.getErrorSummary()==null){
					UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta(msg, UwsErrorType.FATAL);
					job.setErrorSummary(errorSummary);
				}
				
				job.getErrorSummary().setExceptionOutputFormat(UwsExceptionOutputFormat.VOTABLE);
			}
			throw new UwsException("Error executing job: " + job.getJobId(), e, UwsExceptionOutputFormat.VOTABLE);
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
		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.FINISHED);

		report.success = true;
		if(result != null){
			report.setNumberOfResults(result.getRows());
		}

		return report;
	}
	
	private boolean isQuotaUpdateNeeded(TAPParameters tapParams){
		return isTableCreated(tapParams);
	}
	
	private boolean isTableCreated(TAPParameters tapParams){
		if(tapParams.getQuery().indexOf("crossmatch_positional")>=0){
			return true;
		}
		return false;
	}
	
	private void restoreDbQuota(UwsJobOwner owner, long dbQuotaToRestore, Exception e) throws TAPException{
		if(dbQuotaToRestore < 0){
			return;
		}
		try {
			UwsQuotaSingleton.getInstance().createOrLoadQuota(owner).reduceDbSize(dbQuotaToRestore);
		} catch (UwsException e1) {
			throw new TAPException("Cannot restore user '' quota due to: " + e1.getMessage(), e);
		}
	}

	
	private void removeUploadedTables(DBConnection dbConn, TAPSchema uploadSchemaAndTables, UwsJob job) throws UwsException {
		//remove table from database
		try{
			dbConn.startTransaction();
			deleteUploadedTable(uploadSchemaAndTables, dbConn);
			dbConn.endTransaction();
		}catch(DBException e){
			try {
				dbConn.cancelTransaction();
			} catch (DBException e1) {
				throw new UwsException("Cannot cancel transaction when removing uploaded tables for job " + job.getJobId() + ": " + e.getMessage(), e1, UwsExceptionOutputFormat.VOTABLE);
			}
			throw new UwsException("Cannot remove uploaded tables for job " + job.getJobId() + ": " + e.getMessage(), e);
		}
	}

	
	private void assignOutputFormatIfRequried(UwsJob job) throws UwsException{
		UwsJobParameters params = job.getParameters();
		if(params == null){
			params = new UwsJobParameters();
			job.setParameters(params);
		}
		if(!params.containsParameter(TAPParameters.PARAM_FORMAT)){
			params.setParameter(TAPParameters.PARAM_FORMAT, TAPParameters.DEFAULT_OUTPUT_FORMAT);
		}
	}
	
	
	@Override
	public void cancel(UwsJob job) throws UwsException {
		job.cancel();
		UwsExecutorJobHandler ex = job.getExecutorJobHandler();
		if (ex != null) {
			Object handler = ex.getHandler();
			if (handler != null) {
				try {
					((DBConnection) handler).cancelTransaction();
				} catch (DBException e) {
					throw new UwsException("Cannont cancel query for job: " + job.getJobId(), e);
				}
//				try {
//					//Statement st = (Statement) handler;
//					//st.cancel();
//					((DBConnection) handler).cancelTransaction();
//				} catch (SQLException e) {
//					throw new UwsException("Cannont cancel query for job: " + job.getJobId(), e);
//				}
			}
		}
	}	
	
	private void checkParameters(TAPParameters parameters) throws UwsException {
		// Check that required parameters are not NON-NULL:
		String requestParam = parameters.getRequest();
		if (requestParam == null){
			throw new UwsException("The parameter \""+TAPParameters.PARAM_REQUEST+"\" must be provided " +
					"and its value must be equal to \""+TAPParameters.REQUEST_DO_QUERY+"\" or " +
							"\""+TAPParameters.REQUEST_GET_CAPABILITIES+"\" !");
		}
		if (requestParam.equals(TAPParameters.REQUEST_DO_QUERY)){
			if (parameters.getParameter(TAPParameters.PARAM_LANGUAGE) == null){
				throw new UwsException("The parameter \""+TAPParameters.PARAM_LANGUAGE+"\" must be provided " +
						"if "+TAPParameters.PARAM_REQUEST+"="+TAPParameters.REQUEST_DO_QUERY+" !");
			} else if (parameters.getParameter(TAPParameters.PARAM_QUERY) == null){
				throw new UwsException("The parameter \""+TAPParameters.PARAM_QUERY+"\" must be provided " +
						"if "+TAPParameters.PARAM_REQUEST+"="+TAPParameters.REQUEST_DO_QUERY+" !");
			}
		}
	}


	private void closeDBConnection(DBConnection dbConn) throws TAPException {
		if (dbConn != null){
			dbConn.close();
			dbConn = null;
		}
	}

	/**
	 * Dumps an uploaded file into a table in db
	 * @param tapParams
	 * @param report
	 * @param dbConn
	 * @return
	 * @throws TAPException
	 */
	private final TAPSchema uploadTables(UwsJobOwner owner, TAPParameters tapParams, TAPExecutionReport report, DBConnection dbConn, UwsUploadResourceLoader[] uploaders) throws TAPException, UwsException {
		TAPSchema tapSchema = null;
		logger.info("JOB " + report.jobID + "\tLoading uploaded tables (" + uploaders.length + ")...");
		long start = System.currentTimeMillis();
		Uploader uploader;
		try{
			uploader = service.getFactory().createUploader();
		}catch(TAPException tapEx){
			TAPException e = new TAPException("(Creation) Cannot upload table for job '"+report.jobID+"': " + tapEx.getMessage(), tapEx);
			throw e;
		}finally{
			report.setDuration(ExecutionProgression.UPLOADING, System.currentTimeMillis() - start);
		}
		
		try{
			tapSchema = uploader.upload(owner, uploaders, dbConn);
		}catch(TAPException tapEx){
			TAPException e = new TAPException("(Upload) Cannot upload table for job '"+report.jobID+"': " + tapEx.getMessage(), tapEx);
			throw e;
		}finally{
			report.setDuration(ExecutionProgression.UPLOADING, System.currentTimeMillis() - start);
		}
		return tapSchema;
	}
	
	private void deleteUploadedTable(TAPSchema uploadedSchema, DBConnection dbConn) throws DBException{
		if(uploadedSchema == null){
			return;
		}
		//Iterator<TAPTable> tablesIterator = uploadedSchema.iterator();
		Iterator<TAPTable> tablesIterator = uploadedSchema.getTables().iterator();
		TAPTable table;
		while(tablesIterator.hasNext()){
			table = tablesIterator.next();
			dbConn.dropTable(table, true);
		}
	}

	private final ResultSet executeADQL(UwsJobOwner owner, TAPParameters tapParams, TAPExecutionReport report, TAPSchema uploadSchema, DBConnection dbConn) throws InterruptedException, TranslationException, SQLException, TAPException, UwsException {
		long start;

		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.PARSING);
		start = System.currentTimeMillis();
		ADQLQuery adql;
		try{
			adql = parseADQL(owner, tapParams, uploadSchema);
		}catch(ParseException pe){
			UwsException e = new UwsException("Cannot parse query '"+tapParams.getQuery()+"' for job '" + report.jobID + "': " + pe.getMessage(), pe);
			throw e;
		}
		report.setDuration(ExecutionProgression.PARSING, System.currentTimeMillis()-start);

		report.resultingColumns = adql.getResultingColumns();

		int limit = adql.getSelect().getLimit();
		final int maxRec = tapParams.getMaxRec();
		if (maxRec > -1){
			if (limit <= -1 || limit > maxRec){
				adql.getSelect().setLimit(maxRec);
			}
		}

		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.TRANSLATING);
		start = System.currentTimeMillis();
		String sqlQuery;
		try{
			sqlQuery = translateADQL(adql);
		}catch(TranslationException trEx){
			TranslationException e = new TranslationException("(Tx) Cannot translate query '"+tapParams.getQuery()+"' for job '" + report.jobID + "': " + trEx.getMessage(), trEx);
			throw e;
		}catch(TAPException tapEx){
			TAPException e = new TAPException("(TAP) Cannot translate query '"+tapParams.getQuery()+"' for job '" + report.jobID + "': " + tapEx.getMessage(), tapEx);
			throw e;
		}
		report.setDuration(ExecutionProgression.TRANSLATING, System.currentTimeMillis()-start);
		report.sqlTranslation = sqlQuery;

		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, ExecutionProgression.EXECUTING_SQL);
		start = System.currentTimeMillis();
		ResultSet result;
		try{
			result = executeQuery(sqlQuery, adql, dbConn, report.jobID);
		}catch(SQLException sqlEx){
			SQLException e = new SQLException("(SQL) Cannot execute query '"+sqlQuery+"' for job ' (source query: '"+tapParams.getQuery()+"')" + report.jobID + "': " + sqlEx.getMessage(), sqlEx);
			throw e;
		}catch(TAPException tapEx){
			TAPException e = new TAPException("(TAP) Cannot execute query '"+sqlQuery+"' for job ' (source query: '"+tapParams.getQuery()+"')" + report.jobID + "': " + tapEx.getMessage(), tapEx);
			throw e;
		}
		report.setDuration(ExecutionProgression.EXECUTING_SQL, System.currentTimeMillis()-start);
		
		return result;
	}

	private ADQLQuery parseADQL(UwsJobOwner owner, TAPParameters tapParams, TAPSchema uploadSchema) throws ParseException, InterruptedException, TAPException {
		//boolean includeAccessibleSharedItems = tapParams.getIncludeAccessibleSharedItems();
		boolean includeAccessibleSharedItems = true;
		ADQLQueryFactory queryFactory = service.getFactory().createQueryFactory(owner);
		QueryChecker queryChecker = service.getFactory().createQueryChecker(uploadSchema, owner, includeAccessibleSharedItems);
		ADQLParser parser;
		if (queryFactory == null) {
			parser = new ADQLParser(queryChecker);
		} else {
			parser = new ADQLParser(queryChecker, queryFactory);
		}
		parser.setCoordinateSystems(service.getCoordinateSystems());
		parser.setDebug(false);
		return parser.parseQuery(tapParams.getQuery());
	}

	private String translateADQL(ADQLQuery query) throws TranslationException, InterruptedException, TAPException {
		ADQLTranslator translator = service.getFactory().createADQLTranslator();
		return translator.translate(query);
	}

	private ResultSet executeQuery(String sql, ADQLQuery adql, DBConnection dbConn, String jobid) throws SQLException, InterruptedException, TAPException {
		final long startTime = System.currentTimeMillis();
		ResultSet result = (ResultSet)dbConn.executeQuery(sql, adql);
		if (result == null){
			logger.info("JOB "+jobid+" - QUERY ABORTED AFTER "+(System.currentTimeMillis()-startTime)+" MS !");
		}else{
			logger.info("JOB "+jobid+" - QUERY SUCCESSFULLY EXECUTED IN "+(System.currentTimeMillis()-startTime)+" MS !");
		}
		return result;
	}

	private OutputFormat getFormatter(TAPParameters tapParams) throws TAPException {
		// Search for the corresponding formatter:
		String format = tapParams.getFormat();
		OutputFormat formatter = service.getFactory().getOutputFormat((format == null)?TAPParameters.DEFAULT_OUTPUT_FORMAT:format);
		if (format != null && formatter == null){
			formatter = service.getFactory().getOutputFormat(TAPParameters.DEFAULT_OUTPUT_FORMAT);
		}

		// Format the result:
		if (formatter == null) {
			throw new TAPException("Impossible to format the query result: no formatter has been found for the given MIME type \""+format+"\" and for the default MIME type \"votable\" (short form) !");
		}

		return formatter;
	}

	private UwsJobResultMeta writeResult(ResultSet queryResult, final UwsJob job, TAPParameters tapParams, TAPExecutionReport report) throws InterruptedException, TAPException, UwsException {
		OutputFormat formatter = getFormatter(tapParams);
		UwsJobResultMeta result = new UwsJobResultMeta(UwsJobResultMeta.DEFAULT_IDENTIFIER);
		result.setMimeType(formatter.getMimeType());
		
		long numberOfRows;
		if(UwsManager.SYNC_LIST.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
			//sync
			long start = System.currentTimeMillis();
			try{
				HttpServletResponse response = job.getArgs().getResponse();
				response.setContentType(formatter.getMimeType());
				ServletOutputStream sos = response.getOutputStream();
				numberOfRows = formatter.writeResult(job, queryResult, sos, report);
			} catch (IOException e) {
				throw new UwsException("Cannot get response output stream: " + e.getMessage(), e);
			}finally{
				report.setDuration(ExecutionProgression.WRITING_RESULT, System.currentTimeMillis()-start);
			}
		}else{
			//async
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
					throw new UwsException(e.getMessage());
				}
				if(job.isPhaseAborted()){
					return result;
				}
				long size = storage.getJobResultDataSize(job, result.getId());
				result.setSize(size);
			}finally{
				report.setDuration(ExecutionProgression.WRITING_RESULT, System.currentTimeMillis()-start);
				if(output != null){
					try{
						output.close();
					}catch(Exception e){
						logger.error("Cannot close output stream when writing error details for job "+job.getJobId(), e); 
					}
				}
			}
		}
		result.setRows(numberOfRows);
		return result;
	}

}
