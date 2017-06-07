package esavo.sl.services.upload;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.TableFormatException;
import esavo.sl.services.status.CustomServletFileUpload;
import esavo.sl.services.status.ProgressInputStream;
import esavo.sl.services.util.Utils;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

import com.oreilly.servlet.multipart.ExceededSizeException;

/**
 * This class is used to upload/remove user tables and to upload query results.
 * <ol>
 * <li>If the form is multipart:
 *   <ol>
 *   <li>If no jobid is present: a user table upload is requested.</li>
 *   <li>If jobid is present: a query results upload is requested.</li>
 *   </ol>
 * </li>
 * <li>If no multipart:
 *   <ol>
 *   <li>'delete' parameter must be present: user table delete requested.</li>
 *   <li>If no 'delete' is present, an error is raised.</li>
 *   </ol>
 * </li>
 * </ol>
 * 
 * <p>Required parameters:
 * <ul>
 * <li>FORMAT: required for any action</li>
 * <li>TABLE_NAME: required for any action</li>
 * <li>JOBID: required for query results upload only. (This is the job associated to the query, the job that contains the results to upload)</li>
 * <li>DELETE: required for table removal action only.</li>
 * <li>FORCE_REMOVAL: optional for delete operations only.</li>
 * <li>TASKID: optional for uploads actions, the progress of the task associated to the action is updated (percentage). -1 to avoid notifications</li>
 * <li>FILE: mandatory for uploads actions only.</li>
 * <li>RACOL: optional for uploads actions.</li>
 * <li>DECCOL: optional for uploads actions.</li>
 * </ul>
 * </p>
 *
 */

public class Upload {
	
	/** Part of HTTP content type header. */
	public static final String MULTIPART = "multipart/";

	public static final String PARAM_JOBID   = "JOBID";
	//public static final String PARAM_FILE   = "FILE";
	public static final String PARAM_FORMAT  = "FORMAT";
	public static final String PARAM_RACOL  = "RACOL";
	public static final String PARAM_DECCOL = "DECCOL";
	public static final String PARAM_TABLENAME = "TABLE_NAME";
	public static final String PARAM_TABLEDESC = "TABLE_DESC";
	public static final String PARAM_OVERWRITE = "OVERWRITE";
	public static final String PARAM_FORCE_REMOVAL = "FORCE_REMOVAL";
	public static final String PARAM_TASKID = "TASKID";
	public static final String PARAM_DELETE = "DELETE";
	public static final String TAP_SERVICE_STATUS = "TAP_SERVICE_STATUS";
	
	protected final TAPService service;

	private Map<String,String> parameters = null;
	private File	file	   = null;
	private boolean isFileUpload = false;
	

	
	public Upload(TAPService serviceConnection) {
		service = serviceConnection;
	}

	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		parameters = new HashMap<String,String>();
		file = null;
		long taskId=-1; 
		String jobId=null; 
		
		response.setContentType("text/html");
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e) {
			throw new ServletException("Cannot obtain current user: " + e.getMessage(), e);
		}
		if(user == null){
			throw new ServletException("Cannot obtain current user");
		}
		
		FileItemFactory factory = new DiskFileItemFactory();

		long maxConfigurationFileSize = service.getConfiguration().getLongProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
		long maxFileSize = 0;
		UwsQuota quota = null;
		try {
			quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(user);
			maxFileSize = quota.getMinFileQuotaAvailable(maxConfigurationFileSize);
		} catch (UwsException e1) {
			throw new IOException(e1);
		}

		CustomServletFileUpload upload = new CustomServletFileUpload(factory);
		upload.setSizeMax(maxFileSize);
		
		File uploadDir = service.getFactory().getStorageManager().getUploadDir(user);
		TAPSchemaInfo tapSchemaInfo;
		try {
			tapSchemaInfo = service.getTapSchemaInfo(user);
		} catch (TAPException e) {
			throw new IOException("Cannot obtain tap schema info for user '"+user.getId()+"': " + e.getMessage(), e);
		}
		
		if(!uploadDir.exists()){
			uploadDir.mkdirs();
		}

		UploadProgressListener uploadProgressListener = new UploadProgressListener();
		upload.setProgressListener(uploadProgressListener);

		try {
			//DENY ACCESS TO UNAUTHENTICATED/UNAUTHORIZED USERS
			Utils.checkAuthentication(user);
			
			//For uploading tables, the request must be multipart.
			//For removing tables, the request it not multipart.
			if(ServletFileUpload.isMultipartContent(request)){
				//Upload user tables or query results
				//for query results, a job id is mandatory
				List<FileItem> fields = upload.parseRequest(request);
				Iterator<FileItem> it = fields.iterator();
				if (!it.hasNext()) {
					throw new Exception("No parameters provided.");
				}
				while (it.hasNext()) {
					FileItem fileItem = it.next();
					boolean isFormField = fileItem.isFormField();
					if (isFormField) {
						String name = fileItem.getFieldName();
						String value = fileItem.getString();
						parameters.put(name, value);
						if(name.equals(PARAM_TASKID)){
							taskId = Long.parseLong(value);
							uploadProgressListener.setTaskId(taskId);
						}
						if(name.equals(PARAM_JOBID)){
							// IF jobId present: upload results from job
							jobId = value;
							if(jobId!=null && jobId.trim().length()>0 && file==null){
								file = getResultDataFileForJob(jobId, user);
								UwsStatusData statusUpload = new UwsStatusData(UwsStatusData.TYPE_UPLOAD, "100");
								if(taskId >= 0){
									try{
										UwsStatusManager.getInstance().updateStatus(taskId, statusUpload); 
									} catch (IllegalArgumentException iae){
										iae.printStackTrace();
									}
								}
							}
						}
						
					} else {
						uploadProgressListener.setTheContentLength(fileItem.getSize());
						if(file==null){
							isFileUpload = true;
							file = new File(uploadDir, UwsUtils.getUniqueIdentifier()+"_"+fileItem.getName());
							fileItem.write(file);
							
							try{
								quota.addFileSize(file.length());
							}catch(QuotaException qe){
								long fileSize = file.length();
								file.delete();
								quota.reduceFileSize(fileSize);
								throw qe;
							}
						}
					}
				}
			}else{
				//No upload functionality.
				//Currently: removal operations only
				
				// Extract and identify each pair (key,value):
				@SuppressWarnings("unchecked")
				Enumeration<String> e = request.getParameterNames();
				while(e.hasMoreElements()){
					String name = e.nextElement();
					String value = request.getParameter(name);
					parameters.put(name, value);
					if(name.equals(PARAM_TASKID)){
						taskId = Long.parseLong(value);
						uploadProgressListener.setTaskId(taskId);
					}
					if(name.equals(PARAM_JOBID)){
						jobId = value;
					}
				}
			}

			//Change to lowercase
			changeToLowerCaseIfNeeded(parameters, PARAM_TABLENAME);
			changeToLowerCaseIfNeeded(parameters, PARAM_RACOL);
			changeToLowerCaseIfNeeded(parameters, PARAM_DECCOL);
			changeToLowerCaseIfNeeded(parameters, PARAM_FORMAT);
			changeToLowerCaseIfNeeded(parameters, PARAM_OVERWRITE);
			changeToLowerCaseIfNeeded(parameters, PARAM_FORCE_REMOVAL);

			String message="";

			///////////////////////////////////////////
			/// CHECKS
			///////////////////////////////////////////
			try{
				check(maxFileSize);
			}catch(Exception e){
				if(file!=null && isFileUpload){
					long fileSize = file.length();
					file.delete();
					quota.reduceFileSize(fileSize);
				}
				throw e;
			}
			
			
			long fileSize = 0;
			if(parameters.get(PARAM_DELETE)!=null){
				///////////////////////////////////////////
				/// DELETE TABLE
				///////////////////////////////////////////
				try{
					delete(user, tapSchemaInfo);
				}catch(Exception e){
					throw e;
				}
			}else{
				///////////////////////////////////////////
				/// UPLOAD TABLE
				///////////////////////////////////////////
				try{
					message = upload(taskId, user, tapSchemaInfo);
				}catch(Exception e){
					if(isFileUpload){
						fileSize = file.length();
						file.delete();
						quota.reduceFileSize(fileSize);
					}
					throw e;
				}
			}
			
			response.getWriter().println(PARAM_RACOL+"="+parameters.get(PARAM_RACOL));
			response.getWriter().println(PARAM_DECCOL+"="+parameters.get(PARAM_DECCOL));
			response.getWriter().println(PARAM_TABLENAME+"="+parameters.get(PARAM_TABLENAME));
			response.getWriter().println(PARAM_FORMAT+"="+parameters.get(PARAM_FORMAT));
			response.getWriter().println(PARAM_TABLEDESC+"="+parameters.get(PARAM_TABLEDESC));
			response.getWriter().println(PARAM_DELETE+"="+parameters.get(PARAM_DELETE));
			
			if(message!=null && !message.trim().isEmpty()){
				response.getWriter().println(TAP_SERVICE_STATUS+"="+message);
			}
			
			if (file != null) {
				response.getWriter().println("File size: "+fileSize);
			}

			response.flushBuffer();
		}catch(Throwable t){
			t.printStackTrace();
			String action = parameters.containsKey(PARAM_DELETE) ? "Delete" : "Upload";
            
			String msg = "Cannot execute action: " + action + " table '"+parameters.get(PARAM_TABLENAME)+"'";
			try {
				service.getFactory().getOutputHandler().writeServerErrorResponse(
						response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, msg, action, t, UwsExceptionOutputFormat.HTML);
			} catch (UwsException e) {
				throw new ServletException(e);
			}
            
            response.getWriter().flush();
            response.flushBuffer();
		}

	}

	
	/**
	 * Checks the validity of the upload or delete request.
	 * @throws IOException 
	 */
	private void check(long maxFileSize) throws IOException{
		// Common checks
		if(parameters.get(PARAM_TABLENAME)==null) {
			throw new InvalidParameterException("Error: table name not provided.");
		}

		if(parameters.containsKey(PARAM_DELETE)) {
			checkDelete();
//		} else if(parameters.containsKey(PARAM_JOBID)) {
//			checkUploadJobResults(maxFileSize);
		} else {
			checkUpload(maxFileSize);
		}
	}
	
	/**
	 * Checks corresponding to a table delete.
	 */
	private void checkDelete(){
	}
	
	/**
	 * Checks corresponding to a table upload.
	 * @throws IOException
	 */
	private void checkUpload(long maxFileSize) throws IOException{
		if (file == null) {
			throw new InvalidParameterException("Upload error: no file provided.");
		}
		if (file.length()>maxFileSize) {
			throw new InvalidParameterException("Upload error: too big file, max upload size is "+((long)(maxFileSize/1024/1024))+" MB");
		}
		if(parameters.containsKey(PARAM_RACOL) || parameters.containsKey(PARAM_DECCOL)){
			//If RA or DEC parameters are present, check the table contains the specified fields
			//checkVOTable(file);
		}
	}

	/**
	 * Checks the validity of the uploaded VOTable.
	 * @param f
	 * @throws IOException
	 */
	private void checkVOTable(File f) throws IOException{
		String raParam = (String)parameters.get(PARAM_RACOL);
		String decParam = (String)parameters.get(PARAM_DECCOL);
		
		if((raParam == null || "".equals(raParam)) && (decParam == null || "".equals(decParam))){
			//no ra/dec params provided: do not check.
			return;
		}
		
		InputStream votable = new FileInputStream(f);

		// start parsing the VOTable:
		StarTableFactory factory = new StarTableFactory();
		StarTable table = factory.makeStarTable( file.getAbsolutePath(), "votable" );

		boolean raColPresent=false;
		boolean decColPresent=false;
		
		
		for(int col=0 ; col<table.getColumnCount(); col++){
			ColumnInfo field = table.getColumnInfo(col);
			if(field.getName().trim().equalsIgnoreCase(raParam)) {
				raColPresent=true;
			}
			if(field.getName().trim().equalsIgnoreCase(decParam)) {
				decColPresent=true;
			}
		}

		votable.close();
		
		if(!raColPresent) {
			//RA was provided, but the field is not found in the file
			throw new InvalidParameterException("UPLOAD error: "+(raParam==null?"no ra param specified.":"'"+raParam + "' not found in file."));
		}
		if(!decColPresent) {
			//DEC was provided, but the field is not found in the file
			throw new InvalidParameterException("UPLOAD error: "+(decParam==null?"no dec param specified.":"'"+decParam+"' not found in file."));
		}

	}
	
	
	
	/**
	 * 
	 * @param loaders
	 * @return Status message
	 * @throws TAPException
	 */
	private String upload(Long taskId, UwsJobOwner owner, TAPSchemaInfo tapSchemaInfo) throws TAPException {
		// Force StatusUpload finished
		if(taskId >= 0){
			try{
				UwsStatusData status = new UwsStatusData(UwsStatusData.TYPE_UPLOAD, "100");
				UwsStatusManager.getInstance().updateStatus(taskId, status);
			} catch (IllegalArgumentException iae){
				iae.printStackTrace();
				//throw new IOException("Error updating status: " + iae.getMessage(), iae);
			}
		}
		
		TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
				"UploadConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);

		// Begin a DB transaction:
		dbConn.startTransaction();

		String userSchema = TAPMetadata.getUserSchema(owner);
		TAPSchema schema = dbConn.loadSchema(tapSchemaInfo, userSchema);
		String tableName = null;
		String tableDesc = null;
		ProgressInputStream pins = null;
		long maxFileSize = 0;
		UwsQuota quota = null;
		long dbQuotaToRestore = -1;
		
		String message;
		try{
			tableName = parameters.get(PARAM_TABLENAME);
			tableDesc = parameters.get(PARAM_TABLEDESC);

			//Quota initialization: file quota is not used for Upload capability. Upload capability is available for
			//registered users only. Registered users are managed by quota always.
			long maxConfigurationFileSize = service.getConfiguration().getLongProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
			try {
				quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
				maxFileSize = quota.getMinFileQuotaAvailable(maxConfigurationFileSize);
			} catch (UwsException e1) {
				throw new IOException(e1);
			}

			//// START STREAMED UPLOAD HANDLER
			
			// start parsing the VOTable:
			InputStream isTmp = new FileInputStream(file);
			InputStream fins = Utils.decompressStream(isTmp);
			pins = new ProgressInputStream(fins,file.length(),taskId);
			
			String tableSpace = service.getUserUploadTableSpace();
			
			// TODO remove hard setting of FORMAT
			message = StreamedUploadManager.upload(pins, 
					service, 
					dbConn,
					owner,
					schema, 
					tableName, 
					tableDesc,
					parameters.get(PARAM_FORMAT), 
					parameters.get(PARAM_RACOL), 
					parameters.get(PARAM_DECCOL),
					tableSpace);

			
			dbQuotaToRestore = dbConn.getTableSize(schema.getDBName(),tableName);
			
			// Commit modifications:
			dbConn.endTransaction();
			
			UwsEventsManager eventsManager = service.getFactory().getEventsManager();
			eventsManager.setEventTime(owner, TAPFactory.TABLE_CREATED_EVENT);
			eventsManager.setEventTime(owner, UwsEventType.QUOTA_DB_UPDATED_EVENT);

			//Vacuum Analyze the new table (ouside of transaction, with autocommit)
			dbConn.vacuumAnalyze(schema.getName(), tableName);
			
			
		}catch(IOException ioe){
			//Substract added DB size
			dbConn.cancelTransaction();
			restoreDbQuota(owner, dbQuotaToRestore, ioe);

			if(ioe instanceof QuotaException){
				throw new TAPException(ioe);
			}else if(ioe instanceof ExceededSizeException){
				throw new TAPException("Upload limit exceeded ! You can upload at most "+maxFileSize+" bytes.");
			}else if(ioe instanceof TableFormatException){
				throw new TAPException("Error while uploading table \""+tableName+"\" : "+UwsUtils.getNotNullExceptionMessage(ioe), ioe);
			}else if(ioe instanceof UploadDBException){
				throw new TAPException("Error while uploading table \""+tableName+"\" : "+UwsUtils.getNotNullExceptionMessage(ioe), ioe);
			}else if(ioe instanceof UploadUwsException){
				throw new TAPException("Error while uploading table \""+tableName+"\" : "+UwsUtils.getNotNullExceptionMessage(ioe), ioe);
			}else{
				throw new TAPException("Error while uploading table \""+tableName+"\" : "+UwsUtils.getNotNullExceptionMessage(ioe), ioe);
			}
		}catch(NullPointerException npe){
			//Substract added DB size
			dbConn.cancelTransaction();
			restoreDbQuota(owner, dbQuotaToRestore, npe);
			throw new TAPException(npe);
		} catch (UwsException e) {
			//Substract added DB size
			dbConn.cancelTransaction();
			restoreDbQuota(owner, dbQuotaToRestore, e);
			throw new TAPException(e);
		}finally{
			try{
				dbConn.close();
			}catch(Exception ioe){
			}

			if(pins != null){
				try{
					pins.close();
				}catch(Exception ioe){
				}
			}

			if(isFileUpload){
				try{
					long fileSize = file.length();
					file.delete();
					quota.reduceFileSize(fileSize);
				}catch(Exception e){
					
				}
			}
		}

		return message;
	}
	
	private void restoreDbQuota(UwsJobOwner owner, long dbQuotaToRestore, Exception e) throws TAPException{
		if(dbQuotaToRestore < 0){
			return;
		}
		try {
			UwsQuotaSingleton.getInstance().createOrLoadQuota(owner).reduceDbSize(dbQuotaToRestore);
		} catch (UwsException e1) {
			throw new TAPException(e.getMessage() + "\nWARNING: Cannot restore user '"+owner.getId()+"' quota due to: " + e1.getMessage(), e);
		}
	}

	
	/**
	 * Delete a user table.
	 * @return
	 * @throws TAPException
	 */
	private TAPSchema delete(UwsJobOwner owner, TAPSchemaInfo tapSchemaInfo) throws TAPException {
		TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
				"UploadConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);

		// Begin a DB transaction:
		dbConn.startTransaction();

		String userSchema = TAPMetadata.getUserSchema(owner);
		TAPSchema schema = dbConn.loadSchema(tapSchemaInfo, userSchema);
		String tableName = null;
		try{
			tableName = parameters.get(PARAM_TABLENAME);
			boolean forceRemoval = getForceRemoval();

			// Drop table from the database:
			//Delete table are available only for authenticated users
			//authenticated users tables are private
			TAPTable tapTable = new TAPTable(tableName, TAPTable.PRIVATE_TABLE);
			schema.addTable(tapTable);
			
			// Preserve table size before removing it.
			long tableSize = dbConn.getTableSize(schema.getDBName(), tapTable.getDBName());
			
			dbConn.dropTable(tapTable, forceRemoval);
			dbConn.unregisterFromTapSchema(tapSchemaInfo, tapTable);
			
			// Remove possible shares
			removePossibleShares(tapTable, owner);

			// Commit modifications:
			dbConn.endTransaction();
			
			//UwsEventsManager eventsManager = service.getFactory().getEventsManager();
			//eventsManager.setEventTime(owner, TAPFactory.TABLE_REMOVED_EVENT);
			//eventsManager.setEventTime(owner, UwsEventType.QUOTA_DB_UPDATED_EVENT);

			try{
				//Substract added DB size
				UwsQuotaSingleton.getInstance().createOrLoadQuota(owner).reduceDbSize(tableSize);
			}catch(UwsException e){
				throw new TAPException(e);
			}
			
		}catch(DBException dbe){
			dbConn.cancelTransaction();	// ROLLBACK
			throw dbe;
		}catch(NullPointerException npe){
			dbConn.cancelTransaction();	// ROLLBACK
		}catch(UwsException uws){
			dbConn.cancelTransaction(); // ROLLBACK
			throw new TAPException(uws);
		}finally{
			generateRemovalEvents(owner);
			dbConn.close();
		}

		return schema;
	}
	
	private void generateRemovalEvents(UwsJobOwner owner){
		UwsEventsManager eventsManager = service.getFactory().getEventsManager();
		try{
			eventsManager.setEventTime(owner, TAPFactory.TABLE_REMOVED_EVENT);
			eventsManager.setEventTime(owner, UwsEventType.QUOTA_DB_UPDATED_EVENT);
		}catch(UwsException e){
			//ignore
		}
	}
	
	private boolean getOverwrite(){
		String fc = parameters.get(PARAM_OVERWRITE);
		if (fc == null) {
			return false;
		} else {
			return Boolean.parseBoolean(fc);
		}
	}
	
	private boolean getForceRemoval(){
		String fc = parameters.get(PARAM_FORCE_REMOVAL);
		if (fc == null) {
			return false;
		} else {
			return Boolean.parseBoolean(fc);
		}
	}
	
	/**
	 * Removes sharing info
	 * @param tapTable
	 * @throws UwsException
	 */
	private void removePossibleShares(TAPTable tapTable, UwsJobOwner owner) throws UwsException{
		UwsShareManager shareManager = UwsManager.getInstance().getFactory().getShareManager();
		List<UwsShareItemBase> sharedItems = shareManager.getUserSharedItems(owner.getId(), false);
		String title = tapTable.getSchema().getDBName() + '.' + tapTable.getDBName();
		String resourceid = null;
		for(UwsShareItemBase sib: sharedItems){
			if(sib.getResourceType() == TAPMetadataLoader.SHARED_RESOURCE_TYPE_TABLE && sib.getTitle().equals(title)){
				resourceid = sib.getResourceId();
				break;
			}
		}
		//String resourceid = tapTable.getSchema().getDBName() + '.' + tapTable.getDBName();
		if(resourceid == null){
			//throw new UwsException("Resource identifier not found for table '"+title+"'");
			//nothing to remove
			return;
		}
		shareManager.removeSharedItem(resourceid, TAPMetadataLoader.SHARED_RESOURCE_TYPE_TABLE, owner);
	}



	private void changeToLowerCaseIfNeeded(Map<String,String> parameters, String paramId){
		if(parameters.containsKey(paramId)){
			String v = parameters.get(paramId);
			if(v == null || "".equals(v)){
				return;
			} else {
				parameters.put(paramId, v.toLowerCase());
			}
		}
	}
	
	private File getResultDataFileForJob(String jobId, UwsJobOwner currentUser) throws UwsException{
		UwsStorage storage = service.getFactory().getStorageManager();
		UwsManager uwsManager = UwsManager.getInstance();
		UwsJob job = uwsManager.tryLoadJob(jobId, currentUser);
		List<UwsJobResultMeta> results = job.getResults();
		if(results == null || results.size() != 1){
			throw new UwsException("Found no results for job '"+jobId+"'");
		}
		if(results.size() != 1){
			throw new UwsException("Found more than one results job '"+jobId+"'");
		}
		UwsJobResultMeta r = results.get(0);
		File f = storage.getJobResultDataFile(job, r.getId());
		return f;
	}
}
