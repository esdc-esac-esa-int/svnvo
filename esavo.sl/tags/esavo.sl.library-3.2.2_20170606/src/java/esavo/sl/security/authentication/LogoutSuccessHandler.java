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

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import esavo.sl.security.UserContextServiceImpl;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.utils.UwsUtils;

public class  LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    @Override
    public void  onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
    	
    	UwsQuotaSingleton.getInstance().removeUser(authentication.getName());
    	response.setStatus(UwsOutputResponseHandler.OK);
        //clearAuthenticationAttributes(request);
    	
    	UwsManager uwsManager = UwsManager.getInstance();
    	UwsEventsManager eventsManager = uwsManager.getFactory().getEventsManager();
    	
		String ownerId = "";
		
		if(authentication.getName() != null && !"".equals(authentication.getName())){
			ownerId = authentication.getName();
		} else {
			ownerId = UwsUtils.ANONYMOUS_USER;
		}
		
		UwsJobOwner owner = new UwsJobOwner(ownerId, UwsJobOwner.ROLE_USER);
		//owner.setSession(request.getSession().getId());
		String session = UserContextServiceImpl.getSession();
		owner.setSession(session);
		String ip = UwsUtils.getClientIpAddr(request);
		owner.setIp(ip);

    	try {
			eventsManager.setEventTime(owner, UwsEventType.LOGIN_OUT_EVENT);
			
	    	UserContextServiceImpl.clearContext();
		} catch (UwsException e) {
			throw new IOException("Error updating user quotas for user "+authentication.getName(),e);
		}
    }
    
}
