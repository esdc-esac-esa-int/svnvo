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
package esavo.uws.actions.handlers.admin;

import java.text.MessageFormat;

import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.utils.UwsParameterValueType;

/**
 * Templates to create HTML and JSON documents.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Templates {
	
	public static final String APPLICATION_TITLE = "Admin";
	public static final String APPLICATION_ERROR_TITLE = "SERVICE ERROR";
	
	//public static final String MANAGER_SERVLET = "Admin";
	//public static final String LOGIN_SERVLET = "login";
	
	//public static final String CSS = "./tap_manager.css";
	
//	private static final String LINK_CSS = "<link rel=\"stylesheet\" type=\"text/css\" href=\""+CSS+"\">";
	
//	/**
//	 * <ul>
//	 * <li>0: header title</li>
//	 * <li>1: extra head</li>
//	 * <li>2: body</li>
//	 * </ul>
//	 */
//	public static final String BASE = 
//			"<html>\n"+
//			"<head><title>{0}</title>\n"+LINK_CSS+"\n{1}</head>\n"+
//			"<body>\n{2}</body>"+
//			"</html>";
//
//	/**
//	 * <ul>
//	 * <li>0: title</li>
//	 * <li>1: body</li>
//	 * </ul>
//	 */
//	public static final String ERROR_BODY = 
//			"<table class=\"error-table\">\n"+
//			"<tr>\n<td class=\"error-table-header\">{0}</td></tr>\n"+
//			"<tr>\n<td class=\"error-table-body\">{1}</td></tr>\n"+
//			"</table>";
	
	
	/**
	 * <ul>
	 * <li>0: msg</li>
	 * </ul>
	 */
	public static final String JSON_SIMPLE_MSG = "'{' {0} '}'";
	
	
	/**
	 * <ul>
	 * <li>0: all elements</li>
	 * </ul>
	 */
	public static final String JSON_LIST = "[\n{0}]";
	
	/**
	 * JSON format: <code>{"id": "userIdentifier", "roles": "userRoles", "quota_db": "userQuotaDB", "current_size_db": "userCurrentSizeDB", "quota_files": "userQuotaFiles", "current_size_files": "userCurrentSizeFiles", "async_max_exec_time": "asyncMaxExecTime"}</code>
	 * <ul>
	 * <li>0: user id (string)</li>
	 * <li>1: roles (int)</li>
	 * <li>2: quota db (long)</li>
	 * <li>3: current size db (long)</li>
	 * <li>4: quota files (long)</li>
	 * <li>5: current size files (long)</li>
	 * <li>6: current async max exec time (long)</li>
	 * </ul>
	 */
	public static final String JSON_USER_LIST_RECORD = 
			"'{'\"id\": \"{0}\", \"roles\": \"{1}\", \"quota_db\": \"{2}\", \"curent_size_db\": \"{3}\", " +
			"\"quota_files\": \"{4}\", \"current_size_files\": \"{5}\", \"async_max_exec_time\": \"{6}\", \"sync_max_exec_time\": \"{7}\"'}'";
	
	
	/**
	 * JSON format: <code>{"job_id": "jobIdentifier", "jobOwner": "owner", "jobPhase": "phase", "start_time": "jobStartTime", "end_time": "jobEndTime", "query": "jobQuery", "relative_path": "jobRelativePath"}</code>
	 * <ul>
	 * <li>0: job id (string)</li>
	 * <li>1: owner id (sting)</li>
	 * <li>2: phase id (string)</li>
	 * <li>3: start time (long)</li>
	 * <li>4: end time (long)</li>
	 * <li>5: query (string)</li>
	 * <li>6: relative path (string)</li>
	 * </ul>
	 */
	public static final String JSON_JOB_LIST_RECORD = 
			"'{'\"job_id\": \"{0}\", \"owner_id\": \"{1}\", \"phase_id\": \"{2}\", \"start_time\": \"{3}\", \"end_time\": \"{4}\" , \"query\": \"{5}\", \"relative_path\": \"{6}\"'}'";
	

	/**
	 * JSON format: <code>{"parameter_id": "parameterIdentifier", "parameter_type": "parameterType", "data_type": "parameterDataType", "string_representation": "parameterStringRepresentation"}</code>
	 * <ul>
	 * <li>0: parameter id</li>
	 * <li>1: parameter type</li>
	 * <li>2: data type</li>
	 * <li>3: string representation</li>
	 * </ul>
	 */
	public static final String JSON_JOB_PARAMETER_RECORD =
			"'{'\"parameter_id\": \"{0}\", \"parameter_type\": \"{1}\", \"data_type\": \"{2}\", \"string_representation\": \"{3}\"'}'";
	
	/**
	 * JSON format: <code>{"message": "errorSummaryMessage", "type": "errorSummaryType", "details": "errorSummaryDetails"}</code>
	 * <ul>
	 * <li>0: message</li>
	 * <li>1: type</li>
	 * <li>2: details</li>
	 * </ul>
	 */
	public static final String JSON_JOB_ERROR_SUMMARY =
			"'{'\"message\": \"{0}\", \"type\": \"{1}\", \"details\": \"{2}\"'}'";


	/**
	 * JSON format: <code>{"id": "resultIdentifier", "type": "resultType", "mime_type": "resultMimeType", "href": "resultHref", "rows": "resultsNumberOfRows", "size": "resultsSize"}</code>
	 * <ul>
	 * <li>0: result id</li>
	 * <li>1: type</li>
	 * <li>2: mime_type</li>
	 * <li>3: href</li>
	 * <li>4: rows</li>
	 * <li>5: size</li>
	 * </ul>
	 */
	public static final String JSON_JOB_RESULTS_RECORD =
			"'{'\"id\": \"{0}\", \"type\": \"{1}\", \"mime_type\": \"{2}\", \"href\": \"{3}\", \"rows\": \"{4}\", \"size\": \"{5}\" '}'";
	
	

//	/**
//	 * Returns a basic HTML page (see {@link #getHtml(String, String)}})
//	 * @param headerTitle header title
//	 * @param body HTML body.
//	 * @return a basic HTML page.
//	 */
//	public static String getHtml(String headerTitle, String body){
//		return getHtml(headerTitle, null, body);
//	}
	
//	/**
//	 * Returns a basic HTML page (base on the format specified by {@link #BASE})
//	 * @param extraHeader header to be added to the default header.
//	 * @param headerTitle header title
//	 * @param body HTML body.
//	 * @return a basic HTML page.
//	 */
//	public static String getHtml(String headerTitle, String extraHeader, String body){
//		if(extraHeader == null){
//			extraHeader = "";
//		}
//		if(body == null){
//			body = "";
//		}
//		if(headerTitle == null){
//			headerTitle = "";
//		}
//		return MessageFormat.format(BASE, headerTitle, extraHeader, body);
//	}
	
	
	/**
	 * Returns the specified 'text' between '&lt;code&gt' tags. Any ampersand symbol (<code>'&'</code>) is transformed into ampersand entity <code>'&amp;amp;'</code>
	 * @param text text to codify
	 * @return a plain ('code' tags) text
	 */
	public static String getPlainText(String text){
		if(text == null){
			return "";
		}
		String escaped = text.replaceAll("&", "&amp;");
		return "<code>"+escaped+"</code>";
	}
	
	/**
	 * Returns a basic JSON string based on the format specified by {@link #JSON_SIMPLE_MSG}}
	 * Warning: msg must not contain '&' nor CR/LF characters.
	 * @param id json object keyword.
	 * @param msg json object value.
	 * @return a basic JSON string.
	 */
	public static String getSimpleJsonMsg(String id, String msg){
		String jsonMsg = "\""+id+"\": \"" + msg + "\"";
		return MessageFormat.format(JSON_SIMPLE_MSG, jsonMsg);
	}
	
	/**
	 * Returns the 'data' argument escaped to be used in a JSON object.<br/>
	 * Every double quote (<code>"</code>) is transformed into a single quote (<code>'</code>)<br/>
	 * Every carriage return (<code>\r</code>) is transformed into empty string (<code>""</code>)<br/>
	 * Every new line (<code>\n</code>) is transformed into 'BR' tag (<code>&lt;br/&gt;</code>)<br/>
	 * @param data
	 * @return
	 */
	public static String escapeToJson(String data){
		if(data == null){
			return "";
		}
		data = data.replaceAll("\"", "'");
		data = data.replaceAll("\r", "");
		data = data.replaceAll("\n", "<br/>");
		return data;
	}
	
	/**
	 * Returns a JSON string based on the format specified by {@link #JSON_USER_LIST_RECORD}}
	 * @param user user details (can be null)
	 * @return a JSON string or an empty string if user is null.
	 */
	public static String getJsonUserDetails(UwsJobOwner user){
		if(user == null){
			return "\"\"";
		} else {
			UwsJobOwnerParameters parameters = user.getParameters();
			if(parameters == null){
//				return MessageFormat.format(JSON_USER_LIST_RECORD, 
//						user.getId(), ""+user.getRoles(), 
//						""+user.getQuotaDb(), ""+user.getCurrentSizeDb(), 
//						""+user.getQuotaFile(), ""+user.getCurrentSizeFile());
				return MessageFormat.format(JSON_USER_LIST_RECORD, 
					user.getId(), ""+user.getRoles(), "-1", "-1", "-1", "-1");
			}else{
			return MessageFormat.format(JSON_USER_LIST_RECORD, 
				user.getId(), ""+user.getRoles(), 
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA), 
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE), 
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA), 
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE),
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME),
				""+parameters.getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME));
			}
		}
	}
	
//	/**
//	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_LIST_RECORD}}
//	 * @param user user details (can be null)
//	 * @return a JSON string or an empty string if user is null.
//	 */
//	public static String getJsonJobListDetails(JobDetails job){
//		if(job == null){
//			return "";
//		}
//		return MessageFormat.format(JSON_JOB_LIST_RECORD,
//				job.getJobid(), job.getOwnerid(), job.getPhaseid(), 
//				""+job.getStartTime(), ""+job.getEndTime(), 
//				escapeToJson(job.getQuery()), job.getRelativePath());
//	}
	
//	/**
//	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_LIST_RECORD}
//	 * @param job job.
//	 * @return a JSON string or an empty string if job is null.
//	 */
//	public static String getJsonJobDetails(UwsJobDetails job){
//		if(job == null){
//			return "\"\"";
//		} else {
//			return MessageFormat.format(JSON_JOB_LIST_RECORD,
//				job.getJobid(), job.getOwnerid(), job.getPhaseid(), 
//				""+job.getStartTime(), ""+job.getEndTime(), 
//				escapeToJson(job.getQuery()), job.getRelativePath());
//		}
//	}

//	/**
//	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_LIST_RECORD}
//	 * @param job job.
//	 * @return a JSON string or an empty string if job is null.
//	 */
//	public static String getJsonJobDetails(UwsJob job){
//		if(job == null){
//			return "\"\"";
//		} else {
//			UwsJobParameters param = job.getParameters();
//			String query = param.getStringParameter("query");
//			//end date can be null
//			Date d = job.getEndTime();
//			String endDate = null;
//			if(d != null){
//				endDate = ""+d.getTime();
//			}
//			return MessageFormat.format(JSON_JOB_LIST_RECORD,
//				job.getJobId(), job.getOwner().getId(), job.getPhase().name(), 
//				""+job.getStartTime().getTime(), endDate, 
//				escapeToJson(query), job.getLocationId());
//		}
//	}

	/**
	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_ERROR_SUMMARY}.
	 * @param errorDetails error summary.
	 * @return a JSON string. If errorDetails is null, the JSON object contains empty strings.
	 */
	public static String getJsonJobErrorSummary(UwsJobErrorSummaryMeta errorDetails){
		if(errorDetails != null){
			return MessageFormat.format(JSON_JOB_ERROR_SUMMARY, 
					escapeToJson(errorDetails.getMessage()),
					errorDetails.getType().name(),
					escapeToJson(errorDetails.getDetailsMimeType()));
		}else{
			return MessageFormat.format(JSON_JOB_ERROR_SUMMARY, 
					"",
					"",
					"");
		}
	}
	
	/**
	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_PARAMETER_RECORD}}
	 * @param p job parameters
	 * @return a JSON string or an empty string if p is null.
	 */
	public static String getJsonJobParameters(UwsJobParameters p, String paramName){
		if(p == null){
			return "\"\"";
		} else {
			Object o = p.getParameter(paramName);
			UwsParameterValueType pvt = UwsJobParameters.getParameterValueType(o);
			String stringRep = UwsJobParameters.getParameterStringRepresentation(pvt, o);
			return MessageFormat.format(JSON_JOB_PARAMETER_RECORD,
				paramName, "COMMON", pvt.name(), 
				escapeToJson(stringRep));
		}
	}
	
	/**
	 * Returns a JSON string based on the format specified by {@link #JSON_JOB_RESULTS_RECORD}
	 * @param r job results.
	 * @return a JSON string or an empty string if r is null.
	 */
	public static String getJsonJobResult(UwsJobResultMeta r){
		if(r == null){
			return "\"\"";
		} else {
			return MessageFormat.format(Templates.JSON_JOB_RESULTS_RECORD, 
				r.getId(), r.getType(), r.getMimeType(), r.getXlinkHrefType(), ""+r.getRows(), ""+r.getSize());
		}
	}
	
}
