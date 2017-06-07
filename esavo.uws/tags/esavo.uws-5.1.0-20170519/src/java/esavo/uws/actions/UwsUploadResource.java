package esavo.uws.actions;

import java.io.IOException;
import java.io.InputStream;

public interface UwsUploadResource {
	
	public InputStream openStream() throws IOException;
	
	public boolean deleteFile();
	
	public String getName();

}
