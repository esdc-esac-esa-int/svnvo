package esavo.uws.jobs;

public class UwsJobResultMeta {
	
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
}
