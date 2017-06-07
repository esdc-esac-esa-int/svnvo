package esavo.sl.services.nameresolution.actions.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.nameresolution.TargetResolutionException;

public interface TargetResolutionActionHandler {
	
	public String getAction();
	
	/**
	 * Returns 'true' if the action can be handled.
	 * 
	 * @param request
	 * @return
	 * @throws TargetResolutionException
	 */
	public boolean canHandle(HttpServletRequest request) throws TargetResolutionException;
	
	/**
	 * Handles the action.
	 * @param uwsManager application manager.
	 * @param currentUser current user accessing the service.
	 * @param actionRequest request.
	 * @param response
	 * @throws IOException
	 */
	public void handle(HttpServletRequest actionRequest, HttpServletResponse response) throws TargetResolutionException;

}
