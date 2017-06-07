package esavo.uws.actions.handlers.admin.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * <pre><tt>
 * admin?ACTION=events&COMMAND=list&OWNER=userid
 * admin?ACTION=events&COMMAND=set&OWNER=userid&EVENT=401
 * admin?ACTION=events&COMMAND=removeAll&OWNER=userid
 * </tt></pre>
 * 
 * @author jsegovia
 *
 */
public class UwsAdmEventsHandler implements UwsFunctionsHandler {

	public static final String ACTION = "events";
	
	//public static final String PARAM_COMMAND = "COMMAND";
	//public static final String PARAM_TIMESTAMP = "TIMESTAMP";
	//public static final String PARAM_EVENT = "EVENT";
	//public static final String PARAM_OWNER = "OWNER";
	
	public enum Command {
		set,
		remove,
		removeAll,
		list
	};

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException  {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, outputHandler, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}
		
		Command command = getCommand(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_COMMAND));
		if(command == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unknown events command '"+actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_COMMAND)+"'.");
		}
		UwsJobOwner owner = createOwner(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_OWNER));
		if(owner == null){
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Onwer parameter not found.");
		}

		UwsEventsManager eventsManager = UwsManager.getInstance().getFactory().getEventsManager();
		//String timeStamp = parameters.get(PARAM_TIMESTAMP);
		int event = getEventId(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_EVENT));
		String result = null;
		switch(command){
		case set:
			try {
				result = setEvent(eventsManager, owner, event);
			} catch (IOException e) {
				throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Cannot set event: " + e.getMessage(), e);
			}
			break;
		case remove:
			//result = removeEvent(eventsManager, owner, event);
			result = "Not implemented yet.";
			break;
		case removeAll:
			result = removeAllEvents(eventsManager, owner);
			break;
		default:
			//list
			result = listEvents(eventsManager, owner);
		}
		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		uwsOutput.writeSimpleJsonResponse(response, "result", result);
		//Utils.writeMsg(response, UwsOutputResponseHandler.OK, "result", result);
		//response.flushBuffer();
		return;
	}
	
	private UwsJobOwner createOwner(String ownerid){
		if(ownerid == null){
			return null;
		}
		UwsJobOwner owner = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		return owner;
	}
	
	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
	
	private Command getCommand(String command){
		if(command == null){
			return null;
		}
		try{
			return Command.valueOf(command);
		}catch(Exception e){
			return null;
		}
	}
	
	private int getEventId(String event){
		if(event == null){
			return -1;
		}
		try{
			return Integer.parseInt(event);
		}catch(NumberFormatException nfe){
			return -1;
		}
	}
	
	private String setEvent(UwsEventsManager eventsManager, UwsJobOwner owner, int eventId) throws IOException {
		try{
			eventsManager.setEventTime(owner, eventId);
			return "Event '"+eventId+"' set for all sessions for user '"+owner.getId()+"'";
		}catch(UwsException e){
			throw new IOException("Cannot set event '"+eventId+"' for all sessions for user '"+owner.getId()+"' due to: " + e.getMessage(), e);
		}
	}
	
	private String removeAllEvents(UwsEventsManager eventsManager, UwsJobOwner owner){
		eventsManager.removeEventItem(owner);
		return "Removed all events for all sessions for user '"+owner.getId()+"'";
	}
	
	private String listEvents(UwsEventsManager eventsManager, UwsJobOwner owner){
		List<String> sessions = eventsManager.getAllSessionsForUser(owner.getId());
		if(sessions == null){
			return "No sessions found for user '"+owner.getId()+"'";
		}
		StringBuilder sb = new StringBuilder();
		for(String session: sessions){
			sb.append("session: ").append(session).append('\n');
			owner.setSession(session);
			Map<Integer, Long> events = eventsManager.reportEvents(owner);
			if(events == null){
				sb.append("No events set for user '").append(owner.getId()).append("'\n");
			}else{
				boolean firstTime = true;
				for(Entry<Integer, Long> e: events.entrySet()){
					if(firstTime){
						firstTime = false;
					}else{
						sb.append(", ");
					}
					sb.append("Event: ").append(e.getKey()).append(": ").append(e.getValue());
				}
				sb.append('\n');
			}
		}
		return sb.toString();
	}


}