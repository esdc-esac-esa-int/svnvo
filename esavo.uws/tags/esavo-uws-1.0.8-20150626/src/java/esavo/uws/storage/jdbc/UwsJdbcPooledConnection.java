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
package esavo.uws.storage.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobDetailsComparison;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.utils.UwsErrorType;
import esavo.uws.utils.UwsParameterValueType;
import esavo.uws.utils.UwsUtils;

/**
 * Database handler. See {@link UwsJdbcStorage}
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class UwsJdbcPooledConnection {
	
	private static final Logger LOG = Logger.getLogger(UwsJdbcPooledConnection.class.getName());
	
	public static final String PARAMETER_COMMON = "COMMON";
//	public static final String PARAMETER_ADDITIONAL = "ADDITIONAL";
//	public static final String PARAMETER_CONTROLLER = "CONTROLLER";
//	public static final String PARAMETER_EXPECTED = "EXPECTED";
	
	private static final String JOB_BY_ID = "SELECT owner_id, session_id, phase_id, quote, " +
			"start_time, end_time, destruction_time, execution_duration, relative_path, list_id, priority "+
			"FROM uws2_schema.jobs_meta WHERE job_id = ?";
	
	private static final String JOBS_BY_OWNER = "SELECT owner_id, session_id, phase_id, quote, " +
			"start_time, end_time, destruction_time, execution_duration, relative_path, list_id, priority, job_id "+
			"FROM uws2_schema.jobs_meta WHERE owner_id = ?";
	private static final int JOBS_BY_OWNER_JOB_ID_INDEX = 12; //position of 'job_id' in JOBS_BY_ONWER
	
	private static final String JOBS_BY_LIST = "SELECT owner_id, session_id, phase_id, quote, " +
			"start_time, end_time, destruction_time, execution_duration, relative_path, list_id, priority, job_id "+
			"FROM uws2_schema.jobs_meta WHERE list_id = ?";
	private static final int JOBS_BY_LIST_JOB_ID_INDEX = 12; //position of 'job_id' in JOBS_BY_LIST
	
//	private static final String JOBS_BY_OWNER_AND_SESSION = "SELECT owner_id, session_id, phase_id, quote, " +
//			"start_time, end_time, wait_for_stop, relative_path, job_id "+
//			"FROM uws2_schema.jobs_meta WHERE owner_id = ? AND session_id = ?";
	
	private static final String PARAMETER_INSERT = "INSERT INTO uws2_schema.job_parameters "+
			"(parameter_id, job_id, parameter_type, data_type, string_representation) "+
			"VALUES (?,?,?,?,?)";
	
	private static final String PARAMETER_UPDATE = "UPDATE uws2_schema.job_parameters SET "+
			"data_type = ?, string_representation = ? WHERE job_id = ? AND parameter_id = ? AND parameter_type = ?";
	
	private static final String PARAMETER_OWNER_INSERT = "INSERT INTO uws2_schema.owner_parameters "+
			"(parameter_id, owner_id, data_type, string_representation) "+
			"VALUES (?,?,?,?)";
	
	private static final String PARAMETER_OWNER_UPDATE = "UPDATE uws2_schema.owner_parameters SET data_type = ?, string_representation = ? " +
			"WHERE parameter_id = ? AND owner_id = ?";
	
	private static final String NOTIFICATION_INSERT = "INSERT INTO notifications_schema.notifications "+
			"(notification_id, type, subtype, description, creation_time) "+
			"VALUES (?,?,?,?,?)";

	private static final String NOTIFICATION_RELATION_INSERT = "INSERT INTO notifications_schema.notifications_users "+
			"(notification_id, user_id) "+
			"VALUES (?,?)";

	
	private static final Set<String> TO_BE_LAUNCHED_JOBS_PHASES = new HashSet<String>();
	static{
		for(UwsJobPhase phase: UwsUtils.TO_BE_RELAUNCHED_JOBS_PHASES){
			TO_BE_LAUNCHED_JOBS_PHASES.add(phase.name());
		}
	}

	
	private Connection dbConn = null;
	private Statement stmt = null;	
	private UwsJdbcStorageSingleton jdbcSingleton;
	
	public UwsJdbcPooledConnection(UwsJdbcStorageSingleton jdbcSingleton) throws SQLException {
		this.jdbcSingleton = jdbcSingleton;
		this.dbConn = jdbcSingleton.getConnection();
		dbConn.createStatement().execute("SET statement_timeout TO 1800000");
	}
	
	public void close() throws SQLException {
		try {
			dbConn.close();
		} catch (SQLException e) {
			throw new SQLException("Impossible to close DB connection, because: " + e.getMessage(), e);
		}
	}

	public UwsJob getJobMeta(String jobid) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement(JOB_BY_ID);
		statement.setString(1, jobid);
		ResultSet rs = statement.executeQuery();

		if (rs.next()) {
			return loadJobMeta(rs, jobid);
		} else {
			throw new SQLException("Job id '" + jobid + "' not found");
		}
	}
	
	public boolean checkJobExists(String jobid) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement(JOB_BY_ID);
		statement.setString(1, jobid);
		ResultSet rs = statement.executeQuery();

		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<UwsJobDetails> retrieveJobsByFilter(UwsJobsFilter filter, long offset, long limit) throws SQLException {
		String query = "SELECT "
				+ "j.job_id, j.owner_id, j.phase_id, j.start_time, j.end_time, j.relative_path, p.string_representation FROM "
				+ "uws2_schema.jobs_meta AS j, uws2_schema.job_parameters p WHERE "
				+ "(j.job_id = p.job_id) AND (p.parameter_id = 'query')";
		String jobsQueryFilter = getJobsFilter(filter);
		if (jobsQueryFilter != null && !"".equals(jobsQueryFilter)) {
			query += jobsQueryFilter;
		}
		query += " ORDER BY j.owner_id";
		if (offset > -1) {
			query += " OFFSET " + offset;
		}
		if (limit > -1) {
			query += " LIMIT " + limit;
		}
		List<UwsJobDetails> jobs = new ArrayList<UwsJobDetails>();
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			while (result.next()) {
				UwsJobDetails job = new UwsJobDetails();
				job.setJobid(result.getString("job_id"));
				job.setOwnerid(result.getString("owner_id"));
				job.setPhaseid(result.getString("phase_id"));
				job.setStartTime(result.getLong("start_time"));
				job.setEndTime(result.getLong("end_time"));
				job.setRelativePath(result.getString("relative_path"));
				job.setQuery(result.getString("string_representation"));
				jobs.add(job);
			}
			return jobs;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain user list.", e);
		}

	}

	
	public UwsJobOwner getOwner(String ownerid, boolean force) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement("SELECT auth_name, pseudo, roles FROM uws2_schema.owners WHERE owner_id = ?");
		statement.setString(1, ownerid);
		ResultSet rs = statement.executeQuery();
		if(rs.next()){
			int roles = rs.getInt("roles");
			UwsJobOwner owner = new UwsJobOwner(ownerid, roles);
			owner.setPseudo(rs.getString("pseudo"));
			owner.setAuthUsername(rs.getString("auth_name"));
			UwsJobOwnerParameters parameters = loadOwnerParameters(ownerid);
			owner.setParameters(parameters);
			return owner;
		}else{
			if(force){
				throw new SQLException("Owner id '"+ownerid+"' not found");
			}else{
				return null;
			}
		}

	}
	
	/**
	 * Obtains the database size used by the given owner (in bytes)
	 * @param ownerid
	 * @return
	 * @throws SQLException
	 */
	public long getDbSize(String ownerid) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement("SELECT tap_schema.db_user_usage(?)");
		statement.setString(1, ownerid);
		ResultSet rs = statement.executeQuery();

		if(rs.next()){
			long value = rs.getLong("db_user_usage");
			statement.close();
			return value;
		}else{
			statement.close();
			throw new SQLException("Owner id '"+ownerid+"' not found");
		}

	}
	
	/**
	 * Obtains the database size used by the given owner and table (in bytes)
	 * @param ownerid
	 * @return
	 * @throws SQLException
	 */
	public long getDbTableSize(String ownerid, String table) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement("SELECT tap_schema.db_table_usage(?,?)");
		statement.setString(1, ownerid);
		statement.setString(2, table);
		ResultSet rs = statement.executeQuery();

		if(rs.next()){
			long value = rs.getLong("db_table_usage");
			statement.close();
			return value;
		}else{
			statement.close();
			throw new SQLException("Owner id '"+ownerid+"' not found");
		}

	}

	/**
	 * This method checks whether the job already exists (useful when restoring jobs from database: the job already exists)
	 * @param Job
	 * @throws SQLException
	 */
	public boolean addNewJobMetaIfNeeded(UwsJob job) throws SQLException {
		if(!checkExists("SELECT job_id FROM uws2_schema.jobs_meta WHERE job_id = '"+job.getJobId()+"'")){
			return addNewJobMeta(job);
		} else {
			//TODO something to update instead...?
			return false;
		}
	}

	/**
	 * Searches for jobs with an specific value in a specific parameter.<br/>
	 * 'value' can be null.
	 * @param parameterName
	 * @param value
	 * @return
	 * @throws SQLException
	 * @throws UWSException
	 */
	public List<UwsJob> searchByParameter(String parameterName, String value) throws SQLException {
		String sql;
		if(value != null) {
			sql = "SELECT job_id FROM uws2_schema.job_parameters WHERE parameter_id = '"+parameterName+"' AND string_representation = '"+value+"'";
		} else {
			sql = "SELECT job_id FROM uws2_schema.job_parameters WHERE parameter_id = '"+parameterName+"' AND string_representation IS NULL";
		}
		
		ResultSet rs = executeQuery(sql);
		List<UwsJob> jobs = new ArrayList<UwsJob>();
		
		while(rs.next()){
			jobs.add(getJobMeta(rs.getString(1)));
		}

		return jobs;
	}
	
	public void updateAllParameters(UwsJob job) throws SQLException {
		Set<String> params = job.getParameters().getParameterNames();
		startTransaction();
		try{
			//1. all parameters not in params must be deleted
			removeUnusedParameters(job.getJobId(), params);
			
			//2. parameters must be created or updated
			for(String p: params){
				createOrUpdateParameter(job, p);
			}
			endTransaction();
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}

	/**
	 * Returns true if the parameter is removed.
	 * @param jobid
	 * @param parameterid
	 * @return
	 * @throws SQLException
	 */
	public boolean removeParameter(String jobid, String parameterid) throws SQLException{
		return executeUpdate("DELETE FROM uws2_schema.job_parameters WHERE job_id = '"+jobid+
				"' AND parameter_id = '"+parameterid+"' AND parameter_type = '"+PARAMETER_COMMON+"'") == 1;
	}
	
	public boolean updateParameter(UwsJob job, String parameterid) throws SQLException {
		Object o = job.getParameters().getParameter(parameterid);
		UwsParameterValueType pvt = UwsJobParameters.getParameterValueType(o);

		PreparedStatement statement = dbConn.prepareStatement(PARAMETER_UPDATE);
		statement.setString(1, pvt.name());
		setStatementString(statement, 2, UwsJobParameters.getParameterStringRepresentation(pvt, o));
		statement.setString(3, job.getJobId());
		statement.setString(4, parameterid);
		statement.setString(5, PARAMETER_COMMON);
		
		return statement.executeUpdate() == 1;
	}
	
	public boolean createParameter(UwsJob job, String parameterid) throws SQLException {
		return addNewParameter(
				job.getJobId(), 
				parameterid, 
				PARAMETER_COMMON, 
				job.getParameters().getParameter(parameterid));
	}
	
	public boolean createOrUpdateParameter(UwsJob job, String parameterid) throws SQLException {
		//Performs an update: if update is '0' rows it means it does not exist, so create it
		if(!updateParameter(job, parameterid)){
			//Parameter does not exist: create it
			return createParameter(job, parameterid);
		} else {
			return true;
		}
	}

	public boolean updateJobAttribute(UwsJob job, UwsJobAttribute updateType) throws SQLException {
		String column = null;
		switch(updateType){
		case EndTime:
			column = "end_time";
			break;
		case StarTime:
			column = "start_time";
			break;
		case Quote:
			column = "quote";
			break;
		case ExecutionDuration:
			column = "execution_duration";
			break;
		case DestructionTime:
			column = "destruction_time";
			break;
		case Phase:
			column = "phase_id";
			break;
		default:
			throw new SQLException("Unable to update attribute " + updateType.name() + " for job: " + job.getJobId());
		}
		PreparedStatement statement = dbConn.prepareStatement("UPDATE uws2_schema.jobs_meta SET "+column+" = ? WHERE job_id = ?");
		statement.setString(2, job.getJobId());
		switch(updateType){
		case EndTime:
			setStatementLongFromDate(statement, 1, job.getEndTime());
			break;
		case StarTime:
			setStatementLongFromDate(statement, 1, job.getStartTime());
			break;
		case Quote:
			statement.setLong(1, job.getQuote());
			//setStatementLongFromDate(statement, 1, job.getQuote());
			break;
		case ExecutionDuration:
			statement.setLong(1, job.getExecutionDuration());
			break;
		case DestructionTime:
			setStatementLongFromDate(statement, 1, job.getDestructionTime());
			break;
		case Phase:
			setStatementString(statement, 1, job.getPhase().name());
			break;
		default:
			throw new SQLException("Unable to update attribute " + updateType.name() + " for job: " + job.getJobId());
		}
		return statement.executeUpdate() == 1;
	}

	/**
	 * Returns 'true' if the job result is added.
	 * @param job
	 * @param res
	 * @return
	 * @throws SQLException 
	 */
	public boolean addJobResultMeta(String jobid, UwsJobResultMeta res) throws SQLException {
		String resid = res.getId();
		PreparedStatement statement = dbConn.prepareStatement("INSERT INTO uws2_schema.results_meta (result_id, job_id, type, mime_type, size, rows) VALUES (?,?,?,?,?,?)");
		statement.setString(1, resid);
		statement.setString(2, jobid);
		setStatementString(statement, 3, res.getType().name());
		setStatementString(statement, 4, res.getMimeType());
		statement.setLong(5, res.getSize());
		statement.setLong(6, res.getRows());
		return statement.executeUpdate() == 1;
	}
	
	/**
	 * Returns 'true' if the error summary is added.
	 * @param jobid
	 * @param errorSummary
	 * @return
	 * @throws SQLException
	 */
	public boolean addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws SQLException {
		startTransaction();
		try{
			deleteErrorSummary(jobid);
			PreparedStatement statement = dbConn.prepareStatement("INSERT INTO uws2_schema.error_summary_meta (job_id, message, type, details, details_mime_type, details_size) VALUES (?,?,?,?,?,?)");
			statement.setString(1, jobid);
			setStatementString(statement, 2, errorSummary.getMessage());
			statement.setString(3, errorSummary.getType().name());
			statement.setBoolean(4, errorSummary.hasDetails());
			setStatementString(statement, 5, errorSummary.getDetailsMimeType());
			statement.setLong(6, errorSummary.getDetailsSize());
			boolean result = statement.executeUpdate() == 1;
			if(!result){
				cancelTransaction();
				throw new SQLException("Cannot insert error summary in database. (Jobid: "+jobid+")");
			}
			endTransaction();
			return true;
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}

	public int getNumJobs() throws SQLException {
		return executeSingleValueInteger("SELECT COUNT(*) FROM uws2_schema.jobs_meta");
	}
	
	public int getNumOwners() throws SQLException {
		return executeSingleValueInteger("SELECT COUNT(*) FROM uws2_schema.owners");
	}
	
	public List<UwsJob> getJobsByOwner(String ownerid) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement(JOBS_BY_OWNER);
		statement.setString(1, ownerid);
		ResultSet rs = statement.executeQuery();
		
		List<UwsJob> jobs = new ArrayList<UwsJob>();
		String jobid;
		while(rs.next()){
			jobid = rs.getString(JOBS_BY_OWNER_JOB_ID_INDEX);
			jobs.add(loadJobMeta(rs, jobid));
		}
		return jobs;
	}
	
	
	public List<UwsJob> getJobsByList(String listName, List<UwsOwnerSessionFilter> ownersFilter, String appid) throws SQLException {
		PreparedStatement statement;
		if(ownersFilter == null || ownersFilter.size() < 1){
			statement = dbConn.prepareStatement(JOBS_BY_LIST);
			statement.setString(1, listName);
		}else{
			StringBuilder sb = new StringBuilder(JOBS_BY_LIST);
			sb.append(populateOwnersIdSessionFilter(ownersFilter));
			statement = dbConn.prepareStatement(sb.toString());
			statement.setString(1, listName);
		}
		
		ResultSet rs = statement.executeQuery();
		
		List<UwsJob> jobs = new ArrayList<UwsJob>();
		String jobid;
		while(rs.next()){
			jobid = rs.getString(JOBS_BY_LIST_JOB_ID_INDEX);
			if(UwsUtils.isJobValidForAppid(jobid, appid)){
				jobs.add(loadJobMeta(rs, jobid));
			}
		}
		return jobs;
	}
	
	public List<String> getOldJobs(String appid, long currentTime) throws SQLException {
		String query = "SELECT job_id FROM uws2_schema.jobs_meta WHERE job_id LIKE '%"+appid+"' AND "+
				"destruction_time > 0 AND destruction_time < " + currentTime;
		ResultSet result;
		LOG.info("Removal check query: '"+query+"'");
		try {
			result = dbConn.createStatement().executeQuery(query);
			List<String> jobs = new ArrayList<String>();
			String jobid;
			while (result.next()) {
				jobid = result.getString(1);
				jobs.add(jobid);
			}
			return jobs;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain user list.", e);
		}
	}
	
	private String populateOwnersIdSessionFilter(List<UwsOwnerSessionFilter> ownersFilter){
		if(ownersFilter == null){
			return "";
		}
		int size = ownersFilter.size();
		StringBuilder sb = new StringBuilder();
		sb.append(" AND (");
		if(size == 1){
			sb.append(getOwnerIdSessionFilter(ownersFilter.get(0)));
		}else{
			boolean firstTime = true;
			for(UwsOwnerSessionFilter f: ownersFilter){
				if(firstTime){
					firstTime = false;
				}else{
					sb.append(" OR ");
				}
				sb.append("(").append(getOwnerIdSessionFilter(f)).append(")");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String getOwnerIdSessionFilter(UwsOwnerSessionFilter filter){
		if(filter.hasSession()){
			return "owner_id = '" + filter.getOwnerid() + "' AND session_id = '"+filter.getSessionid()+"'";
		}else{
			return "owner_id = '" + filter.getOwnerid() + "'";
		}

	}
	
	
	public List<UwsJob> getPendingJobs() throws SQLException {
		String sql = "SELECT job_id FROM uws2_schema.jobs_meta WHERE phase_id IN ("+createIn(TO_BE_LAUNCHED_JOBS_PHASES)+')';
		ResultSet rs = executeQuery(sql);
		
		List<String> jobids = new ArrayList<String>();
		while(rs.next()){
			jobids.add(rs.getString(1));
		}
		
		if(jobids.size() > 0){
			List<UwsJob> pendingJobs = new ArrayList<UwsJob>();
			for(String jobid: jobids){
				pendingJobs.add(getJobMeta(jobid));
			}
			return pendingJobs;
		} else {
			return null;
		}
	}

	/**
	 * Removes job output data (Results and ErrorSummary).<br/>
	 * Job main data is not removed.<br/>
	 * This method is useful for restarting a job.
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public boolean removeJobOutputMeta (String jobid) throws SQLException {
		//results + error summar
		startTransaction();
		try{
			deleteErrorSummary(jobid);
			deleteResults(jobid);
			endTransaction();
			return true;
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}
	
	/**
	 * Removes a job: metadata, parameters, results and error
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public boolean removeJobAndAssociatedMeta(String jobid) throws SQLException {
		startTransaction();
		try{
			deleteErrorSummary(jobid);
			deleteResults(jobid);
			deleteParameters(jobid);
			if(executeUpdate("DELETE FROM uws2_schema.jobs_meta WHERE job_id = '"+jobid+"'") != 1){
				cancelTransaction();
				return false;
			}
			endTransaction();
			return true;
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}

	private void removeUnusedParameters(String jobid, Set<String> validParamNames) throws SQLException {
		if(validParamNames == null || validParamNames.size() < 1){
			return;
		}
		
		Set<String> toRemove = getUnusedParameters(jobid, validParamNames);
		
		if(toRemove.size() > 0){
			//Remove
			String sqlRemove = "DELETE FROM uws2_schema.job_parameters WHERE job_id = '' AND parameter_type = '' AND parameter_id IN (" +
			createIn(toRemove) + ')';
			int r = executeUpdate(sqlRemove.toString());
			if(r != toRemove.size()){
				throw new SQLException("Job parameters removal error. Expected to be removed: " + toRemove.size() + ", removed: " + r +
						".\n sql: " + sqlRemove.toString() + "\nValid parameter names: " + validParamNames);
			}
		}
	}
	
	private Set<String> getUnusedParameters(String jobid, Set<String> validParamNames) throws SQLException {
		String inItems = createIn(validParamNames);
		if("".equals(inItems)){
			return null;
		}
		String sql = "SELECT parameter_id FROM uws2_schema.job_parameters WHERE job_id = '"+jobid+
				"' AND parameter_type = '"+PARAMETER_COMMON+"' AND parameter_id NOT IN (" + inItems + ')';
		ResultSet rs = executeQuery(sql.toString());
		
		Set<String> toRemove = new HashSet<String>();
		while(rs.next()){
			toRemove.add(rs.getString(1));
		}
		
		return toRemove;
	}
	
	private UwsJobParameters getParameters (String jobid) throws SQLException {
		String sql = "SELECT parameter_id, data_type, string_representation FROM uws2_schema.job_parameters WHERE job_id = '"+jobid+"'";
		ResultSet rs = executeQuery(sql);
		Map<String,Object> params = new HashMap<String, Object>();
		while(rs.next()){
			params.put(rs.getString(1), UwsJobParameters.getParameterValue(rs.getString(2), rs.getString(3)));
		}
		if(params.size() > 0){
			return new UwsJobParameters(params);
		} else {
			return new UwsJobParameters();
		}
	}

	/**
	 * Returns 'null' if no results are associated to the specified job id.
	 * @param jobid
	 * @return
	 * @throws SQLException 
	 */
	private List<UwsJobResultMeta> getResultsMeta(String jobid) throws SQLException{
		String sql = "SELECT result_id, type, mime_type, size, rows "+
				"FROM uws2_schema.results_meta where job_id = '"+jobid+"'";
		ResultSet rs = executeQuery(sql);
		List<UwsJobResultMeta> results = new ArrayList<UwsJobResultMeta>();
		while(rs.next()){
			String name = rs.getString(1);
			UwsJobResultMeta r = new UwsJobResultMeta(name);
			String resultType = rs.getString(2);
			r.setType(UwsJobResultMeta.ResultType.valueOf(resultType));
			r.setMimeType(rs.getString(3));
			r.setSize(rs.getLong(4));
			r.setRows(rs.getLong(5));
			results.add(r);
		}
		if(results.size() > 0){
			return results;
		}else{
			return null;
		}
	}

	
	/**
	 * Returns 'null' if no error associated to the specified job id is found.
	 * @param jobid
	 * @return
	 * @throws SQLException 
	 */
	private UwsJobErrorSummaryMeta getErrorSummaryMeta(String jobid) throws SQLException{
		String sql = "SELECT message, type, details, details_mime_type, details_size FROM uws2_schema.error_summary_meta WHERE job_id = '"+jobid+"'";
		ResultSet rs = executeQuery(sql);
		if(rs.next()){
			String msg = rs.getString("message");
			UwsErrorType errorType = UwsErrorType.valueOf(rs.getString("type"));
			boolean hasDetails = rs.getBoolean("details");
			String detailsMimeType = rs.getString("details_mime_type");
			long detailsSize = rs.getLong("details_size");
			return new UwsJobErrorSummaryMeta(msg, errorType, hasDetails, detailsMimeType, detailsSize);
		}else{
			return null;
		}
	}

	private UwsJob loadJobMeta(ResultSet rs, String jobid) throws SQLException {
		//Job data from result set
		String ownerid = rs.getString(1);
		String phase = rs.getString(3);
		long quote = rs.getLong(4);
		long startTime = rs.getLong(5);
		long endTime = rs.getLong(6);
		long destructionTime = rs.getLong(7);
		long executionDuration = rs.getLong(8);
		String locationid = rs.getString(9);
		String listid = rs.getString(10);
		int priority = rs.getInt(11);
		
		//Results, parameters, error
		List<UwsJobResultMeta> results = getResultsMeta(jobid);
		UwsJobParameters params = getParameters(jobid);
		UwsJobErrorSummaryMeta error = getErrorSummaryMeta(jobid);
		UwsJobOwner owner = getOwner(ownerid, true);
		
		//job creation
		UwsJob job;
		
		try{
			job = jdbcSingleton.getCreator().createJob(jobid, owner, listid, locationid, results, priority);
		}catch(UwsException e){
			throw new SQLException("Cannot create job '"+jobid+"' due to: " + e.getMessage(), e);
		}
		
		//disable notifications
		job.setEnableUpdates(false);
		
		//set data
		try{
			job.setPhase(UwsJobPhase.valueOf(phase));
			job.setQuote(quote);
			//job.setQuote(getDateFromStorage(quote));
			job.setStartTime(getDateFromStorage(startTime));
			job.setEndTime(getDateFromStorage(endTime));
			job.setDestructionTime(getDateFromStorage(destructionTime));
			job.setExecutionDuration(executionDuration);
			job.setParameters(params);
			job.setErrorSummary(error);
		}catch(UwsException e){
			//Cannot happen. The UwsException is raised when the notification mechanism fails.
			//The notification mechaism is disabled (setEnabledNotification(false)) so, UwsException cannot appear. 
		}
		
		//enable notifications
		job.setEnableUpdates(true);
		
		return job;
	}
	
	private boolean addNewJobMeta(UwsJob job) throws SQLException{
		startTransaction();
		
		PreparedStatement statement = null;
		try{
			//Not necessary: UwsJobsOwnersManager creates the owner if necessary
//			//1. Create owner if required
//			addOwnerIfRequired(job.getOwner());
			
			//2. Inser job
			statement = dbConn.prepareStatement(
					"INSERT INTO uws2_schema.jobs_meta "+
					"(job_id, list_id, owner_id, session_id, phase_id, quote, " +
					"start_time, end_time, destruction_time, execution_duration, " +
					"relative_path, priority) VALUES "+
					"(?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, job.getJobId());
			statement.setString(2, job.getListid());
			statement.setString(3, job.getOwner().getId());
			setStatementString(statement, 4, job.getOwner().getSession());
			setStatementString(statement, 5, job.getPhase().name());
			statement.setLong(6, job.getQuote());
			//setStatementLongFromDate(statement, 6, job.getQuote());
			setStatementLongFromDate(statement, 7, job.getStartTime());
			setStatementLongFromDate(statement, 8, job.getEndTime());
			setStatementLongFromDate(statement, 9, job.getDestructionTime());
			statement.setLong(10, job.getExecutionDuration());
			setStatementString(statement, 11, job.getLocationId());
			statement.setInt(12, job.getPriority());
			
			int inserted = statement.executeUpdate();
			if(inserted != 1){
				cancelTransaction(statement);
				throw new SQLException("Cannot insert job in database");
			}
		
			//3. Add parameters:
			addNewParameters(job);
		
			//At this point, there is no results nor errors associated to this job
		
			//4. End transaction
			endTransaction();
			return true;
		}catch(SQLException sql){
			cancelTransaction(statement);
			throw sql;
		}
	}
	
	public void addOwner(UwsJobOwner owner) throws SQLException {
		String authName = owner.getAuthUsername();
		String pseudo = owner.getPseudo();
		
		startTransaction();
		
		PreparedStatement statement = null;
		try{
			//Add owner
			statement = dbConn.prepareStatement(
				"INSERT INTO uws2_schema.owners (owner_id, auth_name, pseudo, roles) " +
				"VALUES (?,?,?,?)");
			statement.setString(1, owner.getId());
			setStatementString(statement, 2, authName);
			setStatementString(statement, 3, pseudo);
			statement.setInt(4, owner.getRoles());
			
			int inserted = statement.executeUpdate();
			if(inserted != 1){
				cancelTransaction(statement);
				throw new SQLException("Cannot insert owner in database");
			}
			
			//Add parameters
			addNewOwnerParameters(owner);
			
			//End
			endTransaction();
		}catch(SQLException sql){
			cancelTransaction();
			throw sql;
		}
	}

	public void updateOwner(UwsJobOwner owner) throws SQLException {
		String authName = owner.getAuthUsername();
		String pseudo = owner.getPseudo();
		
		startTransaction();
		
		PreparedStatement statement = null;
		try{
			//Add owner
			statement = dbConn.prepareStatement(
				"UPDATE uws2_schema.owners SET auth_name = ?, pseudo = ?, roles = ? " +
				"WHERE owner_id = ?");
			setStatementString(statement, 1, authName);
			setStatementString(statement, 2, pseudo);
			statement.setInt(3, owner.getRoles());
			statement.setString(4, owner.getId());
			
			int update = statement.executeUpdate();
			if(update != 1){
				cancelTransaction(statement);
				throw new SQLException("Cannot insert owner in database");
			}
			
			//Add parameters
			updateOwnerParameters(owner);
			
			//End
			endTransaction();
		}catch(SQLException sql){
			cancelTransaction();
			throw sql;
		}
	}
	
	public void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws SQLException {
		String ownerid = owner.getId();
		UwsJobOwnerParameters parameters = owner.getParameters();
		Object o = parameters.getParameter(parameterName);
		if(!updateOwnerParameter(ownerid, parameterName, o)){
			throw new SQLException("Unable to update parameter '"+parameterName+"' (value: '"+o+"') to owner '"+ownerid+"'");
		}
	}
	
	public void updateOwnerRoles(UwsJobOwner owner) throws SQLException {
		String ownerid = owner.getId();
		PreparedStatement statement = dbConn.prepareStatement("UPDATE uws2_schema.owners roles = ? WHERE owner_id = ?");
		statement.setInt(1, owner.getRoles());
		statement.setString(2, owner.getId());
		int update = statement.executeUpdate();
		if(update != 1){
			throw new SQLException("Cannot update owner '"+ownerid+"' roles");
		}
	}
	
	public List<UwsJobOwner> retrieveOwners(UwsJobsOwnersFilter filter, long offset, long limit) throws SQLException {
		//String query = "SELECT auth_name, pseudo, roles FROM uws2_schema.owners WHERE owner_id = ";
		String query = "SELECT owner_id FROM uws2_schema.owners";
		if(filter != null && filter.hasIdFilter()){
			query += " WHERE owner_id ILIKE '%"+filter.getIdFilter()+"%'";
		}
		query += " ORDER BY owner_id";
		if(offset > -1){
			query += " OFFSET " + offset;
		}
		if(limit > -1){
			query += " LIMIT " + limit;
		}
		List<UwsJobOwner> users = new ArrayList<UwsJobOwner>();
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			while(result.next()){
				String id = result.getString("owner_id");
				UwsJobOwner userDetails = getOwner(id, true);
				users.add(userDetails);
			}
			return users;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain user list.", e);
		}
	}


	private void addNewParameters(UwsJob job) throws SQLException{
		String jobid = job.getJobId();
		Object o;
		UwsJobParameters parameters = job.getParameters();
		if(parameters != null){
			for(String name: parameters.getParameterNames()){
				o = parameters.getParameter(name);
				if(!addNewParameter(jobid, name, PARAMETER_COMMON, o)){
					throw new SQLException("Unable to add parameter '"+o+"' to job '"+jobid+"'");
				}
			}
		}
	}
	
	private boolean addNewParameter(String jobid, String parameterName, String parameterType, Object parameterValue) throws SQLException{
		UwsParameterValueType pvt = UwsJobParameters.getParameterValueType(parameterValue);
		PreparedStatement statement = dbConn.prepareStatement(PARAMETER_INSERT);
		statement.setString(1, parameterName);
		statement.setString(2, jobid);
		statement.setString(3, parameterType);
		statement.setString(4, pvt.name());
		setStatementString(statement, 5, UwsJobParameters.getParameterStringRepresentation(pvt, parameterValue));
		return statement.executeUpdate() == 1;
	}
	
	private boolean deleteErrorSummary(String jobid) throws SQLException {
		return executeUpdate("DELETE FROM uws2_schema.error_summary_meta WHERE job_id = '"+jobid+"'") == 1;
	}

	private int deleteResults(String jobid) throws SQLException {
		return executeUpdate("DELETE FROM uws2_schema.results_meta WHERE job_id = '"+jobid+"'");
	}
	
	private int deleteParameters(String jobid) throws SQLException {
		return executeUpdate("DELETE FROM uws2_schema.job_parameters WHERE job_id = '"+jobid+"'");
	}
	
	private UwsJobOwnerParameters loadOwnerParameters(String ownerid) throws SQLException {
		String sql = "SELECT parameter_id, data_type, string_representation FROM uws2_schema.owner_parameters WHERE owner_id = '"+ownerid+"'";
		ResultSet rs = executeQuery(sql);
		Map<String,Object> params = new HashMap<String, Object>();
		while(rs.next()){
			params.put(
					rs.getString("parameter_id"), 
					UwsJobOwnerParameters.getParameterValue(rs.getString("data_type"), rs.getString("string_representation")));
		}
		if(params.size() > 0){
			return new UwsJobOwnerParameters(params);
		} else {
			return new UwsJobOwnerParameters();
		}
	}

	private void addNewOwnerParameters(UwsJobOwner owner) throws SQLException{
		String ownerid = owner.getId();
		Object o;
		UwsJobOwnerParameters parameters = owner.getParameters();
		if(parameters != null){
			for(String name: parameters.getParameterNames()){
				o = parameters.getParameter(name);
				if(!addNewOwnerParameter(ownerid, name, o)){
					throw new SQLException("Unable to add parameter '"+name+"' (value: '"+o+"') to owner '"+ownerid+"'");
				}
			}
		}
	}
	
	private boolean addNewOwnerParameter(String ownerid, String parameterName, Object parameterValue) throws SQLException{
		UwsParameterValueType pvt = UwsJobOwnerParameters.getParameterValueType(parameterValue);
		PreparedStatement statement = dbConn.prepareStatement(PARAMETER_OWNER_INSERT);
		statement.setString(1, parameterName);
		statement.setString(2, ownerid);
		statement.setString(3, pvt.name());
		setStatementString(statement, 4, UwsJobOwnerParameters.getParameterStringRepresentation(pvt, parameterValue));
		return statement.executeUpdate() == 1;
	}
	
	private void updateOwnerParameters(UwsJobOwner owner) throws SQLException {
		String ownerid = owner.getId();
		Object o;
		UwsJobOwnerParameters parameters = owner.getParameters();
		if(parameters != null){
			for(String name: parameters.getParameterNames()){
				o = parameters.getParameter(name);
				if(!updateOwnerParameter(ownerid, name, o)){
					throw new SQLException("Unable to update parameter '"+name+"' (value: '"+o+"') to owner '"+ownerid+"'");
				}
			}
		}
	}

	private boolean updateOwnerParameter(String ownerid, String parameterName, Object parameterValue) throws SQLException{
		UwsParameterValueType pvt = UwsJobOwnerParameters.getParameterValueType(parameterValue);
		PreparedStatement statement = dbConn.prepareStatement(PARAMETER_OWNER_UPDATE);
		statement.setString(1, pvt.name());
		setStatementString(statement, 2, UwsJobOwnerParameters.getParameterStringRepresentation(pvt, parameterValue));
		statement.setString(3, parameterName);
		statement.setString(4, ownerid);
		return statement.executeUpdate() == 1;
	}

	private void startTransaction() throws SQLException {
		try {
			dbConn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new SQLException("Impossible to start transaction, because: "
					+ e.getMessage(), e);
		}
	}
	
	private void cancelTransaction(PreparedStatement statement) throws SQLException {
		try {
			if (statement != null) {
				statement.cancel();
			}
		} catch (SQLException e) {

		}
		cancelTransaction();
	}

	private void cancelTransaction() throws SQLException {
		try {
			if(stmt != null){
				stmt.cancel();
			}
			dbConn.rollback();
		} catch (SQLException e) {
			throw new SQLException("Impossible to cancel transaction, because: "
					+ e.getMessage(), e);
		}finally{
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLException("Impossible to reset autocommit to true, because: "
						+ e.getMessage(), e);
			}
		}
	}

	private void endTransaction() throws SQLException {
		try {
			dbConn.commit();
		} catch (SQLException e) {
			throw new SQLException("Impossible to commit transaction, because: "
					+ e.getMessage(), e);
		}finally{
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLException("Impossible to reset autocommit to true, because: "
						+ e.getMessage(), e);
			}
		}
	}

	private ResultSet executeQuery(String sql) throws SQLException {
		try {
			stmt = dbConn.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException se) {
			se.printStackTrace();
			throw new SQLException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage(), se);
		}
	}
	
	private int executeUpdate(String sql) throws SQLException {
		try {
			stmt = dbConn.createStatement();
			return stmt.executeUpdate(sql);
		} catch (SQLException se) {
			se.printStackTrace();
			throw new SQLException("Can not execute the following SQL: \n" + sql
					+ "\n. Because: " + se.getMessage(), se);
		}
	}

	private boolean checkExists(String sql) throws SQLException{
		ResultSet rs = executeQuery(sql);
		return rs.next();
	}
	
	private void setStatementString(PreparedStatement st, int index, String value) throws SQLException {
		if (value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			st.setString(index, value);
		}
	}
	
	private void setStatementLongFromDate(PreparedStatement st, int index, Date date) throws SQLException {
		if(date == null) {
			st.setLong(index, 0);
		} else {
			st.setLong(index, date.getTime());
		}
	}
	
	private Date getDateFromStorage(long date){
		if(date == 0){
			return null;
		}else{
			return new Date(date);
		}
	}
	
	private String createIn(Set<String> items){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(String item: items){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(',');
			}
			sb.append('\'').append(item).append('\'');
		}
		return sb.toString();
	}
	
	private int executeSingleValueInteger(String sql) throws SQLException {
		ResultSet rs = executeQuery(sql);
		if(rs.next()){
			return rs.getInt(1);
		} else {
			return 0;
		}
	}
	
	private String getJobsFilter(UwsJobsFilter filter){
		if(filter == null){
			return null;
		}
		UwsJobDetailsComparison details = filter.getJobFilter();
		StringBuilder sb = new StringBuilder();
		if(filter.hasFilterByJobId()){
			sb.append(" AND ");
			sb.append(getStringFilter("j.job_id", details.getJobid(), filter.isJobidComparisonLike()));
		}
		if(filter.hasFilterByOwnerId()){
			sb.append(" AND ");
			sb.append(getStringFilter("j.owner_id", details.getOwnerid(), filter.isOwneridComparisonLike()));
		}
		if(filter.hasFilterByPhaseid()){
			sb.append(" AND ");
			sb.append(getStringFilter("j.phase_id", details.getPhaseid(), filter.isPhaseidComparisonLike()));
		}
		if(filter.hasFilterByQuery()){
			sb.append(" AND ");
			sb.append(getStringFilter("p.string_representation", details.getQuery(), filter.isQueryComparisonLike()));
		}
		if(filter.hasFilterByStartTime() || filter.hasFilterByStartTimeLimit()){
			sb.append(" AND ");
			sb.append(getTimeFilter("j.start_time", details.getStartTime(), details.getStartTimeLimit(), filter.hasFilterByStartTime(), filter.hasFilterByStartTimeLimit()));
		}
		if(filter.hasFilterByEndTime() || filter.hasFilterByEndTimeLimit()){
			sb.append(" AND ");
			sb.append(getTimeFilter("j.end_time", details.getEndTime(), details.getEndTimeLimit(), filter.hasFilterByEndTime(), filter.hasFilterByEndTimeLimit()));
		}
		return sb.toString();
	}

	private String getStringFilter(String key, String filter, boolean like) {
		if (like) {
			return "(" + key + " ILIKE '%" + filter + "%')";
		} else {
			return "(" + key + " = '" + filter + "')";
		}
	}

	private String getTimeFilter(String key, long init, long end,
			boolean hasInit, boolean hasEnd) {
		if (hasInit && hasEnd) {
			return "(" + key + " >= " + init + " AND " + key + " <= " + end + ")";
		} else if (hasInit) {
			return "(" + key + " >= " + init + ")";
		} else if (hasEnd) {
			return "(" + key + " <= " + end + ")";
		} else {
			return "";
		}
	}
	
	public void createNotification(UwsNotificationItem uwsNotificationItem) throws SQLException {
		startTransaction();
		try{
			PreparedStatement statement = dbConn.prepareStatement(NOTIFICATION_INSERT);
			statement.setString(1, uwsNotificationItem.getId());
			statement.setInt(2, uwsNotificationItem.getType());
			statement.setInt(3, uwsNotificationItem.getSubtype());
			statement.setString(4, uwsNotificationItem.getMsg());
			statement.setLong(5, uwsNotificationItem.getCreationTime());
			boolean result = statement.executeUpdate() == 1;
			if(!result){
				cancelTransaction();
				throw new SQLException("Cannot insert notification in database.");
			}
			//insert relations between users and notification
			createRelationsBetweenUsersAndNotification(uwsNotificationItem);
			endTransaction();
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}
	
	private void createRelationsBetweenUsersAndNotification(UwsNotificationItem uwsNotificationItem) throws SQLException{
		Set<String> users = uwsNotificationItem.getUsers();
		if(users == null){
			return;
		}
		boolean result;
		for(String userid: users){
			PreparedStatement statement = dbConn.prepareStatement(NOTIFICATION_RELATION_INSERT);
			statement.setString(1, uwsNotificationItem.getId());
			statement.setString(2, userid);
			result = statement.executeUpdate() == 1;
			if(!result){
				cancelTransaction();
				throw new SQLException("Cannot insert notification relation in database.");
			}
		}
	}
	
	public List<UwsNotificationItem> getNotificationsForUser(String userid) throws SQLException {
		String query = "SELECT n.notification_id, n.type, n.subtype, n.description, n.creation_time FROM "
				+ "notifications_schema.notifications n, notifications_schema.notifications_users u WHERE "
				+ "n.notification_id = u.notification_id AND u.user_id = '"+userid+"' ORDER BY n.creation_time";
		List<UwsNotificationItem> notifications = new ArrayList<UwsNotificationItem>();
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			while(result.next()){
				String id = result.getString("notification_id");
				int type = result.getInt("type");
				int subtype = result.getInt("subtype");
				String msg = result.getString("description");
				long creationTime = result.getLong("creation_time");
				UwsNotificationItem item = new UwsNotificationItem(id, type, subtype, msg);
				item.setCreationTime(creationTime);
				notifications.add(item);
			}
			return notifications;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain user list.", e);
		}
	}
	
	public void deleteNotificationRelation(String userid, List<String> notificationIds) throws SQLException {
		if(notificationIds == null || notificationIds.size() < 1){
			return;
		}
		StringBuilder sb = new StringBuilder("DELETE FROM notifications_schema.notifications_users WHERE notification_id IN (");
		boolean firstTime = true;
		for(String id: notificationIds){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(',');
			}
			sb.append('\'').append(id).append('\'');
		}
		sb.append(") AND user_id = '").append(userid).append('\'');
		executeUpdate(sb.toString());
	}
	
	public int deleteNotifications(long currentTime, long deltaDestructionTime) throws SQLException {
		long time = currentTime - deltaDestructionTime;
		String query = "SELECT notification_id FROM notifications_schema.notifications WHERE creation_time < " + time;
		ResultSet result = dbConn.createStatement().executeQuery(query);
		StringBuilder notificationsToRemove = new StringBuilder();
		boolean firstTime = true;
		int removedNotifications = 0;
		while(result.next()){
			if(firstTime){
				firstTime = false;
			}else{
				notificationsToRemove.append(',');
			}
			notificationsToRemove.append('\'').append(result.getString(1)).append('\'');
			removedNotifications++;
		}
		startTransaction();
		try{
			query = "DELETE FROM notifications_schema.notifications_users WHERE notification_id IN (" + notificationsToRemove.toString() + ")";
			executeUpdate(query);
			query = "DELETE FROM notifications_schema.notifications WHERE notification_id IN (" + notificationsToRemove.toString() + ")";
			executeUpdate(query);
			endTransaction();
			return removedNotifications;
		}catch(SQLException e){
			cancelTransaction();
			throw e;
		}
	}

}
