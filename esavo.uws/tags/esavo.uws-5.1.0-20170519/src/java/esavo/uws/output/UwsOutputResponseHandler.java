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
package esavo.uws.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.actions.handlers.admin.Templates;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareUser;

public interface UwsOutputResponseHandler {
	
	public static final String HEADER_LOCATION = "Location";
	
	public static final String CONTENT_TYPE_HTML = "text/html";
	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MIME_TYPE_VOTABLE = "application/x-votable+xml";
	public static final String MIME_TYPE_XML = "text/xml"; //"human readable" RFC 3023 (http://www.rfc-editor.org/rfc/rfc3023.txt) section 3
	public static final String MIME_TYPE_APP_XML = "application/xml";
	public static final String MIME_TYPE_JSON = "application/json";
	
	public static final String RESULTS_URI_NAME = "results";
	public static final String ERROR_URI_NAME = "error";
	public static final String ERROR_URI_DETAILS= "details";

	// SUCCESS codes:
	
	/**
	 * HTTP error code 200
	 */
	public final static int OK = 200;
	
	/**
	 * HTTP error code 204
	 */
	public final static int NO_CONTENT = 204;

	// REDIRECTION codes:
	
	/**
	 * HTTP error code 303
	 */
	public final static int SEE_OTHER = 303;

	// CLIENT ERROR codes:
	
	/**
	 * HTTP error code 400
	 */
	public final static int BAD_REQUEST = 400;
	
	/**
	 * HTTP error code 403
	 */
	public final static int FORBIDDEN = 403;
	
	/**
	 * HTTP error code 404
	 */
	public final static int NOT_FOUND = 404;
	
	/**
	 * HTTP error code 405
	 */
	public final static int NOT_ALLOWED = 405;
	
	/**
	 * HTTP error code 507
	 */
	public final static int QUOTA_EXCEEDED = 507;
	
	/**
	 * HTTP error code 413
	 */
	public final static int REQUEST_ENTITY_TOO_LARGE = 413;
	
	/**
	 * HTTP error code 401
	 */
	public final static int UNAUTHORIZED = 401;

	// SERVER ERROR codes:

	/**
	 * HTTP error code 500
	 */
	public final static int INTERNAL_SERVER_ERROR = 500;
	
	/**
	 * HTTP error code 501
	 */
	public final static int NOT_IMPLEMENTED = 501;
	
	/**
	 * HTTP error code 503
	 */
	public final static int SERVICE_UNAVAILABLE = 503;
	
	/**
	 * HTTP error code 530
	 */
	public final static int USER_ACCESS_DENIED = 530;
	
	/**
	 * HTTP error code 550
	 */
	public final static int PERMISSION_DENIED = 550;

	/**
	 * Writes a job list.
	 * @param jobs
	 * @param listName
	 * @param writer
	 * @throws UwsException
	 */
	
	/**
	 * Writes a job list
	 * @param response
	 * @param baseUrl
	 * @param jobs
	 * @throws UwsException
	 */
	public void writeJobListResponse(HttpServletResponse response, 
			String baseUrl,
			String listName,
			List<UwsJob> jobs) throws UwsException;
	
	/**
	 * Writes a job list
	 * @param response
	 * @param baseUrl
	 * @param jobs
	 * @param sort
	 * @param limit
	 * @param offset
	 * @param totalNumberOfResults
	 * @throws UwsException
	 */
	public void writeJobListResponse(HttpServletResponse response, 
			String baseUrl,
			String listName,
			List<UwsJob> jobs,  
			Integer limit, 
			Integer offset, 
			Integer totalNumberOfResults,
			String order) throws UwsException;

	/**
	 * Writes a job list
	 * @param response
	 * @param baseUrl
	 * @param jobs
	 * @throws UwsException
	 */
	public void writeJobListResponseExtended(HttpServletResponse response, 
			String baseUrl,
			String listName,
			List<UwsJob> jobs) throws UwsException;
	
	/**
	 * Writes a job list
	 * @param response
	 * @param baseUrl
	 * @param jobs
	 * @param sort
	 * @param limit
	 * @param offset
	 * @param totalNumberOfResults
	 * @throws UwsException
	 */
	public void writeJobListResponseExtended(HttpServletResponse response, 
			String baseUrl,
			String listName,
			List<UwsJob> jobs,  
			Integer limit, 
			Integer offset, 
			Integer totalNumberOfResults,
			String order) throws UwsException;
	

	/**
	 * Writes a job details.
	 * @param job
	 */
	public void writeJobResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException;
	
	/**
	 * Writes a job parameters.
	 * @param parameters
	 */
	public void writeJobParametersResponse(HttpServletResponse response, UwsJobParameters parameters) throws UwsException;

	/**
	 * Writes a job error summary.<br/>
	 * This method does not dump the error details data.<br/>
	 * To dump error details data, see {@link #writeJobErrorDataResponse(HttpServletResponse, UwsJobErrorSummaryMeta)}
	 * @param response
	 * @param errorSummary
	 * @throws UwsException
	 */
	public void writeJobErrorResponse(HttpServletResponse response, UwsJobErrorSummaryMeta errorSummary) throws UwsException;
	
	/**
	 * Writes (dumps) the job error summary details data.<br/>
	 * To write the summary only, see {@link #writeJobErrorResponse(HttpServletResponse, UwsJobErrorSummaryMeta)}.
	 * @param response
	 * @param jobid
	 * @param errorSummary
	 * @param source
	 * @throws UwsException
	 */
	public void writeJobErrorDataResponse(HttpServletResponse response, String jobid, UwsJobErrorSummaryMeta errorSummary, InputStream source) throws UwsException;
	
	/**
	 * Writes a job results list.<br/>
	 * For each result, a result summary is written.
	 * @param response
	 * @param baseUrl
	 * @param job
	 * @throws UwsException
	 */
	public void writeJobResultListResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException;
	
	
	/**
	 * Writes (dumps) a job result data.<br/>
	 * @param response
	 * @param result
	 * @param source
	 * @throws UwsException
	 */
	public void writeJobResultDataResponse(HttpServletResponse response, UwsJob job, UwsJobResultMeta result, String outputFormat, InputStream source) throws UwsException;
	
	/**
	 * Creates a redirect response.
	 * @param response
	 * @param urlLocation
	 * @param extraMsg
	 */
	public void redirectResponse(HttpServletResponse response, String urlLocation, String extraMsg) throws UwsException;
	
	/**
	 * Writes a text/plain response.
	 * @param response
	 * @param data
	 */
	public void writeTextPlainResponse(HttpServletResponse response, String data) throws UwsException;

	/**
	 * Writes a text/plain response.
	 * @param response
	 * @param status
	 * @param data
	 */
	public void writeTextPlainResponse(HttpServletResponse response, int status, String data) throws UwsException;

	/**
	 * Writes a json response with the following format: 
	 * <pre><tt>
	 * {"id": "msg"}
	 * </tt></pre>
	 * @param response
	 * @param id
	 * @param msg
	 * @throws UwsException
	 */
	public void writeSimpleJsonResponse(HttpServletResponse response, String id, String msg) throws UwsException;
	
	/**
	 * Writes an error message (HTML).
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param action action.
	 * @param context action context.
	 * @param message error message.
	 * @throws UwsException
	 */
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, String context, String message) throws UwsException;

	/**
	 * Writes an error (HTML) from an exception.
	 * @param response communication handler.
	 * @param action action.
	 * @param uwsException exception (can be null). If not null, the error code from the exception is used.
	 * If the error code is lower than 0, {@link #INTERNAL_SERVER_ERROR} is used.
	 * @throws UwsException
	 */
	public void writeServerErrorResponse(HttpServletResponse response, String action, UwsException uwsException) throws UwsException;

	/**
	 * Writes an error (HTML) from an exception.
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param action action.
	 * @param t exception (can be null).
	 * @param outputFormat output format.
	 * @throws UwsException
	 */
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, Throwable t, UwsExceptionOutputFormat outputFormat) throws UwsException;

	/**
	 * Writes an error (HTML) from an exception.
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param action action.
	 * @param context action context.
	 * @param t exception (can be null).
	 * @param outputFormat output format.
	 * @throws UwsException
	 */
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, String context, Throwable t, UwsExceptionOutputFormat outputFormat) throws UwsException;

	/**
	 * Writes the specified groups list.
	 * @param response
	 * @param groups the groups list
	 * @param ownerid
	 * @throws UwsException
	 */
	public void writeSharedGroupsResponse(HttpServletResponse response, List<UwsShareGroup> groups, String ownerid) throws UwsException;

//	/**
//	 * Writes the specified items list.
//	 * @param response
//	 * @param items the items list
//	 * @param ownerid
//	 * @throws UwsException
//	 */
//	public void writeSharedItemsResponse(HttpServletResponse response, List<UwsShareItem> items, String ownerid) throws UwsException;
	
	/**
	 * Writes the specified items descriptions list.
	 * @param response
	 * @param sharedItemsBase the itmes descriptions list.
	 * @param ownerid
	 * @throws UwsException
	 */
	public void writeSharedItemsResponse(HttpServletResponse response, List<UwsShareItemBase> sharedItemsBase, String ownerid) throws UwsException;
	
	/**
	 * Writes a list of users.
	 * @param response
	 * @param users
	 * @throws UwsException
	 */
	public void writeSharedUsers(HttpServletResponse response, List<UwsShareUser> users) throws UwsException;
	
	/**
	 * Writes a task json response
	 * @param response
	 * @param taskId
	 * @param taskType
	 * @param msg
	 * @param msgType
	 * @throws UwsException
	 */
	public void writeTaskStatusResponse(HttpServletResponse response, String taskId, String taskType, String msg, String msgType) throws UwsException;
	

	/**
	 * Writes a jobs removal report
	 * @param response
	 * @param report
	 * @throws UwsException
	 */
	public void writeJobsRemovalReport(HttpServletResponse response, String report) throws UwsException;

	/**
	 * Writes a list of users (JSON): <code>[ record1, record2...]</code> <br/>
	 * Each record is composed of: {@link Templates#JSON_USER_LIST_RECORD} 
	 * @param response communication handler
	 * @param users users list (can be null)
	 * @throws IOException
	 */
	public void writeUserList(HttpServletResponse response, List<UwsJobOwner> users) throws UwsException;

	/**
	 * Writes a list of users (text/plain): <code>userid: user</code> <br/>
	 * @param response communication handler
	 * @param users users list (can be null)
	 * @throws IOException
	 */
	public void writeShareUserList(HttpServletResponse response, List<UwsShareUser> users) throws UwsException;

}
