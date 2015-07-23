package esavo.uws.actions.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.owner.UwsJobOwner;

public interface UwsActionHandler {
	
	public String getActionHandlerIdentifer();
	public String getActionName();
	
	/**
	 * Returns 'true' if the action can be handled.
	 * @param appid application identifier.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @return
	 * @throws IOException
	 */
	public boolean canHandle(String appid, UwsJobOwner currentUser, UwsActionRequest actionRequest) throws UwsException;
	
	/**
	 * Handles the action.
	 * @param uwsManager application manager.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @param response
	 * @throws IOException
	 */
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException;

}
