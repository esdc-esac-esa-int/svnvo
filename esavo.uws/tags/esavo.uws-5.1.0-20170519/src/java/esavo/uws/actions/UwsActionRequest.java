package esavo.uws.actions;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.utils.UwsUtils;

/**
 * A request must have the following format:
 * <ul>
 * <li><code>/{job_list}</code>: returns all jobs of the specified list. E.g. <code>http://localhost:8080/servlet_context/servlet_name/job_list</code>
 * <li><code>/{job_list}/{job_id}</code>: returns the job metadata that belongs to 'job_id' under 'job_list'. E.g. <code>http://localhost:8080/servlet_context/servlet_name/job_list/123456789A</code>
 * <li><code>/<ACTION>/</code>: this is not a job list. This identifier is reserved for non Job related actions. Each handler defines the ACTION name and if it should be treated as non-job action</li>
 * 
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsActionRequest {

	public static final String MULTIPART = "multipart/";
	
	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	
	public enum UwsActionMethod{
		GET,
		POST,
		DELETE,
		PUT
	}
	
	public enum UwsActionProtocolPreference {
		UseSource,
		Http,
		Https
	}
	
	private HttpServletRequest request;
	
	private String jobListName;
	private String jobid;
	private String actionName;
	private String subActionName;
	private UwsActionMethod method;
	//private MultipartRequest multipart;
	private UwsUploadResource[] uploadResourceLoaders;
	private List<String> parametersToIgnore;
	private List<String> noJobActions;
	private String appid;
	private Map<String, UwsJobHttpParametersHandler> httpJobParameterHandlers;
	private UwsJobDefaultHttpParametersHandler defaultJobHttpParametersHandler;
	private UwsConfiguration configuration;
	
	public UwsActionRequest(String appid, UwsConfiguration configuration, HttpServletRequest request, List<String> noJobActions, List<String> parametersToIgnore){
		this.appid = appid;
		this.request = request;
		this.noJobActions=noJobActions;
		this.parametersToIgnore = parametersToIgnore;
		this.configuration = configuration;
		this.httpJobParameterHandlers = new HashMap<String, UwsJobHttpParametersHandler>();
		this.defaultJobHttpParametersHandler = new UwsJobDefaultHttpParametersHandler();
		//register default http parameters handler
		registerJobHttpParameterHandler(defaultJobHttpParametersHandler);
		parseRequest();
		this.method = extractMethod();
	}
	
	public HttpSession getSession(){
		return request.getSession();
	}

	public void registerJobHttpParametersHandler(List<UwsJobHttpParametersHandler> handlers){
		if(handlers == null){
			return;
		}
		for(UwsJobHttpParametersHandler handler: handlers){
			registerJobHttpParameterHandler(handler);
		}
	}
	
	public void registerJobHttpParameterHandler(UwsJobHttpParametersHandler handler){
		if(handler == null){
			return;
		}
		String[] supportedContentTypes = handler.getSupportedContentTypes();
		if(supportedContentTypes == null){
			return;
		}
		for(String contentType: supportedContentTypes){
			registerJobHttpParametersHandler(contentType, handler);
		}
	}
	
	public void registerJobHttpParametersHandler(String contentType, UwsJobHttpParametersHandler handler){
		httpJobParameterHandlers.put(contentType, handler);
	}
	
	/**
	 * Extract parameters from request.
	 * @throws IOException
	 */
	private void parseRequest() {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null){
			//throw new IllegalArgumentException("No action found");
			return;
		}
		String normalizedUri = UwsUtils.normalizeURI(pathInfo);
		String data = normalizedUri.substring(1); //skip initial '/'
		String items[] = data.split("/");
		if(items.length < 1){
			throw new IllegalArgumentException("Invalid request. Wrong request. Missing main parameters");
		}
		jobListName = items[0];
		if(items.length > 1){
			jobid = items[1];
		}
		if(items.length > 2){
			actionName = items[2];
		}
		if(items.length > 3){
			subActionName = items[3];
		}
	}
	
	private UwsActionMethod extractMethod(){
		String method = request.getMethod();
		if("post".equalsIgnoreCase(method)){
			return UwsActionMethod.POST;
		} else if ("get".equalsIgnoreCase(method)){
			return UwsActionMethod.GET;
		} else if ("delete".equalsIgnoreCase(method)){
			return UwsActionMethod.DELETE;
		} else if ("put".equalsIgnoreCase(method)){
			return UwsActionMethod.PUT;
		} else {
			throw new IllegalArgumentException("Invalid HTTP method: " + method);
		}
	}
	
	public boolean isPost(){
		return method == UwsActionMethod.POST;
	}
	
	public boolean isDelete(){
		return method == UwsActionMethod.DELETE;
	}
	
	public boolean isGet(){
		return method == UwsActionMethod.GET;
	}
	
	public boolean isPut(){
		return method == UwsActionMethod.PUT;
	}
	
	public boolean hasHttpParameters(){
		@SuppressWarnings("rawtypes")
		Map m = request.getParameterMap();
		if(m == null){
			return false;
		}
		if(m.size() == 0){
			return false;
		}
		//there are parameters
		//check whether the parameters are all the 'to ignore parameters'
		if(parametersToIgnore == null){
			return true;
		}
		Set<String> keys = m.keySet();
		for(String s: keys){
			if(parametersToIgnore.contains(s.toUpperCase()) || parametersToIgnore.contains(s.toLowerCase())){
				//parameter found in 'to ignore', check next
				continue;
			} else {
				//found one parameter that must not be ignored
				return true;
			}
		}
		//all parameters belong to 'to ignore parameters'
		return false;
	}
	

	/**
	 * Returns 'true' if the requested parameter exists. (It can have an empty value) 
	 * Case insensitive.
	 * @param parameterName
	 * @return
	 */
	public boolean hasHttpParameter(String parameterName){
		@SuppressWarnings("unchecked")
		Enumeration<String> e = request.getParameterNames();
		while(e.hasMoreElements()){
			if(parameterName.equalsIgnoreCase(e.nextElement())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Case insensitive
	 * @param parameterName
	 * @return
	 */
	public String getHttpParameter(String parameterName){
		String s = request.getParameter(parameterName.toLowerCase());
		if(s != null){
			return s;
		} else {
			return request.getParameter(parameterName.toUpperCase());
		}
//		String s = request.getParameter(parameterName);
//		if(s != null){
//			return s;
//		}else{
//			return request.getParameter(parameterName.toLowerCase());
//		}
	}
	

	public boolean isAction(String actionName){
		if(this.actionName == null){
			return actionName == null;
		} else {
			return this.actionName.equalsIgnoreCase(actionName);
		}
	}
	
	public boolean hasAction(){
		return actionName != null && !"".equals(actionName);
	}
	
	public boolean hasJobList(){
		if(jobListName == null){
			return false;
		}
		if("".equals(jobListName)){
			return false;
		}
		for(String noJobActionId: this.noJobActions){
			if(noJobActionId.equalsIgnoreCase(jobListName)){
				return false;
			}
		}
		return true;
	}
	
	public boolean hasHandlerAction(String action){
		return action.equalsIgnoreCase(jobListName);
	}
	
	/*
	public boolean hasShare(){
		return SHARE_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasEvent(){
		return EVENT_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasNotification(){
		return NOTIFICATION_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasStats(){
		return STATS_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasTasks(){
		return TASKS_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasDeleteMultipleJobs(){
		return DELETE_MULTIPLE_JOBS_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasJobs(){
		return JOBS.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasSearch(){
		return SEARCH_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasAdmin(){
		return ADMIN_ID.equalsIgnoreCase(jobListName);
	}
	
	public boolean hasScheduler(){
		return SCHEDULER_ID.equalsIgnoreCase(jobListName);
	}

	public boolean hasUsers(){
		return USERS_ID.equalsIgnoreCase(jobListName);
	}
	*/
	
	public boolean hasJobId(){
		return jobid != null && !"".equals(jobid);
	}
	
	public boolean hasSubAction(){
		return subActionName != null && !"".equals(subActionName);
	}
	
	public boolean isSubAction(String subActionName) {
		if (this.subActionName == null) {
			return subActionName == null;
		} else {
			return this.subActionName.equalsIgnoreCase(subActionName);
		}
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return the jobListName
	 */
	public String getJobListName() {
		return jobListName;
	}

	/**
	 * @return the jobid
	 */
	public String getJobid() {
		return jobid;
	}

	/**
	 * @return the actionName
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * @return the subActionName
	 */
	public String getSubActionName() {
		return subActionName;
	}

	/**
	 * @return the method
	 */
	public UwsActionMethod getActionMethod() {
		return method;
	}
	
	public String getBaseUrl(){
		String protocol = (request.isSecure()? "https" : "http");
		String host = request.getServerName();
		String port = (request.getServerPort()==80 || request.getServerPort()==443)? "" : ":"+request.getServerPort();
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		return protocol + "://" + host + port + contextPath + (servletPath == null ? "" : servletPath);
	}
	
	public UwsJobParameters createJobParametersAsStrings() throws UwsException {
		return createJobParametersAsStrings(null, 0);
	}
	
	private String getSuitableContentType(String contentType){
		if(contentType == null){
			return null;
		}
		int p = contentType.indexOf(';');
		if(p >= 0){
			return contentType.substring(0, p);
		}else{
			return contentType;
		}
	}
	
	public UwsJobParameters createJobParametersAsStrings(File uploadDir, int maxFileSize) throws UwsException {
		String contentType = getSuitableContentType(request.getContentType());
		UwsJobHttpParametersHandler handler = httpJobParameterHandlers.get(contentType);
		if(handler == null){
			handler = defaultJobHttpParametersHandler;
		}
		if(isMultipartContent()){
			checkMultipartIsAccepted(configuration);
		}
		UwsJobParametersItems items = handler.parse(request, uploadDir, maxFileSize);
		UwsJobParameters jobParameters = items.getJobParameters();
		uploadResourceLoaders = items.getUploadResourceLoaders();
		return jobParameters;
	}
	
//	public UwsJobParameters createJobParametersAsStrings(UwsConfiguration configuration, File uploadDir, int maxFileSize) throws UwsException {
//		//clear mulipart information
//		multipart = null;
//		
//		if (isMultipartContent()){
//			// Multipart HTTP parameters:
//			return createMultipartParameters(configuration, uploadDir, maxFileSize);
//		} else {
//			// Classic HTTP parameters (GET or POST):
//			// Extract and identify each pair (key,value):
//			return createParameters();
//		}
//		
//	}
//	
//	/**
//	 * All parameters name are converted to lowercase.
//	 * @return
//	 * @throws UwsException 
//	 */
//	public UwsJobParameters createParameters() throws UwsException{
//		UwsJobParameters parameters = new UwsJobParameters();
//
//		@SuppressWarnings("unchecked")
//		Enumeration<String> e = request.getParameterNames();
//		String name;
//		while(e.hasMoreElements()){
//			name = e.nextElement();
//			parameters.setParameter(name.toLowerCase(), request.getParameter(name));
//		}
//		
//		return parameters;
//	}
//	
//	
//	private UwsJobParameters createMultipartParameters(UwsConfiguration configuration, File uploadDir, int maxUploadSize) throws UwsException {
//		UwsJobParameters parameters = new UwsJobParameters();
//
//		checkMultipartIsValid(configuration);
//		
//		if(!uploadDir.exists()){
//			uploadDir.mkdirs();
//		}
//
//		try{
//			multipart = new MultipartRequest(request, uploadDir.getAbsolutePath(), maxUploadSize, new FileRenamePolicy() {
//				@Override
//				public File rename(File file) {
//					File parent = file.getParentFile();
//					String name = UwsUtils.getUniqueIdentifier()+"_"+file.getName();
//					return new File(parent, name);
//				}
//			});
//			
//			//Parameter names are converted to lowercase
//			@SuppressWarnings("unchecked")
//			Enumeration<String> e = multipart.getParameterNames();
//			while(e.hasMoreElements()){
//				String param = e.nextElement();
//				parameters.setParameter(param.toLowerCase(), multipart.getParameter(param));
//			}
//		}catch(IOException ioe){
//			removeMultipartFiles(multipart);
//			throw new UwsException("Error while reading the Multipart content !", ioe);
//		}
//
//		// Identify the tables to upload, if any:
//		if(parameters.containsParameter(PARAM_UPLOAD)){
//			String uploadParam = parameters.getStringParameter(PARAM_UPLOAD);
//			uploadResourceLoaders = buildLoaders(uploadParam, multipart);
//		}
//		
//		return parameters;
//	}
//	
//	private void removeMultipartFiles(final MultipartRequest multipart){
//		if(multipart == null){
//			return;
//		}
//		@SuppressWarnings("unchecked")
//		Enumeration<String> enumeration = multipart.getFileNames();
//		File f;
//		while (enumeration.hasMoreElements()) {
//			String fileName = enumeration.nextElement();
//			f = multipart.getFile(fileName);
//			f.delete();
//		}
//	}
//	
//	private UwsUploadResourceLoader[] buildLoaders(final String upload, final MultipartRequest multipart) throws UwsException {
//		if (upload == null || upload.trim().isEmpty()){
//			return null;
//		}
//
//		String[] pairs = upload.split(";");
//		UwsUploadResourceLoader[] loaders = new UwsUploadResourceLoader[pairs.length];
//
//		for(int i=0; i<pairs.length; i++){
//			String[] table = pairs[i].split(",");
//			if (table.length != 2){
//				throw new UwsException("Bad syntax ! An UPLOAD parameter must contain a list of pairs separated by a ';'. " +
//						"Each pair is composed of 2 parts, a table name and a URI separated by a ','.");
//			}
//			loaders[i] = new UwsUploadResourceLoader(table[0], table[1], multipart);
//		}
//
//		return loaders;
//	}
//
//	private void checkMultipartIsValid(UwsConfiguration configuration) throws UwsException {
//		if(!configuration.hasProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED)){
//			throw new UwsException("Request error! Upload capabitlity is not defined. " +
//					"You may enable this capability by using '"+UwsConfiguration.CONFIG_UPLOAD_ENABLED+"' configuration variable.");
//		}
//
//		boolean uploadEnabled = Boolean.parseBoolean(configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED));
//
//		if (!uploadEnabled){
//			throw new UwsException("Request error! Upload capabitlity is not enabled. " +
//					"You may enable this capability by using '"+UwsConfiguration.CONFIG_UPLOAD_ENABLED+"' configuration variable.");
//		}
//		
//		if(!configuration.hasProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE)){
//			throw new UwsException("No maximum upload file size specified. " +
//					"Plase, use configuration variable '"+UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE+"'");
//		}
//		
//		try{
//			long maxUploadSize = Long.parseLong(configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE));
//			if(maxUploadSize < 0){
//				throw new UwsException("Maximum upload size must be positive. Found: " + maxUploadSize);
//			}
//			if(maxUploadSize > Integer.MAX_VALUE){
//				throw new UwsException("Invalid maximum upload size value '"+maxUploadSize+"': Greater than java.lang.int maximum value.");
//			}
//		}catch(NumberFormatException nfe){
//			throw new UwsException("Wrong maximum upload size value: '"+configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE)+"'", nfe);
//		}
//	}
	
	public boolean isMultipartContent(){
		return isMultipartContent(request);
	}
	
	/**
	 * Utility method that determines whether the request contains multipart
	 * content.
	 *
	 * @param request The servlet request to be evaluated. Must be non-null.
	 *
	 * @return <code>true</code> if the request is multipart;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isMultipartContent(HttpServletRequest request) {
		if (!"post".equals(request.getMethod().toLowerCase())) {
			return false;
		}
		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}
		if (contentType.toLowerCase().startsWith(MULTIPART)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return the uploadResourceLoaders
	 */
	public UwsUploadResource[] getUploadResourceLoaders() {
		return uploadResourceLoaders;
	}
	
	public String dumpHttpParameters(){
		StringBuilder sb = new StringBuilder();
		@SuppressWarnings("unchecked")
		Enumeration<String> e = request.getParameterNames();
		String name;
		while(e.hasMoreElements()){
			name = e.nextElement();
			sb.append(name).append(": ").append(request.getParameter(name));
		}
		return sb.toString();
	}
	
	private void checkMultipartIsAccepted(UwsConfiguration configuration) throws UwsException {
		if(!configuration.hasProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED)){
			throw new UwsException("Request error! Upload capabitlity is not defined. " +
					"You may enable this capability by using '"+UwsConfiguration.CONFIG_UPLOAD_ENABLED+"' configuration variable.");
		}
	
		boolean uploadEnabled = Boolean.parseBoolean(configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED));
	
		if (!uploadEnabled){
			throw new UwsException("Request error! Upload capabitlity is not enabled. " +
					"You may enable this capability by using '"+UwsConfiguration.CONFIG_UPLOAD_ENABLED+"' configuration variable.");
		}
		
		if(!configuration.hasProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE)){
			throw new UwsException("No maximum upload file size specified. " +
					"Plase, use configuration variable '"+UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE+"'");
		}
		
		try{
			long maxUploadSize = Long.parseLong(configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE));
			if(maxUploadSize < 0){
				throw new UwsException("Maximum upload size must be positive. Found: " + maxUploadSize);
			}
			if(maxUploadSize > Integer.MAX_VALUE){
				throw new UwsException("Invalid maximum upload size value '"+maxUploadSize+"': Greater than java.lang.int maximum value.");
			}
		}catch(NumberFormatException nfe){
			throw new UwsException("Wrong maximum upload size value: '"+configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE)+"'", nfe);
		}
	}
}
