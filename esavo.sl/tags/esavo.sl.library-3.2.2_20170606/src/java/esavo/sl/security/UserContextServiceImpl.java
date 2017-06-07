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
package esavo.sl.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContextServiceImpl implements UserContextService {

	/* (non-Javadoc)
	 * @see esac.archive.gacs.security.UserContextService#getCurrentUser()
	 */
	@Override
	public String getCurrentUser(){
//		SecurityContext scc = SecurityContextHolder.getContext();
//		SecurityContextHolderStrategy s = SecurityContextHolder.getContextHolderStrategy();
//		SecurityContext sc = s.getContext();
//		System.out.println(SecurityContextHolder.getContextHolderStrategy());
//		System.out.println(System.getProperty("spring.security.strategy"));
		
		//SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

		return getUser();
	}
	
	public static String getUser(){
		if(SecurityContextHolder.getContext().getAuthentication()==null 
				|| SecurityContextHolder.getContext().getAuthentication().getName()==null 
				|| SecurityContextHolder.getContext().getAuthentication().getName().equalsIgnoreCase("anonymousUser")) return null;
			return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
	@Override
	public String getCurrentSessionId(){
		return getSession();
	}
	
	public static String getSession(){
		ServletRequestAttributes requestAttr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return requestAttr.getSessionId();
	}
	
	public static void clearContext() {
		SecurityContextHolder.clearContext();
	}


//	@Override
//	public String getAnonymousUserId() {
//		 ServletRequestAttributes requestAttr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//		 if(requestAttr.getRequest().getSession(true).getAttribute("anonymousUserId")==null){
//			 //System.out.println("test");
//			 //requestAttr.getRequest().getSession().setAttribute("anonymousUserId", requestAttr.getSessionId());
//			 requestAttr.getRequest().getSession().setAttribute("anonymousUserId", GeneralConstants.JOBOWNER_ANONYMOUS_ID);
//		 }
//		 return (String)requestAttr.getRequest().getSession(true).getAttribute("anonymousUserId");
//	}
}
