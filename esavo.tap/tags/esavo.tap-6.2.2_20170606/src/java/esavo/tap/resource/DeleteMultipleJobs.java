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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionsManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.TAPException;
import esavo.tap.TAPService;

public class DeleteMultipleJobs implements TAPResource {
	
	private static final String NAME = "deletejobs";
	private static final String[] RESOURCE_ITEMS = {NAME};
	
	private TAPService service;
	
	@Override
	public void init(TAPService service) {
		this.service = service;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String[] getResourceItems(){
		return RESOURCE_ITEMS;
	}

	@Override
	public boolean canHandle(String action) {
		return NAME.equals(action);
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException,
			TAPException, UwsException {
		UwsManager manager = UwsManager.getInstance();
		UwsActionsManager actionsManager = UwsActionsManager.getInstance();
		List<String> toIgnoreParameters = service.getUwsJobsToIgnoreParameters();
		actionsManager.executeRequest(manager, user, request, response, toIgnoreParameters);
		return true;
	}

}
