package esavo.sl.services.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLSession;







import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import esavo.sl.services.transform.handlers.TransformHandler;
import esavo.sl.services.transform.handlers.TransformHandlerFactory;
import esavo.sl.services.transform.handlers.TransformHandlerFactory.TransformHandlerType;
import esavo.sl.services.util.Utils;
import esavo.sl.tap.actions.EsacTapService;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TapUtils;
import esavo.tap.formatter.OutputFormat;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.storage.UwsStorage;

/**
 * Converts query data into Json.<br/>
 * Query data formats are: Json, VOTable, CVS and CDF.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TransformToJson {
	
	private static final String JOBID_PARAMETER = "JOBID";
	private static final String RESULTID_PARAMETER = "RESULTID";
	private static final String RESULTS_OFFSET_PARAMETER = "RESULTS_OFFSET";
	private static final String PAGE_SIZE_PARAMETER = "PAGESIZE";
	private static final String ALL_STRINGS_PARAMETER = "ALL_STRINGS";
	
	private static Logger LOGGER = Logger.getLogger(TransformToJson.class);
	
	//TODO when pagination is enabled:
	//private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int DEFAULT_PAGE_SIZE = -1; //no limits
	
	private File tmpDir;
	private EsacTapService service;
	
	public TransformToJson(EsacTapService service, File tmpdir){
		this.service = service;
		this.tmpDir = tmpdir;
	}
	
	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String jobId = TapUtils.getParameter(JOBID_PARAMETER, request);
		String resultId = TapUtils.getParameter(RESULTID_PARAMETER, request);
		
		//Authentication
		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e1) {
			throw new ServletException("Cannot obtain current user: " + e1.getMessage(), e1);
		}

		//Get inputstream to results
		UwsJobResultMeta result;
		try {
			// Get job results
			result = getJobResult(jobId,resultId, user);
		} catch (UwsException e) {
			throw new ServletException("Cannot obtain results: " + e.getMessage(), e);
		}

		//dump data
		dumpData(request, response, jobId, result, user);
	}
	
	private UwsJobResultMeta getJobResult(String jobId, String resultId, UwsJobOwner currentUser) throws UwsException{
		UwsManager uwsManager = UwsManager.getInstance();
		UwsJob job = uwsManager.tryLoadJob(jobId, currentUser);
		List<UwsJobResultMeta> results = job.getResults();
		for(UwsJobResultMeta result: results){
			if(resultId.equals(result.getId())){
				return result;
			}
		}
		throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Result identifier '"+resultId+"' not found in job '"+jobId+"'");
	}
	
	private void dumpData(HttpServletRequest request, HttpServletResponse response, String jobId, UwsJobResultMeta result, UwsJobOwner currentUser) throws IOException{
		long resultsOffset = TapUtils.getLongParameter(RESULTS_OFFSET_PARAMETER, request, "0");
		long pageSize = TapUtils.getLongParameter(PAGE_SIZE_PARAMETER, request, ""+DEFAULT_PAGE_SIZE);
		boolean allStrings = TapUtils.getBooleanParameter(ALL_STRINGS_PARAMETER, request, false);

		OutputFormat format = service.getFactory().getOutputFormat(result.getMimeType());
		
		PrintStream out = new PrintStream(response.getOutputStream());
		InputStream is = null;
		try {
			TransformHandlerType type;
			if (format.getShortMimeType().equalsIgnoreCase(TransformHandlerType.VOTABLE.name())) {
				response.setContentType("text/plain");
				type = TransformHandlerType.VOTABLE;
			} else if (format.getShortMimeType().equalsIgnoreCase(TransformHandlerType.CDF.name())) {
				response.setContentType("text/plain");
				type = TransformHandlerType.CDF;
			} else if (format.getShortMimeType().equalsIgnoreCase(TransformHandlerType.CSV.name())) {
				response.setContentType("text/plain");
				type = TransformHandlerType.CSV;
			} else if (format.getShortMimeType().equalsIgnoreCase(TransformHandlerType.JSON.name())) {
				response.setContentType("text/plain");
				type = TransformHandlerType.JSON;
			} else {
				// ERROR unknown format.
				throw new IOException("Unknown format '" + result.getMimeType() + "'");
			}
			
			try {
				is = getResultInputStream(jobId, result.getId(), currentUser);
			} catch (UwsException e) {
				throw new IOException(e.getMessage(), e);
			}

			TransformHandler handler = TransformHandlerFactory.createHandler(type, out, resultsOffset, pageSize, tmpDir, allStrings);
			handler.parse(is);
		} finally {
			closeAll(is, out);
		}
	}
	
	private InputStream getResultInputStream(String jobId, String resultId, UwsJobOwner currentUser) throws UwsException{
		TAPFactory factory = service.getFactory();
		//UwsManager uwsManager = UwsManager.getInstance(factory.getAppId());
		UwsManager uwsManager = UwsManager.getInstance();
		UwsJob job = uwsManager.tryLoadJob(jobId, currentUser);
		List<UwsJobResultMeta> results = job.getResults();
		for(UwsJobResultMeta result: results){
			if(resultId.equals(result.getId())){
				//found
				UwsStorage storage = factory.getStorageManager();
				InputStream source = storage.getJobResultDataInputSource(job, result.getId());
				try {
					return Utils.decompressStream(source);
				} catch (IOException e) {
					throw new UwsException("Cannot create input stream for: " + jobId + ": " + resultId, e);
				}
			}
		}
		throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Result identifier '"+resultId+"' not found in job '"+jobId+"'");
	}

	
	private void closeAll(InputStream is, PrintStream out){
		if (is != null) {
			try {
				is.close();
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception closing InputStream", e);
			}
		}
		if(out != null){
			try {
				out.flush();
				out.close();
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception closing PrintWriter", e);
			}
		}
	}

	
//	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
//		String url = Utils.getParameter(URL_PARAMETER, request);
//		String format = Utils.getParameter(DATA_FORMAT_PARAMETER, request);
//		long resultsOffset = Utils.getLongParameter(RESULTS_OFFSET_PARAMETER, request, "0");
//		long pageSize = Utils.getLongParameter(PAGE_SIZE_PARAMETER, request, ""+DEFAULT_PAGE_SIZE);
//		boolean allStrings = Utils.getBooleanParameter(ALL_STRINGS_PARAMETER, request, false);
//		
//		String cookie = request.getHeader("cookie");
//		LOGGER.info("Accessing to: " + url);
//		LOGGER.info("Cookie: " + cookie);
//		
//		HttpURLConnection urlConnection = openRequest(url, cookie);
//		InputStream is = urlConnection.getInputStream();
//		PrintStream out = new PrintStream(response.getOutputStream());
//		try {
//			TransformHandlerType type;
//			if (format.equalsIgnoreCase(TransformHandlerType.VOTABLE.name())) {
//				response.setContentType("text/plain");
//				type = TransformHandlerType.VOTABLE;
//			} else if (format.equalsIgnoreCase(TransformHandlerType.CDF.name())) {
//				response.setContentType("text/plain");
//				type = TransformHandlerType.CDF;
//			} else if (format.equalsIgnoreCase(TransformHandlerType.CSV.name())) {
//				response.setContentType("text/plain");
//				type = TransformHandlerType.CSV;
//			} else if (format.equalsIgnoreCase(TransformHandlerType.JSON.name())) {
//				response.setContentType("text/plain");
//				type = TransformHandlerType.JSON;
//			} else {
//				// ERROR unknown format.
//				throw new IOException("Unknown format '" + format + "'");
//			}
//			TransformHandler handler = TransformHandlerFactory.createHandler(type, out, resultsOffset, pageSize, tmpDir, allStrings);
//			handler.parse(is);
//		} finally {
//			closeAll(is, out, urlConnection);
//		}
//	}
	
//	private void closeAll(InputStream is, PrintStream out, HttpURLConnection urlConnection){
//		if (is != null) {
//			try {
//				is.close();
//			} catch (Exception e) {
//				LOGGER.log(Level.ERROR, "Exception closing InputStream", e);
//			}
//		}
//		if(urlConnection != null){
//			try{
//				urlConnection.disconnect();
//			}catch (Exception e){
//				LOGGER.log(Level.ERROR, "Exception closing PrintWriter", e);
//			}
//			urlConnection = null;
//		}
//		if(out != null){
//			try {
//				out.flush();
//				out.close();
//			} catch (Exception e) {
//				LOGGER.log(Level.ERROR, "Exception closing PrintWriter", e);
//			}
//		}
//	}
	
	
//	private HttpURLConnection openRequest(String request, String cookie) throws IOException{
//		URL url = new URL(request);
//		if(url.getProtocol().equalsIgnoreCase("https")){
//			//TODO uncomment this when https is ready
////			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
////			urlConnection.setRequestProperty("cookie", cookie);
////
////			urlConnection.setHostnameVerifier(new HostnameVerifier() {
////				@Override
////				public boolean verify(String hostname, SSLSession session) {
////					return true;
////				}
////			});
////			
////			return urlConnection;
//			URL newUrl = new URL("http", url.getHost(), 8080, url.getFile());
//			LOGGER.info("Accessing to new url: " + newUrl);
//			HttpURLConnection urlConnection = (HttpURLConnection) newUrl.openConnection();
//			urlConnection.setRequestProperty("cookie", cookie);
//			return urlConnection;
//		}else{
//			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//			urlConnection.setRequestProperty("cookie", cookie);
//			return urlConnection;
//		}
//		
//	}

}
