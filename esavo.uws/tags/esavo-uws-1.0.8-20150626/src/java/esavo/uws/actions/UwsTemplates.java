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
package esavo.uws.actions;

import java.text.MessageFormat;
import java.util.Date;

import esavo.uws.share.UwsShareUser;
import esavo.uws.utils.UwsUtils;

public class UwsTemplates {

	//================================================== 
	//---------------------- HTML ----------------------- 
	//================================================== 

	public static final String APPLICATION_TITLE = "UWS";
	public static final String APPLICATION_ERROR_TITLE = "SERVICE ERROR";
	
	public static final String ITEM_TYPE = "Type";
	public static final String ITEM_ACTION = "Action";
	public static final String ITEM_CONTEXT = "Context";
	public static final String ITEM_EXCEPTION = "Exception";
	public static final String ITEM_MESSAGE = "Message";
	public static final String ITEM_QUERY_STATUS = "QUERY_STATUS";
	public static final String ITEM_HTTP_ERROR_CODE = "HttpErrorCode";
	public static final String ITEM_STACK_TRACE = "StackTrace";
	public static final String ERROR = "ERROR";
	
	public static final String ERROR_BACKGROUND_COLOR = "red";
	public static final String ERROR_FOREGROUND_COLOR = "white";
	public static final String BACKGROUND_COLOR = "white";
	public static final String FOREGROUND_COLOR = "black";
	public static final String FOREGROUND_COLOR_1 = "#4A4A4A";
	public static final String TR_CLASS_1 = "alt1";
	public static final String TR_CLASS_2 = "alt2";
	
//	public static final String EXCEPTION_TABLE_BORDER_COLOR = "#FC8813";
//	public static final String EXCEPTION_TABLE_HEADER_BACKGROUND = "#F29842";
//	public static final String EXCEPTION_TABLE_ROW_BACKGROUND_NORMAL = "white";
//	public static final String EXCEPTION_TABLE_ROW_BACKGROUND_ALTERNATE = "#FFDAB6";
	
//	public static final String EXCEPTION_TABLE_BORDER_COLOR = "red";
//	public static final String EXCEPTION_TABLE_HEADER_BACKGROUND = "red";
	public static final String EXCEPTION_TABLE_BORDER_COLOR = "#909090";
	public static final String EXCEPTION_TABLE_HEADER_BACKGROUND = "#909090";
	public static final String EXCEPTION_TABLE_ROW_BACKGROUND_NORMAL = "white";
//	public static final String EXCEPTION_TABLE_ROW_BACKGROUND_ALTERNATE = "#FFF0F0";
	public static final String EXCEPTION_TABLE_ROW_BACKGROUND_ALTERNATE = "#F0F0F0";
	
	public static final String CSS_CONTENT = 	
		"<style type=\"text/css\">\n"+
		"body { background-color: "+BACKGROUND_COLOR+"; color: "+FOREGROUND_COLOR+"; }\n"+
		"h2 { font-weight: bold; font-variant: small-caps; text-decoration: underline; font-size: 1.5em; color: "+FOREGROUND_COLOR_1+"; }\n"+
		"ul, ol { margin-left: 2em; margin-top: 0.2em; text-align: justify; }\n"+
		"li { margin-bottom: 0.2em; margin-top: 0; }\n"+
		"p, p.listheader { text-align: justify; text-indent: 2%; margin-top: 0; }\n"+
		"table { border-collapse: collapse; }\n"+
		"table, th, td { border: 1px solid "+EXCEPTION_TABLE_BORDER_COLOR+"; }\n"+
		"th { background-color: "+EXCEPTION_TABLE_HEADER_BACKGROUND+"; color: "+BACKGROUND_COLOR+"; font-size: 1.1em; padding: 3px 5px 3px 5px; }\n"+
		"tr."+TR_CLASS_1+" { background-color: "+EXCEPTION_TABLE_ROW_BACKGROUND_NORMAL+"; }\n" +
		"tr."+TR_CLASS_2+" { background-color: "+EXCEPTION_TABLE_ROW_BACKGROUND_ALTERNATE+"; }\n" +
		"td { padding: 2px 5px 2px 5px; }\n"+
		"</style>";
	
	public static final String HTML_INIT =
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />";
	
	/**
	 * <ul>
	 * <li>0: title (i.e. SERVICE ERROR)</li>
	 * </ul>
	 */
	public static final String HTML_HEADER_TITLE = "<title>{0}</title>";
	
	/**
	 * <ul>
	 * <li>0: body title (i.e. SERVICE ERROR)</li>
	 * <li>1: http error code</li>
	 * </ul>
	 */
	public static final String HTML_END_HEADER_START_BODY = 
			"</head>\n<body>\n"+
			"<h1 style=\"text-align: center; background-color:"+ERROR_BACKGROUND_COLOR+"; color: "+ERROR_FOREGROUND_COLOR+"; font-weight: bold;\">{0} - {1}</h1>";
	
	public static final String HTML_END = "</body>\n</html>";
	

	//---------------------- DESCRIPTION ----------------------- 
	
	public static final String HTML_DESCRIPTION_INIT = "<h2>Description</h2>\n<ul>";
	
	/**
	 * <ul>
	 * <li>0: item title</li>
	 * <li>1: item explanation</li>
	 * </ul>
	 */
	public static final String HTML_UL_ITEM =
			"<li><b>{0}: </b>{1}</li>";
	
	public static final String HTML_DESCRIPTION_END = "</ul>";

	
	//---------------------- STACK TRACE ----------------------- 
	
	public static final String HTML_STACK_TRACE_TITLE = "<h2>Stack trace</h2>";
	
	public static final String HTML_STACK_TRACE_INIT = 
		"<table style=\"width: ihnerit;\">\n"+
		"<tr><th>Class</th><th>Method</th><th>Line</th></tr>";
	
	public static final String HTML_STACK_TRACE_END = "</table><br/>";
	
	/**
	 * <ul>
	 * <li>0: row class name (alt1, alt2)</li>
	 * <li>1: row trace class name</li>
	 * <li>2: row trace method name</li>
	 * <li>3: row trace line number</li>
	 * </ul>
	 */
	public static final String HTML_STACK_TRACE_ITEM =
		"<tr class=\"{0}\"><td>{1}</td><td>{2}</td><td>{3}</td></tr>";
		
	/**
	 * <ul>
	 * <li>0: exception class name</li>
	 * <li>1: exception message</li>
	 * </ul>
	 */
	public static final String HTML_STACK_TRACE_CAUSE = 
		    "<p><b>Caused by {0}</b></p>\n"+
		    "<p>{1}</p>";


	
	//================================================== 
	//---------------------- XML ----------------------- 
	//================================================== 

	public static final String XML_ATTR_NIL_VALUE = "xsi:nil=\"true\"";
	
	/**
	 * <ul>
	 * <li>0: xlink type</li>
	 * </ul>
	 */
	public static final String XML_ATTR_XLINK_TYPE = "xlink:type=\"{0}\"";

	/**
	 * <ul>
	 * <li>0: xlink href (URL escaped)</li>
	 * </ul>
	 */
	public static final String XML_ATTR_XLINK_REF = "xlink:href=\"{0}\"";

	/**
	 * <ul>
	 * <li>0: mime attr</li>
	 * </ul>
	 */
	public static final String XML_ATTR_MIME = "mime=\"{0}\"";
	
	/**
	 * <ul>
	 * <li>0: size attr</li>
	 * </ul>
	 */
	public static final String XML_ATTR_SIZE = "size=\"{0}\"";

	/**
	 * <ul>
	 * <li>0: rows attr</li>
	 * </ul>
	 */
	public static final String XML_ATTR_ROWS = "rows=\"{0}\"";
	
	
	public static final String XML_NAMESPACES =
			" "+
			"xmlns:uws=\"http://www.ivoa.net/xml/UWS/v1.0\" " +
			"xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
			"xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	
	public static final String XML_INIT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	
	
	public static final String XML_TITLE = "<title>{0}</title>";
	public static final String XML_DESCRIPTION = "<description>{0}</description>";
	//public static final String XML_USER = "<user id=\"{0}\"/>";
	public static final String XML_USER_AND_NAME = "<user id=\"{0}\" name=\"{1}\"/>";

	//public static final String XML_SHARED_GROUPS_LIST_INIT = "<sharedGroups owner=\"{0}\">";
	public static final String XML_SHARED_GROUPS_LIST_INIT = "<sharedGroups>";
	public static final String XML_SHARED_GROUP_INIT = "<sharedGroup id=\"{0}\" owner=\"{1}\">";
	public static final String XML_SHARED_GROUP_END = "</sharedGroup>";
	public static final String XML_SHARED_GROUPS_LIST_END = "</sharedGroups>";
	
	
	/**
	 * <ul>
	 * <li>0: owner (escaped to XML)</li>
	 * </ul>
	 */
	public static final String XML_SHARE_ITEMS_LIST_INIT = "<sharedItems owner=\"{0}\">";
	public static final String XML_SHARE_ITEMS_LIST_END = "</sharedItems>";
	
	/**
	 * <ul>
	 * <li>0: id (resource id) (escaped to XML)</li>
	 * <li>1: type (resource type) (escaped to XML)</li>
	 * </ul>
	 */
	public static final String XML_SHARED_ITEM_INIT = "<sharedItem id=\"{0}\" type=\"{1}\">";
	public static final String XML_SHARED_ITEM_END = "</sharedItem>";
	
	public static final String XML_SHARED_TO_ITEMS_LIST_INIT = "<sharedToItems>";
	
	/**
	 * <ul>
	 * <li>0: share to id (escaped to XML)</li>
	 * <li>1: share type (escaped to XML)</li>
	 * <li>2: share mode (escaped to XML)</li>
	 * </ul>
	 */
	public static final String XML_SHARED_TO_ITEM = "<sharedToItem shareTo=\"{0}\" shareType=\"{1}\" shareMode=\"{2}\"/>";
	
	public static final String XML_SHARED_TO_ITEMS_LIST_END = "</sharedToItems>";
	
//	/**
//	 * <ul>
//	 * <li>0: id (escaped to XML)</li>
//	 * <li>1: type (escaped to XML)</li>
//	 * </ul>
//	 */
//	public static final String XML_SHARED_ITEM_DESCRIPTION_INIT = "<sharedItem id=\"{0}\" type=\"{1}\">";
//	public static final String XML_SHARED_ITEM_DESCRIPTION_END = "</sharedItem>";
	
	public static final String XML_SHARED_USERS_LIST_INIT = "<users>";
	public static final String XML_SHARED_USERS_LIST_END = "</users>";


	/**
	 * <ul>
	 * <li>0: list name (escaped to XML)</li>
	 * </ul>
	 */
	public static final String XML_JOBS_LIST_INIT = "<uws:jobs " + XML_NAMESPACES + " name=\"{0}\">";
	/**
	 * <ul>
	 * <li>0: job id (escaped to XML)</li>
	 * <li>1: job href (URL encoded): can be empty</li>
	 * <li>2: job runid (escaped to XML): can be empty</li>
	 * <li>3: job phase</li>
	 * </ul>
	 */
	public static final String XML_JOBS_LIST_ITEM = "<uws:jobRef id=\"{0}\" xlink:href=\"{1}\" runId=\"{2}\"><uws:phase>{3}</uws:phase></uws:jobRef>";
	public static final String XML_JOBS_LIST_END = "</uws:jobs>";
	
	
	public static final String XML_JOB_INIT = "<uws:job "+XML_NAMESPACES+">";
	
	/**
	 * <ul>
	 * <li>0: jobid (escaped to XML CDATA)</li>
	 * </ul>
	 */
	public static final String XML_JOB_ID = "<uws:jobId>{0}</uws:jobId>";

	public static final String XML_JOB_RUNID_EMPTY = "<uws:runId "+XML_ATTR_NIL_VALUE+"/>";
	public static final String XML_JOB_RUNID = "<uws:runId>{1}</uws:runId>";
	
	public static final String XML_JOB_OWNERID_EMPTY = "<uws:ownerId "+XML_ATTR_NIL_VALUE+"/>";
	/**
	 * <ul>
	 * <li>0: onwerid (escaped to XML CDATA)</li>
	 * </ul>
	 */
	public static final String XML_JOB_OWNERID = "<uws:ownerId>{0}</uws:ownerId>";
	
	/**
	 * <ul>
	 * <li>0: phaseid</li>
	 * </ul>
	 */
	public static final String XML_JOB_PHASE = "<uws:phase>{0}</uws:phase>";
	
	public static final String XML_JOB_QUOTE_EMPTY = "<uws:quote "+XML_ATTR_NIL_VALUE+"/>";
	/**
	 * <ul>
	 * <li>0: quote</li>
	 * </ul>
	 */
	public static final String XML_JOB_QUOTE = "<uws:quote>{0}</uws:quote>";

	public static final String XML_JOB_START_TIME_EMPTY = "<uws:startTime "+XML_ATTR_NIL_VALUE+"/>";
	/**
	 * <ul>
	 * <li>0: start time</li>
	 * </ul>
	 */
	public static final String XML_JOB_START_TIME = "<uws:startTime>{0}</uws:startTime>";

	public static final String XML_JOB_END_TIME_EMPTY = "<uws:endTime "+XML_ATTR_NIL_VALUE+"/>";
	/**
	 * <ul>
	 * <li>0: end time</li>
	 * </ul>
	 */
	public static final String XML_JOB_END_TIME = "<uws:endTime>{0}</uws:endTime>";

	/**
	 * <ul>
	 * <li>0: execution duration</li>
	 * </ul>
	 */
	public static final String XML_JOB_EXECUTION_DURATION = "<uws:executionDuration>{0}</uws:executionDuration>";
	
	public static final String XML_JOB_DESTRUCTION_EMPTY = "<uws:destruction "+XML_ATTR_NIL_VALUE+"/>";
	/**
	 * <ul>
	 * <li>0: destruction time</li>
	 * </ul>
	 */
	public static final String XML_JOB_DESTRUCTION = "<uws:destruction>{0}</uws:destruction>";
	
	public static final String XML_JOB_END = "</uws:job>";
	
	
	
	
	/**
	 * <ul>
	 * <li>0: namespaces</li>
	 * </ul>
	 */
	public static final String XML_JOB_ERROR_SUMMARY_EMPTY = "<uws:errorSummary{0} "+XML_ATTR_NIL_VALUE+"/>";
	

	/**
	 * <ul>
	 * <li>0: namespaces</li>
	 * <li>1: error type (escaped to XML))</li>
	 * <li>2: details: true/false</li>
	 * </ul>
	 */
	public static final String XML_JOB_ERROR_SUMMARY_INIT = "<uws:errorSummary{0} type=\"{1}\" hasDetails=\"{2}\">";
	
	/**
	 * <ul>
	 * <li>0: error message (escaped to XML CDATA)</li>
	 * </ul>
	 */
	public static final String XML_JOB_ERROR_SUMMARY_MESSAGE = "<uws:message>{0}</uws:message>";

	public static final String XML_JOB_ERROR_SUMMARY_END = "</uws:errorSummary>";

	/**
	 * <ul>
	 * <li>0: namespaces</li>
	 * </ul>
	 */
	public static final String XML_JOB_RESULTS_LIST_INIT = "<uws:results{0}>";
	
	/**
	 * <ul>
	 * <li>0: result id (escaped to XML)</li>
	 * <li>1: result data (properly escaped)</li>
	 * </ul>
	 */
	public static final String XML_JOB_RESULT_ITEM = "<uws:result id=\"{0}\" {1} />";

	public static final String XML_JOB_RESULTS_LIST_END = "</uws:results>";

	/**
	 * <ul>
	 * <li>0: namespaces</li>
	 * </ul>
	 */
	public static final String XML_JOB_PARAMETERS_INIT = "<uws:parameters{0}>";
	
	/**
	 * <ul>
	 * <li>0: parameter id (escaped to XML)</li>
	 * <li>1: parameter value (escaped to CDATA)</li>
	 * </ul>
	 */
	public static final String XML_JOB_PARAMETER_ITEM =	"<uws:parameter id=\"{0}\">{1}</uws:parameter>";
	
	public static final String XML_JOB_PARAMETERS_END = "</uws:parameters>";
	
	
	
	//================================================== 
	//-------------------- VOTABLE ---------------------
	//==================================================
	
	public static final String VOTABLE_INIT = "<VOTABLE version=\"1.2\" xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\">";
	public static final String VOTABLE_END = "</VOTABLE>";
	public static final String VOTABLE_RESOURCE_INIT = "<RESOURCE type=\"results\">";
	public static final String VOTABLE_RESOURCE_END = "</RESOURCE>";
	/**
	 * <ul>
	 * <li>0: info name</li>
	 * <li>1: info value</li>
	 * </ul>
	 */
	public static final String VOTABLE_INFO_INIT = "<INFO name=\"{0}\" value=\"{1}\">";
	public static final String VOTABLE_INFO_END = "</INFO>";

	/**
	 * <ul>
	 * <li>0: info name</li>
	 * <li>1: info value</li>
	 * </ul>
	 */
	public static final String VOTABLE_INFO = "<INFO name=\"{0}\" value=\"{1}\"/>";
	
	/**
	 * <ul>
	 * <li>0: row trace class name</li>
	 * <li>1: row trace method name</li>
	 * <li>2: row trace line number</li>
	 * </ul>
	 */
	public static final String VOTABLE_STACK_TRACE_ITEM =
		"{0}\t{1}\t{2}";
		
	/**
	 * <ul>
	 * <li>0: exception class name</li>
	 * <li>1: exception message</li>
	 * </ul>
	 */
	public static final String VOTABLE_STACK_TRACE_CAUSE = 
		    "Caused by {0}\n{1}";



	//================================================== 
	//-------------------- METHODS --------------------- 
	//================================================== 

	public static String formatXmlJobId(String jobid){
		return MessageFormat.format(XML_JOB_ID, UwsUtils.escapeXmlData(jobid));
	}

	public static String formatXmlJobRunId(String runid) {
		if (runid == null || "".equals(runid)) {
			return XML_JOB_RUNID_EMPTY;
		} else {
			return MessageFormat.format(XML_JOB_RUNID, UwsUtils.escapeXmlData(runid));
		}
	}

	public static String formatXmlJobOwnerId(String ownerid){
		if (ownerid == null || "".equals(ownerid)) {
			return XML_JOB_OWNERID_EMPTY;
		} else {
			return MessageFormat.format(XML_JOB_OWNERID, UwsUtils.escapeXmlData(ownerid));
		}
	}
	public static String formatXmlJobPhaseId(String phaseid){
		return MessageFormat.format(XML_JOB_PHASE, phaseid);
	}
//	public static String formatXmlJobQuote(Date quote){
//		if(quote == null || "".equals(quote)){
//			return XML_JOB_QUOTE_EMPTY;
//		}else{
//			return MessageFormat.format(XML_JOB_QUOTE, UwsUtils.formatDate(quote));
//		}
//	}
	public static String formatXmlJobQuote(long quote){
		return MessageFormat.format(XML_JOB_QUOTE, ""+quote);
	}
	public static String formatXmlJobStartTime(Date startTime){
		if(startTime == null || "".equals(startTime)){
			return XML_JOB_START_TIME_EMPTY;
		}else{
			return MessageFormat.format(XML_JOB_START_TIME, UwsUtils.formatDate(startTime));
		}
	}
	public static String formatXmlJobEndTime(Date endTime){
		if(endTime == null || "".equals(endTime)){
			return XML_JOB_END_TIME_EMPTY;
		}else{
			return MessageFormat.format(XML_JOB_END_TIME, UwsUtils.formatDate(endTime));
		}
	}
	public static String formatXmlJobDestruction(Date destruction){
		if(destruction == null || "".equals(destruction)){
			return XML_JOB_DESTRUCTION_EMPTY;
		}else{
			return MessageFormat.format(XML_JOB_DESTRUCTION, UwsUtils.formatDate(destruction));
		}
	}
	public static String formatXmlJobExecutionDuration(long execDuration){
		return MessageFormat.format(XML_JOB_EXECUTION_DURATION, ""+execDuration);
	}


	/**
	 * Creates a job summary XML item
	 * @param jobid (escaped to XML)
	 * @param href (URL escaped)
	 * @param runid (escaped to XML)
	 * @param phase (escaped to XML)
	 * @return
	 */
	public static String formatXmlJobSummary(String jobid, String href, String runid, String phase){
		return MessageFormat.format(XML_JOBS_LIST_ITEM, jobid, href, runid, phase);
	}
	
	/**
	 * Creates a job parameter init item.
	 * @param includeNamespaces
	 * @return
	 */
	public static String formatXmlJobParameterInit(boolean includeNamespaces){
		if(includeNamespaces){
			return MessageFormat.format(XML_JOB_PARAMETERS_INIT, XML_NAMESPACES);
		} else {
			return MessageFormat.format(XML_JOB_PARAMETERS_INIT, "");
		}
	}

	/**
	 * Creates a job parameter item
	 * @param id parameter identifier (escaped to XML)
	 * @param value parameter value (escaped to XML CDATA)
	 * @return
	 */
	public static String formatXmlJobParameter(String id, String value){
		return MessageFormat.format(XML_JOB_PARAMETER_ITEM, id, value);
	}
	
	/**
	 * Creates a job result init item.
	 * @param includeNamespaces
	 * @return
	 */
	public static String formatXmlJobResultInit(boolean includeNamespaces){
		if(includeNamespaces){
			return MessageFormat.format(XML_JOB_RESULTS_LIST_INIT, XML_NAMESPACES);
		}else{
			return MessageFormat.format(XML_JOB_RESULTS_LIST_INIT, "");
		}
	}

	/**
	 * Creates a job result item.
	 * @param id result identifier (escaped to XML)
	 * @param data result extra attributes (properly escaped)
	 * @return
	 */
	public static String formatXmlJobResultItem(String id, String data){
		return MessageFormat.format(XML_JOB_RESULT_ITEM, id, data);
	}

	/**
	 * Creates a Xlink:Href XML attribute
	 * @param value (URL escaped)
	 * @return
	 */
	public static String formatXmlAttrXlinkHref(String value) {
		return formatXmlAttr(XML_ATTR_XLINK_REF, value);
	}

	/**
	 * Creates a Xlink:Type XML attribute.
	 * @param value (escaped to XML)
	 * @return
	 */
	public static String formatXmlAttrXlinkType(String value) {
		return formatXmlAttr(XML_ATTR_XLINK_TYPE, value);
	}

	/**
	 * Creates a mime type XML attribute.
	 * @param value
	 * @return
	 */
	public static String formatXmlAttrMime(String value) {
		return formatXmlAttr(XML_ATTR_MIME, value);
	}

	/**
	 * Creates a size XML attribute.
	 * @param value
	 * @return
	 */
	public static String formatXmlAttrSize(long value) {
		return formatXmlAttr(XML_ATTR_SIZE, "" + value);
	}

	/**
	 * Creates a rows XML attribute.
	 * @param rows
	 * @return
	 */
	public static String formatXmlAttrRows(long rows) {
		return formatXmlAttr(XML_ATTR_ROWS, "" + rows);
	}
	
	/**
	 * Creates a XML attribute.
	 * @param msgId (properly escaped)
	 * @param value (properly escaped)
	 * @return
	 */
	public static String formatXmlAttr(String msgId, String value){
		return MessageFormat.format(msgId, value);
	}

	
	
	/**
	 * Creates a XML jobs list item
	 * @param listName (escaped to XML)
	 * @return
	 */
	public static String formatXmlJobsList(String listName){
		return MessageFormat.format(XML_JOBS_LIST_INIT, listName);
	}
	
	/**
	 * Creates a XML errorSummary empty item.
	 * @param includeNamespaces
	 * @return
	 */
	public static String formatXmlErrorSummaryEmpty(boolean includeNamespaces){
		if(includeNamespaces){
			return MessageFormat.format(XML_JOB_ERROR_SUMMARY_EMPTY, XML_NAMESPACES);
		}else{
			return MessageFormat.format(XML_JOB_ERROR_SUMMARY_EMPTY, "");
		}
	}
	
	/**
	 * Creates a XML errorSummary init item.
	 * @param includeNamespaces
	 * @param hasErrorSummary
	 * @return
	 */
	public static String formatXmlErrorSummaryInit(String type, boolean includeNamespaces, boolean hasDetails){
		if(includeNamespaces){
			return MessageFormat.format(XML_JOB_ERROR_SUMMARY_INIT, XML_NAMESPACES, type, ""+hasDetails);
		}else{
			return MessageFormat.format(XML_JOB_ERROR_SUMMARY_INIT, "", type, ""+hasDetails);
		}
	}
	
	/**
	 * Creates a XML error summary message (escaped to CDATA).
	 * @param errorMessage (escaped to XML CDATA)
	 * @return
	 */
	public static String formatXmlErrorMessage(String errorMessage){
		return MessageFormat.format(XML_JOB_ERROR_SUMMARY_MESSAGE, UwsUtils.escapeXmlData(errorMessage));
	}
	
	/**
	 * Creates a HTML header title.
	 * @param errorHeaderTitle
	 * @return
	 */
	public static String formatErrorHeaderTitle(String errorHeaderTitle){
		return MessageFormat.format(HTML_HEADER_TITLE, errorHeaderTitle);
	}
	
	/**
	 * Creates a HTML initial body.
	 * @param errorTitle
	 * @param httpErrorCode
	 * @return
	 */
	public static String formatErrorBodyInit(String errorTitle, int httpErrorCode){
		return MessageFormat.format(HTML_END_HEADER_START_BODY, errorTitle, ""+httpErrorCode);
	}
	
	/**
	 * Creates a 'LI' HTML item
	 * @param itemTitle
	 * @param itemExplanation
	 * @return
	 */
	public static String formatUlItem(String itemTitle, String itemExplanation){
		return MessageFormat.format(HTML_UL_ITEM, itemTitle, ""+itemExplanation);
	}
	
	/**
	 * Creates a 'TR' HTML item for a stack trace item.
	 * @param rowIndex
	 * @param className
	 * @param methodName
	 * @param lineNumber
	 * @return
	 */
	public static String formatStackTraceItem(int rowIndex, String className, String methodName, int lineNumber){
		return MessageFormat.format(HTML_STACK_TRACE_ITEM, (rowIndex%2 == 0 ? TR_CLASS_1 : TR_CLASS_2), className, methodName, ""+lineNumber);
	}

	/**
	 * Creates a HTML explanation for a Throwable object.
	 * @param exceptionClass
	 * @param exceptionMessage
	 * @return
	 */
	public static String formatStackTraceCause(String exceptionClass, String exceptionMessage){
		return MessageFormat.format(HTML_STACK_TRACE_CAUSE, exceptionClass, ""+exceptionMessage);
	}
	
	/**
	 * Creates a VO INFO item (init)
	 * @param name
	 * @param value
	 * @return
	 */
	public static String formatVoInfoInit(String name, String value){
		return MessageFormat.format(VOTABLE_INFO_INIT, name, value);
	}

	/**
	 * Creates a VO INFO item
	 * @param name
	 * @param value
	 * @return
	 */
	public static String formatVoInfo(String name, String value){
		return MessageFormat.format(VOTABLE_INFO, name, value);
	}

	/**
	 * Creates a VO text line for a stack trace item.
	 * @param className
	 * @param methodName
	 * @param lineNumber
	 * @return
	 */
	public static String formatStackTraceVoItem(String className, String methodName, int lineNumber){
		return MessageFormat.format(VOTABLE_STACK_TRACE_ITEM, className, methodName, ""+lineNumber);
	}

	/**
	 * Creates a VO test line that contains the explanation for a Throwable object.
	 * @param exceptionClass
	 * @param exceptionMessage
	 * @return
	 */
	public static String formatStackTraceVoCause(String exceptionClass, String exceptionMessage){
		return MessageFormat.format(VOTABLE_STACK_TRACE_CAUSE, exceptionClass, ""+exceptionMessage);
	}
	
//	public static String formatXmlSharedGroups(String ownerid){
//		return MessageFormat.format(XML_SHARED_GROUPS_LIST_INIT, ownerid);
//	}
	
	public static String formatXmlGroupItem(String groupid, String ownerid){
		return MessageFormat.format(XML_SHARED_GROUP_INIT, groupid, ownerid);
	}
	
	public static String formatXmlDescription(String description){
		return MessageFormat.format(XML_DESCRIPTION, description);
	}
	
	public static String formatXmlTitle(String title){
		return MessageFormat.format(XML_TITLE, title);
	}
	
//	public static String formatXmlUser(String userid){
//		return MessageFormat.format(XML_USER, userid);
//	}

	public static String formatXmlUser(String userid, String name){
		return MessageFormat.format(XML_USER_AND_NAME, userid, name);
	}

	//	public static String formatXmlSharedItems(String ownerid){
//		return MessageFormat.format(XML_SHARED_ITEMS_LIST_INIT, ownerid);
//	}

	public static String formatXmlSharedItemsList(String ownerid){
		return MessageFormat.format(XML_SHARE_ITEMS_LIST_INIT, ownerid);
	}

	public static String formatXmlSharedItem(String resourceId, String resourceType){
		return MessageFormat.format(XML_SHARED_ITEM_INIT, resourceId, resourceType);
	}
	
	public static String formatXmlSharedToItem(String shareToId, String shareType, String shareMode){
		return MessageFormat.format(XML_SHARED_TO_ITEM, shareToId, shareType, shareMode);
	}
	
//	public static String formatXmlSharedItemDescription(String resourceId, String resourceType){
//		return MessageFormat.format(XML_SHARED_ITEM_DESCRIPTION_INIT, resourceId, resourceType);
//	}
	

}
