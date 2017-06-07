package esavo.uws.utils.test.uws;

import java.util.List;
import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventTypesRegistry;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.owner.UwsJobOwner;

public class DummyUwsEventsManager implements UwsEventsManager {

//	@Override
//	public long getTimeForEvent(UwsJobOwner user, int eventTypeCode) throws UwsException {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public void setEventTime(UwsJobOwner user, int eventTypeCode) throws UwsException {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public long getTimeForEvent(UwsJobOwner user, UwsEventType eventType) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public Map<Integer, Long> getTimesForEvents(UwsJobOwner user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEventTime(UwsJobOwner user, UwsEventType eventType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEventItem(UwsJobOwner user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UwsEventTypesRegistry getEventsTypeRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String checkEventsRemovalProcedure(long deltaDestructionTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Long> reportEvents(UwsJobOwner user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllSessionsForUser(String userid) {
		// TODO Auto-generated method stub
		return null;
	}

}
