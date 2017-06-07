package esavo.uws.actions.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

public class UwsHandlersUtils {
	
	public static final String PARAMETER_SESSION = "session";
	public static final String PARAMETER_LIMIT = "limit";
	public static final String PARAMETER_OFFSET = "offset";
	public static final String PARAMETER_ORDER = "order";
	public static final String PARAMETER_PHASE = "phase";
	public static final String PARAMETER_METADATA_ONLY = "metadata_only";
	public static final String PARAMETER_ROLE = "role";
	
	public static final String PARAMETER_GROUP_ID = "group_id";
	public static final String PARAMETER_GROUPS_LIST = "groups_list";
	public static final String PARAMETER_RESOURCE_ID = "resource_id";
	public static final String PARAMETER_RESOURCE_TYPE = "resource_type";
	public static final String PARAMETER_SHARE_TYPE = "share_type";
	public static final String PARAMETER_SHARE_TO_ID = "share_to_id";
	public static final String PARAMETER_SHARE_MODE = "share_mode";
	public static final String PARAMETER_TITLE = "title";
	public static final String PARAMETER_DESCRIPTION = "description";
	public static final String PARAMETER_USERS_LIST = "users_list";
	public static final String PARAMETER_ITEMS_LIST = "items_list";
	public static final String PARAMETER_INCLUDE_USERS = "include_users";

	
	public static final String PARAMETER_EXEC_DURATION = "EXECUTIONDURATION";
	public static final String PARAMETER_DESTRUCTION = "DESTRUCTION";
	public static final String PARAMETER_JOB_IDS = "JOB_IDS";
	
	public static final String PARAMETER_USER = "user";
	public static final String PARAMETER_JOB_ID = "jobid";
	public static final String PARAMETER_USER_ID = "userid";
	public static final String PARAMETER_OWNER_ID = "ownerid";
	public static final String PARAMETER_JOB_NAME = "jobname";
	public static final String PARAMETER_QUERY = "query";
	public static final String PARAMETER_PHASE_ID = "phaseid";
	public static final String PARAMETER_START_TIME_INIT = "start_time_init";
	public static final String PARAMETER_END_TIME_INIT = "end_time_init";
	public static final String PARAMETER_START_TIME_LIMIT = "start_time_limit";
	public static final String PARAMETER_END_TIME_LIMIT = "end_time_limit";

	public static final String PARAMETER_ROLES = "role";
	public static final String PARAMETER_QUOTA_DB = "quota_db";
	public static final String PARAMETER_QUOTA_FILES = "quota_files";
	public static final String PARAMETER_ASYNC_MAX_EXEC_TIME = "async_max_exec_time";
	public static final String PARAMETER_SYNC_MAX_EXEC_TIME = "sync_max_exec_time";
	public static final String PARAMETER_LIST_NAME = "list_name";
	
	public static final String PARAMETER_COMMAND = "command";
	public static final String PARAMETER_TIMESTAMP = "timestamp";
	public static final String PARAMETER_EVENT = "event";
	public static final String PARAMETER_OWNER = "owner";
	
	public static final String PARAMETER_CLOSE = "close";
	public static final String PARAMETER_BLOCK = "block";

	public static final String PARAMETER_FILTER_BY_SESSION = "filter_by_session";
	public static final String PARAMETER_SET_SCHEDULER_MODE = "set_mode";

	public static final String PARAMETER_ACTION = "action";
//	public static final String PARAM_USER_ID = "USERID";
//	public static final String PARAM_JOB_ID = "JOBID";
//	public static final String PARAM_OWNER_ID = "OWNERID";
//	public static final String PARAM_JOB_NAME = "JOBNAME";
//	public static final String PARAM_QUERY = "QUERY";
//	public static final String PARAM_PHASE_ID = "PHASEID";
//	public static final String PARAM_START_TIME_INIT = "START_TIME_INIT";
//	public static final String PARAM_END_TIME_INIT = "END_TIME_INIT";
//	public static final String PARAM_START_TIME_LIMIT = "START_TIME_LIMIT";
//	public static final String PARAM_END_TIME_LIMIT = "END_TIME_LIMIT";
//	public static final String PARAM_QUERY_RESULTS_OFFSET = "RESULTS_OFFSET";
//	public static final String PARAM_QUERY_RESULTS_LIMIT = "RESULTS_LIMIT";
//	public static final String PARAM_ROLES = "ROLES";
//	public static final String PARAM_QUOTA_DB = "QUOTA_DB";
//	public static final String PARAM_QUOTA_FILES = "QUOTA_FILES";
//	public static final String PARAM_LIST_NAME = "list_name";
	

	
	/**
	 * Returns the specified parameter or raises an exception if the parameter does not exist.
	 * @param actionRequest
	 * @param parameterName
	 * @return
	 * @throws UwsException
	 */
	public static String getExistingParam(UwsActionRequest actionRequest, String parameterName) throws UwsException {
		if(!actionRequest.hasHttpParameter(parameterName)){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Not found required parameter '"+parameterName+"'");
		}
		String value = actionRequest.getHttpParameter(parameterName);
		if(value == null || "".equals(value)){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Required parameter '"+parameterName+"' cannot be null.");
		}
		return value;
	}
	
	
	public static List<String> getList(String param){
		if(param == null){
			return null;
		}
		if("".equals(param.trim())){
			return null;
		}
		List<String> l = new ArrayList<String>();
		String[] items = param.split(",");
		if(items == null){
			return l;
		}
		String tmp;
		for(String s: items){
			tmp = s.trim();
			if(!validListItem(tmp)){
				throw new IllegalArgumentException("Invalid item: '"+tmp+"' for list");
			}
			l.add(s.trim());
		}
		return l;
	}
	
	private static boolean validListItem(String s){
		if(s == null){
			return true;
		}
		if(s.indexOf(';') >= 0){
			//to avoid SQL injection
			return false;
		}
		return true;
	}

	public static UwsJobsFilter createFilter(UwsActionRequest actionRequest, String ownerIdFilter, String sessionId){
		UwsJobsFilter filter = new UwsJobsFilter();
		boolean filterFound = false;
		String value;
		
		value = actionRequest.getHttpParameter(PARAMETER_JOB_ID);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByJobId(value, true);
		}
		value = actionRequest.getHttpParameter(PARAMETER_JOB_NAME);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByJobName(value, true);
		}
		//If the filter is already set (i.e. the user is not admin), use it
		if(ownerIdFilter != null){
			filterFound = true;
			filter.setFilterByOwnerId(ownerIdFilter, true);
		}else{
			value = actionRequest.getHttpParameter(PARAMETER_OWNER_ID);
			if(value != null && !"".equals(value)){
				filterFound = true;
				filter.setFilterByOwnerId(value, true);
			}
		}
		//value = actionRequest.getHttpParameter(PARAMETER_SESSION);
		if(sessionId != null && !"".equals(sessionId)){
			filterFound = true;
			filter.setFilterBySessionId(sessionId, false);
		}
		value = actionRequest.getHttpParameter(PARAMETER_PHASE_ID);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByPhaseId(value, false);
		}
		value = actionRequest.getHttpParameter(PARAMETER_QUERY);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByQuery(value, true);
		}
		value = actionRequest.getHttpParameter(PARAMETER_START_TIME_INIT);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByStartTime(Long.parseLong(value));
		}
		value = actionRequest.getHttpParameter(PARAMETER_END_TIME_INIT);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByEndTime(Long.parseLong(value));
		}
		value = actionRequest.getHttpParameter(PARAMETER_START_TIME_LIMIT);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByStartTimeLimit(Long.parseLong(value));
		}
		value = actionRequest.getHttpParameter(PARAMETER_END_TIME_LIMIT);
		if(value != null && !"".equals(value)){
			filterFound = true;
			filter.setFilterByEndTimeLimit(Long.parseLong(value));
		}

		if(filterFound){
			return filter;
		}else{
			return null;
		}
	}

	public static boolean checkAdminUser(UwsJobOwner currentUser, UwsOutputResponseHandler outputHandler, HttpServletResponse response) throws UwsException{
		if(currentUser == null){
			outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "AdminAccess", null, "User 'null' does not have enough privileges.");
			return false;
		}
		if(!currentUser.isAdmin()){
			outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "AdminAccess", null, "User '"+currentUser.getId()+"' does not have enough privileges.");
			return false;
		}
		return true;
	}
	
	public static boolean checkValidUser(UwsJob job, UwsJobOwner currentUser, UwsOutputResponseHandler outputHandler, HttpServletResponse response) throws UwsException{
		if(currentUser == null){
			outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "AdminAccess", null, "User 'null' does not have enough privileges.");
			return false;
		}
		if(currentUser.isAdmin()){
			return true;
		}
		//check job belongs user
		if(!job.getOwner().getId().equals(currentUser.getId())){
			outputHandler.writeServerErrorResponse(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "AdminAccess", null, "User '"+currentUser.getId()+"' is not autorized to load '"+job.getJobId()+"' details.");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a long value from a parameter.
	 * @param parameterValue parameter value.
	 * @param defaultValue default value.
	 * @return a long value from a parameter.
	 */
	public static long getLongFromParameter(String parameterValue, long defaultValue){
		if(parameterValue == null){
			return defaultValue;
		}else{
			try{
				return Long.parseLong(parameterValue);
			}catch(NumberFormatException nfe){
				return defaultValue;
			}
		}
	}


}
