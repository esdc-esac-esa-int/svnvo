package esavo.sl.services.actions;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.services.status.IdManager;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

public class IdManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(IdManagerServlet.class);
			
	private String appid;
	
	private EsacTapService service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
//		appid = EnvironmentManager.getAppId(config);
//		EnvironmentManager.initGenericServletEnvironment(appid, config);
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
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside IdManagerServlet.service()");
		try {
			IdManager idManager = new IdManager(service);
			// 2. Forward all requests to the IdManager instance:
			idManager.executeRequest(request, response);

		}catch(Throwable t){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}


}
