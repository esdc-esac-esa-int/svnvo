package esavo.sl.services.transform.handlers;

import java.io.IOException;
import java.io.InputStream;

/**
 * Functionality that each handler that creates Json from another format must implement.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface TransformHandler {
	
	/**
	 * Parses the input stream.
	 * @param in input stream.
	 * @throws IOException
	 */
	public void parse(InputStream in) throws IOException;
}
