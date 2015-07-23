package esavo.uws.actions.handlers.events;


import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Handles <code>{event}?[id=id]</code></br>
 * GET: return a plain text with the different events and the timestamp associated.<br/>
 * If id (event identifier) is provided, only the specified event is returned.<br/>
 * GET: Response: 200 OK</br> 
 * 
 * <pre><tt>
 * no id:
 * 	-all events and timestamps are returned (plain text)
 * 		i.e:
 * 		event_id_1=time
 * 		event_id_2=time
 * 		...
 * 		event_id_n=time
 * 
 * id provided:
 * 	-id=event_type_id
 * 	-the specified timestamp event is returned (plain text) or empty string if the event type identifier is not found.
 * 		i.e.:
 * 		event_id_requested=time
 * 
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsEventQuery implements UwsActionHandler {
	
	public static final String ID = "event_request";
	public static final String ACTION_NAME = "event";
	
	public static final String PARAM_EVENT_TYPE_CODE = "id";
	
	class Parameters{
		int eventTypeCode;
	}
	
	public UwsEventQuery(){
	}
	
	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
			return false;
		}
		if(!actionRequest.hasEvent()){
			return false;
		}

		//valid request.
		//check event type code if exists
		getEventTypeCode(actionRequest);

		return true;
	}
	
	private Parameters getParameters(UwsActionRequest actionRequest) throws UwsException {
		Parameters actionParameters = new Parameters();
		actionParameters.eventTypeCode = getEventTypeCode(actionRequest);
		return actionParameters;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		Parameters actionParameters = getParameters(actionRequest);
		UwsEventsManager eventsManager = uwsManager.getFactory().getEventsManager();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		String toWrite;
		if(actionParameters.eventTypeCode != -1){
			UwsEventType eventType = eventsManager.getEventsTypeRegistry().getEventType(actionParameters.eventTypeCode);
			long time = eventsManager.getTimeForEvent(currentUser, eventType);
			toWrite = "" + eventType.getCode() + "=" + time;
		} else {
			Map<Integer, Long> times = eventsManager.getTimesForEvents(currentUser);
			toWrite = getTimesToWrite(times);
		}
		outputHandler.writeTextPlainResponse(response, toWrite);
	}
	
	private String getTimesToWrite(Map<Integer,Long> times){
		StringBuilder sb = new StringBuilder();
		if(times != null){
			for(Entry<Integer, Long> e: times.entrySet()){
				sb.append(e.getKey()).append('=').append(+e.getValue()).append('\n');
			}
		}
		return sb.toString();
	}

	private int getEventTypeCode(UwsActionRequest actionRequest) throws UwsException {
		String sEventTypeCode = actionRequest.getHttpParameter(PARAM_EVENT_TYPE_CODE);
		if(sEventTypeCode != null && !"".equals(sEventTypeCode)){
			try{
				return Integer.parseInt(sEventTypeCode);
			}catch(NumberFormatException e){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Invalide event type code parameter '"+sEventTypeCode+"'. An integer is expected.");
			}
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}


}
