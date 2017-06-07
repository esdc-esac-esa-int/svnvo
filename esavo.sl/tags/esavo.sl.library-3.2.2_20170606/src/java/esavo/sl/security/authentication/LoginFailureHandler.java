package esavo.sl.security.authentication;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import esavo.uws.output.UwsOutputResponseHandler;


public class  LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	static Logger logger = Logger.getLogger(LoginFailureHandler.class);
	
	@Override
    public void  onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws ServletException, IOException {
    	
    	// Ask for basic athentication only if no username is provided
    	if(exception instanceof AuthenticationCredentialsNotFoundException){
    		response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication needed\"");
    	}
   	   	response.sendError(UwsOutputResponseHandler.UNAUTHORIZED, "Unauthorized" );
    }
}