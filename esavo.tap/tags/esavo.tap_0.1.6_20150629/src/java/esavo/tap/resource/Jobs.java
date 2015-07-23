package esavo.tap.resource;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionsManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.TAPException;
import esavo.tap.TAPService;

public class Jobs implements TAPResource {
	
	private static final String NAME = "jobs";
	private static final String[] RESOURCE_ITEMS = {"sync", "async"};
	
	private TAPService service;
	private CapabilitiesHandler capabilitiesHandler;
	
	public Jobs(CapabilitiesHandler capabilitiesHandler){
		this.capabilitiesHandler = capabilitiesHandler;
	}
	
	@Override
	public void init(TAPService service) {
		this.service = service;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String[] getResourceItems(){
		return RESOURCE_ITEMS;
	}

	@Override
	public boolean canHandle(String action) {
		for(String r: RESOURCE_ITEMS){
			if(r.equals(action)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException,
			TAPException, UwsException {
		if(hasRequestCapabilities(request)){
			capabilitiesHandler.showCapabilities(request, response, user);
			return true;
		}
		//TAPFactory factory = service.getFactory();
		//UwsManager manager = UwsManager.getInstance(factory.getAppId());
		UwsManager manager = UwsManager.getInstance();
		UwsActionsManager actionsManager = UwsActionsManager.getInstance();
		List<String> toIgnoreParameters = service.getUwsJobsToIgnoreParameters();
		actionsManager.executeRequest(manager, user, request, response, toIgnoreParameters);
		return true;
	}
	
	/**
	 * Checks whether the request contains REQUEST=getCapabilities parameter.
	 * In that case, a call to show capabilities is requested.
	 * @param request
	 * @return
	 */
	private boolean hasRequestCapabilities(HttpServletRequest request){
		String param = request.getParameter("REQUEST");
		if(param == null || "".equals(param)){
			param = request.getParameter("request");
		}
		if(param == null || "".equals(param)){
			return false;
		}
		return "getCapabilities".equals(param);
	}

}
