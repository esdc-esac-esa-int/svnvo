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
package esavo.uws.security;

import javax.servlet.http.HttpServletRequest;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

public interface UwsSecurity {
	
	/**
	 * Returns the user in the current communication.
	 * @param request servlet request: can be null
	 * @return
	 */
	public UwsJobOwner getUser(HttpServletRequest request) throws UwsException;
	
	/**
	 * Sets the user object associated to a the current communication.<br/>
	 * (This method can be empty if the security is handled by a subsystem like spring security)
	 * @param user
	 */
	public void setUser(UwsJobOwner user);

}
