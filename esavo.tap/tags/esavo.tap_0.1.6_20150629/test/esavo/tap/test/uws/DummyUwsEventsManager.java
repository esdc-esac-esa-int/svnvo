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
