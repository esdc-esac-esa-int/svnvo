/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.test.uws;

import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventTypesRegistry;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.owner.UwsJobOwner;

public class DummyUwsEventsManager implements UwsEventsManager {

	@Override
	public long getTimeForEvent(UwsJobOwner user, int eventTypeCode)
			throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEventTime(UwsJobOwner user, int eventTypeCode)
			throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getTimeForEvent(UwsJobOwner user, UwsEventType eventType) {
		// TODO Auto-generated method stub
		return 0;
	}

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

}
