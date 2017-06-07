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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsTemplates;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareUser;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsErrorType;
import esavo.uws.utils.UwsUtils;

public class UwsOutputUtils {
	
	/**
	 * Writes (for async jobs) or generate (for sync jobs) the suitable {@link UwsJobErrorSummaryMeta}} object (and file for async jobs).
	 * The {@link UwsJobErrorSummaryMeta} object is attached to the provided job.
	 * @param job
	 * @param storage
	 * @param e error/exception
	 * @param action
	 * @param context error context
	 */
	public static void writeError(UwsJob job, UwsStorage storage, Exception e, String action, String context){
		UwsJobErrorSummaryMeta error = new UwsJobErrorSummaryMeta(e.getMessage(), UwsErrorType.FATAL);
		if(UwsManager.SYNC_LIST.equals(job.getListid()) && job.getArgs().hasHttpServletResponse()){
			try {
				job.setErrorSummary(error);
			} catch (UwsException e1) {
				//ignore
			}
		}else{
			try{
//				error.setDetailsMimeType("text/plain");
//				UwsStorage storage = service.getFactory().getStorageManager();
//				File f = storage.getJobErrorDetailsFile(job);
//				storage.createJobOutputDataDirIfNecessary(f.getParentFile());
//				PrintWriter pw = new PrintWriter(f);
//				String st = UwsUtils.dumpStackTrace(e);
//				pw.println(st);
//				pw.close();
//				error.setHasDetails(true);
//				job.setErrorSummary(error);
				
				error.setDetailsMimeType(UwsOutputResponseHandler.MIME_TYPE_VOTABLE);
				//UwsStorage storage = service.getFactory().getStorageManager();
				File f = storage.getJobErrorDetailsFile(job);
				storage.createJobOutputDataDirIfNecessary(f.getParentFile());
				
				//OutputStream os = storage.getJobErrorDetailsDataOutputStream(job);
				OutputStream os = new FileOutputStream(f);
				PrintWriter pw = new PrintWriter(os);
				//UwsOutputUtils.writeErrorVoTable(pw, "Executing ADQL", null, e, -1);
				writeErrorVoTable(pw, action, context, e, -1);
				pw.close();
				long size = f.length();
				error.setHasDetails(true);
				error.setDetailsSize(size);
				job.setErrorSummary(error);

			}catch(Exception ex){
				//ignore
			}
		}
	}

	
	/**
	 * Writes an HTML exception/error
	 * @param pw
	 * @param httpErrorCode
	 * @param action
	 * @param context
	 * @param t
	 */
	public static void writeErrorHtml(PrintWriter pw, int httpErrorCode, String action, String context, Throwable t){
		writeErrorStartHtml(pw, UwsTemplates.APPLICATION_ERROR_TITLE, httpErrorCode);
		writeErrorDetailsHtml(pw, action, context, t);
		writeStackTraceHtml(pw, t);
		writeErrorEndHtml(pw);
	}

	
	/**
	 * Writes an HTML exception/error
	 * @param pw
	 * @param httpErrorCode
	 * @param action
	 * @param context
	 * @param message
	 */
	public static void writeErrorHtml(PrintWriter pw, int httpErrorCode, String action, String context, String message){
		UwsOutputUtils.writeErrorStartHtml(pw, UwsTemplates.APPLICATION_ERROR_TITLE, httpErrorCode);
		UwsOutputUtils.writeErrorDetails(pw, action, context, message);
		UwsOutputUtils.writeErrorEndHtml(pw);
	}

	
	/**
	 * Utility to write the initial HTML output
	 * @param pw
	 * @param title
	 * @param httpErrorCode
	 */
	private static void writeErrorStartHtml(PrintWriter pw, String title, int httpErrorCode){
		pw.println(UwsTemplates.HTML_INIT);
		pw.println(UwsTemplates.CSS_CONTENT);
		pw.println(UwsTemplates.formatErrorHeaderTitle(title));
		pw.println(UwsTemplates.formatErrorBodyInit(title, httpErrorCode));
		pw.flush();
	}
	
	/**
	 * Utility to write error details.
	 * @param pw
	 * @param action
	 * @param context
	 * @param t
	 */
	private static void writeErrorDetailsHtml(PrintWriter pw, String action, String context, Throwable t){
		pw.println(UwsTemplates.HTML_DESCRIPTION_INIT);
		if(action != null && !"".equals(action)){
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_ACTION, action));
		}
		if(context != null && !"".equals(context)){
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_CONTEXT, context));
		}
		if(t != null){
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_EXCEPTION, t.getClass().getName()));
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_MESSAGE, t.getMessage()));
		}else{
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_MESSAGE, "Unknown"));
		}
		pw.println(UwsTemplates.HTML_DESCRIPTION_END);
		pw.flush();
	}
	
	/**
	 * Utility to write error details
	 * @param pw
	 * @param action
	 * @param context
	 * @param message
	 */
	private static void writeErrorDetails(PrintWriter pw, String action, String context, String message){
		pw.println(UwsTemplates.HTML_DESCRIPTION_INIT);
		if(action != null && !"".equals(action)){
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_ACTION, action));
		}
		if(context != null && !"".equals(context)){
			pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_CONTEXT, context));
		}
		pw.println(UwsTemplates.formatUlItem(UwsTemplates.ITEM_MESSAGE, message));
		pw.println(UwsTemplates.HTML_DESCRIPTION_END);
		pw.flush();
	}
	
	/**
	 * Utility to write a stack trace
	 * @param pw
	 * @param t
	 */
	private static void writeStackTraceHtml(PrintWriter pw, Throwable t) {
		pw.println(UwsTemplates.HTML_STACK_TRACE_TITLE);
		if(t == null){
			return;
		}
		Throwable cause = t;
		for(;;){
			pw.println(UwsTemplates.HTML_STACK_TRACE_INIT);
			StackTraceElement[] trace = cause.getStackTrace();
			for(int i = 0; i < trace.length; i++){
				pw.println(UwsTemplates.formatStackTraceItem(i, trace[i].getClassName(), trace[i].getMethodName(), trace[i].getLineNumber()));
			}
			pw.println(UwsTemplates.HTML_STACK_TRACE_END);
			cause = cause.getCause();
			if(cause == null){
				break;
			}else{
				pw.println(UwsTemplates.formatStackTraceCause(cause.getClass().getName(), cause.getMessage()));
			}
		}
		pw.flush();
	}

	/**
	 * Utility to write the final HTML
	 * @param pw
	 */
	private static void writeErrorEndHtml(PrintWriter pw){
		pw.println(UwsTemplates.HTML_END);
		pw.flush();
	}
	
	/**
	 * Returns a job list href based on the <code>baseUrl</code>.
	 * <p>I.e.: <code>baseUrl/listid </code>
	 * <p>E.g.: <code>http://host:port/tap-service/tap/async</code> where:<br/>
	 * <ul>
	 * <li>baseUrl = <code>http://host:port/tap-service/tap/</code></li>
	 * <li>listid = <code>async</code></li>
	 * </ul>
	 * @param baseUrl
	 * @param listid
	 * @return
	 */
	public static String getJobListHref(String baseUrl, String listid){
		return baseUrl + "/" + listid;
	}
	

	
	/**
	 * Returns a job href based on the <code>baseUrl</code>.
	 * <p>I.e.: <code>baseUrl/listid/jobid </code>
	 * <p>E.g.: <code>http://host:port/tap-service/tap/async/123456789I</code> where:<br/>
	 * <ul>
	 * <li>baseUrl = <code>http://host:port/tap-service/tap/</code></li>
	 * <li>listid = <code>async</code></li>
	 * <li>jobid = <code>123456789I</code></li>
	 * </ul>
	 * @param baseUrl
	 * @param listid
	 * @param jobid
	 * @see #getJobListHref(String, String)
	 * @return
	 */
	public static String getJobHref(String baseUrl, String listid, String jobid){
		return getJobListHref(baseUrl, listid) + "/" + jobid;
	}
	

	/**
	 * Returns a result href based on the <code>baseUrl</code>.
	 * <p>I.e.: <code>baseUrl/listid/jobid/{@link UwsOutputResponseHandler#RESULTS_URI_NAME}/resultid </code>
	 * <p>E.g.: <code>http://host:port/tap-service/tap/async/123456789I/results/result</code> where:<br/>
	 * <ul>
	 * <li>baseUrl = <code>http://host:port/tap-service/tap/</code></li>
	 * <li>listid = <code>async</code></li>
	 * <li>jobid = <code>123456789I</code></li>
	 * <li>resultid = <code>result</code></li>
	 * </ul>
	 * @param baseUrl
	 * @param listid
	 * @param jobid
	 * @param resultid
	 * @see #getJobHref(String, String, String)
	 * @return
	 */
	public static String getResultHref(String baseUrl, String listid, String jobid, String resultid){
		return getJobHref(baseUrl, listid, jobid) + "/" + UwsOutputResponseHandler.RESULTS_URI_NAME + "/" + resultid;
	}
	
	/**
	 * Returns a error summary href based on the <code>baseUrl</code>.
	 * <p>I.e.: <code>baseUrl/listid/jobid/{@link UwsOutputResponseHandler#ERROR_URI_NAME}/{@link UwsOutputResponseHandler#ERROR_URI_DETAILS} </code>
	 * <p>E.g.: <code>http://host:port/tap-service/tap/async/123456789I/error/details</code> where:<br/>
	 * <ul>
	 * <li>baseUrl = <code>http://host:port/tap-service/tap/</code></li>
	 * <li>listid = <code>async</code></li>
	 * <li>jobid = <code>123456789I</code></li>
	 * </ul>
	 * @param baseUrl
	 * @param listid
	 * @param jobid
	 * @see #getJobHref(String, String, String)
	 * @return
	 */
	public static String getErrorHref(String baseUrl, String listid, String jobid){
		return getJobHref(baseUrl, listid, jobid) + "/" + UwsOutputResponseHandler.ERROR_URI_NAME + "/" + UwsOutputResponseHandler.ERROR_URI_DETAILS;
	}
	

	/**
	 * Dumps from an InputStream into an OutputStream.<br/>
	 * This method does not open/close any stream.<br/>
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void dumpToStream(InputStream is, OutputStream os) throws IOException{
		if(is == null || os == null){
			return;
		}
		byte[] buffer = new byte[4096];
		int read;
		while((read = is.read(buffer)) > 0){
			os.write(buffer, 0, read);
		}
		os.flush();
	}
	
	/**
	 * Utility to write error details.
	 * @param pw
	 * @param action
	 * @param context
	 * @param t
	 * @param httpErrorCode
	 */
	public static void writeErrorVoTable(PrintWriter pw, String action, String context, Throwable t, int httpErrorCode){
		writeErrorStartVoTable(pw);
		writeErrorDetailsVoTable(pw, action, context, t, httpErrorCode);
		writeStackTraceVoTable(pw, t);
		writeErrorEndVoTable(pw);
	}
	
	/**
	 * Utility to write the initial VOTABLE output
	 * @param pw
	 */
	private static void writeErrorStartVoTable(PrintWriter pw){
		pw.println(UwsTemplates.VOTABLE_INIT);
		pw.println(UwsTemplates.VOTABLE_RESOURCE_INIT);
		pw.flush();
	}

	/**
	 * Utility to write the final VOTABLE
	 * @param pw
	 */
	private static void writeErrorEndVoTable(PrintWriter pw){
		pw.println(UwsTemplates.VOTABLE_RESOURCE_END);
		pw.println(UwsTemplates.VOTABLE_END);
		pw.flush();
	}
	
	/**
	 * Utility to write error details.
	 * @param pw
	 * @param action
	 * @param context
	 * @param t
	 * @param httpErrorCode
	 */
	private static void writeErrorDetailsVoTable(PrintWriter pw, String action, String context, Throwable t, int httpErrorCode){
		pw.println(UwsTemplates.formatVoInfoInit(UwsTemplates.ITEM_QUERY_STATUS, UwsTemplates.ERROR));
		if(t != null) {
			pw.println(UwsUtils.escapeText(t.getMessage()));
		}
		pw.println(UwsTemplates.VOTABLE_INFO_END);

		pw.println(UwsTemplates.formatVoInfo(UwsTemplates.ITEM_HTTP_ERROR_CODE, ""+httpErrorCode));
		
		pw.flush();
		
		//extra msgs:
		String tmp;
		if(action != null && !"".equals(action)){
			tmp = UwsUtils.escapeXmlAttribute(action);
			pw.println(UwsTemplates.formatVoInfo(UwsTemplates.ITEM_ACTION, tmp));
		}
		if(context != null && !"".equals(context)){
			tmp = UwsUtils.escapeXmlAttribute(context);
			pw.println(UwsTemplates.formatVoInfo(UwsTemplates.ITEM_CONTEXT, tmp));
		}
		if(t != null){
			pw.println(UwsTemplates.formatVoInfo(UwsTemplates.ITEM_EXCEPTION, t.getClass().getName()));
		}
		
		pw.flush();
	}

	/**
	 * Utility to write a stack trace
	 * @param pw
	 * @param t
	 */
	private static void writeStackTraceVoTable(PrintWriter pw, Throwable t) {
		pw.println(UwsTemplates.formatVoInfoInit(UwsTemplates.ITEM_STACK_TRACE, UwsTemplates.ERROR));
		if(t == null){
			pw.println(UwsTemplates.VOTABLE_INFO_END);
			return;
		}
		Throwable cause = t;
		for(;;){
			//pw.println(UwsTemplates.HTML_STACK_TRACE_INIT);
			StackTraceElement[] trace = cause.getStackTrace();
			for(int i = 0; i < trace.length; i++){
				//pw.println(UwsTemplates.formatStackTraceItem(i, trace[i].getClassName(), trace[i].getMethodName(), trace[i].getLineNumber()));
				pw.println(UwsTemplates.formatStackTraceVoItem(trace[i].getClassName(), trace[i].getMethodName(), trace[i].getLineNumber()));
			}
			//pw.println(UwsTemplates.HTML_STACK_TRACE_END);
			pw.println("");
			cause = cause.getCause();
			if(cause == null){
				break;
			}else{
				//pw.println(UwsTemplates.formatStackTraceCause(cause.getClass().getName(), cause.getMessage()));
				pw.println(UwsTemplates.formatStackTraceVoCause(cause.getClass().getName(), cause.getMessage()));
			}
		}
		pw.println(UwsTemplates.VOTABLE_INFO_END);
		pw.flush();
	}
	
	/**
	 * Utility to write a shared group
	 * @param pw
	 * @param group
	 */
	public static void writeSharedGroup(PrintWriter pw, UwsShareGroup group) {
		String groupid = UwsUtils.escapeXmlAttribute(group.getId());
		String ownerid = UwsUtils.escapeXmlAttribute(group.getCreator());
		String title = UwsUtils.escapeXmlData(group.getTitle());
		String description = UwsUtils.escapeXmlData(group.getDescription());
		String toWrite;
		toWrite = UwsTemplates.formatXmlGroupItem(groupid, ownerid);
		pw.println(toWrite);
		toWrite = UwsTemplates.formatXmlTitle(title);
		pw.println("\t"+toWrite);
		toWrite = UwsTemplates.formatXmlDescription(description);
		pw.println("\t"+toWrite);
		//writeUsers(pw, group.getUsers());
		writeUsersAndNames(pw, group.getUsers());
		pw.println(UwsTemplates.XML_SHARED_GROUP_END);
		pw.flush();
	}
	
//	public static void writeUsers(PrintWriter pw, List<String> users){
//		if(users == null || users.size() < 1){
//			return;
//		}
//		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_INIT);
//		String toWrite;
//		for(String s: users){
//			toWrite = UwsTemplates.formatXmlUser(s);
//			pw.println("\t\t"+toWrite);
//		}
//		pw.flush();
//		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_END);
//	}

	public static void writeUsersAndNames(PrintWriter pw, List<UwsShareUser> users){
		if(users == null || users.size() < 1){
			return;
		}
		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_INIT);
		String toWrite;
		String name;
		for(UwsShareUser s: users){
			name = UwsUtils.escapeXmlAttribute(s.getName());
			toWrite = UwsTemplates.formatXmlUser(s.getId(), name);
			pw.println("\t\t"+toWrite);
		}
		pw.flush();
		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_END);
	}

//	public static void writeSharedUsers(PrintWriter pw, List<UwsShareUser> users){
//		if(users == null || users.size() < 1){
//			return;
//		}
//		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_INIT);
//		String toWrite;
//		for(UwsShareUser s: users){
//			toWrite = UwsTemplates.formatXmlUser(s);
//			pw.println("\t\t"+toWrite);
//		}
//		pw.flush();
//		pw.println("\t" + UwsTemplates.XML_SHARED_USERS_LIST_END);
//	}

//	/**
//	 * Utility to write a shared item
//	 * @param pw
//	 * @param sharedItem
//	 */
//	public static void writeSharedItem(PrintWriter pw, UwsShareItem sharedItem) {
//		String resourceId = UwsUtils.escapeXmlAttribute(sharedItem.getResourceId());
//		String resourceType = UwsUtils.escapeXmlAttribute(""+sharedItem.getResourceType());
//		String shareToId = UwsUtils.escapeXmlAttribute(sharedItem.getShareToId());
//		String shareType = UwsUtils.escapeXmlAttribute(sharedItem.getShareType().name());
//		String shareMode = UwsUtils.escapeXmlAttribute(sharedItem.getShareMode().name());
//		String toWrite;
//		toWrite = UwsTemplates.formatXmlSharedItem(resourceId, resourceType, shareToId, shareType, shareMode);
//		pw.println(toWrite);
//		pw.flush();
//	}

	/**
	 * Utility to write a shared item
	 * @param pw
	 * @param sharedItem
	 */
	public static void writeSharedItems(PrintWriter pw, List<UwsShareItem> sharedItems) {
		if(sharedItems == null || sharedItems.size() < 1){
			return;
		}
		pw.println("\t"+UwsTemplates.XML_SHARED_TO_ITEMS_LIST_INIT);
		String shareToId;
		String shareType;
		String shareMode;
		for(UwsShareItem sharedItem: sharedItems){
			shareToId = UwsUtils.escapeXmlAttribute(sharedItem.getShareToId());
			shareType = UwsUtils.escapeXmlAttribute(sharedItem.getShareType().name());
			shareMode = UwsUtils.escapeXmlAttribute(sharedItem.getShareMode().name());
			String toWrite;
			toWrite = UwsTemplates.formatXmlSharedToItem(shareToId, shareType, shareMode);
			pw.println(toWrite);
		}
		pw.println("\t"+UwsTemplates.XML_SHARED_TO_ITEMS_LIST_END);
		pw.flush();
	}


	/**
	 * Utility to write a shared item description
	 * @param pw
	 * @param sharedItemBase
	 */
	public static void writeSharedItem(PrintWriter pw, UwsShareItemBase sharedItemBase) {
		String resourceId = UwsUtils.escapeXmlAttribute(sharedItemBase.getResourceId());
		String resourceType = UwsUtils.escapeXmlAttribute(""+sharedItemBase.getResourceType());
		String title = UwsUtils.escapeXmlData(sharedItemBase.getTitle());
		String description = UwsUtils.escapeXmlData(sharedItemBase.getDescription());
		String toWrite;
		toWrite = UwsTemplates.formatXmlSharedItem(resourceId, resourceType);
		pw.println(toWrite);
		toWrite = UwsTemplates.formatXmlTitle(title);
		pw.println("\t"+toWrite);
		toWrite = UwsTemplates.formatXmlDescription(description);
		pw.println("\t"+toWrite);
		writeSharedItems(pw, sharedItemBase.getShareToItems());
		pw.println(UwsTemplates.XML_SHARED_ITEM_END);
		pw.flush();
	}

}
