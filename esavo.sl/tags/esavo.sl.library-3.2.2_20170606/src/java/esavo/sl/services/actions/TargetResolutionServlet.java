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
package esavo.sl.services.actions;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.services.nameresolution.TargetResolutionManager;

/**
 * 
 * @author Raul Gutierrez-Sanchez
 *
 */
public class TargetResolutionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(TargetResolutionServlet.class);

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside TargetResolutionServlet.service()");
		try {
			TargetResolutionManager manager = TargetResolutionManager.getInstance();
			// 2. Forward all requests to the TAP instance:
			manager.executeRequest(request, response);

		}catch(Throwable t){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}
}
