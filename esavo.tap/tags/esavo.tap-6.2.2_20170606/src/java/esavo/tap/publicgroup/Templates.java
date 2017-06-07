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
package esavo.tap.publicgroup;

import java.text.MessageFormat;

import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.utils.UwsJobDetails;
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
	
	public static final String APPLICATION_TITLE = "PublicGroup";
	public static final String APPLICATION_ERROR_TITLE = "SERVICE ERROR";
	
	public static final String CSS = "./tap_manager.css";
	
	private static final String LINK_CSS = "<link rel=\"stylesheet\" type=\"text/css\" href=\""+CSS+"\">";
	
	/**
	 * <ul>
	 * <li>0: header title</li>
	 * <li>1: extra head</li>
	 * <li>2: body</li>
	 * </ul>
	 */
	public static final String BASE = 
			"<html>\n"+
			"<head><title>{0}</title>\n"+LINK_CSS+"\n{1}</head>\n"+
			"<body>\n{2}</body>"+
			"</html>";

	/**
	 * <ul>
	 * <li>0: title</li>
	 * <li>1: body</li>
	 * </ul>
	 */
	public static final String ERROR_BODY = 
			"<table class=\"error-table\">\n"+
			"<tr>\n<td class=\"error-table-header\">{0}</td></tr>\n"+
			"<tr>\n<td class=\"error-table-body\">{1}</td></tr>\n"+
			"</table>";
	
	
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
	 * JSON format: <code>{"id": "userIdentifier", "roles": "userRoles", "quota_db": "userQuotaDB", "current_size_db": "userCurrentSizeDB", "quota_files": "userQuotaFiles", "current_size_files": "userCurrentSizeFiles"}</code>
	 * <ul>
	 * <li>0: user id (string)</li>
	 * <li>1: roles (int)</li>
	 * <li>2: quota db (long)</li>
	 * <li>3: current size db (long)</li>
	 * <li>4: quota files (long)</li>
	 * <li>5: current size files (long)</li>
	 * </ul>
	 */
	public static final String JSON_TABLES_LIST_RECORD = 
			"'{'\"schema_name\": \"{0}\", \"table_name\": \"{1}\", \"table_owner\": \"{2}\"'}'";
	
	
	

	/**
	 * Returns a basic HTML page (see {@link #getHtml(String, String)}})
	 * @param headerTitle header title
	 * @param body HTML body.
	 * @return a basic HTML page.
	 */
	public static String getHtml(String headerTitle, String body){
		return getHtml(headerTitle, null, body);
	}
	
	/**
	 * Returns a basic HTML page (base on the format specified by {@link #BASE})
	 * @param extraHeader header to be added to the default header.
	 * @param headerTitle header title
	 * @param body HTML body.
	 * @return a basic HTML page.
	 */
	public static String getHtml(String headerTitle, String extraHeader, String body){
		if(extraHeader == null){
			extraHeader = "";
		}
		if(body == null){
			body = "";
		}
		if(headerTitle == null){
			headerTitle = "";
		}
		return MessageFormat.format(BASE, headerTitle, extraHeader, body);
	}
	
	/**
	 * Returns a basic HTML error page (base on the format specified by {@link #ERROR_BODY})
	 * @param errorTitle error title.
	 * @param errorMsg error explanation.
	 * @param extraMsg error extra info.
	 * @return a basic HTML error page.
	 */
	public static String getErrorMessage(String errorTitle, String errorMsg, String extraMsg){
		String msg;
		if(extraMsg != null){
			msg = errorMsg + "<br/>" + extraMsg;
		}else{
			msg = errorMsg;
		}
		String htmlBody = MessageFormat.format(ERROR_BODY, errorTitle, msg);
		return getHtml(APPLICATION_ERROR_TITLE, htmlBody);
	}
	
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
	 * Returns a table list row.
	 * @param item
	 * @return
	 */
	public static String getTableListRow(PublicGroupItem item) {
		return MessageFormat.format(JSON_TABLES_LIST_RECORD, item.getTableSchemaName(), item.getTableName(), item.getOwner());
	}
	
}
