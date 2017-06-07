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
package esavo.sl.dd.requests;

import java.util.List;

import esavo.sl.dd.util.DDFilePath;

public class DDRequestResult {
	
	private List<DDFilePath> filePathList;
	private int statusCode;
	private String message;
	private String reqid;
	private String username;
	private long totalSize;
	private boolean compressionRequested;
	private int propStatus;
	
	public DDRequestResult(String reqid, List<DDFilePath> filePathList){
		this.reqid = reqid;
		this.filePathList = filePathList;
	}

	/**
	 * @return the urls
	 */
	public List<DDFilePath> getFilePaths() {
		return filePathList;
	}
	
	public DDFilePath getFirsFilePath(){
		if(filePathList.size() < 1){
			return null;
		}else{
			return filePathList.iterator().next();
		}
	}
	
	public int getNumberOfResults(){
		return filePathList.size();
	}

	/**
	 * @return the contentType
	 */
	public String getFirstContentType() {
		DDFilePath item = getFirsFilePath();
		if(item != null){
			return item.getContentType();
		}else{
			return null;
		}
	}

	/**
	 * @return the compressed
	 */
	public boolean isFirstCompressed() {
		DDFilePath item = getFirsFilePath();
		if(item != null){
			return item.isCompressed();
		}else{
			return false;
		}
	}

	/**
	 * @return the reqid
	 */
	public String getReqid() {
		return reqid;
	}

	/**
	 * @param reqid the reqid to set
	 */
	public void setReqid(String reqid) {
		this.reqid = reqid;
	}

	/**
	 * @return the username
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUserName(String username) {
		this.username = username;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param errorCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param errorMessage the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the totalSize
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * @param totalSize the totalSize to set
	 */
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	/**
	 * @return the compressionRequested
	 */
	public boolean isCompressionRequested() {
		return compressionRequested;
	}

	/**
	 * @param compressionRequested the compressionRequested to set
	 */
	public void setCompressionRequested(boolean compressionRequested) {
		this.compressionRequested = compressionRequested;
	}

	/**
	 * @return the propStatus
	 */
	public int getPropStatus() {
		return propStatus;
	}

	/**
	 * @param propStatus the propStatus to set
	 */
	public void setPropStatus(int propStatus) {
		this.propStatus = propStatus;
	}

}
