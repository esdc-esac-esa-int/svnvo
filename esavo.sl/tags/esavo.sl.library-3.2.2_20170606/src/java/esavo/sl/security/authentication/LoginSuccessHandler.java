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
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import esavo.sl.security.UserContextServiceImpl;
import esavo.uws.UwsManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class  LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    @Override
    public void  onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

		try {
			UwsManager uwsManager = UwsManager.getInstance();
			UwsJobsOwnersManager jobsOwnersManager = uwsManager.getFactory().getJobsOwnersManager();
			UwsStorage storage = uwsManager.getFactory().getStorageManager();
			String ownerId = "";
			
			if(authentication.getName() != null && !"".equals(authentication.getName())){
				ownerId = authentication.getName();
			} else {
				ownerId = UwsUtils.ANONYMOUS_USER;
			}
			
			UwsJobOwner owner = jobsOwnersManager.loadOrCreateOwner(ownerId);
			
			//owner.setSession(request.getSession().getId());
			String session = UserContextServiceImpl.getSession();
			owner.setSession(session);
			String ip = UwsUtils.getClientIpAddr(request);
			owner.setIp(ip);
			
			// Get user quota (force calculation of real current size)
			//UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner,true);
			
			// Update user quotas and persist to DB
			UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(owner);
			storage.updateOwner(owner);
			
			UwsEventsManager eventsManager = uwsManager.getFactory().getEventsManager();
			eventsManager.setEventTime(owner, UwsEventType.LOGIN_IN_EVENT);
			
		} catch (Exception e) {
			throw new IOException("Error updating user quotas for user "+authentication.getName(),e);
		};
		
		String queryString = request.getQueryString();
		
		//String redirect = request.getParameter("redirect");
		if(queryString != null && queryString.contains("ticket")){
			super.onAuthenticationSuccess(request, response, authentication);
		}else{
			response.setStatus(UwsOutputResponseHandler.OK);
		}


    }
    
}
