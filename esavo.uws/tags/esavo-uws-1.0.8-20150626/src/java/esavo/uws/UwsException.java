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
package esavo.uws;

import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;

/**
 * UWS main exception class.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int code;
	
	private UwsExceptionOutputFormat outputFormat;
	
	/**
	 * Constructor.<br/>
	 * The default output format is HTML<br/>
	 * The default code is -1 (must be handled properly: see {@link UwsOutputResponseHandler#writeServerErrorResponse(javax.servlet.http.HttpServletResponse, String, UwsException)}).<br/>
	 * @param msg exception message.
	 */
	public UwsException(String msg){
		this(-1, msg, null, UwsExceptionOutputFormat.HTML);
	}
	
	/**
	 * Constructor<br/>
	 * The default output format is HTML<br/>
	 * The default code is -1 (must be handled properly: see {@link UwsOutputResponseHandler#writeServerErrorResponse(javax.servlet.http.HttpServletResponse, String, UwsException)}).<br/>
	 * @param source original exception
	 */
	public UwsException(Exception source){
		this(-1, source.getMessage(), source, UwsExceptionOutputFormat.HTML);
	}
	
	/**
	 * Constructor<br/>
	 * The default output format is HTML<br/>
	 * @param code this code will be used as HTTP error code. See constants in {@link UwsOutputResponseHandler}
	 * @param msg exception message.
	 */
	public UwsException(int code, String msg){
		this(code, msg, null, UwsExceptionOutputFormat.HTML);
	}
	
	/**
	 * Constructor
	 * @param code this code will be used as HTTP error code. See constants in {@link UwsOutputResponseHandler}
	 * @param msg exception message.
	 * @param outputFormat requested output format. See formats in {@link UwsExceptionOutputFormat}
	 */
	public UwsException(int code, String msg, UwsExceptionOutputFormat outputFormat){
		this(code, msg, null, outputFormat);
	}
	
	/**
	 * Constructor<br/>
	 * The default output format is HTML<br/>
	 * The default code is -1 (must be handled properly: see {@link UwsOutputResponseHandler#writeServerErrorResponse(javax.servlet.http.HttpServletResponse, String, UwsException)}).<br/>
	 * @param msg exception message.
	 * @param source original exception.
	 */
	public UwsException(String msg, Exception source){
		this(-1, msg, source, UwsExceptionOutputFormat.HTML);
	}

	/**
	 * Constructor.
	 * @param msg exception message.
	 * @param source original exception.
	 * @param outputFormat requested output format. See formats in {@link UwsExceptionOutputFormat}
	 */
	public UwsException(String msg, Exception source, UwsExceptionOutputFormat outputFormat){
		this(-1, msg, source, outputFormat);
	}

	/**
	 * Constructor.<br/>
	 * The default output format is HTML<br/>
	 * @param code this code will be used as HTTP error code. See constants in {@link UwsOutputResponseHandler}
	 * @param msg exception message.
	 * @param source original exception.
	 */
	public UwsException(int code, String msg, Exception source){
		this(code, msg, source, UwsExceptionOutputFormat.HTML);
	}
	
	/**
	 * Constructor.
	 * @param code this code will be used as HTTP error code. See constants in {@link UwsOutputResponseHandler}
	 * @param msg exception message.
	 * @param source original exception.
	 * @param outputFormat requested output format. See formats in {@link UwsExceptionOutputFormat}
	 */
	public UwsException(int code, String msg, Exception source, UwsExceptionOutputFormat outputFormat){
		super(msg, source);
		this.code = code;
		this.outputFormat = outputFormat;
	}
	
	@Override
	public String toString(){
		String msg = getMessage();
		Throwable source = getCause();
		return "Code: " + code + ", msg: " + msg + (source != null ? "\nSource: " + source.getMessage() :  "");
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Returns the output format. By default: HTML
	 * @return
	 */
	public UwsExceptionOutputFormat getOutputFormat(){
		return outputFormat;
	}
}
