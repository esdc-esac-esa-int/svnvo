package esavo.sl.services.tabletool;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.uws.owner.UwsJobOwner;

public interface TableToolHandler {
	
	public String getAction();
	public void handle(Map<String, String> parameters, long taskIdentifier, UwsJobOwner user, HttpServletResponse response, TAPService service) throws Exception;

}
