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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsTemplates;
import esavo.uws.actions.handlers.admin.Templates;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.parameters.UwsParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareUser;
import esavo.uws.utils.UwsUtils;

/**
 * This Output handler generates XML output.<br/>
 * The exception is a job result. It is dumped as it is found.<br/>
 * If you want to write the data in a different way, extend this class and overwrite the required methods.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsDefaultOutputHandler implements UwsOutputResponseHandler {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(UwsDefaultOutputHandler.class.getName());

	private String appid;
	
	public UwsDefaultOutputHandler(String appid){
		this.appid = appid;
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public void writeJobListResponse(HttpServletResponse response,
			String baseUrl, String listName, List<UwsJob> jobs) throws UwsException {
		
//		writeJobListResponse(response, 
//				baseUrl,
//				listName,
//				jobs);
		Integer totalNumberOfResults;
		if(jobs != null){
			totalNumberOfResults = new Integer(jobs.size());
		}else{
			totalNumberOfResults = new Integer(0);
		}
		writeJobListResponse(response, baseUrl, listName, jobs, null, null, totalNumberOfResults, null);
	}

	@Override
	public void writeJobListResponse(HttpServletResponse response, 
									String baseUrl,
									String listName,
									List<UwsJob> jobs, 
									Integer limit, 
									Integer offset, 
									Integer totalNumberOfResults,
									String sort) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write jobs list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		String toWrite;
		toWrite = UwsTemplates.formatXmlJobsList(limit, offset, totalNumberOfResults, sort );
		pw.println(toWrite);
		String url;
		if(jobs != null){
			for(UwsJob job: jobs){
				url = UwsOutputUtils.getJobHref(baseUrl, listName, job.getJobId());
				toWrite = getJobSummary(job, url);
				pw.println(toWrite);
			}
		}
		pw.println(UwsTemplates.XML_JOBS_LIST_END);
		pw.flush();
	}
	
	@Override
	public void writeJobListResponseExtended(HttpServletResponse response, String baseUrl, String listName, List<UwsJob> jobs) throws UwsException {
		Integer totalNumberOfResults;
		if(jobs != null){
			totalNumberOfResults = new Integer(jobs.size());
		}else{
			totalNumberOfResults = new Integer(0);
		}
		writeJobListResponseExtended(response, baseUrl, listName, jobs, null, null, totalNumberOfResults, null);
	}

	@Override
	public void writeJobListResponseExtended(HttpServletResponse response,
			String baseUrl, String listName, List<UwsJob> jobs, Integer limit,
			Integer offset, Integer totalNumberOfResults, String order)
			throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job list extended", e);
		}
		
		pw.println(UwsTemplates.XML_INIT);
		
		String toWrite;
		toWrite = UwsTemplates.formatXmlJobsList(limit, offset, totalNumberOfResults, order);
		pw.println(toWrite);
		if(jobs != null){
			for(UwsJob job: jobs){
				pw.println(UwsTemplates.XML_JOB_INIT);
				writeJobResponseInternal(pw, baseUrl, job, job.getResults());
				pw.println(UwsTemplates.XML_JOB_END);
			}
		}
		pw.println(UwsTemplates.XML_JOBS_LIST_END);
		
		//end
		pw.flush();
	}

	
	private String getJobSummary(UwsJob job, String url) throws UwsException {
		String href;
//		try {
//			href = UwsUtils.encodeUrl(url);
//		} catch (UnsupportedEncodingException e) {
//			throw new UwsException("Error trying to codify href: '" + url + "'");
//		}
		href = UwsUtils.escapeXmlAttribute(url);
		String jobid = UwsUtils.escapeXmlAttribute(job.getJobId());
		String runid = UwsUtils.escapeXmlAttribute(job.getRunid());
		String phase = UwsUtils.escapeXmlAttribute(job.getPhase().name());
		//Date creationTime = job.getCreationTime();
		//String jobName = UwsUtils.escapeXmlData(UwsUtils.findParameter(job.getParameters(), "jobname"));
		//String jobType = UwsUtils.escapeXmlData(UwsUtils.findParameter(job.getParameters(), "jobtype"));
		//long totalRows = UwsUtils.getTotalRows(job.getResults());

		//return UwsTemplates.formatXmlJobSummary(jobid, href, phase, creationTime, jobName, totalRows, jobType);
		return UwsTemplates.formatXmlJobSummary(jobid, href, phase);
	}

	@Override
	public void writeJobResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job parameters list", e);
		}
		
		pw.println(UwsTemplates.XML_INIT);
		pw.println(UwsTemplates.XML_JOB_INIT_WITH_NAMESPACES);
		
		writeJobResponseInternal(pw, baseUrl, job, job.getResults());
		
		//end
		pw.println(UwsTemplates.XML_JOB_END);
		pw.flush();
	}
	
	protected void writeJobResponseInternal(PrintWriter pw, String baseUrl, UwsJob job, List<UwsJobResultMeta> results) throws UwsException {
		String jobid = job.getJobId();
		String listid = job.getListid();
		UwsJobOwner owner = job.getOwner();
		
		//main data
		pw.println(UwsTemplates.formatXmlJobId(jobid));
		pw.println(UwsTemplates.formatXmlJobRunId(job.getRunid()));
		if(owner == null){
			pw.println(UwsTemplates.formatXmlJobOwnerId(null));
		}else{
			pw.println(UwsTemplates.formatXmlJobOwnerId(owner.getId()));
		}
		pw.println(UwsTemplates.formatXmlJobPhaseId(job.getPhase().name()));
		pw.println(UwsTemplates.formatXmlJobQuote(job.getQuote()));
		pw.println(UwsTemplates.formatXmlJobStartTime(job.getStartTime()));
		pw.println(UwsTemplates.formatXmlJobEndTime(job.getEndTime()));
		pw.println(UwsTemplates.formatXmlJobExecutionDuration(job.getExecutionDuration()));
		pw.println(UwsTemplates.formatXmlJobDestruction(job.getDestructionTime()));
		pw.println(UwsTemplates.formatXmlJobCreationTime(job.getCreationTime()));
		pw.println(UwsTemplates.formatXmlJobLocation(job.getLocationId()));
		pw.println(UwsTemplates.formatXmlJobName(job.getName()));
		pw.flush();
		
		//Parameters
		writeJobParametersResponseInternal(pw, job.getParameters(), false);

		//Results
		writeJobResultsListResponseInternal(pw, baseUrl, listid, jobid, results, false);
		
		//Error
		writeJobErrorResponseInternal(pw, job.getErrorSummary(), false);
		pw.flush();
	}

	@Override
	public void writeJobParametersResponse(HttpServletResponse response, UwsJobParameters parameters) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job parameters list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		writeJobParametersResponseInternal(pw, parameters, true);
	}
	
	private void writeJobParametersResponseInternal(PrintWriter pw, UwsJobParameters parameters, boolean includeNamespaces){
		String toWrite;
		toWrite = UwsTemplates.formatXmlJobParameterInit(includeNamespaces);
		pw.println(toWrite);
		Object value;
		String dataType;
		if(parameters != null){
			for(String parameterName: parameters.getParameterNames()){
				value = parameters.getParameter(parameterName);
				dataType = UwsJobParameters.getParameterValueType(value).name();
				toWrite = UwsTemplates.formatXmlJobParameter(
						UwsUtils.escapeXmlAttribute(parameterName), 
						UwsUtils.escapeXmlData(value.toString()), UwsParameters.PARAMETER_COMMON, dataType);
				pw.println(toWrite);
			}
		}
		pw.println(UwsTemplates.XML_JOB_PARAMETERS_END);
		pw.flush();
	}

	@Override
	public void writeJobErrorResponse(HttpServletResponse response, UwsJobErrorSummaryMeta errorSummary) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_VOTABLE);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write a job error summary", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		writeJobErrorResponseInternal(pw, errorSummary, true);
	}
	
	private void writeJobErrorResponseInternal(PrintWriter pw, UwsJobErrorSummaryMeta errorSummary, boolean includeNamespaces){
		if (errorSummary == null) {
			pw.println(UwsTemplates.formatXmlErrorSummaryEmpty(includeNamespaces));
		} else {
			pw.println(UwsTemplates.formatXmlErrorSummaryInit(errorSummary.getType().name(), includeNamespaces, errorSummary.hasDetails()));
			pw.println(UwsTemplates.formatXmlErrorMessage(errorSummary.getMessage()));
			pw.println(UwsTemplates.XML_JOB_ERROR_SUMMARY_END);
		}
		pw.flush();
	}
	
	@Override
	public void writeJobErrorDataResponse(HttpServletResponse response, String jobid, UwsJobErrorSummaryMeta errorSummary, InputStream source) throws UwsException {
		dumpData(response, source, jobid + "_error", errorSummary.getDetailsMimeType(), null, null, errorSummary.getDetailsSize());
	}

	@Override
	public void writeJobResultDataResponse(HttpServletResponse response, UwsJob job, UwsJobResultMeta result, String outputFormat, InputStream source) throws UwsException {
		StringBuilder filename = new StringBuilder();
		String jobName = job.getName();
		
		if(jobName!=null && !jobName.trim().isEmpty()){
			filename.append(jobName.trim());
		}else{
			filename.append(job.getJobId());
		}
		
		filename.append("-").append(result.getId().trim());
		
		String suitableFileName = UwsOutputUtils.getSuitableFilename(filename.toString());
		
		dumpData(response, source, suitableFileName, result.getMimeType(), result.getFileExtension(), result.getContentEncoding(), result.getSize());
	}
	
	private void dumpData(HttpServletResponse response, InputStream source, String dataid, String mimeType, String fileExtension, String contentEncoding, long size) throws UwsException{
		
	    UwsOutputUtils.writeDataResponseHeader(response, dataid, mimeType, fileExtension, contentEncoding, size);
		
		OutputStream output = null;
		try{
			output = response.getOutputStream();
		}catch(IOException e){
			throw new UwsException("Cannot obtain output handler to write a job result", e);
		}
		try {
			UwsOutputUtils.dumpToStream(source, output);
		} catch (IOException ioe) {
			throw new UwsException("Cannot write result for job", ioe);
		} finally {
			try {
				output.flush();
			} catch (IOException e) {
				throw new UwsException(
						"Cannot flush output handler when writing a job result",
						e);
			}
		}
	}

	@Override
	public void writeJobResultListResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job results list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		String jobid = job.getJobId();
		String listid = job.getListid();
		List<UwsJobResultMeta> results = job.getResults();
		writeJobResultsListResponseInternal(pw, baseUrl, listid, jobid, results, true);
	}
	
	protected void writeJobResultsListResponseInternal(PrintWriter pw, String baseUrl, String listid, String jobid, List<UwsJobResultMeta> results, boolean includeNamespaces) throws UwsException {
		String toWrite;
		toWrite = UwsTemplates.formatXmlJobResultInit(includeNamespaces);
		pw.println(toWrite);
		String jobResultData = null;
		if(results != null){
			for(UwsJobResultMeta result: results){
				jobResultData = getJobResultExtraAttrs(baseUrl, listid, jobid, result);
				toWrite = UwsTemplates.formatXmlJobResultItem(UwsUtils.escapeXmlAttribute(result.getId()), jobResultData);
				pw.println(toWrite);
			}
		}
		pw.println(UwsTemplates.XML_JOB_RESULTS_LIST_END);
		pw.flush();
	}
	
	private String getJobResultExtraAttrs(String baseUrl, String listid, String jobid, UwsJobResultMeta result) throws UwsException{
		StringBuilder sb = new StringBuilder();
		String sValue = UwsOutputUtils.getResultHref(baseUrl, listid, jobid, result.getId());
		String tmp;
		String type = result.getXlinkHrefType();
		if(type != null){
			tmp = UwsUtils.escapeXmlAttribute(type);
			sb.append(UwsTemplates.formatXmlAttrXlinkType(tmp));
//			try{
//				tmp = UwsUtils.encodeUrl(sValue);
//			}catch(UnsupportedEncodingException e){
//				throw new UwsException("Error trying to codify href: '" + sValue + "'");
//			}
			tmp = UwsUtils.escapeXmlAttribute(sValue);
			sb.append(' ').append(UwsTemplates.formatXmlAttrXlinkHref(tmp));
		}
		sValue = result.getMimeType();
		if (sValue != null){
			sb.append(' ').append(UwsTemplates.formatXmlAttrMime(sValue));
		}
		sValue = result.getContentEncoding();
		if (sValue != null){
			sb.append(' ').append(UwsTemplates.formatXmlAttrEncoding(sValue));
		}
		long lValue = result.getSize();
		if (lValue >= 0){
			sb.append(' ').append(UwsTemplates.formatXmlAttrSize(lValue));
		}
		lValue = result.getRows();
		if (lValue >= 0){
			sb.append(' ').append(UwsTemplates.formatXmlAttrRows(lValue));
		}
		return sb.toString();
	}

	/**
	 * Creates a redirect response.
	 * @param response
	 * @param urlLocation
	 * @param extraMsg
	 */
	public void redirectResponse(HttpServletResponse response, String urlLocation, String extraMsg) throws UwsException{
		response.setStatus(SEE_OTHER);
		response.setContentType(CONTENT_TYPE_TEXT_PLAIN);
		response.setHeader(HEADER_LOCATION, urlLocation);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write a redirect", e);
		}
		pw.println(extraMsg == null ? "":extraMsg);
		pw.flush();
	}
	
	/**
	 * Writes a text/plain response.
	 * @param response
	 * @param data
	 */
	public void writeTextPlainResponse(HttpServletResponse response, String data) throws UwsException{
		writeTextPlainResponse(response, OK, data);
	}

	/**
	 * Writes a text/plain response.
	 * @param response
	 * @param status
	 * @param data
	 */
	public void writeTextPlainResponse(HttpServletResponse response, int status, String data) throws UwsException{
		response.setStatus(status);
		response.setContentType(CONTENT_TYPE_TEXT_PLAIN);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write text/plain", e);
		}
		//pw.println(data);
		pw.print(data);
		pw.flush();
	}

	@Override
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, String context, String message) throws UwsException{
		response.setStatus(httpErrorCode);
		response.setContentType(CONTENT_TYPE_HTML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write an error " + httpErrorCode + ": "+action, e);
		}
		UwsOutputUtils.writeErrorHtml(pw, httpErrorCode, action, context, message);
		//UwsOutputUtils.writeErrorStartHtml(pw, UwsTemplates.APPLICATION_ERROR_TITLE, httpErrorCode);
		//UwsOutputUtils.writeErrorDetails(pw, action, context, message);
		//UwsOutputUtils.writeErrorEndHtml(pw);
		pw.flush();
	}

	@Override
	public void writeServerErrorResponse(HttpServletResponse response, String action, UwsException uwsException) throws UwsException{
		if(uwsException == null){
			writeServerErrorResponse(response, INTERNAL_SERVER_ERROR, action, uwsException, uwsException.getOutputFormat());
		}else{
			int code = uwsException.getCode();
			if(code < 0){
				code = INTERNAL_SERVER_ERROR;
			}
			writeServerErrorResponse(response, code, action, uwsException, uwsException.getOutputFormat());
		}
	}

	@Override
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, Throwable t, 
			UwsExceptionOutputFormat outputFormat) throws UwsException{
		writeServerErrorResponse(response, httpErrorCode, action, null, t, outputFormat);
	}

	@Override
	public void writeServerErrorResponse(HttpServletResponse response, int httpErrorCode, String action, String context, Throwable t, 
			UwsExceptionOutputFormat outputFormat) throws UwsException{
		response.setStatus(httpErrorCode);
		if(httpErrorCode == UNAUTHORIZED){
			response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication needed\"");
		}
		//response.setContentType(CONTENT_TYPE_HTML);
		writeContentTypeForError(outputFormat, response);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write an error " + httpErrorCode + ": " + action , e);
		}
		if(outputFormat == UwsExceptionOutputFormat.VOTABLE || outputFormat==UwsExceptionOutputFormat.VOTABLE_XML){
			UwsOutputUtils.writeErrorVoTable(pw, action, context, t, httpErrorCode);
		}else{
			//deafult: HTML
			UwsOutputUtils.writeErrorHtml(pw, httpErrorCode, action, context, t);
			//UwsOutputUtils.writeErrorStartHtml(pw, UwsTemplates.APPLICATION_ERROR_TITLE, httpErrorCode);
			//UwsOutputUtils.writeErrorDetailsHtml(pw, action, context, t);
			//UwsOutputUtils.writeStackTraceHtml(pw, t);
			//UwsOutputUtils.writeErrorEndHtml(pw);
		}
		pw.flush();
	}
	
	private void writeContentTypeForError(UwsExceptionOutputFormat outputFormat, HttpServletResponse response){
		switch(outputFormat){
		case HTML:
			response.setContentType(CONTENT_TYPE_HTML);
			break;
		case VOTABLE_XML:
			response.setContentType(MIME_TYPE_APP_XML);
			break;
		case VOTABLE:
			response.setContentType(MIME_TYPE_VOTABLE);
			break;
		}
	}

	@Override
	public String toString(){
		return "Default output handler for application '"+appid+"'";
	}

	@Override
	public void writeSharedGroupsResponse(HttpServletResponse response, List<UwsShareGroup> groups, String ownerid) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write groups list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		//String toWrite = UwsTemplates.formatXmlSharedGroups(ownerid);
		//pw.println(toWrite);
		pw.println(UwsTemplates.XML_SHARED_GROUPS_LIST_INIT);
		if(groups != null){
			for(UwsShareGroup group: groups){
				UwsOutputUtils.writeSharedGroup(pw, group);
			}
		}
		pw.println(UwsTemplates.XML_SHARED_GROUPS_LIST_END);
		pw.flush();
	}

//	@Override
//	public void writeSharedItemsResponse(HttpServletResponse response, List<UwsShareItem> items, String ownerid) throws UwsException {
//		response.setStatus(OK);
//		response.setContentType(MIME_TYPE_XML);
//		PrintWriter pw = null;
//		try {
//			pw = response.getWriter();
//		} catch (IOException e) {
//			throw new UwsException("Cannot obtain output handler to write items list", e);
//		}
//		pw.println(UwsTemplates.XML_INIT);
//		String toWrite = UwsTemplates.formatXmlSharedItems(ownerid);
//		pw.println(toWrite);
//		if(items != null){
//			for(UwsShareItem item: items){
//				UwsOutputUtils.writeSharedItem(pw, item);
//			}
//		}
//		pw.println(UwsTemplates.XML_SHARED_ITEMS_LIST_END);
//		pw.flush();
//	}

	@Override
	public void writeSharedItemsResponse(HttpServletResponse response, List<UwsShareItemBase> sharedItemsBase, String ownerid) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write items list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		String toWrite = UwsTemplates.formatXmlSharedItemsList(ownerid);
		pw.println(toWrite);
		if(sharedItemsBase != null){
			for(UwsShareItemBase item: sharedItemsBase){
				UwsOutputUtils.writeSharedItem(pw, item);
			}
		}
		pw.println(UwsTemplates.XML_SHARE_ITEMS_LIST_END);
		pw.flush();
	}
	
	@Override
	public void writeSharedUsers(HttpServletResponse response, List<UwsShareUser> users) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write items list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		UwsOutputUtils.writeUsersAndNames(pw, users);
		pw.flush();
	}

	@Override
	public void writeTaskStatusResponse(HttpServletResponse response,
			String taskId, String taskType, String msg, String msgType)
			throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_JSON);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write task status", e);
		}
		String toWrite = UwsTemplates.formatJsonTaskStatus(taskId, taskType, msg, msgType);
		pw.println(toWrite);
		pw.flush();
		
	}

	@Override
	public void writeSimpleJsonResponse(HttpServletResponse response, String id, String msg) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_JSON);
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch(IOException e){
			throw new UwsException("Cannot obtain output handler to write json output", e);
		}
		String toWrite = UwsTemplates.formatJsonMessage(id, msg);
		pw.println(toWrite);
		pw.flush();
	}

	@Override
	public void writeJobsRemovalReport(HttpServletResponse response, String report) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_JSON);
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch(IOException e){
			throw new UwsException("Cannot obtaion output handler to write json removal report", e);
		}
		if (report == null) {
			pw.println("{}");
		} else {
			String[] items = report.split("\n");
			pw.println("{\"report\": [");
			if(items != null){
				for(String i: items){
					pw.println("\""+i+"\"");
				}
			}
			pw.println("]}");
		}
		pw.flush();
	}

	@Override
	public void writeUserList(HttpServletResponse response, List<UwsJobOwner> users) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_JSON);
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch(IOException e){
			throw new UwsException("Cannot obtaion output handler to write json user list", e);
		}
		pw.println("[");
		boolean firstTime = true;
		String row;
		if(users != null){
			for(UwsJobOwner user: users){
				if(firstTime){
					firstTime = false;
				}else{
					pw.println(",");
				}
				row = Templates.getJsonUserDetails(user);
				pw.print(row);
			}
		}
		pw.println("]");
		pw.flush();
	}

	@Override
	public void writeShareUserList(HttpServletResponse response, List<UwsShareUser> users) throws UwsException {
		response.setStatus(OK);
		response.setContentType(UwsOutputResponseHandler.CONTENT_TYPE_TEXT_PLAIN);
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch(IOException e){
			throw new UwsException("Cannot obtaion output handler to write text/plain user list", e);
		}
		if(users == null){
			return;
		}
		for(UwsShareUser user: users){
			pw.println(user.getId() + ": " + user.getName());
		}
		pw.flush();
	}


}
