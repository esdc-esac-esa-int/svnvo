package esavo.uws.scheduler;

import org.junit.Assert;
import org.mockito.Mockito;
import org.junit.Test;

import esavo.uws.actions.TaskStatusTest;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.owner.UwsJobOwner;

public class UwsQueueTest {

	public static final String TEST_APP_ID = "__TEST__" + UwsQueueTest.class.getName();
	
	@Test
	public void testAddJob() {
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid("__TEST__");
		args.setOwner(UwsJobOwner.ANONYMOUS_OWNER);
		args.setPriority(2);
		UwsJob job = new UwsJob(args);
		UwsJobThread jobThread = new UwsJobThread(job,null,null);
		
		
	
	}
	
	
	/*
	private createJob
	
	private UwsJob createJob(String id, int role, int prio ){
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(TEST_APP_ID);
		args.setOwner(role);
		args.setPriority(2);
		UwsJob job = new UwsJob(args);
		
		
	
	}
	*/

}
