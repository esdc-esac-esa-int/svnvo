package esavo.tap.metadata;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.metadata.TAPMetadata.OutputType;

public interface TAPMetadataWriter {
	
	

	public boolean writeMetadata(Collection<TAPSchema> schemas, HttpServletResponse response, OutputType outputType, boolean includeShareInfo ) throws ServletException, IOException;
	public String getFormat();
}
