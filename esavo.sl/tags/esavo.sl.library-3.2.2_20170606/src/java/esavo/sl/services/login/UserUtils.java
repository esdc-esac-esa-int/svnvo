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
package esavo.sl.services.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

//import esac.archive.gacs.common.constants.RetrievalConstants;
import esavo.uws.UwsException;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuotaSingleton;


public class UserUtils {

	public static final java.lang.String XML_TAG_VALUE_USER = "user";
	public static final java.lang.String XML_TAG_VALUE_USER_USERNAME = "username";
	
	private static final Logger logger = Logger.getLogger(UserUtils.class);

	public static void sendLogInRequiredResponse(HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		try {
			response.setStatus(UwsOutputResponseHandler.UNAUTHORIZED);
			response.setHeader("WWW-Authenticate", "Basic realm=\"GACS authentication needed\"");
			out.println("User must be logged in to perform this action");
		} catch (Exception e) {
			logger.log(Level.ERROR, "Exception writing HTTP response object", e);
		} finally {
			try {
				out.flush();
				out.close();
			} catch (Exception e) {
				logger.log(Level.ERROR, "Exception closing PrintWriter", e);
			}
		}
	}

	
	public static Document createXmlFromUser(UwsJobOwner user) throws ParserConfigurationException, UwsException {
		UwsQuotaSingleton.getInstance().updateOwnerQuotaParameters(user);
		
		UwsJobOwnerParameters parameters = user.getParameters();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(XML_TAG_VALUE_USER);
		doc.appendChild(rootElement);
		
		appendUserProperty(doc, rootElement, XML_TAG_VALUE_USER_USERNAME, user.getAuthUsername());
		
		if(parameters != null){
			for(String paramName: parameters.getParameterNames()){
				Object paramValue = parameters.getParameter(paramName);
				String paramStrValue = UwsJobOwnerParameters.getParameterStringRepresentation(UwsJobOwnerParameters.getParameterValueType(paramValue), paramValue);
				appendUserProperty(doc, rootElement, paramName, paramStrValue);
			}
		}
		
		appendUserProperty(doc, rootElement, "user_name_details", user.getName());
		
		return doc;

	}

	
	private static void appendUserProperty(Document doc, Element rootElement, String tagName, String tagValue) {
		Element username = doc.createElement(tagName);
		username.appendChild(doc.createTextNode(tagValue));
		rootElement.appendChild(username);
	}
	
	public static void sendResponseWithUserDetails(UwsJobOwner user, HttpServletResponse response) throws IOException {
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		response.setStatus(UwsOutputResponseHandler.OK);
		
		try {
			Document doc = UserUtils.createXmlFromUser(user);
			// Get the XML doc as a String
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();
			String xmlAsString = lsSerializer.writeToString(doc);
			logger.debug("user details as XML:");
			logger.debug(printXmlPretty(xmlAsString));
			
			// Send the response (encrypt and encode data before sending)
			//String xmlEncryptedAndEncoded = Utils.encrytpAndEncodeData(xmlAsString);
			out.print(xmlAsString);
		} 
		catch (Exception e) {
			logger.log(Level.ERROR, "Exception writing HTTP response object", e);
		} 
		finally {
			try {
				out.flush();
				out.close();
			} catch (Exception e) {
				logger.log(Level.ERROR, "Exception closing PrintWriter", e);
			}
		}		
	}

	public static String printXmlPretty(String xml)
	{
		String xmlPretty = null;
		
        try {
			// Instantiate transformer input
			Source xmlInput = new StreamSource(new StringReader(xml));
			StreamResult xmlOutput = new StreamResult(new StringWriter());

			// Configure transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(xmlInput, xmlOutput);
			
			xmlPretty = xmlOutput.getWriter().toString();
		} catch (Exception e) {
			logger.log(Level.ERROR, "Exception printing XML in a pretty way", e);
		} 
        return xmlPretty;
	}

}
