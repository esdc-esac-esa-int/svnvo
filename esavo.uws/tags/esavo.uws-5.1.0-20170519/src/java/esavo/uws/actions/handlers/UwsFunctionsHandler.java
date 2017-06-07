package esavo.uws.actions.handlers;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.owner.UwsJobOwner;

public interface UwsFunctionsHandler {
	
	/**
	 * Returns the action associated to this handler
	 * @return
	 */
	public String getActionIdentifier();
	
	/**
	 * Returns 'true' if the handler can handle the action.
	 * @param action
	 * @return
	 */
	public boolean canHandle(String action);
	
	/**
	 * Handles an action
	 * @param actionRequest
	 * @param response
	 * @param uwsStorage
	 * @param currentUser
	 * @throws IOException
	 */
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException;

}
