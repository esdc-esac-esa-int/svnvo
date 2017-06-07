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
package esavo.sl.services.status;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.sl.test.TestUtils;
import esavo.sl.test.tap.DummyGacsTapServiceConnection;
import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.status.UwsStatusManager;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpRequest;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class IdManagerTest {
	
	public static final String TEST_APP_ID = "__TEST__" + IdManagerTest.class.getName();
	
	private static DummyGacsTapServiceConnection service;
	
	@BeforeClass
	public static void beforeClass() throws UwsException, TAPException{
		service = new DummyGacsTapServiceConnection(TEST_APP_ID, StorageType.database);
	}
	
	@AfterClass
	public static void afterClass(){
		service.clearStorage();
	}
	
	@Test
	public void test1() throws Exception {
		UwsStatusManager.enableCheckThread(false);
		
		IdManager im = new IdManager(service);

		Map<String,String> reqParams = new HashMap<String, String>();

		//reqParams
		DummyHttpRequest request = TestUtils.createSimpleHttpUploadGetRequest(reqParams);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		//User not logged in
		im.executeRequest(request, response);

		if(!UwsTestUtils.findErrorInJSon(response.getOutputAsString())){
			Assert.fail("Expected error: user not authenticated.");
		}
		
		//Anonymous user
		//DummyUserIdentifier userIdentifier = new DummyUserIdentifier(null);
		//sc.setUserIdentifier(userIdentifier);
		UwsSecurity security = service.getFactory().getSecurityManager();
		security.setUser(null);
		
		response.clearOutput();
		im.executeRequest(request, response);
		
		if(!UwsTestUtils.findErrorInJSon(response.getOutputAsString())){
			Assert.fail("Expected error: anonymous user.");
		}

//		//Authenticated user
//		UwsJobOwner user = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
//		String ownerAuthUserName = "test";
//		userIdentifier.setAuthUserId(ownerAuthUserName);
//
//		response.clearOutput();
//		im.executeRequest(request, response);
//
//		if(TestUtils.findErrorInJSon(response.getOutputAsString())){
//			Assert.fail("Unexpected error: " + response.getOutputAsString());
//		}
	}

}
