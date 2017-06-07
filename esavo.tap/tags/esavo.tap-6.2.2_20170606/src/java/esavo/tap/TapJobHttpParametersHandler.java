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
package esavo.tap;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.FileRenamePolicy;

import esavo.tap.parameters.TAPParameters;
import esavo.uws.UwsException;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.UwsJobHttpParametersHandler;
import esavo.uws.actions.UwsJobParametersItems;
import esavo.uws.actions.UwsUploadResource;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.utils.UwsUtils;

public class TapJobHttpParametersHandler implements UwsJobHttpParametersHandler {
	public static final String PARAM_UPLOAD = "upload";

	public static String[] SUPPORTED_TYPES = {
		UwsActionRequest.CONTENT_TYPE_FORM_URLENCODED,
		UwsActionRequest.CONTENT_TYPE_MULTIPART
	};

//	private MultipartRequest multipart;
//	private UwsJobParameters jobParameters;
//	private UwsUploadResource[] uploadResourceLoaders;

	public TapJobHttpParametersHandler(){
		//uploadResourceLoaders = null;
	}
	
	
	
	@Override
	public UwsJobParametersItems parse(HttpServletRequest request, File uploadDir, int maxFileSize) throws UwsException {
//		uploadResourceLoaders = null;
//		multipart = null;
//		jobParameters = createJobParametersAsStrings(request, uploadDir, maxFileSize);
//		return createJobParametersAsStrings(request, uploadDir, maxFileSize);
		if (UwsActionRequest.isMultipartContent(request)){
			// Multipart HTTP parameters:
			return createMultipartParameters(request, uploadDir, maxFileSize);
		} else {
			// Classic HTTP parameters (GET or POST):
			// Extract and identify each pair (key,value):
			return createParameters(request);
		}
	}

//	@Override
//	public UwsJobParameters getParameters() {
//		return jobParameters;
//	}
//
//	@Override
//	public Map<String, String> getParameterTypes() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public UwsUploadResource[] getUploadedResources() {
//		return uploadResourceLoaders;
//	}

	
	
//	private UwsJobParameters createJobParametersAsStrings(HttpServletRequest request, File uploadDir, int maxFileSize) throws UwsException {
//		//clear mulipart information
//		multipart = null;
//		
//		if (UwsActionRequest.isMultipartContent(request)){
//			// Multipart HTTP parameters:
//			return createMultipartParameters(request, uploadDir, maxFileSize);
//		} else {
//			// Classic HTTP parameters (GET or POST):
//			// Extract and identify each pair (key,value):
//			return createParameters(request);
//		}
//		
//	}

	/**
	 * All parameters name are converted to lowercase.
	 * @return
	 * @throws UwsException 
	 */
	private UwsJobParametersItems createParameters(HttpServletRequest request) throws UwsException{
		UwsJobParametersItems items = new UwsJobParametersItems();
		UwsJobParameters parameters = new UwsJobParameters();
	
		@SuppressWarnings("unchecked")
		Enumeration<String> e = request.getParameterNames();
		String name;
		while(e.hasMoreElements()){
			name = e.nextElement();
			parameters.setParameter(name.toLowerCase(), request.getParameter(name));
		}
		
		addJobTypeParameterIfNeeded(parameters);
		
		items.setJobParameters(parameters);
		return items;
	}


	private UwsJobParametersItems createMultipartParameters(HttpServletRequest request, File uploadDir, int maxUploadSize) throws UwsException {
		UwsJobParametersItems items = new UwsJobParametersItems();
		UwsJobParameters parameters = new UwsJobParameters();
		MultipartRequest multipart = null;
	
		if(!uploadDir.exists()){
			uploadDir.mkdirs();
		}
	
		try{
			multipart = new MultipartRequest(request, uploadDir.getAbsolutePath(), maxUploadSize, new FileRenamePolicy() {
				@Override
				public File rename(File file) {
					File parent = file.getParentFile();
					String name = UwsUtils.getUniqueIdentifier()+"_"+file.getName();
					return new File(parent, name);
				}
			});
			
			//Parameter names are converted to lowercase
			@SuppressWarnings("unchecked")
			Enumeration<String> e = multipart.getParameterNames();
			while(e.hasMoreElements()){
				String param = e.nextElement();
				parameters.setParameter(param.toLowerCase(), multipart.getParameter(param));
			}
		}catch(IOException ioe){
			removeMultipartFiles(multipart);
			throw new UwsException("Error while reading the Multipart content !", ioe);
		}
		
		UwsUploadResource[] uploadResourceLoaders = null;
	
		// Identify the tables to upload, if any:
		if(parameters.containsParameter(PARAM_UPLOAD)){
			String uploadParam = parameters.getStringParameter(PARAM_UPLOAD);
			uploadResourceLoaders = buildLoaders(uploadParam, multipart);
		}
		
		addJobTypeParameterIfNeeded(parameters);
		
		items.setJobParameters(parameters);
		items.setMultipart(multipart);
		items.setUploadResourceLoaders(uploadResourceLoaders);

		return items;
	}

	private void removeMultipartFiles(final MultipartRequest multipart){
		if(multipart == null){
			return;
		}
		@SuppressWarnings("unchecked")
		Enumeration<String> enumeration = multipart.getFileNames();
		File f;
		while (enumeration.hasMoreElements()) {
			String fileName = enumeration.nextElement();
			f = multipart.getFile(fileName);
			f.delete();
		}
	}
	
	private UwsUploadResource[] buildLoaders(final String upload, final MultipartRequest multipart) throws UwsException {
		if (upload == null || upload.trim().isEmpty()){
			return null;
		}
	
		String[] pairs = upload.split(";");
		UwsUploadResource[] loaders = new UwsUploadResource[pairs.length];
	
		for(int i=0; i<pairs.length; i++){
			String[] table = pairs[i].split(",");
			if (table.length != 2){
				throw new UwsException("Bad syntax ! An UPLOAD parameter must contain a list of pairs separated by a ';'. " +
						"Each pair is composed of 2 parts, a table name and a URI separated by a ','.");
			}
			loaders[i] = new TapUploadResource(table[0], table[1], multipart);
		}
	
		return loaders;
	}

	@Override
	public String[] getSupportedContentTypes() {
		return SUPPORTED_TYPES;
	}
	
	/**
	 *  If query is crossmatch and jobType parameter has not been set, set it to XMATCH
	 * @param jobParameters
	 * @throws UwsException
	 */
	private void addJobTypeParameterIfNeeded(UwsJobParameters jobParameters) throws UwsException{
		if(!jobParameters.containsParameter(TAPParameters.PARAM_JOB_TYPE) && jobParameters.containsParameter(TAPParameters.PARAM_QUERY)){
			String query = jobParameters.getStringParameter(TAPParameters.PARAM_QUERY);
			if(query!=null && query.trim().toLowerCase().contains(TAPService.TAP_ADQL_FUNCTION_XMATCH+"(")){
				jobParameters.setParameter(TAPParameters.PARAM_JOB_TYPE, "XMATCH");
			}
		}
	}

}
