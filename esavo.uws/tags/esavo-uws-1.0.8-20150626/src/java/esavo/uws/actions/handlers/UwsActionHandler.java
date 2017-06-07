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
package esavo.uws.actions.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.owner.UwsJobOwner;

public interface UwsActionHandler {
	
	public String getActionHandlerIdentifer();
	public String getActionName();
	
	/**
	 * Returns 'true' if the action can be handled.
	 * @param appid application identifier.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @return
	 * @throws IOException
	 */
	public boolean canHandle(String appid, UwsJobOwner currentUser, UwsActionRequest actionRequest) throws UwsException;
	
	/**
	 * Handles the action.
	 * @param uwsManager application manager.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @param response
	 * @throws IOException
	 */
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException;

}
