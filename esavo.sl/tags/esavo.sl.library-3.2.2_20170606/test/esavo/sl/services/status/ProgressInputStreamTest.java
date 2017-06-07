package esavo.sl.services.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;
import esavo.uws.utils.status.UwsUserInfo;

public class ProgressInputStreamTest {
	
	@Test
	public void test1() throws IOException{
		UwsStatusManager.enableCheckThread(false);
		String data = "12345";
		long totalSize = data.length();
		ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
		long taskId = 1;
		
		ProgressInputStream input;

		//Null input stream
		input = new ProgressInputStream(null,0,0);
		try{
			input.read();
			Assert.fail("Exception expected: null input stream");
		}catch(IOException ioe){
		}

		UwsStatusManager sm = UwsStatusManager.getInstance();
		//DefaultJobOwner owner = TapUtils.createJobOwner("test");
		UwsJobOwner owner = new UwsJobOwner("anonymous", UwsJobOwner.ROLE_USER);
		UwsUserInfo userInfo = new UwsUserInfo(owner);
		taskId = sm.createUserIdentifier(userInfo);

		
		//updateTriggerValue = totalSize+1000 (never updates)
		input = new ProgressInputStream(bais, totalSize, taskId, totalSize+1000);
		int r;
		int index = 0;
		UwsStatusData sd = null;
		while((r = input.read()) != -1){
			Assert.assertEquals("char at: " + index, data.charAt(index), (char)r );
			sd = sm.getStatus(taskId, UwsStatusData.TYPE_PARSE);
			//Assert.assertFalse("100% reached", sd.getData().equals("100"));
			Assert.assertNull("Without trigger value", sd);
			index++;
		}
		
		sd = sm.getStatus(taskId, UwsStatusData.TYPE_PARSE);
		Assert.assertTrue("100% reached", sd.getData().equals("100"));
		
		//updates
		bais = new ByteArrayInputStream(data.getBytes());
		input = new ProgressInputStream(bais, totalSize, taskId, 0);
		index = 0;
		int percentage = 20;
		int percentageReached = 0;
		while((r = input.read()) != -1){
			Assert.assertEquals("char at: " + index, data.charAt(index), (char)r );
			sd = sm.getStatus(taskId, UwsStatusData.TYPE_PARSE);
			//System.out.println(sd.getData());
			percentageReached += percentage;
			Assert.assertEquals("Percentage", ""+percentageReached, sd.getData());
			index++;
		}

		sd = sm.getStatus(taskId, UwsStatusData.TYPE_PARSE);
		Assert.assertTrue("100% reached", sd.getData().equals("100"));
	}

}
