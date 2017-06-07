package esavo.sl.services.nameresolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.nameresolution.actions.handlers.TargetResolutionActionHandler;

/**
 * @author raul.gutierrez@sciops.esa.int
 *
 */
public class TargetResolutionManager {
	
	private static final Logger LOG = Logger.getLogger(TargetResolutionManager.class.getName());
	
	private List<TargetResolutionActionHandler> handlers;
	
	private static TargetResolutionManager manager;
	
	private TargetResolutionManager(){
		handlers = new ArrayList<TargetResolutionActionHandler>();
	}
	
	public static synchronized TargetResolutionManager getInstance(){
		if(manager == null){
			manager = new TargetResolutionManager();
		}
		return manager;
	}
	
	public synchronized void addHandler(TargetResolutionActionHandler handler){
		handlers.add(handler);
	}
	
	/**
	 * Performs an action based on the request.
	 * @param appid application identifier
	 * @param owner user accessing to the service
	 * @param request
	 * @param response
	 * @param parametersToIgnore
	 */
	public void executeRequest(HttpServletRequest request, HttpServletResponse response){
		boolean canHandle=false;
		for(TargetResolutionActionHandler handler: handlers){
			try {
				canHandle = handler.canHandle(request);
			} catch (TargetResolutionException e) {
				LOG.severe("Cannot check target resolution handler: " + e.getMessage() + "\n");
			}
			if(canHandle){
				try {
					handler.handle(request, response);
					break;
				} catch (TargetResolutionException e) {
					LOG.severe("Cannot handle request: " + e.getMessage() + "\n");
					try {
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
					} catch (IOException e1) {
						LOG.severe("Severe error: " + e.getMessage() + "\n");						
						e1.printStackTrace();
					}
					return;
				}
			}
		}
		
		//LOG.severe("Cannot write output: " + e.getMessage() + "\n" + UwsUtils.dumpStackTrace(e));
		//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
	
}
