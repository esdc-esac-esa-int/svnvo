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
package esavo.uws.jobs;

import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.utils.UwsErrorType;

public class UwsJobErrorSummaryMeta {

	/** 
	 * <b>[Required]</b> A short description of the error.
	 */
	private String message;

	/** 
	 * <b>[Required]</b> The type of the error.
	 */
	private UwsErrorType type;

	/** 
	 * <i>[Optional]</i> specifies whether this error summary contains details.
	 */
	private boolean hasDetails = false;
	
	
	private String detailsMimeType;
	private long detailsSize;
	
	
	/**
	 * this is used when an exception associated to this job must be raised. In order to obtain the suitable output format.
	 */
	private transient UwsExceptionOutputFormat exceptionOutputFormat;
	
	private transient int httpErrorCode;

	
	
	public int getHttpErrorCode() {
		return httpErrorCode;
	}

	public void setHttpErrorCode(int httpErrorCode) {
		this.httpErrorCode = httpErrorCode;
	}

	public UwsJobErrorSummaryMeta(String message, UwsErrorType type){
		this(message, type, false, null, 0);
	}

	public UwsJobErrorSummaryMeta(String message, UwsErrorType type, boolean hasDetails, String detailsMimeType, long detailsSize){
		this.message = message;
		this.type = type;
		this.hasDetails = hasDetails;
		this.detailsMimeType = detailsMimeType;
		this.detailsSize = detailsSize;
		this.exceptionOutputFormat = UwsExceptionOutputFormat.HTML;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the type
	 */
	public UwsErrorType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(UwsErrorType type) {
		this.type = type;
	}

//	/**
//	 * @return the URL details
//	 */
//	public String getUrlDetails() {
//		return urlDetails;
//	}
//
//	/**
//	 * @param details the URL details to set
//	 */
//	public void setUrlDetails(String urlDetails) {
//		this.urlDetails = urlDetails;
//	}
	
	public boolean hasDetails(){
		//return urlDetails != null && !"".equals(urlDetails.trim());
		return hasDetails;
	}
	
	public void setHasDetails(boolean hasDetails){
		this.hasDetails = hasDetails;
	}

	/**
	 * @return the detailsMimeType
	 */
	public String getDetailsMimeType() {
		return detailsMimeType;
	}

	/**
	 * @param detailsMimeType the detailsMimeType to set
	 */
	public void setDetailsMimeType(String detailsMimeType) {
		this.detailsMimeType = detailsMimeType;
	}

	/**
	 * @return the detailsSize
	 */
	public long getDetailsSize() {
		return detailsSize;
	}

	/**
	 * @param detailsSize the detailsSize to set
	 */
	public void setDetailsSize(long detailsSize) {
		this.detailsSize = detailsSize;
	}
	
	@Override
	public String toString(){
		return "Error: " + message + ", type: " + type.name() + ", has details: " + hasDetails + ", details mime-type: " + detailsMimeType +
				", details size: " + detailsSize;
	}

	/**
	 * @return the exceptionOutputFormat
	 */
	public UwsExceptionOutputFormat getExceptionOutputFormat() {
		return exceptionOutputFormat;
	}

	/**
	 * @param exceptionOutputFormat the exceptionOutputFormat to set
	 */
	public void setExceptionOutputFormat(UwsExceptionOutputFormat exceptionOutputFormat) {
		this.exceptionOutputFormat = exceptionOutputFormat;
	}

}
