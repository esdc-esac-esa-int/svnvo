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
package esavo.uws.utils.test.uws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

public class DummyUwsSecurityManager implements UwsSecurity {
	
	private UwsJobOwner user;
	
	public DummyUwsSecurityManager(){
		
	}

	@Override
	public UwsJobOwner getUser(HttpServletRequest request) throws UwsException {
		if(request != null){
			HttpSession session = request.getSession();
			if(session != null){
				user.setSession(session.getId());
			}
		}
		return user;
	}

	@Override
	public void setUser(UwsJobOwner user) {
		this.user = user;
	}
	
	public void reset(){
		this.user = null;
	}

}
