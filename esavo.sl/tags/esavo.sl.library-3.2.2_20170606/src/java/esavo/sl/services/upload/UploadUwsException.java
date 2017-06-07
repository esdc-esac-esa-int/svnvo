package esavo.sl.services.upload;

import java.io.IOException;

public class UploadUwsException extends IOException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadUwsException( String message ) {
        super( message );
    }

    public UploadUwsException() {
        super();
    }

    public UploadUwsException( String message, Throwable cause ) {
        super( message );
        initCause( cause );
    }

    public UploadUwsException( Throwable cause ) {
        super(cause != null ? cause.getMessage() : "");
        initCause( cause );
    }
}
