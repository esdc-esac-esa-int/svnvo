package esavo.sl.services.actions;

//Import from Java packages
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import esavo.sl.tap.actions.TapServiceConnection;

/**
 * 
 * Usage:
 * curl http://host:port/tap-context/WhoAmI
 * @author Raul Gutierrez
 *
 */
public class WhoAmIConsumerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8286041779915325179L;
	
	private static Logger LOGGER = Logger.getLogger(WhoAmIConsumerServlet.class);
	
	TapServiceConnection service;
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.debug("");
		LOGGER.debug("=====================================================================");
		LOGGER.debug("Inside WhoAmIConsumerServlet.service()");
		
//		   // NOTE: The CasAuthenticationToken can also be obtained using
	    final CasAuthenticationToken token = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
	    //final CasAuthenticationToken token = (CasAuthenticationToken) request.getUserPrincipal();
	    // proxyTicket could be reused to make calls to the CAS service even if the
	    // target url differs
	    String targetUrl="https://localhost:8443/testappb/WhoAmI";
	    AttributePrincipal principal = token.getAssertion().getPrincipal();
	    final String proxyTicket = principal.getProxyTicketFor(targetUrl);

	    // Make a remote call using the proxy ticket
	    final URL serviceUrl = new URL(targetUrl+"?ticket="+URLEncoder.encode(proxyTicket, "UTF-8"));
	    String proxyResponse = CommonUtils.getResponseFromServer(serviceUrl,new HttpsURLConnectionFactory(), "UTF-8");
	    
	    PrintWriter out = response.getWriter();
	    out.println(proxyResponse);

   }
	

}

