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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;
import esavo.tap.LimitUnit;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPService;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.parameters.TAPParameters;

public class TAP implements VOSIResource, CapabilitiesHandler {
	
	public static final String VOSI_ID = "ivo://ivoa.net/std/TAP";

	private static final Logger LOG = Logger.getLogger(TAP.class.getName());
	
	protected final Map<String, TAPResource> resources;

	private TAPService service;
	

	public TAP(TAPService service) throws UwsException, TAPException {
		this.service = service;
		
		resources = new HashMap<String, TAPResource>();
		List<VOSIResource> vosiResources = new ArrayList<VOSIResource>();

		vosiResources.add(this);

		Jobs jobs = new Jobs(this);
		jobs.init(service);
		resources.put(jobs.getName(), jobs);

		JobsList jobsList = new JobsList();
		jobsList.init(service);
		resources.put(jobsList.getName(), jobsList);
		
		Availability availability = new Availability();
		availability.init(service);
		resources.put(availability.getName(), availability);
		vosiResources.add(availability);

		Tables tables = new Tables();
		tables.init(service);
		resources.put(tables.getName(), tables);
		
		Schemas schemas = new Schemas();
		schemas.init(service);
		resources.put(schemas.getName(), schemas);
		
		Share share = new Share();
		share.init(service);
		resources.put(share.getName(), share);
		
		DeleteMultipleJobs deleteMultipleJobs = new DeleteMultipleJobs();
		deleteMultipleJobs.init(service);
		resources.put(deleteMultipleJobs.getName(), deleteMultipleJobs);
		
		Notifications notifications = new Notifications();
		notifications.init(service);
		resources.put(notifications.getName(), notifications);
		
		Stats stats = new Stats();
		stats.init(service);
		resources.put(stats.getName(), stats);
		
		Tasks tasks = new Tasks();
		tasks.init(service);
		resources.put(tasks.getName(), tasks);
		
		Events events = new Events();
		events.init(service);
		resources.put(events.getName(), events);

		Users users = new Users();
		users.init(service);
		resources.put(users.getName(), users);

		Admin admin = new Admin();
		admin.init(service);
		resources.put(admin.getName(), admin);

		Scheduler scheduler = new Scheduler();
		scheduler.init(service);
		resources.put(scheduler.getName(), scheduler);

		Functions functions = new Functions();
		functions.init(service);
		resources.put(functions.getName(), functions);

		Capabilities capabilities = new Capabilities();
		capabilities.init(service);
		resources.put(capabilities.getName(), capabilities);
		vosiResources.add(capabilities);
		capabilities.setVosiResouces(vosiResources);
	}

	@Override
	public String getStandardID() {
		return VOSI_ID;
	}

	@Override
	public String getCapability(String baseUrl, UwsJobOwner owner) {
		StringBuffer xml = new StringBuffer();

		xml.append("\t<capability standardID=\"").append(getStandardID()).append("\" xsi:type=\"tr:TableAccess\">\n");
		xml.append("\t\t<interface role=\"std\" xsi:type=\"vs:ParamHTTP\">\n");
		xml.append("\t\t\t<accessURL use=\"base\">").append(baseUrl+"/tap").append("</accessURL>\n");
		xml.append("\t\t</interface>\n");
		xml.append("\t\t<language>\n");
		xml.append("\t\t\t<name>ADQL</name>\n");
		xml.append("\t\t\t<version ivo-id=\"ivo://ivoa.net/std/ADQL#v2.0\">2.0</version>\n");
		xml.append("\t\t\t<description>ADQL 2.0</description>\n");
		xml.append("\t\t</language>\n");
		
		OutputFormat[] formatters = service.getFactory().getOutputFormats();

		if(formatters != null){
			for(OutputFormat formatter: formatters){
				xml.append("\t\t<outputFormat>\n");
				xml.append("\t\t\t<mime>").append(formatter.getMimeType()).append("</mime>\n");
				if (formatter.getShortMimeType() != null)
					xml.append("\t\t\t<alias>").append(formatter.getShortMimeType()).append("</alias>\n");
				if (formatter.getDescription() != null)
					xml.append("\t\t\t<description>").append(formatter.getDescription()).append("</description>\n");
				xml.append("\t\t</outputFormat>\n");
			}
		}

		UwsConfiguration config = service.getFactory().getConfiguration();
		boolean uploadEnabled = Boolean.parseBoolean(config.getProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED));
		
		if (uploadEnabled){
			// Write upload methods: INLINE, HTTP, FTP:
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.org/tap/uploadmethods#inline\" />\n");
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.org/tap/uploadmethods#http\" />\n");
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.org/tap/uploadmethods#ftp\" />\n");
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.net/std/TAPRegExt#upload-inline\" />\n");
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.net/std/TAPRegExt#upload-http\" />\n");
			xml.append("\t\t\t<uploadMethod ivo-id=\"ivo://ivoa.net/std/TAPRegExt#upload-ftp\" />\n");
		}

		int[] retentionPeriod = service.getRetentionPeriod();
		if (retentionPeriod != null && retentionPeriod.length >= 2){
			if (retentionPeriod[0] > -1 || retentionPeriod[1] > -1){
				xml.append("\t\t<retentionPeriod>\n");
				if (retentionPeriod[0] > -1)
					xml.append("\t\t\t<default>").append(retentionPeriod[0]).append("</default>\n");
				if (retentionPeriod[1] > -1)
					xml.append("\t\t\t<hard>").append(retentionPeriod[1]).append("</hard>\n");
				xml.append("\t\t</retentionPeriod>\n");
			}
		}

		long[] executionDuration=new long[]{0,0};
		try {
			executionDuration = service.getExecutionDuration("sync",owner);
		} catch (UwsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (executionDuration != null && executionDuration.length >= 2){
			if (executionDuration[0] > -1 || executionDuration[1] > -1){
				xml.append("\t\t<executionDuration>\n");
				if (executionDuration[0] > -1)
					xml.append("\t\t\t<default>").append(executionDuration[0]).append("</default>\n");
				if (executionDuration[1] > -1)
					xml.append("\t\t\t<hard>").append(executionDuration[1]).append("</hard>\n");
				xml.append("\t\t</executionDuration>\n");
			}
		}

		int[] outputLimit = service.getOutputLimit();
		LimitUnit[] outputLimitType = service.getOutputLimitType();
		if (outputLimit != null && outputLimit.length >= 2 && outputLimitType != null && outputLimitType.length >= 2){
			if (outputLimit[0] > -1 || outputLimit[1] > -1){
				xml.append("\t\t<outputLimit>\n");
				if (outputLimit[0] > -1)
					xml.append("\t\t\t<default unit=\"").append(outputLimitType[0].name().toLowerCase()).append("\">").append(outputLimit[0]).append("</default>\n");
				if (outputLimit[1] > -1)
					xml.append("\t\t\t<hard unit=\"").append(outputLimitType[1].name().toLowerCase()).append("\">").append(outputLimit[1]).append("</hard>\n");
				xml.append("\t\t</outputLimit>\n");
			}
		}

		if (uploadEnabled){
			// Write upload limits:
			int[] uploadLimit = service.getUploadLimit();
			LimitUnit[] uploadLimitType = service.getUploadLimitType();
			if (uploadLimit != null && uploadLimit.length >= 2 && uploadLimitType != null && uploadLimitType.length >= 2){
				if (uploadLimit[0] > -1 || uploadLimit[1] > -1){
					xml.append("\t\t<uploadLimit>\n");
					if (uploadLimit[0] > -1)
						xml.append("\t\t\t<default unit=\"").append(uploadLimitType[0].name().toLowerCase()).append("\">").append(uploadLimit[0]).append("</default>\n");
					if (uploadLimit[1] > -1)
						xml.append("\t\t\t<hard unit=\"").append(uploadLimitType[1].name().toLowerCase()).append("\">").append(uploadLimit[1]).append("</hard>\n");
					xml.append("\t\t</uploadLimit>\n");
				}
			}
		}

		xml.append("\t</capability>");

		return xml.toString();
	}

	public void executeRequest(HttpServletRequest request, HttpServletResponse response) {
		//response.setContentType("text/plain");
		
//		Enumeration ee = request.getHeaderNames();
//		String hn;
//		System.out.println("--Headers--" + request.getRequestURL());
//		while(ee.hasMoreElements()){
//			hn = ee.nextElement().toString();
//			System.out.println(hn + ": '"+request.getHeader(hn)+"'");
//		}

		TAPFactory factory = service.getFactory();
		UwsSecurity security = factory.getSecurityManager();
		UwsJobOwner user;
		try {
			//System.out.println("TAP req: " + request.getRequestURL());
			user = security.getUser(request);
			//System.out.println("User: " + user + "#session:" + user.getSession() + ", #IP: " + user.getIp());
		} catch (UwsException e2) {
			try{
			service.getFactory().getOutputHandler().writeServerErrorResponse(
					response, 
					UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, 
					"Getting current user: " + e2.getMessage(), 
					e2,
					UwsExceptionOutputFormat.HTML);
			}catch(Exception e){
			}
			return;
		}
		
		//This code allows to act like the specified user
		//
//		UwsJobOwner u = new UwsJobOwner("<USER_LOGIN_ID>", UwsJobOwner.ROLE_USER);
//		u.setParameters(user.getParameters());
//		u.setAuthUsername("<USER_LOGIN_ID>");
//		user = u;
		
		//FIXME session is extracted from servlet/security context
		//Set current session to user
		//user.setSession(getJobSession(request));
		
		String resourceName = null;
		String[] resourcePath = (request.getPathInfo() == null) ? null : request.getPathInfo().split("/");
		
		try {
			if (resourcePath == null || resourcePath.length < 1) {
				resourceName = "homePage";
				response.setContentType("text/html");
				UwsActionRequest actionRequest = new UwsActionRequest(service.getFactory().getAppId(), service.getFactory().getConfiguration(), request, null, null);
				writeHomePage(response.getWriter(), user,
						actionRequest.getBaseUrl());
			} else {
				resourceName = resourcePath[1].trim().toLowerCase();
				for(TAPResource res: resources.values()){
					if(res.canHandle(resourceName)){
						res.executeResource(request, response, user);
						return;
					}
				}
				//No resource handler found: error
				try{
					service.getFactory().getOutputHandler().writeServerErrorResponse(
							response,
							UwsOutputResponseHandler.INTERNAL_SERVER_ERROR,
							"Getting TAP resource",
							null,
							"This TAP service does not have a resource named '"	+ resourceName + "' !");
				}catch(IllegalStateException e){
					String msg = "Cannot write error for request: " + request.getRequestURI() + ", user: " + user + ", due to: " + e.getMessage();
					LOG.severe(msg + "\n" + UwsUtils.dumpStackTrace());
					throw new IllegalStateException(msg, e);
				}
			}
		} catch (Exception e) {
			try {
				String msg = "Cannot execute request (I): " + request.getRequestURI() + ", user: " + user + ", due to: " + e.getMessage();
				LOG.severe(msg + "\n" + UwsUtils.dumpStackTrace());
				LOG.severe("Exception stack (I):\n" + UwsUtils.dumpStackTrace(e));
				service.getFactory().getOutputHandler().writeServerErrorResponse(
						response, 
						UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, 
						"Executing " + resourceName, 
						e,
						UwsExceptionOutputFormat.HTML);
			} catch (IllegalStateException e2){
				String msg = "Cannot write error (II) for request: " + request.getRequestURI() + ", user: " + 
			user + ", due to: " + e.getMessage() + " ("+e2.getMessage()+")";
				LOG.severe(msg + "\n" + UwsUtils.dumpStackTrace());
				LOG.severe("Exception stack (II):\n" + UwsUtils.dumpStackTrace(e));
				throw new IllegalStateException(msg, e2);
			} catch (UwsException e1) {
				e1.printStackTrace();
			}
		}		
	}
	
	private String getJobSession(HttpServletRequest request){
		String value = request.getParameter(TAPParameters.PARAM_SESSION);
		if(value != null){
			return value;
		}
		return request.getParameter(TAPParameters.PARAM_SESSION.toUpperCase());
	}


	public void writeHomePage(final PrintWriter writer, final UwsJobOwner user, final String tapBaseUrl) throws IOException {
		writer.println("<html><head><title>TAP HOME PAGE</title></head><body><h1 style=\"text-align: center\">TAP HOME PAGE</h1><h2>Available resources:</h2><ul>");
		String[] resourceItems;
		for(TAPResource res : resources.values()){
			resourceItems = res.getResourceItems();
			for(String ri: resourceItems){
				writer.println("<li><a href=\""+tapBaseUrl+"/"+ri+"\">"+ri+"</a></li>");
			}
		}
		writer.println("</ul></body></html>");
		writer.flush();
	}

	public void writeError(TAPException ex, HttpServletResponse response) throws ServletException, IOException {
		service.getFactory().getLogger().error(ex);
		response.reset();
		response.setStatus(ex.getHttpErrorCode());
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);
		writeError(ex, response.getWriter());
	}

	protected void writeError(TAPException ex, PrintWriter output) throws ServletException, IOException {
		output.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		output.println("<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.2\" version=\"1.2\">");
		output.println("\t<RESOURCE type=\"results\">");

		// Print the error:
		output.println("\t\t<INFO name=\"QUERY_STATUS\" value=\"ERROR\">");
		output.print("\t\t\t<![CDATA[ ");
		if (ex.getExecutionStatus()!=null)
			output.print("[WHILE "+ex.getExecutionStatus()+"] ");
		output.print(ex.getMessage().replace('«', '\"').replace('»', '\"'));
		output.println("]]>\t\t</INFO>");

		// Print the current date:
		
		output.print("\t\t<INFO name=\"DATE\" value=\""); output.print(UwsUtils.formatDate(new Date())); output.println("\" />");
		
		UwsConfiguration config = service.getFactory().getConfiguration();

		// Print the provider (if any):
		String provider = config.getProperty(TAPService.CONF_PROP_TAP_PROVIDER_NAME);
		if (provider != null){
			output.print("\t\t<INFO name=\"PROVIDER\" value=\""); output.print(provider);
			String providerDesc = config.getProperty(TAPService.CONF_PROP_TAP_PROVIDER_DESC);
			if (providerDesc != null){
				output.print("\">\n\t\t\t<![CDATA[");
				output.print(providerDesc);
				output.println("]]>\n\t\t</INFO>");
			}else
				output.println("\">");
		}

		// Print the query (if any):
		if (ex.getQuery() != null){
			output.print("\t\t<INFO name=\"QUERY\">\n\t\t\t<![CDATA[");
			output.println(ex.getQuery());
			output.println("]]>\t\t</INFO>");
		}

		output.println("\t</RESOURCE>");
		output.println("</VOTABLE>");

		output.flush();
	}

	@Override
	public void showCapabilities(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) 
			throws ServletException, IOException, TAPException, UwsException {
		resources.get(Capabilities.RESOURCE_NAME).executeResource(request, response, user);
	}

}
