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
package esavo.sl.dd;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.dd.requests.DDAuthorizationException;
import esavo.sl.dd.requests.DDException;
import esavo.sl.dd.requests.DDFunctions;
import esavo.sl.dd.requests.DDProperties;
import esavo.sl.dd.requests.DDRequestResult;
import esavo.sl.dd.requests.DDRetrievalAccess;
import esavo.sl.dd.requests.DDRetrievalRequest;
import esavo.sl.dd.util.DDFilePath;
import esavo.sl.dd.util.DDUtils;
import esavo.sl.tap.actions.EsacTapService;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;

public class DDDataRetrieval {
	private Logger logger = Logger.getLogger(DDDataRetrieval.class);
	
	private EsacTapService service;

	public DDDataRetrieval(EsacTapService service){
		this.service = service;
	}
	
	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, DDException {
		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e1) {
			throw new IOException("Cannot obtain current user: " + e1.getMessage(), e1);
		}

		UwsConfiguration configuration = service.getConfiguration();

		//Get parameters and create a request
		Map<String, String[]> properties = DDUtils.getPropertiesFromRequest(request);
		
		//Check properties: raise an exception
		try{
			checkProperties(properties);
		}catch(IllegalArgumentException iae){
			String completeUrl = DDUtils.getCompleteUrl(request);
			String msg = "Cannot process request '"+completeUrl+"' for user: "+user+",  due to: " + iae.getMessage();
			logger.info(msg);
			throw new IOException(msg, iae);
		}
		
		DDRetrievalRequest retrievalRequest = new DDRetrievalRequest(properties, user);
		String ip = UwsUtils.getClientIpAddr(request);
		retrievalRequest.setIp(ip);
		String dnsName = getDnsName(ip);
		retrievalRequest.setDnsName(dnsName);
		
		DDFunctions ddProcessor = service.getDataDistribution();
		
		DDRequestResult result = null;
		try {
			result = ddProcessor.process(retrievalRequest);
		} catch (DDAuthorizationException e){
			String completeUrl = DDUtils.getCompleteUrl(request);
			String msg = "Cannot process request '"+completeUrl+"' (req: "+retrievalRequest+"), for user: "+user+",  due to: " + e.getMessage();
			logger.info(msg);
			logger.info(UwsUtils.dumpStackTrace(e));
			throw new DDException(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		} catch (Exception e) {
			String completeUrl = DDUtils.getCompleteUrl(request);
			String msg = "Cannot process request '"+completeUrl+"' (req: "+retrievalRequest+"), for user: "+user+",  due to: " + e.getMessage();
			logger.info(msg);
			logger.info(UwsUtils.dumpStackTrace(e));
			throw new IOException(msg, e);
		}
		
		if(result.getStatusCode() != HttpServletResponse.SC_OK){
			throw new DDException(result.getStatusCode(), result.getMessage());
		}
		
		//If DIRECT retrieval: stream it
		try{
			DDRetrievalAccess retrievalAccess = retrievalRequest.getRetrievalAccess();
			switch(retrievalAccess){
			case DIRECT:
				executeDirectRetrieval(result, response, retrievalRequest, configuration);
				break;
			case IDS:
				executeIdsRetrieval(result, response, retrievalRequest, configuration);
				break;
			default:
				throw new IOException("Retrieval access '"+retrievalRequest.getRetrievalAccess()+"' not implemented yet. Request: " + retrievalRequest);
			}
		}catch(IOException ioe){
			String completeUrl = DDUtils.getCompleteUrl(request);
			String msg = "Cannot process request '"+completeUrl+"' (req: "+retrievalRequest+"), for user: "+user+",  due to: " + ioe.getMessage();
			logger.info(msg);
			logger.info(UwsUtils.dumpStackTrace(ioe));
			throw new IOException(msg, ioe);
		}
	}
	
	private void executeDirectRetrieval(DDRequestResult result, HttpServletResponse response, DDRetrievalRequest retrievalRequest, UwsConfiguration configuration) throws IOException{
		boolean compress = retrievalRequest.getPropertyBoolean(DDProperties.PARAM_ID_COMPRESS);
		boolean uncompress = retrievalRequest.getPropertyBoolean(DDProperties.PARAM_ID_UNCOMPRESS);
		//flag for creating tar always: default false
		boolean forceTar = retrievalRequest.getPropertyBoolean(DDProperties.PARAM_ID_FORCE_TAR);
		if(result.getNumberOfResults() == 1 && !forceTar){
			DDFilePath fpItem = result.getFirsFilePath();
			DDUtils.dumpToStream(fpItem, response, compress, uncompress);
		}else{
			//more than one file or forceTar=true: create a tar(.gz)
			String fileName = retrievalRequest.getReqIdComplete();
			List<DDFilePath> fpItems = result.getFilePaths();
			String reqIdComplete = result.getReqid();
			String repoTopLevel = configuration.getProperty(DDProperties.PROP_REPO_TOP_LEVEL);
			DDUtils.dumpTarToStream(fpItems, reqIdComplete, repoTopLevel, response, compress, fileName);
		}
	}
	
	private void executeIdsRetrieval(DDRequestResult result, HttpServletResponse response, DDRetrievalRequest retrievalRequest, UwsConfiguration configuration) throws IOException{
		List<DDFilePath> fpItems = result.getFilePaths();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_JSON);
		String format = retrievalRequest.getProperty(DDProperties.PARAM_ID_RA_IDS_FORMAT);
		PrintWriter pw = response.getWriter();
		if("json".equalsIgnoreCase(format)){
			executeIdsRetrievalJson(pw, fpItems);
		}else{
			executeIdsRetrievalDefault(pw, fpItems);
		}
	}
	
	private void executeIdsRetrievalJson(PrintWriter pw, List<DDFilePath> fpItems){
		pw.println("[");
		if(fpItems != null){
			boolean firstTime = true;
			String retrievalId;
			String info;
			String distributionPath;
			for(DDFilePath fp: fpItems){
				if(firstTime){
					firstTime = false;
				}else{
					pw.println(",");
				}
				retrievalId = fp.getRetrievalId();
				info = fp.getRetrievalInfo();
				distributionPath = fp.getDistributionPath();
				if(info != null){
					info = info.replaceAll("\"", "'");
					pw.print("{\""+retrievalId+"\": [\""+info+"\", \""+distributionPath+"\"]}");
				}else{
					pw.print("{\""+retrievalId+"\": [\"\", \""+distributionPath+"\"]}");
				}
			}
		}
		pw.println("\n]");
		pw.flush();
	}


	private void executeIdsRetrievalDefault(PrintWriter pw, List<DDFilePath> fpItems){
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<items>");
		if(fpItems != null){
			String retrievalId;
			String info;
			String distributionPath;
			String tmp;
			for(DDFilePath fp: fpItems){
				retrievalId = fp.getRetrievalId();
				tmp = UwsUtils.escapeXmlAttribute(retrievalId);
				pw.println("  <item id=\""+tmp+"\">");
				info = fp.getRetrievalInfo();
				distributionPath = fp.getDistributionPath();
				if(info != null){
					tmp = UwsUtils.escapeXmlData(info);
					pw.println("    <info>"+tmp+"</info>");
				}else{
					pw.println("    <info/>");
				}
				if(distributionPath != null){
					tmp = UwsUtils.escapeXmlData(distributionPath);
					pw.println("    <distribution_path>"+tmp+"</distribution_path>");
				}else{
					pw.println("    <distribution_path/>");
				}
				pw.println("  </item>");
			}
		}
		pw.println("</items>");
		pw.flush();
	}
	
	private void checkProperties(Map<String,String[]> properties){
		String[] values;
		if(!properties.containsKey(DDProperties.PARAM_ID_RETRIEVAL_TYPE)){
			throw new IllegalArgumentException("Parameter not found: " + DDProperties.PARAM_ID_RETRIEVAL_TYPE);
		}
		values = properties.get(DDProperties.PARAM_ID_RETRIEVAL_TYPE);
		if(values == null || values.length == 0){
			throw new IllegalArgumentException("Value not found for parameter: " + DDProperties.PARAM_ID_RETRIEVAL_TYPE);
		}
		if(values[0] == null || values[0].isEmpty()){
			throw new IllegalArgumentException("Value not found for parameter: " + DDProperties.PARAM_ID_RETRIEVAL_TYPE);
			
		}
	}
	
	private String getDnsName(String ip){
		if(ip == null){
			return null;
		}
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		} 
		String dnsName = inetAddress.getHostName(); 
		return dnsName;
	}

}
