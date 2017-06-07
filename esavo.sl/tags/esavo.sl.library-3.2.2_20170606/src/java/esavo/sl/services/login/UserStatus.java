package esavo.sl.services.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

public class UserStatus {
	
	private TAPService service;

	public UserStatus(TAPService serviceConnection) {
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


		if(user.getAuthUsername()!=null){
			UserUtils.sendResponseWithUserDetails(user, response);
		}

	}

}
