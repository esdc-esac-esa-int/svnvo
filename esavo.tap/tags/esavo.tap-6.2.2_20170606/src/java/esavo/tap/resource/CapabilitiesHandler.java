package esavo.tap.resource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

/**
 * Shows TAP capabilities
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface CapabilitiesHandler {
	
	/**
	 * Call to show the TAP system capabilities
	 * @param request
	 * @param response
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws TAPException
	 * @throws UwsException
	 */
	public void showCapabilities(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user)
			throws ServletException, IOException, TAPException, UwsException;
}
