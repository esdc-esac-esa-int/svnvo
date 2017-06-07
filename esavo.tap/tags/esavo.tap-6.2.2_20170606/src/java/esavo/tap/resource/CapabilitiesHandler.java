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
package esavo.tap.resource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

/**
 * Shows TAP capabilities
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface CapabilitiesHandler {
	
	/**
	 * Call to show the TAP system capabilities
	 * @param request
	 * @param response
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws TAPException
	 * @throws UwsException
	 */
	public void showCapabilities(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user)
			throws ServletException, IOException, TAPException, UwsException;
}
