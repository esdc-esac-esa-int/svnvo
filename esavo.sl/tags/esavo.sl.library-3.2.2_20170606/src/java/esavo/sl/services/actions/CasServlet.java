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
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

/**
 * 
 * Usage:
 * curl http://host:port/tap-context/CasLogin
 * @author Raul Gutierrez
 *
 */
public class CasServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8286041779915325179L;
	
	private static Logger logger = Logger.getLogger(CasServlet.class);
	
	private EsacTapService service;
	private static String appid;
	
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
		
		//Initialize
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
		logger.debug("");
		logger.debug("=====================================================================");
		logger.debug("Inside CasServlet.service()");
		String redirect = request.getParameter("redirect");
		boolean logout = false;
		if(request.getRequestURI().endsWith("CasLogout")){
			logout=true;
		}

		if(redirect!=null && !redirect.trim().isEmpty()){
			if(logout){
				response.sendRedirect(service.getCasServerUrlBase()+"/logout?service="+redirect);
			}else{
				response.sendRedirect(redirect);
			}
		}
   }

}

