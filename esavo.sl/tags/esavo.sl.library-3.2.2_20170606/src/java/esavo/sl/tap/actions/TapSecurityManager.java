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
package esavo.sl.tap.actions;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import esavo.sl.security.UserContextService;
import esavo.sl.security.UserContextServiceImpl;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;

public class TapSecurityManager implements UwsSecurity {
	
	private static final Logger LOG = Logger.getLogger(TapSecurityManager.class.getName());
	
	private String appid;
	
	public TapSecurityManager(String appid){
		//Black magic to make sync/async threads work with springsecuirty 4.0 in command line, blah,blah,blah...
		//It is set in springsecurity xml config (beans...)
		//SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		this.appid = appid;
	}
	
	@Override
	public UwsJobOwner getUser(HttpServletRequest request) throws UwsException {
		UserContextService userContextService = new UserContextServiceImpl();
		String authUsername = userContextService.getCurrentUser();
		String ownerid = getOwnerId(authUsername);
		String pseudo = getPseudo(authUsername);
		
//		String sessionId = null;
//		if(request != null){
//			HttpSession servletSession = request.getSession();
//			if(servletSession != null){
//				sessionId = servletSession.getId();
//			}
//		}
		String sessionId = null;
		if(request != null){
			try{
				sessionId = userContextService.getCurrentSessionId();
			}catch(IllegalStateException e){
				String msg = "Cannot get session for user: " + ownerid + 
						", request: '" + request.getRequestURI() + "'";
				LOG.severe(msg + ": " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
				throw new IllegalStateException(msg, e);
			}
		}
		
		UwsManager uwsManager = UwsManager.getInstance();
		UwsJobsOwnersManager jobsOwnersManager = uwsManager.getFactory().getJobsOwnersManager(); 
		UwsJobOwner owner = jobsOwnersManager.loadOrCreateOwner(ownerid);
		
		owner.setAuthUsername(authUsername);
		owner.setPseudo(pseudo);
		owner.setSession(sessionId);
		String ip = UwsUtils.getClientIpAddr(request);
		owner.setIp(ip);
		//System.out.println(owner);

		return owner;
	}


	
	/**
	 * If authUserName == null   : ownerId = "anonymous";
	 * If authUserName == ""     : ownerId = "anonymous";
	 * If authUserName == [value]: ownerId = [value];
	 * @param authUsername
	 * @return
	 */
	private String getOwnerId(String authUsername){
		if(authUsername != null && !"".equals(authUsername)){
			return authUsername;
		} else {
			return UwsUtils.ANONYMOUS_USER;
		}
	}
	
	/**
	 * If authUserName == null   : pseudo = "anonymous"
	 * If authUserName == ""     : pseudo = ""
	 * If authUserName == [value]: pseudo = [value]
	 * @param authUserName
	 * @return
	 */
	private String getPseudo(String authUserName){
		if(authUserName != null){
			return authUserName;
		} else {
			return UwsUtils.ANONYMOUS_USER;
		}
	}
	
	@Override
	public void setUser(UwsJobOwner user) {
		//Nothing to do, setUser is done by spring security
	}
	
	@Override
	public String toString(){
		return "TAP Security Manager for application '"+appid+"'";
	}


}
