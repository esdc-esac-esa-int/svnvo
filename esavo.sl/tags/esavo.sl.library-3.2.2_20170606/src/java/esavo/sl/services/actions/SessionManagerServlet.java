package esavo.sl.services.actions;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esavo.sl.services.session.SessionManager;

/**
 * Provides unique identifiers.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class SessionManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = Logger.getLogger(SessionManagerServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside SessionManagerServlet.service()");
		SessionManager sessionManager = new SessionManager();
		sessionManager.executeRequest(request, response);
	}


}
