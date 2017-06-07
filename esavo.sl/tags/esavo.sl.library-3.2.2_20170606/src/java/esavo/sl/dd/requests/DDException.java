package esavo.sl.dd.requests;

public class DDException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	
	public DDException(int code, String msg){
		super(msg);
		this.code = code;
	}
	
	public DDException(int code, String msg, Exception cause){
		super(msg, cause);
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

}
