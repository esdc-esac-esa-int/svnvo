package esavo.sl.services.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.dd.DDDataRetrieval;
import esavo.sl.dd.requests.DDException;
import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.utils.UwsUtils;

public class DataRetrievalServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(DataRetrievalServlet.class);

	private String appid;
	
	private EsacTapService service;
	
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
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside DataRetrievalServlet.service()");
		try {
			DDDataRetrieval dataRetrieval = new DDDataRetrieval(service);
			dataRetrieval.executeRequest(request, response);
		}catch(DDException dde){
			dumpException(dde.getCode(), response, dde);
		}catch(IOException ioe){
			dumpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, ioe);
		}catch(Throwable t){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}
	
	private void dumpException(int httpErrorCode, HttpServletResponse response, Throwable t) throws IOException {
		String context = "DataRetrieval";
		String action = "";
		response.setStatus(httpErrorCode);
		response.setContentType(UwsOutputResponseHandler.CONTENT_TYPE_HTML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new IOException("Cannot obtain output handler to write an error " + httpErrorCode + ": "+action, e);
		}
		UwsOutputUtils.writeErrorHtml(pw, httpErrorCode, action, context, t);
		pw.flush();
	}

}