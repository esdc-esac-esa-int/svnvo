/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
