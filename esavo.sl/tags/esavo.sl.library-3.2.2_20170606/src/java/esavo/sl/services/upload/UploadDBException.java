package esavo.sl.services.upload;

import java.io.IOException;

public class UploadDBException extends IOException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadDBException( String message ) {
        super( message );
    }

    public UploadDBException() {
        super();
    }

    public UploadDBException( String message, Throwable cause ) {
        super( message );
        initCause( cause );
    }

    public UploadDBException( Throwable cause ) {
        super();
        initCause( cause );
    }
}
