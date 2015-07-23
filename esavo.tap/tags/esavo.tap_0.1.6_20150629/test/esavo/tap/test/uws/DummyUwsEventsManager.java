package esavo.tap.test.uws;

import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventTypesRegistry;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.owner.UwsJobOwner;

public class DummyUwsEventsManager implements UwsEventsManager {

	@Override
	public String checkEventsRemovalProcedure(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UwsEventTypesRegistry getEventsTypeRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeForEvent(UwsJobOwner arg0, int arg1) throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimeForEvent(UwsJobOwner arg0, UwsEventType arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<Integer, Long> getTimesForEvents(UwsJobOwner arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEventItem(UwsJobOwner arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEventTime(UwsJobOwner arg0, int arg1) throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEventTime(UwsJobOwner arg0, UwsEventType arg1) {
		// TODO Auto-generated method stub
		
	}

}
