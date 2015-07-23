package esavo.uws.actions;

import javax.servlet.http.HttpServletResponse;

public interface UwsAction {
	
	public boolean canHandle(UwsActionRequest action);
	public void handle(UwsActionRequest action, HttpServletResponse response);

}
