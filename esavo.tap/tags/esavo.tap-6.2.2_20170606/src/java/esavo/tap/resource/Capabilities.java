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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.actions.UwsActionRequest;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.TAPService;

public class Capabilities implements TAPResource, VOSIResource {

	public static final String RESOURCE_NAME = "capabilities";
	private static final String[] RESOURCE_ITEMS = {RESOURCE_NAME};

	public static final String VOSI_ID = "ivo://ivoa.net/std/VOSI#capabilities";

	private TAPService service;
	private List<VOSIResource> resources;

	public void setVosiResouces(List<VOSIResource> resources){
		this.resources = resources;
	}
	
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
	public String getCapability(String baseUrl, UwsJobOwner owner) {
		return getCapability(getStandardID(), baseUrl, RESOURCE_NAME);
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException {
		UwsActionRequest actionRequest = new UwsActionRequest(service.getFactory().getAppId(), service.getFactory().getConfiguration(), request, null, null);
		String baseUrl = actionRequest.getBaseUrl();
		
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);

		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		xml.append("<vosi:capabilities xmlns:tr=\"http://www.ivoa.net/xml/TAPRegExt/v1.0\" \n");
		xml.append("		xmlns:vosi=\"http://www.ivoa.net/xml/VOSICapabilities/v1.0\" \n");
		xml.append("		xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.0\" \n");
		xml.append("		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n");
		xml.append("		xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VOSICapabilities/v1.0 \n");
		xml.append("			http://www.ivoa.net/xml/TAPRegExt/v1.0 http://www.ivoa.net/xml/TAPRegExt/v1.0 \n");
		xml.append("			http://www.ivoa.net/xml/VODataService/v1.0 http://www.ivoa.net/xml/VODataService/v1.0\"> \n");

		String tmp;
		for(VOSIResource vr: resources){
			tmp = vr.getCapability(baseUrl, user);
			xml.append(tmp).append("\n");
		}

		xml.append("\n</vosi:capabilities>");

		// Write the Capabilities resource into the ServletResponse:
		PrintWriter out = response.getWriter();
		out.print(xml.toString());
		out.flush();

		return true;
	}
	
	@Override
	public boolean canHandle(String action) {
		return RESOURCE_NAME.equals(action);
	}

	public static String getCapability(String id, String url, String capabilityName){
		String fullUrl = ((url==null)?""+capabilityName:url+"/"+capabilityName);
		return "\t<capability standardID=\""+id+"\">\n"
		+ "\t\t<interface xsi:type=\"vs:ParamHTTP\" role=\"std\">\n"
		+ "\t\t\t<accessURL use=\"full\"> "+fullUrl+" </accessURL>\n"
		+ "\t\t</interface>\n"
		+ "\t</capability>";
	}


}
