package esavo.sl.services.vospace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import esavo.sl.services.status.ProgressInputStream;
import esavo.sl.services.status.TaskTypes;
import esavo.sl.services.util.Utils;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;
/**
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class SendToVOSpace {
	public static final String PARAM_JOBID = "JOBID";
	public static final String PARAM_LOCATION = "LOCATION";
	public static final String PARAM_FILE_NAME = "FILE_NAME";
	public static final String PARAM_OVERWRITE = "OVERWRITE";
	public static final String PARAM_TASKID = "TASKID";
	
	public static final String GENERIC_ERROR_MSG = "VOSpace ERROR";
	
	public static final String VOSPACE_LOCATION_PATTERN = "Location:";
	
	//VOSpace system errors
    public static final int INTERNAL_FAULT_EXCEPTION = 500;
    public static final int INVALID_URI_EXCEPTION = 400;
    public static final int PERMISSION_DENIED_EXCEPTION = 401;
    public static final int QUOTA_EXCEEDED_EXCEPTION = 507;
    public static final int TYPE_NOT_SUPPORTED_EXCEPTION = 500;
    public static final int UNSUPPORTED_OPERATION_EXCEPTION = 501;
    public static final int CONTAINER_NOT_FOUND_EXCEPTION = 404;
    public static final int DUPLICATE_NODE_EXCEPTION = 409;
    public static final int LINK_FOUND_EXCEPTION = 404;
    public static final int NODE_NOT_FOUND_EXCEPTION = 404;
    
	protected final EsacTapService service;
	
	private static Logger LOGGER = Logger.getLogger(SendToVOSpace.class.getName());

	public SendToVOSpace(EsacTapService serviceConnection) throws UwsException, TAPException {
		service = serviceConnection;
	}
	
	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e) {
			throw new ServletException("Cannot obtain current user: " + e.getMessage(), e);
		}
		
		try {
			// DENY ACCESS TO UNAUTHENTICATED/UNAUTHORIZED USERS
			Utils.checkAuthentication(user);
		} catch (InvalidParameterException ipe) {
			//Login required
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "User not authenticated", "Login required");
			response.flushBuffer();
			return;
		}
		
		try{
			execute(request, response, user);
		}catch(Exception e){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error", e);
			response.flushBuffer();
			return;
		}
	}
	
	private void execute(HttpServletRequest request, HttpServletResponse response, UwsJobOwner currentUser) throws Exception {
		if(currentUser == null){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid user", "User 'null' does not have enough privileges.");
			response.flushBuffer();
			return;
		}
		
		String jobid = request.getParameter(PARAM_JOBID);
		if(jobid == null || jobid.isEmpty()){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Missing jobid", "Required mandatory parameter: '"+PARAM_JOBID+"'.\n<br/>");
			response.flushBuffer();
			return;
		}
		
		String location = request.getParameter(PARAM_LOCATION);
		if(location == null || location.isEmpty()){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Missing location", "Required mandatory parameter: '"+PARAM_LOCATION+"'.\n<br/>");
			response.flushBuffer();
			return;
		}
		
		String fileName = request.getParameter(PARAM_FILE_NAME);
		if(fileName == null || fileName.isEmpty()){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Missing file name", "Required mandatory parameter: '"+PARAM_FILE_NAME+"'.\n<br/>");
			response.flushBuffer();
			return;
		}
		
		boolean overwrite = false;
		String overwriteParam = request.getParameter(PARAM_OVERWRITE);
		if(overwriteParam != null && !overwriteParam.isEmpty()){
			overwrite = Boolean.parseBoolean(overwriteParam);
		}
		
		long taskid = -1;
		String taskidParam = request.getParameter(PARAM_TASKID);
		if(taskidParam != null && !taskidParam.isEmpty()){
			try{
				taskid = Long.parseLong(taskidParam);
			}catch(NumberFormatException nfe){
				Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Invalid taksk identifier", "Invalid: '"+PARAM_TASKID+"' value: '"+taskidParam+"'.\n<br/>");
				response.flushBuffer();
				return;
			}
		}
		
		VOSpaceParameters voSpaceParameters = new VOSpaceParameters();
		voSpaceParameters.setUserid(currentUser.getId());
		voSpaceParameters.setJobid(jobid);
		voSpaceParameters.setLocation(location);
		voSpaceParameters.setFileName(fileName);
		voSpaceParameters.setOverwrite(overwrite);
		voSpaceParameters.setTaskid(taskid);
		
		try{
			String msg = launchVoSpaceRequest(response, voSpaceParameters);
			Utils.writeMsg(response, UwsOutputResponseHandler.OK, "Send to VOSpace", msg);
		}catch(Exception ex){
			Utils.writeError(GENERIC_ERROR_MSG, response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Error", ex.getMessage()+"\n<br/>");
		}
		
		response.flushBuffer();
	}
	
	private String launchVoSpaceRequest(HttpServletResponse response, VOSpaceParameters voSpaceParameters) throws Exception {
		UwsConfiguration configuration = service.getFactory().getConfiguration();
		String urlBase = configuration.getProperty(TapServiceConnection.VOSPACE_HOST_URL);
		if(!urlBase.endsWith("/")){
			urlBase = urlBase + "/";
		}
	
		String voSpaceProtocol = configuration.getProperty(TapServiceConnection.VOSPACE_PROTOCOL);
		String voSpaceTarget = configuration.getProperty(TapServiceConnection.VOSPACE_TARGET);
		String userAgent = "GACS - " + service.getSlVersion();
		
		voSpaceParameters.setUrlBase(urlBase);
		voSpaceParameters.setVoSpaceProtocol(voSpaceProtocol);
		voSpaceParameters.setVoSpaceTarget(voSpaceTarget);
		voSpaceParameters.setUserAgent(userAgent);

		
		long taskid = voSpaceParameters.getTaskid();
		if(taskid != -1){
			//StatusVOSpaceInit status = new StatusVOSpaceInit("0");
			UwsStatusData status = new UwsStatusData(TaskTypes.TYPE_VOSPACE_INIT, "0");
			UwsStatusManager.getInstance().updateStatus(taskid, status);
		}
		
		// 1. Check FILE NODE exists
		// 1.1. YES: If OVERWRITE FLAG = FALSE: RETURN ERROR
		if(!handleLocation(voSpaceParameters)){
			throw new UwsException(DUPLICATE_NODE_EXCEPTION, "Location '"+voSpaceParameters.getFileNodeURI()+"' already exists.\n\nNo overwrite flag found.");
		}

		if(taskid != -1){
			//StatusVOSpaceInit status = new StatusVOSpaceInit("30");
			UwsStatusData status = new UwsStatusData(TaskTypes.TYPE_VOSPACE_INIT, "30");
			UwsStatusManager.getInstance().updateStatus(taskid, status);
		}

		// 2. Check PARENT NODE exists:
		// 2.1 NO: create PARENT NODE
		// 2.2.2 Check ERRORS (!= 200 RAISE EXCEPTION : (no nodes removal required))
		handleParentNodeCreation(voSpaceParameters);

		if(taskid != -1){
			//StatusVOSpaceInit status = new StatusVOSpaceInit("60");
			UwsStatusData status = new UwsStatusData(TaskTypes.TYPE_VOSPACE_INIT, "60");
			UwsStatusManager.getInstance().updateStatus(taskid, status);
		}

		//at this point, PARENT NODE exists & OVERWRITE IF EXISTS IS TRUE
		return launchVoSpaceRequestExecute(response, voSpaceParameters);
	}

	
	private String launchVoSpaceRequestExecute(HttpServletResponse response, VOSpaceParameters voSpaceParameters) throws Exception {
		String jobid = voSpaceParameters.getJobid();
		long taskid = voSpaceParameters.getTaskid();
		InputStream is = getJobResultsStream(jobid, taskid);

		String proxyResponse = null;
		try{
			// 3. Create file node (current code) add error checks
			// 3.1. IF HTTP STATUS != 30x RAISE ERROR (no nodes removal required)
			
			//CREATE NODE
			String voSpaceJobid = createNode(voSpaceParameters);
			voSpaceParameters.setVoSpaceUwsJobId(voSpaceJobid);
			
			//Give time to VOSpace to create the required files.
			Thread.currentThread().sleep(3000);
			
			if(taskid != -1){
				//StatusVOSpaceInit status = new StatusVOSpaceInit("100");
				UwsStatusData status = new UwsStatusData(TaskTypes.TYPE_VOSPACE_INIT, "100");
				UwsStatusManager.getInstance().updateStatus(taskid, status);
			}
			
			// 4. Transfer data
			// 4.1. IF HTTP STATUS != 200 RAISE ERROR

			//PUSH DATA
			proxyResponse = pushData(voSpaceParameters, is);
		}finally{
			try{
				is.close();
			}catch(IOException ioe){
				
			}
		}
		
		return proxyResponse;
	}
	
	private String createNode(VOSpaceParameters voSpaceParameters) throws Exception {
		String urlNodeCreation = createVOSpaceNodeCreationUrl(voSpaceParameters);

		LOGGER.info("Creating node for: " + urlNodeCreation);
		
		String proxyTicket = createProxyTicket(urlNodeCreation);
		if(proxyTicket == null){
			LOGGER.info("Cannot get ST (creating node) for url: '"+urlNodeCreation+"'");
			throw new UwsException("Cannot get ST (creating node)");
		}
	    URL url = new URL(urlNodeCreation+"&ticket="+URLEncoder.encode(proxyTicket, "UTF-8"));
		
	    LOGGER.info("Creating node url: " + url.toString());
	    
	    String userAgent = voSpaceParameters.getUserAgent();
	    ConnectionHandler connectionHandler = new ConnectionHandler(url, userAgent);
	    connectionHandler.sendPost(null, false, ConnectionHandler.METHOD_POST);
		//returns Location: https://eacs.esac.esa.int/vospace-oper/transfers/async/1436197767215A
	    
	    int status = connectionHandler.getResponseCode();
	    if(status != UwsOutputResponseHandler.SEE_OTHER){
	    	throw new UwsException(
	    			UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Expected job redirection.\nOriginal response: " + 
	    					connectionHandler.getResponseMessage() + " (status: "+status+")");
	    }
		
	    LOGGER.info("Creating node status: "+status+", for: " + url.toString());
	    
		String voSpaceLocation = connectionHandler.getResponseHeader("Location");
		String voSpaceJobid = extractVOSpaceJobid(voSpaceLocation);
		
		connectionHandler.close();
		
		LOGGER.info("Creating node jobid: "+voSpaceJobid+", location: '"+voSpaceLocation+"', for: " + url.toString());
		
		return voSpaceJobid;
	}
	
	private String pushData(VOSpaceParameters voSpaceParameters, InputStream is) throws Exception {
		String urlPushToNode = createVOSpaceNodePushToNodeUrl(voSpaceParameters);
		
		LOGGER.info("Push data to: " + urlPushToNode);
		
		String proxyTicket = createProxyTicket(urlPushToNode);
		if(proxyTicket == null){
			throw new UwsException("Cannot get ST (push data)");
		}

		String userAgent = voSpaceParameters.getUserAgent();
		String fileName = voSpaceParameters.getFileName();
		URL url = new URL(urlPushToNode+"?ticket="+URLEncoder.encode(proxyTicket, "UTF-8"));
		
		LOGGER.info("Push data url: "+ url.toString());
		
		ConnectionHandler connectionHandler = new ConnectionHandler(url, userAgent);
		//String proxyResponse = connectionHandler.sendPost(is, false);
		String proxyResponse = connectionHandler.sendMultipart(is, fileName, ConnectionHandler.METHOD_POST);
		
		//connectionHandler.dumpHeaders();
		
		int responseCode = connectionHandler.getResponseCode();
		//System.out.println(responseCode);
		String responseMsg = connectionHandler.getResponseMessage();
		
		LOGGER.info("Push data status: "+responseCode+", for: " + urlPushToNode);
		
		connectionHandler.close();
		
		if(responseCode != 200){
			LOGGER.info("Push data status: "+responseCode+", for: " + urlPushToNode + ". Error: " + responseMsg);
			throw new UwsException(responseCode, proxyResponse + " ("+responseMsg+")");
		}

		return proxyResponse;
	}
	
	private String createVOSpaceNodeCreationUrl(VOSpaceParameters voSpaceParameters){
		String urlBase = voSpaceParameters.getUrlBase();
		String voSpaceTarget = voSpaceParameters.getVoSpaceTarget();
		String voSpaceProtocol = voSpaceParameters.getVoSpaceProtocol();
		String location = voSpaceParameters.getLocation();
		
		if(location.endsWith("/")){
			location = location.substring(0, location.length()-1);
		}
		
		StringBuilder sb = new StringBuilder(urlBase);
		sb.append("transfers/async?");
		sb.append("TARGET=").append(voSpaceTarget);
		//Not required, done at the beginning.
//		if(!location.startsWith("/")){
//			sb.append('/');
//		}
		sb.append(location);
		//sb.append("%26DIRECTION=pushToVospace");
		//sb.append("%26PROTOCOL=").append(voSpaceProtocol);
		sb.append("&DIRECTION=pushToVospace");
		sb.append("&PROTOCOL=").append(voSpaceProtocol);
		sb.append("&PHASE=RUN");
		return sb.toString();
	}
	
//	private String extractVOSpaceLocation(String response){
//		if(response == null){
//			return null;
//		}
//		int pos = response.indexOf(VOSPACE_LOCATION_PATTERN);
//		if(pos >= 0){
//			return response.substring(VOSPACE_LOCATION_PATTERN.length()).trim();
//		}else{
//			return null;
//		}
//	}
	
	private String extractVOSpaceJobid(String voSpaceLocation){
		if(voSpaceLocation == null){
			return null;
		}
		int pos = voSpaceLocation.lastIndexOf('/');
		if(pos < 0){
			return null;
		}
		return voSpaceLocation.substring(pos+1);
	}

	private String createVOSpaceNodePushToNodeUrl(VOSpaceParameters voSpaceParameters){
		String urlBase = voSpaceParameters.getUrlBase();
		String userid = voSpaceParameters.getUserid();
		String voSpaceJobid = voSpaceParameters.getVoSpaceUwsJobId();

		StringBuilder sb = new StringBuilder(urlBase);
		sb.append("data/").append(userid).append('/').append(voSpaceJobid);
		return sb.toString();
	}

	private String createProxyTicket(String targetUrl) throws UnsupportedEncodingException{
//		String codifiedUrl = URLEncoder.encode(targetUrl,"UTF-8");
//	    final CasAuthenticationToken token = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//	    AttributePrincipal principal = token.getAssertion().getPrincipal();
//	    final String proxyTicket = principal.getProxyTicketFor(codifiedUrl);
//	    return proxyTicket;
	    final CasAuthenticationToken token = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
	    AttributePrincipal principal = token.getAssertion().getPrincipal();
	    final String proxyTicket = principal.getProxyTicketFor(targetUrl);
	    return proxyTicket;
	}
	
	private InputStream getJobResultsStream(String jobid, long taskid) throws UwsException{
		UwsStorage storage = service.getFactory().getStorageManager();
		UwsJob job = storage.getJobMeta(jobid);
		if(taskid == -1){
			return storage.getJobResultDataInputSource(job, UwsJobResultMeta.DEFAULT_IDENTIFIER);
		}else{
			long size = storage.getJobResultDataSize(job, UwsJobResultMeta.DEFAULT_IDENTIFIER);
			InputStream is = storage.getJobResultDataInputSource(job, UwsJobResultMeta.DEFAULT_IDENTIFIER);
			return new ProgressInputStream(is, size, taskid);
		}
	}


	/**
	 * Checks whether the file already exists. Returns 'true' if the file can be uploaded<br/>
	 * If the file already exists and the flag overwrite is false, it returns false.<br/>
	 * If the file already exists and the flag overwrite is true, returns true.<br/>
	 * IF the file does not exist, returns true.<br/>
	 * @param voSpaceParameters
	 * @return 'true' if the file can be uploaded.
	 */
	private boolean handleLocation(VOSpaceParameters voSpaceParameters) throws Exception {
		//	1. Check FILE NODE exists
		//	1.1. YES: If OVERWRITE FLAG = FALSE: RETURN ERROR
		//if(!handleLocation(location, overwrite)){
		LOGGER.info("Handle location starts for: " + voSpaceParameters.getFileNodeURI());
		String urlNodeRetrieval = createGetFileNodeUrl(voSpaceParameters);
		
		LOGGER.info("Handle location getting proxy ticket for: " + voSpaceParameters.getFileNodeURI());
		String proxyTicket = createProxyTicket(urlNodeRetrieval);
		if(proxyTicket == null){
			LOGGER.info("Cannot get ST (checking file) for url: '"+urlNodeRetrieval+"'");
			throw new UwsException("Cannot get ST (checking file)");
		}
	    URL url = new URL(urlNodeRetrieval+"?ticket="+URLEncoder.encode(proxyTicket, "UTF-8"));
		
	    String userAgent = voSpaceParameters.getUserAgent();
	    ConnectionHandler connectionHandler = new ConnectionHandler(url, userAgent);
	    String response = null;
	    
		LOGGER.info("Handle location sending get request for: " + url.toString());
		try {
			response = connectionHandler.sendGet();
		} catch (IOException e) {
			//System.out.println(e.getMessage());
			LOGGER.info("Handle location error:" + e.getMessage());
		}
	    
	    // 401 permission denied
	    // 404 not found
	    // 500 internal error	    
	    
	    int status = connectionHandler.getResponseCode();
		connectionHandler.close();

		LOGGER.info("Handle location get status: " + status + " for: " + voSpaceParameters.getFileNodeURI());
		
	    switch(status){
	    case PERMISSION_DENIED_EXCEPTION:
	    	throw new UwsException(status, "Permission denied to '"+voSpaceParameters.getFileNodeURI()+"':\n" + response);
	    case NODE_NOT_FOUND_EXCEPTION:
	    	return true;
	    case 200:
	    	return voSpaceParameters.isOverwrite();
	    default:
	    	throw new UwsException(status, "Cannot accesss to '"+voSpaceParameters.getFileNodeURI()+"':\n" + response);
	    }
	}
	
	private String createGetFileNodeUrl(VOSpaceParameters voSpaceParameters){
		//GET http://eacs.esac.esa.int/vospace-oper/rest/nodes/platest/GACS/file_name
		String urlBase = voSpaceParameters.getUrlBase();
		StringBuilder sb = new StringBuilder(urlBase);
		sb.append("nodes").append(voSpaceParameters.getFileNodeURI());
		return sb.toString();

	}
	
	private void handleParentNodeCreation(VOSpaceParameters voSpaceParameters) throws Exception {
		//URI = vos://esavo!vospace/platest/GACS
		String uri = voSpaceParameters.getContainerNodeURI();
		String data = VOSpaceUtils.formatNodeCreation(uri, VOSpaceUtils.DEFAULT_GACS_NODE_DESCRIPTION);
		
		// 2. Check PARENT NODE exists:
		// 2.1 NO: create PARENT NODE
		// 2.2.2 Check ERRORS (!= 200 RAISE EXCEPTION : (no nodes removal required))
		
		String urlCreateContainerNode = createContainerNodeUrl(voSpaceParameters);
		
		LOGGER.info("Handle parent node creation for: " + voSpaceParameters.getFileNodeURI());
		
		String proxyTicket = createProxyTicket(urlCreateContainerNode);
		if(proxyTicket == null){
			throw new UwsException("Cannot get ST (creating container node)");
		}

		String userAgent = voSpaceParameters.getUserAgent();
		URL url = new URL(urlCreateContainerNode+"?ticket="+URLEncoder.encode(proxyTicket, "UTF-8"));
		ConnectionHandler connectionHandler = new ConnectionHandler(url, userAgent);
		//String proxyResponse = connectionHandler.sendPost(is, false);
		
		LOGGER.info("Handle parent node creation sending post request for: " + url.toString());
		
		String proxyResponse;
		InputStream is = new ByteArrayInputStream(data.getBytes());
		try{
			//proxyResponse = connectionHandler.sendMultipart(is, "data", ConnectionHandler.METHOD_PUT);
			proxyResponse = connectionHandler.sendPost(is, false, ConnectionHandler.METHOD_PUT);
		}finally{
			is.close();
		}
		
		//connectionHandler.dumpHeaders();
		
		int responseCode = connectionHandler.getResponseCode();
		//System.out.println(responseCode);
		
		connectionHandler.close();
		
		LOGGER.info("Handle parent node creation response status: " + responseCode + ", for: " + voSpaceParameters.getFileNodeURI());
		
//		201 OK
//		400 Invalid URI / Type Not Supported / Parent node is LinkNode
//		401 Permission denied
//		404 Parent not found
//		409 Duplicated node
//		500 Internal error 
		
		if(responseCode != 200 && responseCode != 409){
			throw new UwsException(responseCode, proxyResponse);
		}

		//return proxyResponse;
	}
	
	private String createContainerNodeUrl(VOSpaceParameters voSpaceParameters){
		//http://eacs.esac.esa.int/vospace-oper/rest/nodes/platest/GACS 
		String urlBase = voSpaceParameters.getUrlBase();
		StringBuilder sb = new StringBuilder(urlBase);
		//sb.append("nodes/").append(voSpaceParameters.getUserid()).append('/').append(VOSpaceUtils.GACS_NODE);
		sb.append("nodes").append(voSpaceParameters.getLocation());
		return sb.toString();
	}

}
