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

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsUserInfo;
import esavo.uws.utils.status.UwsUserStatusData;

public class UwsUserStatusDataTest {
	
	@Test
	public void test1(){
		long taskid = 1;
		UwsJobOwner owner = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
		UwsUserInfo userInfo = new UwsUserInfo(owner);
		UwsUserStatusData usd = new UwsUserStatusData(taskid, userInfo);
		
		Assert.assertEquals("task id", taskid, usd.getTaskId());
		Assert.assertEquals("user info", userInfo, usd.getUserInfo());
		
		UwsStatusData sd;
		sd = new UwsStatusData(UwsStatusData.TYPE_UPLOAD, "x");
		usd.updateStatus(sd);
		sd = new UwsStatusData(UwsStatusData.TYPE_INGESTION, "y");
		usd.updateStatus(sd);
		//usd.updateStatus(StatusDataFactory.createStatusData(TaskType.UPLOAD, "x"));
		//usd.updateStatus(StatusDataFactory.createStatusData(TaskType.INGESTION, "y"));
		
		
		long t1 = usd.getLastUpdate();
		while(System.currentTimeMillis() == t1);
		
		//Map<TaskType, StatusData> dataMap = usd.getAllStatus();
		Map<String, UwsStatusData> dataMap = usd.getAllStatus();
		Assert.assertTrue("Two status must be available", 2 == dataMap.size());
		
		Assert.assertNull("No parse status available", usd.consumeStatus(UwsStatusData.TYPE_PARSE));
		Assert.assertNotNull("Upload status not removed", usd.getStatus(UwsStatusData.TYPE_UPLOAD));
		Assert.assertNotNull("Ingestion status not removed", usd.getStatus(UwsStatusData.TYPE_INGESTION));
		Assert.assertNotNull("Upload status must be available", usd.consumeStatus(UwsStatusData.TYPE_UPLOAD));
		Assert.assertNull("Upload status removed (call to consume)", usd.getStatus(UwsStatusData.TYPE_UPLOAD));

		dataMap = usd.getAllStatus();
		Assert.assertTrue("One status must be available", 1 == dataMap.size());
		Assert.assertNotNull("Ingestion status not removed", usd.getStatus(UwsStatusData.TYPE_INGESTION));
		Assert.assertNull("No parse status available", usd.getStatus(UwsStatusData.TYPE_PARSE));
		Assert.assertNull("No upload status available", usd.getStatus(UwsStatusData.TYPE_UPLOAD));

		usd.consumeAllStatus();
		Assert.assertFalse("Times must be updated", t1 == usd.getLastUpdate());
		
		dataMap = usd.getAllStatus();
		Assert.assertTrue("No status must be available", 0 == dataMap.size());
		
		//test-coverage
		usd.toString();
	}

}
