package esavo.sl.services.status;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.status.UwsStatusManager;
import esavo.uws.utils.status.UwsUserInfo;

public class IdManager {
	
	/** Part of HTTP content type header. */
	protected final TAPService service;

	public IdManager(TAPService serviceConnection) throws UwsException, TAPException {
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

		
		try{
			// DENY ACCESS TO UNAUTHENTICATED/UNAUTHORIZED USERS
			checkAuthentication(user);

			try{
				check();
			}catch(Exception e){
				throw e;
			}
			
			// Obtain task id:
			UwsStatusManager statusManager = UwsStatusManager.getInstance();
			Long id = statusManager.createUserIdentifier(new UwsUserInfo(user));
			
			String jsonOutput= "{ 'id': '"+id.longValue()+"' }";
			
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(jsonOutput);
			out.flush();
		}catch(Throwable t){
			String jsonOutput= "{ 'failed': '"+t.getMessage()+"' }";
			
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(jsonOutput);
			out.flush();
		}
	}


	/**
	 * Checks the validity of the upload or delete request.
	 * @throws IOException 
	 */
	private void check() throws IOException{
	}
	
	/**
	 * Checks corresponding to a table upload.
	 * @throws IOException
	 */
	private void checkAuthentication(UwsJobOwner owner){
		if (owner == null || owner.getAuthUsername()==null) {
			throw new InvalidParameterException("Task ID request error: user must be logged in to get a task ID.");
		}
	}

}
