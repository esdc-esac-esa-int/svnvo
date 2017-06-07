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

public class UwsJobResultMeta implements Cloneable{
	
	public static final String DEFAULT_IDENTIFIER = "result";
	
	/**
	 * XLINK URL type.<br/>
	 * see http://www.w3.org/TR/xlink/#linking-elements for more details
	 *
	 */
	public enum ResultType{
		simple,
		extended,
		locator,
		arc,
		resource,
		title,
		NONE
	}
	
	/** 
	 * <b>[Required ; Default="{@link #DEFAULT_IDENTIFIER}"]</b> Name or ID of this result.
	 */
	private String id = DEFAULT_IDENTIFIER;

	/** 
	 * <i>[Optional]</i> The XLINK URL type (default: simple).
	 */
	private ResultType type = ResultType.simple;

	/** 
	 * <i>[Optional]</i> The MIME type of the result.
	 */
	private String mimeType = null;

	/** 
	 * <i>[Optional]</i> The File Extension of the result.
	 */
	private String fileExtension = null;

	/** 
	 * <i>[Optional]</i> The Encoding of the result.
	 */
	private String contentEncoding = null;


	/** 
	 * <i>[Optional]</i> The size of the corresponding result file. 
	 */
	private long size = -1;

	/** 
	 * <i>[Optional]</i> The number of rows when applicable.
	 */
	private long rows = -1;
	
	/**
	 * Constructor
	 * @param id result identifier
	 */
	public UwsJobResultMeta(String id){
		this.id = id;
	}
	
	/**
	 * @return the type
	 */
	public ResultType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(ResultType type) {
		this.type = type;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param mimeType the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the contentEncoding
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	/**
	 * @param content encoding of the result
	 */
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * @return the rows
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(long rows) {
		this.rows = rows;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the XLINK URL type. (see http://www.w3.org/TR/xlink/#linking-elements for more details)
	 * If the 'type' attribute is 'NONE', a null value is returned. Otherwise, the string representation of type is returned.
	 * @return
	 */
	public String getXlinkHrefType(){
		if(type == ResultType.NONE){
			return null;
		}else{
			return type.name();
		}
	}

	@Override
	public String toString(){
		return "Result: " + id + ", type: " + type + ", mime-type: " + mimeType + ", size: " + size + ", rows: " + rows;
	}
	
	@Override
	public UwsJobResultMeta clone(){
		UwsJobResultMeta copy = new UwsJobResultMeta(this.id);
		copy.setContentEncoding(this.contentEncoding);
		copy.setFileExtension(this.fileExtension);
		copy.setMimeType(this.mimeType);
		copy.setRows(this.rows);
		copy.setSize(this.size);
		copy.setType(this.type);
		
		return copy;
	}
}
