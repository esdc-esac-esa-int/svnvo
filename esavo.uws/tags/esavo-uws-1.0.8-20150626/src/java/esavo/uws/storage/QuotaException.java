package esavo.uws.storage;

import java.io.IOException;

public class QuotaException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuotaException(String message){
		super(message);
	}
	
	public QuotaException(Throwable cause){
		super(cause);
	}
}
