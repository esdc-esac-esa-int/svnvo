/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
