package esavo.uws.actions;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import esavo.uws.UwsException;
import esavo.uws.jobs.parameters.UwsJobParameters;

public interface UwsJobHttpParametersHandler {
	
	/**
	 * Returns a list of supported content-types
	 * @return
	 */
	public String[] getSupportedContentTypes();
	
	/**
	 * Parse parameters. It must populate a map of parameter_id/parameter_type + uploaded files
	 * @param request
	 * @param uploadDir
	 * @param maxFileSize
	 * @return
	 * @throws UwsException
	 */
	public UwsJobParametersItems parse(HttpServletRequest request, File uploadDir, int maxFileSize) throws UwsException;
	
//	/**
//	 * Get parameter key/value
//	 * @return
//	 */
//	public UwsJobParameters getParameters();
//	
//	/**
//	 * Get parameter key/type
//	 * @return
//	 */
//	public Map<String, String> getParameterTypes();
//	
//	/**
//	 * Get uploaded files
//	 * @return
//	 */
//	public UwsUploadResource[] getUploadedResources();

}
