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

//Import from Java packages
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;

/**
 * 
 * Usage:
 * curl http://host:port/tap-context/WhoAmI
 * @author Raul Gutierrez
 *
 */
public class WhoAmIServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8286041779915325179L;
	
	private static Logger LOGGER = Logger.getLogger(WhoAmIServlet.class);
	
	private static String appid;
	EsacTapService service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = getServletContext();
		appid = UwsUtils.getAppIdFromContext(context, config);
		if(appid == null){
			throw new IllegalArgumentException("Application identifier must be defined. Use configuration variable: '"+UwsConfiguration.CONFIG_APP_ID+"'");
		}

		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		UwsUtils.updateConfiguration(configuration, context);
		UwsUtils.updateConfiguration(configuration, config);
		
		try {
			service = TapServiceConnection.getInstance(appid);
		} catch (UwsException e) {
			throw new ServletException(e);
		} catch (TAPException e) {
			throw new ServletException(e);
		}
	}

	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside WhoAmIServlet.service()");
		
		String protocol = (request.isSecure()? "https" : "http");
		String host = request.getServerName();
		int port = request.getServerPort();
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		
		String url = protocol + "://" + host + ":"+port + contextPath + (servletPath == null ? "" : servletPath);
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		response.setStatus(200);
		String uwsVersion = UwsConfigurationManager.getConfiguration(appid).getUwsVersion();
		String tapVersion = service.getTapVersion();
		String slVersion = service.getSlVersion();
		try {
			out.println(url);
			out.println("UWS engine: " + uwsVersion);
			out.println("TAP engine: " + tapVersion);
			out.println("SL version: " + slVersion);
			UwsSecurity security = service.getFactory().getSecurityManager();
			UwsJobOwner user = security.getUser(request);
			out.println("User: " + user.getId());
			if(user.isAdmin()){
				out.println("Number of jobs in memory: " + service.getFactory().getUwsManager().getNumberOfJobsInMemory());
			}
		} 
		catch (Exception e) {
		} 
		finally {
			try {
				out.flush();
				out.close();
			} catch (Exception e) {
			}
		}	

   }
	

//	private String insertInDB(){
//		try{
//			TAPService service = TapServiceConnection.getInstance(appid);
//			Connection dbConn = JDBCPoolSingleton.getInstance(service).getConnection();
//			//OK
//			//Statement st = dbConn.createStatement();
//			//String sql = "insert into user_jsegovia.tt (a) values ('1858-11-18T00:00:00')";
//			//st.executeUpdate(sql);
//			
//			//String sql2 = "insert into user_jsegovia.tt (a) values (?)";
//			//String sql2 = "insert into user_jsegovia.tt (a) values (?::timestamp)";
//			String sql2 = "insert into user_jsegovia.tt (a) values (CAST(? AS timestamp))";
//			PreparedStatement pst = dbConn.prepareStatement(sql2);
//			pst.setString(1, "1859-11-18T00:00:00");
//			pst.execute();
//			
//			return "OK";
//		}catch(Exception e){
//			return e.getMessage();
//		}
//	}

}

