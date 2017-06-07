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
package esavo.sl.services.nameresolution.actions.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.nameresolution.TargetResolutionException;

public interface TargetResolutionActionHandler {
	
	public String getAction();
	
	/**
	 * Returns 'true' if the action can be handled.
	 * 
	 * @param request
	 * @return
	 * @throws TargetResolutionException
	 */
	public boolean canHandle(HttpServletRequest request) throws TargetResolutionException;
	
	/**
	 * Handles the action.
	 * @param uwsManager application manager.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @param response
	 * @throws IOException
	 */
	public void handle(HttpServletRequest actionRequest, HttpServletResponse response) throws TargetResolutionException;

}
