package esavo.uws.scheduler;

import org.junit.Assert;
import org.mockito.Mockito;
import org.junit.Test;

import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.owner.UwsJobOwner;

public class UwsDefaultSchedulerTest {

	@Test
	public void testAddJob() {
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid("__TEST__");
		args.setOwner(UwsJobOwner.ANONYMOUS_OWNER);
		args.setPriority(2);
		UwsJob job = new UwsJob(args);
	
		
		
	
	}

}
