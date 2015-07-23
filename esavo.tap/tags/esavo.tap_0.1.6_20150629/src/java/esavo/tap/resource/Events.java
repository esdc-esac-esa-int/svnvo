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

public class Events implements TAPResource {
	
	private static final String NAME = "event";
	private static final String[] RESOURCE_ITEMS = {NAME};
	
	private TAPService service;
	
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
		return NAME.equals(action);
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException,
			TAPException, UwsException {
		UwsManager manager = UwsManager.getInstance();
		UwsActionsManager actionsManager = UwsActionsManager.getInstance();
		List<String> toIgnoreParameters = service.getUwsJobsToIgnoreParameters();
		actionsManager.executeRequest(manager, user, request, response, toIgnoreParameters);
		return true;
	}

}
