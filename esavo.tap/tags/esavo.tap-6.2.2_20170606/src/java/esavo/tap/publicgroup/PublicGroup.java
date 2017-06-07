package esavo.tap.publicgroup;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import esavo.tap.TAPService;
import esavo.tap.TapUtils;
import esavo.uws.UwsException;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;

/**
 * Public group functionality
 * <pre><tt>
 * ACTION=add|remove|list|list_all
 * TABLE=list of tables (comma separated values). Only required for add and remove
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class PublicGroup {
	
	protected final TAPService service;
	
	public PublicGroup(TAPService serviceConnection) {
		service = serviceConnection;
	}

	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e1) {
			throw new ServletException("Cannot obtain current user: " + e1.getMessage(), e1);
		}
		
		try {
			// Identify the user:
			
			//DENY ACCESS TO UNAUTHENTICATED/UNAUTHORIZED USERS
			if(UwsUtils.isAnonymous(user)){
				throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, 
						"User '" + user.getId() + "' is not authorized to use 'publi group' capabilities");
			}
			
			String action = request.getParameter(PublicGroupHandler.PARAM_ACTION);
			PublicGroupManager manager = PublicGroupManager.getInstance();
			PublicGroupHandler handler = manager.getSuitableHandler(action);
			if(handler == null) {
				throw new IllegalArgumentException("Cannot find handler for action: '"+action+
						"'.\nAvailable actions are: " + manager.getValidActions());
			}
			
			Map<String,String> parameters = new HashMap<String, String>();
			@SuppressWarnings("unchecked")
			Enumeration<String> e = request.getParameterNames();
			String paramName;
			while(e.hasMoreElements()){
				paramName = e.nextElement();
				parameters.put(paramName, request.getParameter(paramName));
			}
			
			//add public group id as parameter
			//UwsConfiguration configuration = service.getConfiguration();
			//String publicGroupId = configuration.getProperty(TapServiceConnection.PUBLIC_GROUP_ID_PROPERTY);
			//parameters.put(PublicGroupHandler.PARAM_PUBLIC_GROUP_ID, publicGroupId);
			//String publicGroupOwnerId = configuration.getProperty(TapServiceConnection.PUBLIC_GROUP_OWNER_ID_PROPERTY);
			//parameters.put(PublicGroupHandler.PARAM_PUBLIC_GROUP_OWNER_ID, publicGroupOwnerId);

			//JDBCPooledFunctions dbConn = (JDBCPooledFunctions)service.getFactory().createDBConnection("PublicGroupConnection");

			handler.handle(user, response, service, parameters);

			response.flushBuffer();
		}catch(Throwable t){
			//errorWriter.writeError(t, response, request, owner, "Updating tables."); 
			try {
				service.getFactory().getOutputHandler().writeServerErrorResponse(
						response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Public Group Action Error", t, UwsExceptionOutputFormat.HTML);
			} catch (UwsException e1) {
				throw new ServletException(e1);
			}
		}


	}
	

}
