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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsUploadResource;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.executor.UwsAbstractExecutor;
import esavo.uws.executor.UwsExecutor;
import esavo.uws.executor.UwsExecutorJobHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuotaSingleton;
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

/**
 * Abstract class to handle ADQL queries
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public abstract class AbstractADQLExecutor extends UwsAbstractExecutor implements UwsExecutor {

	protected final TAPService service;
	protected final TAPLog logger;
	private ExecutionProgression executionProgression;


	public AbstractADQLExecutor(final TAPService service, String appid, TAPLog logger){
		super(appid);
		this.service = service;
		this.logger = logger;
	}
	
	protected DBConnection getDbConnection(UwsJob job) throws TAPException{
		
		String listId = job.getListid();
		
		if(UwsConfiguration.ASYNC_LIST_ID.equalsIgnoreCase(listId)){
			return service.getFactory().createDBConnection(job.getJobId(), TAPService.TAP_JDBC_STORAGE_JOBS_ASYNC_POOL_ID);
		}else{
			return service.getFactory().createDBConnection(job.getJobId(), TAPService.TAP_JDBC_STORAGE_JOBS_SYNC_POOL_ID);
		}
	}
	
	@Override
	public abstract Object execute(UwsJob job) throws InterruptedException, UwsException;
	
	protected abstract UwsJobResultMeta writeResult(ResultSet queryResult, final UwsJob job, TAPParameters tapParams, TAPExecutionReport report) throws InterruptedException, TAPException, UwsException;
	
	
	protected void restoreDbQuota(UwsJobOwner owner, long dbQuotaToRestore, Exception e) throws TAPException{
		if(dbQuotaToRestore < 0){
			return;
		}
		try {
			UwsQuotaSingleton.getInstance().createOrLoadQuota(owner).reduceDbSize(dbQuotaToRestore);
		} catch (UwsException e1) {
			throw new TAPException("Cannot restore user '' quota due to: " + e1.getMessage(), e);
		}
	}

	
	protected void removeUploadedTables(DBConnection dbConn, TAPSchema uploadSchemaAndTables, UwsJob job) throws UwsException {
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

	
	protected void assignOutputFormatIfRequried(UwsJob job) throws UwsException{
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
					DBConnection dbConn = (DBConnection) handler;
					// Connection is not canceled if the executor is writing results. This is done to allow writer to finish 
					// gracefully. WARNING: THE WRITER MUST CANCEL TRANSACTION before finishing.
					if(getExecutionProgression()!=ExecutionProgression.WRITING_RESULT && !dbConn.isTransactionFinished() ){
						dbConn.cancelTransaction();
					}
				} catch (DBException e) {
					throw new UwsException("Cannont cancel query for job: " + job.getJobId(), e);
				}
			}
		}
	}	
	
	protected void checkParameters(TAPParameters parameters) throws UwsException {
		// Check that required parameters are not NON-NULL:
		String requestParam = parameters.getRequest();
		if (requestParam == null){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST,"The parameter \""+TAPParameters.PARAM_REQUEST+"\" must be provided " +
					"and its value must be equal to \""+TAPParameters.REQUEST_DO_QUERY+"\" or " +
							"\""+TAPParameters.REQUEST_GET_CAPABILITIES+"\" !");
		}
		if (requestParam.equals(TAPParameters.REQUEST_DO_QUERY)){
			if (parameters.getParameter(TAPParameters.PARAM_LANGUAGE) == null){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST,"The parameter \""+TAPParameters.PARAM_LANGUAGE+"\" must be provided " +
						"if "+TAPParameters.PARAM_REQUEST+"="+TAPParameters.REQUEST_DO_QUERY+" !");
			} else if (parameters.getParameter(TAPParameters.PARAM_QUERY) == null){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST,"The parameter \""+TAPParameters.PARAM_QUERY+"\" must be provided " +
						"if "+TAPParameters.PARAM_REQUEST+"="+TAPParameters.REQUEST_DO_QUERY+" !");
			}
		}
	}


	protected void closeDBConnection(DBConnection dbConn) throws TAPException {
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
	protected final TAPSchema uploadTables(UwsJobOwner owner, TAPParameters tapParams, TAPExecutionReport report, DBConnection dbConn, UwsUploadResource[] uploaders) throws TAPException, UwsException {
		
		setExecutionProgression(ExecutionProgression.UPLOADING);
		
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
			report.setDuration(getExecutionProgression(), System.currentTimeMillis() - start);
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
		Iterator<TAPTable> tablesIterator = uploadedSchema.getTables().iterator();
		TAPTable table;
		while(tablesIterator.hasNext()){
			table = tablesIterator.next();
			dbConn.dropTable(table, true);
		}
	}

	protected final ResultSet executeADQL(UwsJobOwner owner, TAPParameters tapParams, TAPExecutionReport report, TAPSchema uploadSchema, DBConnection dbConn) throws InterruptedException, TranslationException, SQLException, TAPException, UwsException {
		long start;
		
		String info = "Owner: "+owner.getId()+", Job: "+report.jobID+", query ("+report.listID+"): ";

		setExecutionProgression(ExecutionProgression.PARSING);
		
		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());
		start = System.currentTimeMillis();
		ADQLQuery adql;
		try{
			adql = parseADQL(owner, tapParams, uploadSchema);
		}catch(ParseException pe){
			UwsException e = new UwsException("Cannot parse query '"+tapParams.getQuery()+"' for job '" + report.jobID + "': " + pe.getMessage(), pe);
			throw e;
		}
		report.setDuration(ExecutionProgression.PARSING, System.currentTimeMillis()-start);

		int limit = adql.getSelect().getLimit();
		final int maxRec = tapParams.getMaxRec();
		if (maxRec > -1){
			if (limit <= -1 || limit > maxRec){
				adql.getSelect().setLimit(maxRec);
			}
		}

		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());
		start = System.currentTimeMillis();
		
		setExecutionProgression(ExecutionProgression.TRANSLATING);

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
		
		sqlQuery = adaptPagination(sqlQuery, tapParams);
		
		report.resultingColumns = adql.getResultingColumns();

		logger.info(info + "'" + sqlQuery + "'");
		
		report.setDuration(getExecutionProgression(), System.currentTimeMillis()-start);
		report.sqlTranslation = sqlQuery;

		setExecutionProgression(ExecutionProgression.EXECUTING_SQL);

		tapParams.setParameter(TAPParameters.PARAM_PROGRESSION, getExecutionProgression());
		start = System.currentTimeMillis();
		ResultSet result;
		try{
			result = executeQuery(sqlQuery, adql, dbConn, report.jobID);
		}catch(TAPException tapEx){
			TAPException e = new TAPException("(TAP) Cannot execute query '"+sqlQuery+"' for job ' (source query: '"+tapParams.getQuery()+"')" + report.jobID + "': " + tapEx.getMessage(), tapEx);
			throw e;
		}
		report.setDuration(getExecutionProgression(), System.currentTimeMillis()-start);
		
		return result;
	}
	
	private String adaptPagination(String sqlQuery, TAPParameters tapParams) throws UwsException {
		String paramPage = tapParams.getPage();
		String paramPageSize = tapParams.getPageSize();
		String result = null;
		if(paramPageSize != null && !paramPageSize.isEmpty()){
			int limit = 0;
			try{
				limit = Integer.parseInt(paramPageSize);
			}catch(NumberFormatException nfe){
				throw new UwsException("Invalid '" + TAPParameters.PARAM_PAGE_SIZE +"' value: " + paramPageSize);
			}
			//change LIMIT
			//if(sqlQuery.matches("(?i:limit \\d+$)")){
			Pattern pattern = Pattern.compile(".+Limit \\d+$", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(sqlQuery);
			if(matcher.matches()){
				//limit found
				int p = sqlQuery.toLowerCase().lastIndexOf("limit ");
				if(p >= 0){
					result = sqlQuery.substring(0, p);
					result += " LIMIT " + limit;
				}
			} else {
				//no limit found, nevertheless, the user has specified a limit: set it
				result = sqlQuery + " LIMIT " + limit;
			}
			//paramPageSize is required to calculate OFFSET
			if(paramPage != null && !paramPage.isEmpty()){
				//page starts in 1
				int page = 0;
				try{
					page = Integer.parseInt(paramPage);
				}catch(NumberFormatException nfe){
					throw new UwsException("Invalid '" + TAPParameters.PARAM_PAGE +"' value: " + paramPage);
				}
				//change OFFSET
				int offset = (page - 1) * limit;
				if(offset != 0){
					if(result == null){
						result = sqlQuery;
					}
					result += " OFFSET " + offset;
				}
			}
		}
		if(result == null){
			return sqlQuery;
		} else {
			return result;
		}
	}

	private ADQLQuery parseADQL(UwsJobOwner owner, TAPParameters tapParams, TAPSchema uploadSchema) throws ParseException, InterruptedException, TAPException {
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
		try{
			ADQLQuery adqlQuery= parser.parseQuery(tapParams.getQuery());
			return adqlQuery;
		}catch(ParseException e){
			throw e;  
		}catch(Throwable e){
			throw new ParseException(e.getMessage());
		}
	}

	private String translateADQL(ADQLQuery query) throws TranslationException, InterruptedException, TAPException {
		ADQLTranslator translator = service.getFactory().createADQLTranslator();
		String translatedQuery = translator.translate(query);
		return translatedQuery;
	}

	private ResultSet executeQuery(String sql, ADQLQuery adql, DBConnection dbConn, String jobid) throws InterruptedException, TAPException {
		//logger.info("JOB " + jobid + " - QUERY: '"+sql+"'");
		final long startTime = System.currentTimeMillis();
		
		// Disable sequential scan for queries using q3c_join
		boolean allowSeqScan = true;
		if(sql.toLowerCase().indexOf("q3c_join")>=0){
			allowSeqScan = false;
		}
		
		dbConn.enableSeqScan(allowSeqScan);
		
		ResultSet result = (ResultSet)dbConn.executeQuery(sql, adql);
		
		// Reset enable_seqscan status
		if(!allowSeqScan){
			dbConn.enableSeqScan(true);
		}
		if (result == null){
			logger.info("JOB "+jobid+" - QUERY ABORTED AFTER "+(System.currentTimeMillis()-startTime)+" MS !");
		}else{
			logger.info("JOB "+jobid+" - QUERY SUCCESSFULLY EXECUTED IN "+(System.currentTimeMillis()-startTime)+" MS !");
		}
		return result;
	}

	protected OutputFormat getFormatter(TAPParameters tapParams) throws TAPException {
		// Search for the corresponding formatter:
		String format = tapParams.getFormat();
		OutputFormat formatter = service.getFactory().getOutputFormat((format == null)?TAPParameters.DEFAULT_OUTPUT_FORMAT:format);
		if (format != null && formatter == null){
			formatter = service.getFactory().getOutputFormat(TAPParameters.DEFAULT_OUTPUT_FORMAT);
		}

		// Format the result:
		if (formatter == null) {
			throw new TAPException("Impossible to format the query result: no formatter has been found for the given MIME type \""+format+"\" and for the default MIME type \""+TAPParameters.DEFAULT_OUTPUT_FORMAT+"\" (short form) !");
		}

		return formatter;
	}
	
	protected OutputFormat getCommonFormatter() throws TAPException {
		// Search for the corresponding formatter:
		OutputFormat formatter = service.getFactory().getOutputFormat(TAPParameters.COMMON_OUTPUT_FORMAT);

		// Format the result:
		if (formatter == null) {
			throw new TAPException("Impossible to format the query result: no formatter has been found for the MIME type \""+TAPParameters.COMMON_OUTPUT_FORMAT+"\" (short form) !");
		}

		return formatter;
	}

	protected void setExecutionProgression(ExecutionProgression progression){
		this.executionProgression=progression;
	}
	
	protected ExecutionProgression getExecutionProgression(){
		return executionProgression;
	}
	

}
