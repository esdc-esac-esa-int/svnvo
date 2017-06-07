package esavo.sl.services.actions;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.services.tabletool.TableTool;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.utils.UwsUtils;

/**
 * This servlet is used to upload/remove user tables
 *
 */
public class TableToolServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(TableToolServlet.class);

	//private Upload<ResultSet> upload = null;
	
	private String appid;
	//private TAPSchemaInfo tapSchemaInfo;
	
	private EsacTapService service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
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

	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside TableToolServlet.service()");
		try {
			TableTool TableTool = new TableTool(service);
			// 2. Forward all requests to the TAP instance:
			TableTool.executeRequest(request, response);

		}catch(Throwable t){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}


}
