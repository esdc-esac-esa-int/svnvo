package esavo.sl.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsParameterValueType;
import esavo.uws.utils.test.database.DummyUwsData;
import esavo.uws.utils.test.database.DummyUwsDatabaseConnection;

public class DatabaseUtils {
	//job query
	public static final List<String> COLUMN_NAMES_QUERY_JOB = new ArrayList<String>(Arrays.asList(
			new String[]{"owner_id", "session_id", "phase_id", "quote", "start_time", "end_time", "wait_for_stop", "relative_path"}));
	
	public static final String QUERY_SELECT_JOB = "SELECT owner_id, session_id, phase_id, quote, start_time, end_time, wait_for_stop, relative_path "+
			"FROM uws2_schema.jobs WHERE job_id = ''{0}''";
	
	public static final String QUERY_INSERT_JOB = "INSERT INTO uws2_schema.jobs "+
			"(job_id, list_id, owner_id, session_id, phase_id, quote, start_time, end_time, wait_for_stop, relative_path) "+
			"VALUES ({0},{1},{2},{3},{4},{5},{6},{7},{8},{9})";
	
	public static final List<String> COLUMN_NAMES_QUERY_JOB_EXISTS = new ArrayList<String>(Arrays.asList(
			new String[]{"job_id"}));
	
	public static final String QUERY_JOB_EXISTS = "SELECT job_id "+
			"FROM uws2_schema.jobs WHERE job_id = ''{0}''";
	
	public static final List<String> COLUMN_NAMES_QUERY_JOB_LOCATION = new ArrayList<String>(Arrays.asList(
			new String[]{"relative_path"}));
	
	public static final String QUERY_JOB_LOCATION = "SELECT relative_path "+
			"FROM uws2_schema.jobs WHERE job_id = ''{0}''";
	
	public static final List<String> COLUMN_NAMES_QUERY_JOB_OLDER_THAN = new ArrayList<String>(Arrays.asList(
			new String[]{"job_id"}));
	
	public static final String QUERY_JOB_OLDER_THAN = "SELECT job_id "+
			"FROM uws2_schema.jobs WHERE owner_id = ''{0}'' AND "+
			"start_time < {1} AND job_id LIKE ''%{2}''";

	//execution phase
	public static final String QUERY_UPDATE_JOB_EXEC_PHASE = "UPDATE uws2_schema.jobs SET {0} = {1} WHERE job_id = {2}";
	
	
	public static final String QUERY_UPDATE_OWNER_PARAMETER = "UPDATE uws2_schema.owner_parameters SET " +
			"data_type = ''{0}'', string_representation = ''{1}'' " +
			"WHERE parameter_id = ''{2}'' AND owner_id = ''{3}''";

	
	public static final String QUERY_SET_STATEMENT_TIMEOUT = "SET statement_timeout TO {0}";
	
	//owner query
	public static final List<String> COLUMN_NAMES_QUERY_OWNER = new ArrayList<String>(Arrays.asList(
			new String[]{"auth_name", "pseudo"}));
	
	public static final String QUERY_SELECT_OWNER = "SELECT auth_name, pseudo "+
			"FROM uws2_schema.owners WHERE owner_id = ''{0}''";

	
	//parameter query
	public static final List<String> COLUMN_NAMES_QUERY_PARAMETERS = new ArrayList<String>(Arrays.asList(
			new String[]{"parameter_id", "data_type", "string_representation"}
			));
	
	public static final String QUERY_SELECT_PARAMETERS = "SELECT parameter_id, data_type, string_representation "+
			"FROM uws2_schema.job_parameters WHERE job_id = ''{0}''";
	
	public static final String QUERY_INSERT_PARAMETERS = "INSERT INTO uws2_schema.job_parameters "+
			"(parameter_id, job_id, parameter_type, data_type, string_representation) "+
			"VALUES ({0},{1},{2},{3},{4})";
	
	
	//error summary
	public static final List<String> COLUMN_NAMES_QUERY_ERROR_SUMMARY = new ArrayList<String>(Arrays.asList(
			new String[]{"message", "type", "details"}));
	
	public static final String QUERY_SELECT_ERROR_SUMMARY = "SELECT message, type, details "+
			"FROM uws2_schema.error_summary WHERE job_id = ''{0}''";
	
	public static final String QUERY_INSERT_ERROR_SUMMARY = "INSERT INTO uws2_schema.error_summary "+
			"(job_id, message, type, details) "+
			"VALUES ({0},{1},{2},{3})";
	
	
	
	//results
	public static final List<String> COLUMN_NAMES_QUERY_RESULTS = new ArrayList<String>(Arrays.asList(
			new String[]{"result_id", "href", "type", "mime_type", "size", "rows"}));
	
	public static final String QUERY_SELECT_RESULTS = "SELECT result_id, href, type, mime_type, size, rows "+
			"FROM uws2_schema.results where job_id = ''{0}''";
	
	public static final String QUERY_INSERT_RESULTS = "INSERT INTO uws2_schema.results "+
			"(result_id, job_id, href, type, mime_type, size, rows) "+
			"VALUES ({0},{1},{2},{3},{4},{5},{6})";
	
	
	//Tap metadata
	public static final List<String> COLUMN_NAMES_TAP_METADATA_INFO = new ArrayList<String>(Arrays.asList(
			new String[]{"column_name", "description", "ucd", "utype", "datatype", "unit", "size", "principal", "std", "indexed", "flags"}));
	

	/**
	 * Creates all the valid insert queries for a job:
	 * <ul>
	 * <li>insert into uws2_schema.jobs () values ()
	 * <li>insert into 
	 * </ul>
	 * @param job
	 */
	public static void createValidJobInsertionQueries(UwsJob job, DummyUwsDatabaseConnection connection){
		String jobid = job.getJobId();
		String ownerid = job.getOwner().getId();
		String session = job.getOwner().getSession();
		long quote = job.getQuote();
		String startTime = "" + getLongFromDate(job.getStartTime());
		String endTime = "" + getLongFromDate(job.getEndTime());
		
		//Insertion
		String jobInsertQuery = MessageFormat.format(QUERY_INSERT_JOB, new Object[]{
				"'"+jobid+"'","'ASYNC'","'"+ownerid+"'","'"+session+"'","'"+job.getPhase().name()+"'",""+quote,""+startTime,""+endTime,"1000","NULL"
		});
		DummyUwsData jobInsertData = createSingleInsertOrUpdateData();
		connection.setDataForQuery(jobInsertQuery, jobInsertData);
		
		//Parameters
		createDefaultParametersData(connection, job);
		
		//Simulate job already exists
		String jobExistsQuery = MessageFormat.format(QUERY_JOB_EXISTS, new Object[]{jobid});
		DummyUwsData jobExistsData = createJobExistsData(jobid);
		connection.setDataForQuery(jobExistsQuery, jobExistsData);
	}
	
	public static void createSetTimeoutForQuery(DummyUwsDatabaseConnection connection, long timeout){
		String query = "SET statement_timeout TO "+timeout;
		DummyUwsData d = createSingleInsertOrUpdateData();
		connection.setDataForQuery(query, d);
	}
	
	public static DummyUwsData createSingleInsertOrUpdateData(){
		return createInsertOrUpdateData(1);
	}
	
	public static DummyUwsData createInsertOrUpdateData(int numRowsAffected){
		DummyUwsData d = new DummyUwsData();
		d.setUpdateAffectedRows(numRowsAffected);
		return d;
	}
	
	public static void createDefaultParametersData(DummyUwsDatabaseConnection connection, UwsJob job){
		String query;
		String parameterid = "executionDuration";
		query = MessageFormat.format(QUERY_INSERT_PARAMETERS, new Object[]{
				"'"+parameterid+"'", 
				"'"+job.getJobId()+"'", 
				"'COMMONG'",
				"'Long'",
				"'"+job.getExecutionDuration()+"'"
		});
		DummyUwsData d = createSingleInsertOrUpdateData();
		connection.setDataForQuery(query, d);
	}
	
	
	public static DummyUwsData createSingleRowDummyData(List<String> columnNames, String[] singleRowData){
		List<List<String>> rowResults = new ArrayList<List<String>>();
		appendRowData(rowResults, singleRowData);
		return createDummyData(columnNames, rowResults);
	}
	
	public static DummyUwsData createDummyData(List<String> columnNames,
			List<List<String>> results) {
		DummyUwsData d = new DummyUwsData();
		d.setColumnNames(columnNames);
		d.setData(results);
		// check dimensions
		if (results != null) {
			for (List<String> r : results) {
				if (r != null) {
					if (r.size() != columnNames.size()) {
						throw new IllegalArgumentException(
								"Wrong number of columns in query.\nColumn names: "
										+ columnNames + "\nRow data:" + r);
					}
				}
			}
		}
		return d;
	}

	public static DummyUwsData createDummyData(String[] columnNames,
			String[][] results) {
		DummyUwsData d = new DummyUwsData();
		d.setColumnNames(columnNames);
		d.setData(results);
		// check dimensions
		if (results != null) {
			for (String[] r : results) {
				if (r != null) {
					if (r.length != columnNames.length) {
						throw new IllegalArgumentException(
								"Wrong number of columns in query.\nColumn names: "
										+ columnNames + "\nRow data:" + r);
					}
				}
			}
		}
		return d;
	}

	public static DummyUwsData createDummyData(List<String> columnNames,
			String[][] results) {
		DummyUwsData d = new DummyUwsData();
		d.setColumnNames(columnNames);
		d.setData(results);
		// check dimensions
		if (results != null) {
			for (String[] r : results) {
				if (r != null) {
					if (r.length != columnNames.size()) {
						throw new IllegalArgumentException(
								"Wrong number of columns in query.\nColumn names: "
										+ columnNames + "\nRow data:" + r);
					}
				}
			}
		}
		return d;
	}

	public static DummyUwsData createSingleJobResultsData(String resultid, String href, String type, String mimeType, String size, String rows){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_RESULTS, new String[]{resultid, href, type, mimeType, size, rows});
	}
	
	public static DummyUwsData createSingleErrorSummaryData(String message, String type, String details){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_ERROR_SUMMARY, new String[]{message, type, details});
	}
	
//	public static DummyData createSingleParameterData(String parameterid, ParameterValueType dataType, String value){
//		return createSingleRowDummyData(COLUMN_NAMES_QUERY_PARAMETERS, new String[]{parameterid, dataType.name(), value});
//	}

	public static DummyUwsData createSingleOwnerData(String authName, String pseudo){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_OWNER, new String[]{authName, pseudo});
	}
	
	public static DummyUwsData createSingleJobData(String ownerid, String session, String phaseid, String quote, String startTime, String endTime, String waitForStop, String locationid){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_JOB, new String[]{ownerid, session, phaseid, quote, startTime, endTime, waitForStop, locationid});
	}
	
	public static DummyUwsData createJobExistsData(String jobid){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_JOB_EXISTS, new String[]{jobid});
	}
	
	public static DummyUwsData createJobLocationData(String location){
		return createSingleRowDummyData(COLUMN_NAMES_QUERY_JOB_LOCATION, new String[]{location});
	}
	
	public static DummyUwsData createTapMetaInfoData(List<List<String>> results){
		return createDummyData(COLUMN_NAMES_TAP_METADATA_INFO, results);
	}
	
	public static void appendRowData(List<List<String>> rows, String[] data){
		List<String> row = new ArrayList<String>();
		for(String d: data){
			row.add(d);
		}
		rows.add(row);
	}

	public static long getLongFromDate(Date d){
		if(d == null){
			return 0;
		}else{
			return d.getTime();
		}
	}
	
	public static void createOwnerUpdateQueries(DummyUwsDatabaseConnection[] dbConn, UwsJobOwner owner, String schemaName, String tableName){
		Map<String, DummyUwsData> queries = DatabaseUtils.createOwnerUpdateQueries(owner, schemaName, tableName);
		for(Entry<String, DummyUwsData> e: queries.entrySet()){
			for(DummyUwsDatabaseConnection d: dbConn){
				d.setDataForQuery(e.getKey(), e.getValue());
			}
		}
	}
	
	public static Map<String, DummyUwsData> createOwnerUpdateQueries(UwsJobOwner owner, String schemaName, String tableName){
		Map<String,DummyUwsData> queries = new HashMap<String, DummyUwsData>();
		
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("uws2_schema.db_table_usage");
		String[] singleRowData = {"0"};
		DummyUwsData data = DatabaseUtils.createSingleRowDummyData(columnNames, singleRowData);
		String query = "SELECT uws2_schema.db_table_usage('"+schemaName+"','"+tableName+"')";
		queries.put(query, data);
		
		columnNames.clear();
		columnNames.add("uws2_schema.db_user_usage");
		singleRowData[0] = "0";
		data = DatabaseUtils.createSingleRowDummyData(columnNames, singleRowData);
		query = "SELECT uws2_schema.db_user_usage('"+owner.getId()+"')";
		queries.put(query, data);
		
		String authName = owner.getAuthUsername() == null ? "NULL" : owner.getAuthUsername();
		String pseudo = owner.getPseudo() == null ? "NULL" : owner.getPseudo();
		
		query = "UPDATE uws2_schema.owners SET auth_name = "+authName+", pseudo = "+pseudo+", roles = "+owner.getRoles()+" " +
				"WHERE owner_id = '"+owner.getId()+"'";
		data = DatabaseUtils.createSingleInsertOrUpdateData();
		queries.put(query, data);
		
		UwsJobOwnerParameters params = owner.getParameters();
		if(params != null){
			Object parameterValue;
			String ownerid = owner.getId();
			String strRepresentation;
			for(String parameterName: params.getParameterNames()){
				parameterValue = params.getParameter(parameterName);
				UwsParameterValueType pvt = UwsJobOwnerParameters.getParameterValueType(parameterValue);
				strRepresentation = UwsJobOwnerParameters.getParameterStringRepresentation(pvt, parameterValue);
				String q = MessageFormat.format(QUERY_UPDATE_OWNER_PARAMETER, 
						new Object[]{pvt.name(), strRepresentation,parameterName, ownerid});
				data = createSingleInsertOrUpdateData();
				queries.put(q,data);
			}
		}
		
		return queries;
	}
	
	public static List<String> getColumnNamesFromSelectQuery(String selectQuery){
		String q = selectQuery.trim();
		int p = selectQuery.indexOf("SELECT ");
		if(p < 0){
			p = selectQuery.indexOf("select ");
			if(p < 0){
				return null;
			}
		}
		String data;
		int f = selectQuery.indexOf(" FROM ");
		if(f < 0){
			f = selectQuery.indexOf(" from ");
			if(f < 0){
				data = selectQuery.substring(p+7);
			}else{
				data = selectQuery.substring(p+7, f);
			}
		}else{
			data = selectQuery.substring(p+7, f);
		}
		data = data.trim();
		String[] items = data.split(",");
		List<String> results = new ArrayList<String>();
		for(String i: items){
			results.add(i.trim());
		}
		return results;
	}
	
}
