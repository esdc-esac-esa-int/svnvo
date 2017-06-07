package esavo.sl.services.transform.handlers;

import java.io.IOException;

/**
 * This class is used to specify an end of processing.<br/>
 * When the user specifies a page size (i.e. a number of rows to dump) it is necessary to tell the parser to stop processing the data.
 * Nevertheless, parsers do not have a method to request to stop processing.
 * This exception is raised by the handlers so the high level functionality can determine whether a real exception has been raised or
 * a stop processing is requested. 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TransformEndProcessException extends IOException{

	private static final long serialVersionUID = 1L;

}
