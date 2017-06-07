package esavo.uws.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.FileRenamePolicy;

import esavo.uws.UwsException;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.utils.UwsUtils;

public class UwsJobDefaultHttpParametersHandler implements UwsJobHttpParametersHandler {

	public static String[] SUPPORTED_TYPES = {
		UwsActionRequest.CONTENT_TYPE_FORM_URLENCODED,
		UwsActionRequest.CONTENT_TYPE_MULTIPART
	};


	public UwsJobDefaultHttpParametersHandler(){
	}
	
	@Override
	public UwsJobParametersItems parse(HttpServletRequest request, File uploadDir, int maxFileSize) throws UwsException {
		//return createJobParametersAsStrings(request, uploadDir, maxFileSize);
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
	
	
//	private UwsJobParametersItems createJobParametersAsStrings(HttpServletRequest request, File uploadDir, int maxFileSize) throws UwsException {
//		//clear mulipart information
//		
//		if (UwsActionRequest.isMultipartContent(request)){
//			// Multipart HTTP parameters:
//			return createMultipartParameters(request, uploadDir, maxFileSize);
//		} else {
//			// Classic HTTP parameters (GET or POST):
//			// Extract and identify each pair (key,value):
//			return createParameters(request);
//		}
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
	
		// Identify the tables to upload, if any:
		UwsUploadResource[] uploadResourceLoaders = buildLoaders(multipart);
		
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
	
	private UwsUploadResource[] buildLoaders(final MultipartRequest multipart) throws UwsException {
		List<UwsUploadResource> builders = new ArrayList<UwsUploadResource>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> enumeration = multipart.getFileNames();
		
		while (enumeration.hasMoreElements()) {
			String fileName = enumeration.nextElement();
			File file = multipart.getFile(fileName);
			UwsUploadResource uploadResource = new UwsUploadResourceLoader(fileName, file);
			builders.add(uploadResource);
		}

		UwsUploadResource[] loaders = builders.toArray(new UwsUploadResource[builders.size()]);
		
		return loaders;
	}

	@Override
	public String[] getSupportedContentTypes() {
		return SUPPORTED_TYPES;
	}
	
		
}
