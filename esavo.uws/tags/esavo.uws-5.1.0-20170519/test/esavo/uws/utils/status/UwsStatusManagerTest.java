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
