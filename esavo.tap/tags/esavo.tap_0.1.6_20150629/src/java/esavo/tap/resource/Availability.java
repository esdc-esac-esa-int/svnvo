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

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.TAPService;


public class Availability implements TAPResource, VOSIResource {

	public static final String RESOURCE_NAME = "availability";
	private static final String[] RESOURCE_ITEMS = {RESOURCE_NAME};
	
	public static final String VOSI_ID = "ivo://ivoa.net/std/VOSI#availability";

	private TAPService service;

	@Override
	public void init(TAPService service) {
		this.service = service;
	}

	@Override
	public final String getName() {
		return RESOURCE_NAME;
	}
	
	@Override
	public String[] getResourceItems(){
		return RESOURCE_ITEMS;
	}

	@Override
	public final String getStandardID() {
		return VOSI_ID;
	}

	@Override
	public String getCapability(String baseUrl) {
		return Capabilities.getCapability(VOSI_ID, baseUrl, RESOURCE_NAME);
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException {
		if (!request.getMethod().equalsIgnoreCase("GET"))	// ERREUR 405 selon VOSI (cf p.4)
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The AVAILABILITY resource is only accessible in HTTP-GET !");

		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);
		
		String explanation = service.getFactory().getConfiguration().getProperty(TAPService.CONF_PROP_TAP_WELCOME_MESSAGE);

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		xml += "<availability xmlns=\"http://www.ivoa.net/xml/VOSIAvailability/v1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSIAvailability/v1.0 http://www.ivoa.net/xml/VOSIAvailability/v1.0\">\n";
		xml += "\t<available>true</available>\n\t<note>"+explanation+"</note>\n";
		xml += "</availability>";

		PrintWriter pw = response.getWriter();
		pw.print(xml);
		pw.flush();

		return true;
	}

	@Override
	public boolean canHandle(String action) {
		return RESOURCE_NAME.equals(action);
	}


}
