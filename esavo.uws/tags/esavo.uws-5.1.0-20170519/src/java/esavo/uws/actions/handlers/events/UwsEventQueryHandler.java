package esavo.uws.actions.handlers.events;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.event.UwsEventsReaderManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Handles <code>{event}</code></br>
 * GET: return a plain text with the different events and the timestamp associated.<br/>
 * GET: Response: 200 OK</br> 
 * <br>
 * The default behavior blocks the client until events are present. The block can be removed with a request of <code>{event}?close=true</code> call.<br/>
 * The system will close (if exists) the current events reading (for the associated user and session only).<br/>
 * <br/>
 * <code>{event}?block=false</code><br/>
 * A call to {event} will block the client until an event is present.<br/>
 * If <code>block=false</code> is present, the events are returned and not consumed.<br/>
 * <br/>
 * <code>{event}?close=true</code><br/>
 * Closes the current reading. When a call to {event} is performed, the system will wait until events are present.<br/>
 * If no events are present, the system blocks the thread and waits for events.<br/>
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
 * </tt></pre>
 * 
 * <p>Order
 * <ul>
 * <li>1. If <code>close=true</code> is present: the current reading (associated to user+session) is unblocked and closed.
 * <li>2. If <code>block=false</code> is present: the events are returned without blocking and the events are not consumed.
 * <li>3. If <code>block=true</code> is present (default): the client is blocked until events are available. Events are consumed.
 * </ul>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsEventQueryHandler implements UwsActionHandler {
	
	public static final String ID = "event_request";
	public static final String ACTION_NAME = "event";
	public static final boolean IS_JOB_ACTION = false;
	
	public static final String PARAM_EVENT_TYPE_CODE = "id";
	
	public static final boolean DEFAULT_BLOCK = true;
	public static final boolean DEFAULT_CLOSE = false;
	
	class Parameters{
		int eventTypeCode;
		boolean close;
		boolean block;
	}
	
	public void UwsEventQuery(){
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
	public boolean isJobAction() {
		return IS_JOB_ACTION;
	};


	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}

		//valid request.
		//check event type code if exists
		getEventTypeCode(actionRequest);

		return true;
	}
	
	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		UwsEventsManager eventsManager = uwsManager.getFactory().getEventsManager();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		String toWrite;
		//System.out.println("**--> Reading events for: " + currentUser);
		
		Parameters parameters = getParameters(actionRequest);
		
		//ORDER
		//1. if close=true is available, close reading for user+session and return OK
		if(parameters.close){
			UwsEventsReaderManager.getInstance().addRequestToStop(currentUser);
			outputHandler.writeTextPlainResponse(response, "OK");
			//System.out.println("**--> close command Finished reading events for: " + currentUser + "\nEvents:\n");
			return;
		}
		
		//2. if block=true (default behavior: blocks until events are available
		//   if block=false (do not block, do not consume)
		Map<Integer, Long> times = new HashMap<Integer, Long>();
		if(parameters.block){
			Map<Integer, Long> timesAll = eventsManager.getTimesForEvents(UwsJobOwner.ALL_USERS_OWNER);
			Map<Integer, Long> timesUser = eventsManager.getTimesForEvents(currentUser);
			
			times.putAll(timesAll);
			times.putAll(timesUser);
		} else {
			Map<Integer, Long> timesAll = eventsManager.reportEvents(UwsJobOwner.ALL_USERS_OWNER);
			Map<Integer, Long> timesUser = eventsManager.reportEvents(currentUser);
			
			times.putAll(timesAll);
			times.putAll(timesUser);
		}
		toWrite = "-1="+currentUser.getId()+"#"+currentUser.getSession()+"\n" + getTimesToWrite(times);
		outputHandler.writeTextPlainResponse(response, toWrite);
		//System.out.println("**--> Finished reading events for: " + currentUser + "\nEvents:\n"+toWrite);
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
	
	private Parameters getParameters(UwsActionRequest actionRequest){
		Parameters parameters = new Parameters();
		parameters.block = DEFAULT_BLOCK;
		parameters.close = DEFAULT_CLOSE;
		if(actionRequest.hasHttpParameter(UwsHandlersUtils.PARAMETER_CLOSE)){
			parameters.close = Boolean.parseBoolean(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_CLOSE));
		}
		if(actionRequest.hasHttpParameter(UwsHandlersUtils.PARAMETER_BLOCK)){
			parameters.block = Boolean.parseBoolean(actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_BLOCK));
		}
		return parameters;
	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}


}
