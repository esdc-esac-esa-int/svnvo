package esavo.sl.dd.requests;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import esavo.sl.dd.util.DDFilePath;
import esavo.sl.dd.util.DDSystemUtils;
import esavo.sl.dd.util.DDUtils;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;


/**
 *  A RequestProcessing initiates the processing of a request coming
 *  from the UI. First, it checks whether the observation asked for
 *  is proprietary; then, it calls the RequestTranslator to translate
 *  the request to specific ones depending on the level, and then it
 *  recevies the total files copied from the different specific requests.
 *  If everything goes OK, sets the status in the database (ddlogs) to
 *  "FINISHED" and informs the user that the data are already available.
 *  If there is a problem, sets the status to "FAILED" and sends a mail
 *  informing the user that there has been a problem.
 **/
public abstract class DDRequestProcessor implements DDFunctions {

    public static int SUCCESS_IN_PROCESS = 0;
    public static int ERROR_IN_PROCESS   = -1;  

	public static int PROCESSING_STATUS_WORKING = 1;
	public static int PROCESSING_STATUS_FAILED = 2;
	public static int PROCESSING_STATUS_FINISHED = 3;
	public static int PROCESSING_STATUS_PROPRIETARY = 4;

	static Logger logger = Logger.getLogger(DDRequestProcessor.class);

	private UwsConfiguration configuration;
	
	public DDRequestProcessor(UwsConfiguration configuration){
		this.configuration = configuration;
	}

	public DDRequestResult process(DDRetrievalRequest retrievalRequest) throws Exception {
		boolean calculateSizeFlag = retrievalRequest.isCalculateSizeFlag();

		String reqID = retrievalRequest.getReqIdComplete();
		Date startTime = new Date();
		
		logger.info("Processing request: " + retrievalRequest + " for user: " + retrievalRequest.getUser());

		// If not a calculateSize request, log in database
		if (!calculateSizeFlag) {
			try {

				logger.info("INSERTING DD LOG for reqID:" + reqID + " and StartTime:" + startTime);
				insertLog(retrievalRequest, PROCESSING_STATUS_WORKING);

			} catch (Exception ex){
				logger.error("A problem occurred when trying to set WORKING status for reqID:" + reqID);
				ex.printStackTrace();
				//This exception will not stop the process: ignore it
			}
		}

		List<DDFilePath> files = execute(retrievalRequest);

		return processPaths(retrievalRequest, files);
	}
	
	/**
	 * 
	 * @param retrievalRequest
	 * @param files
	 * @return
	 */
	public DDRequestResult processPaths(DDRetrievalRequest retrievalRequest, List<DDFilePath> files){
		DDRequestResult requestResult;
		
		if(files == null || files.size() == 0) {
			requestResult = processProductsNotFound(retrievalRequest, 0);
		} else {
			requestResult = processProductsFound(retrievalRequest, files);
		}

		int proprietaryStatus = DDUtils.getPropStatus(files);
		requestResult.setPropStatus(proprietaryStatus);

		try {
			updateLog(retrievalRequest, requestResult.getTotalSize(), PROCESSING_STATUS_FINISHED, proprietaryStatus);
		} catch (Exception e) {
			String reqID = retrievalRequest.getReqIdComplete();
			logger.error("Problem updating DD logs for request '" + reqID + "': " + e.getMessage(), e);
		}
		
		return requestResult;
	}
	
	/**
	 * Obtains files (totalFiles = Set<FilePath>) by calling getPaths()
	 * Sets totalSize. 
	 * @param retrievalRequest
	 * @param thisPropStatus
	 * @throws Exception
	 */
	private List<DDFilePath> execute(DDRetrievalRequest retrievalRequest) throws Exception {
		try {
			List<DDFilePath> pathsSet = getPaths(retrievalRequest);
			logger.debug("RetrievalRequest: PathSet size: " + pathsSet.size());

			List<DDFilePath> filesFound = DDUtils.findFiles(File.separator, pathsSet);
			logger.info(filesFound.size() + " Files found");
			
			return filesFound;
		} catch (Exception exc) {
			int propStatus = DDRetrievalRequest.PUBLIC;
			int processStatus = PROCESSING_STATUS_FAILED;
			if (exc instanceof DDAuthorizationException) {
				processStatus = PROCESSING_STATUS_PROPRIETARY;
				propStatus = DDRetrievalRequest.PROPRIETARY;
			}
			
			logger.error("A problem occurred when trying to processing the request",exc);

			int reqIDNumber = retrievalRequest.getReqId();
			DDRetrievalAccess retrievalAccess = retrievalRequest.getRetrievalAccess();
			
			//failed: update size to 0
			updateLog(retrievalRequest, 0.0, processStatus, propStatus);
			
			if(retrievalAccess == DDRetrievalAccess.FTP) {
				String email = retrievalRequest.getEmail();
				String userName = retrievalRequest.getUsername();
				String reqID = userName + reqIDNumber;
				DDSystemUtils.emailError(email, reqID, configuration);
			}
			throw exc;
		}

	}
	
	
	private void updateLog(DDRetrievalRequest retrievalRequest, double size, int processStatus, int propStatus) throws Exception{
		String propStatusStr = "public";
		if(propStatus == DDRetrievalRequest.PROPRIETARY){
			propStatusStr = "private";
		}

		updateLog(retrievalRequest, size, processStatus, propStatusStr);
	}
	

	private DDRequestResult processProductsFound(DDRetrievalRequest retrievalRequest, List<DDFilePath> totalFiles) {
		logger.info("totalFiles: " + totalFiles.size());

		String reqID = retrievalRequest.getReqIdComplete();
		DDRequestResult requestResult = new DDRequestResult(reqID, totalFiles);
		requestResult.setStatusCode(UwsOutputResponseHandler.OK);

		long totalSize = 0;
		long s;
		if(totalFiles != null){
			for(DDFilePath fp: totalFiles){
				s = fp.getSize();
				if(s > 0){
					totalSize += fp.getSize();
				}
			}
		}
		requestResult.setTotalSize(totalSize);

		logger.info("Updating DD Logs for reqID=" + reqID);

		return requestResult;
	}

	private DDRequestResult processProductsNotFound(DDRetrievalRequest retrievalRequest, int totalFilesSize){
		logger.error("RequestTranslator retuns a null or empty HashSet to RequestProcessing");
		String reqID = retrievalRequest.getReqIdComplete();
		DDRequestResult requestResult = new DDRequestResult(reqID, null);
		requestResult.setStatusCode(UwsOutputResponseHandler.NOT_FOUND);
		requestResult.setMessage("No products found");
		try {
			DDRetrievalAccess retrievalAccess = retrievalRequest.getRetrievalAccess();
			String email = retrievalRequest.getEmail();

			if(retrievalAccess == DDRetrievalAccess.FTP) {
				DDSystemUtils.emailNoProductsNotify(email,reqID,configuration);
			}

			logger.info("UPDATING DD LOG");
			if(totalFilesSize == 0){
				updateLog(retrievalRequest, 0.0, PROCESSING_STATUS_FINISHED, "public");
			} else {
				updateLog(retrievalRequest, 0.0, PROCESSING_STATUS_FAILED, "public");
			}
		} catch (Exception exc) {
			logger.error("No products found for request " + reqID,exc);
		}
		
		return requestResult;

	}

	@Override
	public UwsConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public abstract int getProprietary(Map<String, String> retrievalElement, UwsJobOwner user) throws Exception;

	@Override
	public abstract List<DDFilePath> getPaths(DDRetrievalRequest retrievalRequest) throws Exception;

	@Override
	public abstract void insertLog(DDRetrievalRequest retrievalRequest, int statusOid) throws Exception;

	@Override
	public abstract void updateLog(DDRetrievalRequest retrievalRequest, double size, int statusOid, String propStatus) throws Exception;

	
}
