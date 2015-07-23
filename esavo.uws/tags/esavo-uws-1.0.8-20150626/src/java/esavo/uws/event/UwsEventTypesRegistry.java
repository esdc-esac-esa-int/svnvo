package esavo.uws.event;

import java.util.HashMap;
import java.util.Map;

public class UwsEventTypesRegistry {
	
	private Map<Integer, UwsEventType> registry;
	
	public UwsEventTypesRegistry(){
		registry = new HashMap<Integer, UwsEventType>();
	}
	
	public synchronized void register(UwsEventType eventType){
		registry.put(eventType.getCode(), eventType);
	}
	
	public synchronized void unregister(UwsEventType eventType){
		registry.remove(eventType.getCode());
	}
	
	public synchronized void clear(){
		registry.clear();
	}
	
	public synchronized UwsEventType getEventType(int code){
		return registry.get(code);
	}
	
	@Override
	public String toString(){
		return "Event Types Registry: " + registry;
	}

}
