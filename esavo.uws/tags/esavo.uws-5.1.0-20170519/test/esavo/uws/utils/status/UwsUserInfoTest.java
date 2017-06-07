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

public class UwsUserInfoTest {
	
	@Test
	public void test1(){
		UwsJobOwner owner = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
		UwsUserInfo userInfo = new UwsUserInfo(null);
		
		Assert.assertNull("No owner set", userInfo.getOwner());
		
		userInfo.setOwner(owner);
		Assert.assertEquals("Owner", owner, userInfo.getOwner());
		
		String ipAddress = "address";
		userInfo.setip(ipAddress);
		Assert.assertEquals("ip", ipAddress, userInfo.getip());
		
		String key = "x";
		String value = "value";
		Assert.assertNull("no key", userInfo.get(key));
		userInfo.add(key, value);
		Assert.assertEquals("key", value, userInfo.get(key));
		
		//test-coverage
		userInfo.getStartTime();
		
	}

}
