package esavo.sl.services.actions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import esac.archive.absi.interfaces.common.model.exceptions.RemoteServiceException;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

//TODO extends from AIOServlet, is this required?
//import esac.archive.absi.modules.cl.aio.servlet.AIOServlet;
//public abstract class AbstractFileUploadServlet extends AIOServlet {

public abstract class AbstractFileUploadServlet extends HttpServlet {


	private static final long serialVersionUID = 821078973347641070L;


	/** Logger */
	private static Logger logger = Logger.getLogger(TargetListFileUploadServlet.class);
	
	private String appid;
	private EsacTapService service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
//		appid = EnvironmentManager.getAppId(config);
//		EnvironmentManager.initGenericServletEnvironment(appid, config);
//		
//		/*
//		 * Set init properties (using System props)
//		 */
//		//System.setProperty(UPLOAD_DIR_PROPERTY,	config.getInitParameter(UPLOAD_DIR_PROPERTY));
//		
//		EnvironmentManager.setPropertyIfNotNull(appid, 
//				EnvironmentManager.UPLOAD_DIR_PROPERTY, 
//				config.getInitParameter(EnvironmentManager.UPLOAD_DIR_PROPERTY));
		
		ServletContext context = getServletContext();
		appid = UwsUtils.getAppIdFromContext(context, config);
		if(appid == null){
			throw new IllegalArgumentException("Application identifier must be defined. Use configuration variable: '"+UwsConfiguration.CONFIG_APP_ID+"'");
		}

		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		UwsUtils.updateConfiguration(configuration, context);
		UwsUtils.updateConfiguration(configuration, config);

		//Initialize
		try {
			service = TapServiceConnection.getInstance(appid);
		} catch (UwsException e) {
			throw new ServletException(e);
		} catch (TAPException e) {
			throw new ServletException(e);
		}

	}

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("");
		logger.debug("=====================================================================");
		logger.debug("Inside FileUploadServlet.doGet()");
		logger.debug("GET not supported");
		//super.doGet(req, resp);
		return;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		logger.debug("");
		logger.debug("=====================================================================");
		logger.debug("Inside FileUploadServlet.doPost()");

		// Standard check for file upload
		if (ServletFileUpload.isMultipartContent(request)) {

			// Create FileItemFactory and ServletFileUpload objects
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse request
			PrintWriter out = response.getWriter();
			File uploadedFile = null;
			try {
				List<FileItem> items = upload.parseRequest(request);
				logger.debug("Num items = " + items.size());
				for (FileItem item : items) {
					// Process only file items, discard form fields
					if (item.isFormField())
						continue;

					String fileName = item.getName();
					logger.debug("Filename = " + fileName);

					// Get the file name
					if (fileName != null) {
						fileName = FilenameUtils.getName(fileName);
					}

					// Create locally the file in the server (to be deleted after processing)
					//String uploadDir = System.getProperty(UPLOAD_DIR_PROPERTY);
					String uploadDir = service.getProperty(TapServiceConnection.UPLOAD_DIR_PROPERTY);
					logger.debug("Upload directory is [" + uploadDir  +"]");					
					File uploadDirectory = new File(uploadDir);
					logger.debug("Upload directory full path is [" + uploadDirectory.getAbsolutePath()  +"]");					
					if (!uploadDirectory.exists())
					{
						throw new IOException("Upload directory [" + uploadDir + "] must exist");
					}
					
					uploadedFile = new File(uploadDir, fileName);
					logger.debug("Creating upload file [" + uploadedFile.getAbsolutePath()  +"]");					
					if (uploadedFile.createNewFile()) {
						item.write(uploadedFile);

						// TODO: check whole file content before starting to resolve each target line by line?

						Scanner scanner = new Scanner(new FileReader(uploadedFile));
						try {
							
							// This part must be implemented by the class extending this one
							performActionWithFileContent(out, scanner); 

						} catch (RemoteServiceException e) {
							throw e;
						} finally {
							// ensure the underlying stream is always closed
							// this only has any effect if the item passed to
							// the Scanner
							// constructor implements Closeable (which it does
							// in this case).
							scanner.close();
						}

						response.setStatus(HttpServletResponse.SC_OK);
						response.flushBuffer();

						logger.debug("Deleting file " + uploadedFile.getAbsolutePath());
						uploadedFile.delete();
					} 
					else {
						// The file being uploaded already exists. It should have been deleted.
						// Check why it is still there.
						throw new IOException("File " + uploadedFile + " already exists");
					}
				}
			} catch (Exception e) {
				logger.error("Error Searching by Target from file: " + e.getMessage(), e);
				
				// Rename uploaded file and leave it in the server to check possible issues
				renameUploadedFile(uploadedFile);
				
				// Send error response
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"An error occurred processing Target List File: " + e.getMessage());
				
			} finally {
				// Flush and close the PrintWriter
				try {
					out.flush();
					out.close();
				} catch (Exception e) {
					logger.log(Level.ERROR, "Exception closing PrintWriter", e);
				}
			}

		} else {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"El tipo de contenido solicitado no es compatible con el servlet");
		}
		logger.debug("End FileUploadServlet.doPost()");
	}
	
	protected abstract void performActionWithFileContent(PrintWriter out, Scanner scanner) throws RemoteServiceException;
	
	
	private void renameUploadedFile(File uploadedFile) {
		if (uploadedFile != null && uploadedFile.exists()) {
			//String base = System.getProperty(UPLOAD_DIR_PROPERTY);
			String base = service.getProperty(TapServiceConnection.UPLOAD_DIR_PROPERTY);
			File renamedFile = new File(base, 
					uploadedFile.getName() + "." + System.currentTimeMillis());
			uploadedFile.renameTo(renamedFile);
		}
	}

	
}
