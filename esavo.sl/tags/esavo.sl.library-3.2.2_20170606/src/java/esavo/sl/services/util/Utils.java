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
package esavo.sl.services.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.security.InvalidParameterException;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class Utils {
	
	
	/**
	 * Checks corresponding to a table upload.
	 * @throws InvalidParameterException if the owner is null or user name is null
	 */
	public static void checkAuthentication(UwsJobOwner owner) throws InvalidParameterException{
		if (owner == null || owner.getAuthUsername() == null || UwsUtils.isAnonymous(owner.getId())){
			throw new InvalidParameterException("User must be logged in to perform this action.");
		}
	}
	
	
	/**
	 * Returns an string with the stack trace.
	 * @param t
	 * @return an string with the stack trace.
	 */
	public static String dumpStackTrace (Throwable t){
		if(t == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(StackTraceElement ste: t.getStackTrace()){
			sb.append(ste).append("\n");
		}
		return sb.toString();
	}
	

	/**
	 * Writes a simple JSON message: <code>{ "id": "value" }</code>
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param id id keyword.
	 * @param explanation value.
	 * @throws IOException
	 */
	public static void writeMsg(HttpServletResponse response, int httpErrorCode, String id, String explanation) throws IOException{
		response.setStatus(httpErrorCode);
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println(BasicTemplates.getSimpleJsonMsg(id, explanation));
		pw.flush();
	}
	
	/**
	 * Writes an error message (HTML).
	 * @param genericErrorMsg generic error msg.
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param error error message.
	 * @param extraMsg extra message.
	 * @throws IOException
	 */
	public static void writeError(String genericErrorMsg, HttpServletResponse response, int httpErrorCode, String error, String extraMsg) throws IOException{
		response.setStatus(httpErrorCode);
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println(BasicTemplates.getErrorMessage(genericErrorMsg, error, extraMsg));
		pw.flush();
	}
	
	/**
	 * Writes an error (HTML) from an exception.
	 * @param genericErrorMsg generic error msg.
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param t exception (can be null).
	 * @throws IOException
	 */
	public static void writeError(String genericErrorMsg, HttpServletResponse response, int httpErrorCode, Throwable t) throws IOException {
		response.setStatus(httpErrorCode);
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		if(t == null){
			pw.println(BasicTemplates.getErrorMessage(genericErrorMsg, "Unknown error", null));
		}else{
			pw.println(BasicTemplates.getErrorMessage(genericErrorMsg, t.getMessage(), BasicTemplates.getPlainText(dumpStackTrace(t))));
		}
		pw.flush();
	}

	/**
	 * Writes an error (HTML) from an exception using the 'error' parameter also.
	 * @param genericErrorMsg generic error msg.
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param error error message.
	 * @param t excpetion (can be null).
	 * @throws IOException
	 */
	public static void writeError(String genericErrorMsg, HttpServletResponse response, int httpErrorCode, String error, Throwable t) throws IOException{
		response.setStatus(httpErrorCode);
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		if(t == null){
			pw.println(BasicTemplates.getErrorMessage(genericErrorMsg, error, null));
		}else{
			pw.println(BasicTemplates.getErrorMessage(genericErrorMsg, error + "<br/>" + t.getMessage(), BasicTemplates.getPlainText(dumpStackTrace(t))));
		}
		pw.flush();
	}

	
	public static InputStream decompressStream(InputStream input)
			throws IOException {
		PushbackInputStream pb = new PushbackInputStream(input, 2);
		byte[] signature = new byte[2];
		pb.read(signature); // read the signature
		pb.unread(signature); // push back the signature to the stream

		// check if matches standard gzip magic number
		//if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {
		if ((signature[0] == (byte)(GZIPInputStream.GZIP_MAGIC & 0xFF)) && 
				(signature[1] == (byte) ((GZIPInputStream.GZIP_MAGIC >> 8) & 0xFF))) {
			return new GZIPInputStream(pb);
		} else {
			return pb;
		}
	}

	/**
	 * Generate a valid Postgres column name from the given string.
	 * @param columnName
	 * @return
	 */
	public static String getAdqlPostgresProperColumnName(String columnName){

		//1st substitute everything to lowercase and characters not being Letter/Number/Underscore by underscore
		String formatted =  columnName.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "_");
		
		//2nd Add "col" at the beginning if the first character is not valid
		if(formatted.matches("^[^a-zA-Z].*$")){
			formatted="col"+formatted;
		}
		
		return formatted;
		
	}
}
