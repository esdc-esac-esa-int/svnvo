package esavo.sl.services.actions;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.services.nameresolution.TargetResolutionManager;

/**
 * 
 * @author Raul Gutierrez-Sanchez
 *
 */
public class TargetResolutionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(TargetResolutionServlet.class);

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside TargetResolutionServlet.service()");
		try {
			TargetResolutionManager manager = TargetResolutionManager.getInstance();
			// 2. Forward all requests to the TAP instance:
			manager.executeRequest(request, response);

		}catch(Throwable t){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}
}
