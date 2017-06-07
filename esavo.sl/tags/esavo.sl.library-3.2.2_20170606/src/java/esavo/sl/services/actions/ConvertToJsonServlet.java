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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import esavo.sl.services.transform.TransformToJson;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

public class ConvertToJsonServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	//private static final String KEYSTORE_LOCATION_PARAM = "keystorePath";
	//private static final String TRUSTSTORE_LOCATION_PARAM = "truststorePath";
	private static final String TMP_DIR_PARAM = "tmpFileDir";
	
	//private static final String JSON_TRANSLATOR_TMP_DIR = "esac.archive.gacs.sl.tap.actions.ConvertToJsonServlet.tmpDir";
	
	private static Logger LOGGER = Logger.getLogger(ConvertToJsonServlet.class);
	private String appid;
	private File tmpDir;
	private EsacTapService service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		//TODO verify clashes with SpringSecurity
//		String keystoreFilePath = config.getInitParameter(KEYSTORE_LOCATION_PARAM);
//		String trustStoreFilePath = config.getInitParameter(TRUSTSTORE_LOCATION_PARAM);
//		LOGGER.info("keystore location: " + keystoreFilePath);
//		LOGGER.info("truststore location: " + trustStoreFilePath);
//		System.setProperty("javax.net.ssl.keyStore", keystoreFilePath);
//		System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
//		System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
//		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//		System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
//		try{
//			Security.addProvider( (Provider)Class.forName("com.sun.crypto.provider.SunJCE").newInstance());
//		}catch(Exception e){
//			throw new ServletException("Cannot create class SunJCE", e);
//		}
//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		
		
//		appid = EnvironmentManager.getAppId(config);
//		EnvironmentManager.initGenericServletEnvironment(appid, config);
//
//		tapSchemaInfo = esac.archive.gacs.sl.services.util.Utils.getTapSchemaInfo(config);

		ServletContext context = getServletContext();
		appid = UwsUtils.getAppIdFromContext(context, config);
		if(appid == null){
			throw new IllegalArgumentException("Application identifier must be defined. Use configuration variable: '"+UwsConfiguration.CONFIG_APP_ID+"'");
		}

		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		UwsUtils.updateConfiguration(configuration, context);
		UwsUtils.updateConfiguration(configuration, config);

		//Initialize
		try {
			service = TapServiceConnection.getInstance(appid);
		} catch (UwsException e) {
			throw new ServletException(e);
		} catch (TAPException e) {
			throw new ServletException(e);
		}

		
		//Tmp dir:
		tmpDir = createTmpDir(config.getInitParameter(TMP_DIR_PARAM));
//		System.setProperty(JSON_TRANSLATOR_TMP_DIR, tmpDir.getAbsolutePath());
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside ConvertToJsonServlet.service()");
		try {
			TransformToJson transform = new TransformToJson(service, tmpDir);
			transform.executeRequest(request, response);
		}catch(Throwable t){
			t.printStackTrace();
			LOGGER.log(Level.WARN, t.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}
	
	private File createTmpDir(String dir){
		if(dir == null){
			dir = System.getProperty("java.io.tmpdir");
		}
		if(dir.startsWith("@")){
			dir = System.getProperty("java.io.tmpdir");
		}
		if(dir == null || dir.startsWith("@")){
			dir = ".";
		}
		return new File(dir);

	}


}
