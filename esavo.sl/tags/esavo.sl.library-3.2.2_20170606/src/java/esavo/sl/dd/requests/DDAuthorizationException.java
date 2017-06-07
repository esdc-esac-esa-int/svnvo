package esavo.sl.dd.requests;

public class DDAuthorizationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
     * Constructor
     */
    public DDAuthorizationException() {
    	super();
    }
    
    /**
     * Constructor
     *
     * @param message
     */
    public DDAuthorizationException(String message) {
    	super(message);
    }
    
    /**
     * Constructor
     *
     * @param cause
     */
    public DDAuthorizationException(Throwable cause) {
        super(cause);
    }    

    /**
     * Constructor
     *
     * @param message
     * @param cause
     */
    public DDAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

}
