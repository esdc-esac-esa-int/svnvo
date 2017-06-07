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