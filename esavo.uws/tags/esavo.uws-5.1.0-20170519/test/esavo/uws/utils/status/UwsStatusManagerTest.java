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
package esavo.uws.utils.status;

import junit.framework.Assert;

import org.junit.Test;

import esavo.uws.owner.UwsJobOwner;

public class UwsStatusManagerTest {
	
	@Test
	public void test1(){
		//Disable check mechanism thread
		UwsStatusManager.enableCheckThread(false);
		UwsStatusManager sm = UwsStatusManager.getInstance();
		
		try{
			sm.getStatus(-1);
			Assert.fail("Exception expected: identifier not found");
		}catch(IllegalArgumentException e){
			
		}
		
		//test-coverage
		sm.dump();
		sm.toString();
	}
	
	@Test
	public void testTimeOut(){
		//Disable check mechanism thread
		UwsStatusManager.enableCheckThread(false);
		
		UwsStatusManager sm = UwsStatusManager.getInstance();
		sm.removeAll();
		
		sm.removeInactiveEntries(0, 0);

		//DefaultJobOwner owner = TapUtils.createJobOwner("test");
		UwsJobOwner owner = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
		UwsUserInfo userInfo = new UwsUserInfo(owner);
		long id = sm.createUserIdentifier(userInfo);
		
		UwsStatusData sd = new UwsStatusData(UwsStatusData.TYPE_UPLOAD, "50");
		sm.updateStatus(id, sd);
		
		UwsUserStatusData usd = sm.getUserStatusData(id);
		long lastUpdate = usd.getLastUpdate();
		
		long time = lastUpdate;
		long timeOut = 0;
		
		//No timeout => must contain one client
		time = lastUpdate;
		timeOut = 0;
		sm.removeInactiveEntries(time, timeOut);
		Assert.assertTrue("No tiemout, one client expected.", sm.getNumClients() == 1);
		
		//timeout => the client must be removed
		time += 100;
		sm.removeInactiveEntries(time, timeOut);
		Assert.assertTrue("Timeout, one client expected.", sm.getNumClients() == 0);
	}
	
}
