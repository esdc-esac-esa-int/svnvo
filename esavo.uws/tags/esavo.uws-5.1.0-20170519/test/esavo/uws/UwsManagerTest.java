package esavo.uws;

import java.io.File;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import esavo.uws.actions.UwsUploadResourceLoader;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsEventsManager;
import esavo.uws.utils.test.uws.DummyUwsExecutor;
import esavo.uws.utils.test.uws.DummyUwsFactory;
import esavo.uws.utils.test.uws.DummyUwsScheduler;
import esavo.uws.utils.test.uws.DummyUwsStorageManager;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;

public class UwsManagerTest {
	
	public static final String TEST_ID = UwsManagerTest.class.getName();
	
	private static File fStorageDir;
	private static String appid = TEST_ID;
	private static UwsManager manager;
	private static DummyUwsFactory factory;
	private static UwsConfiguration configuration;
	private static DummyUwsStorageManager storage;
	//private static UwsJobsListManager listManager;
	private static DummyUwsExecutor executor;
	private static DummyUwsScheduler scheduler;
	private static DummyUwsEventsManager events;
	
	@BeforeClass
	public static void beforeClass() throws UwsException{
		fStorageDir = new File(".", TEST_ID);
		fStorageDir.mkdirs();
		configuration = UwsConfigurationManager.getConfiguration(appid);
		factory = new DummyUwsFactory(appid, fStorageDir, configuration, StorageType.fake); 
		manager = UwsManager.getInstance();  //the manager is created by the factory when a factory is created.
		storage = (DummyUwsStorageManager)factory.getStorageManager();
		executor = (DummyUwsExecutor)factory.getExecutor();
		scheduler = (DummyUwsScheduler)factory.getScheduler();
		events = (DummyUwsEventsManager)factory.getEventsManager();
		//listManager = UwsJobsListManager.getInstance(appid);
	}
	
	@AfterClass
	public static void afterClass(){
		UwsTestUtils.removeDirectory(fStorageDir);
	}
	
	@Test
	public void testOther(){
		Assert.assertEquals("appid", appid, manager.getAppId());
	}
	
	@Test
	public void testRemoveJob() throws UwsException {
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		UwsJob job = new UwsJob(args);
		
		String jobid = job.getJobId();
		
		//no security check
		try{
			manager.removeJob(jobid, listName, null);
			Assert.fail("Exception expected: job does not exist");
		}catch(UwsException e){
			//expected
		}
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		listManager.addInMemoryJob(jobid, jobThread);
		//manager.removeJob(jobid, null);
	}
	
	@Test
	public void testTryLoadJob() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		UwsJob job = new UwsJob(args);
		
		String jobid = job.getJobId();
		
		try{
			manager.tryLoadJob(jobid, currentUser);
			Assert.fail("Exception expected: job does not exist");
		}catch(UwsException e){
			//expected
		}
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		listManager.addInMemoryJob(jobid, jobThread);
		
		UwsJob jobRet = manager.tryLoadJob(jobid, currentUser);
		Assert.assertEquals(job, jobRet);

		//non valid user
		UwsJobOwner currentUser2 = new UwsJobOwner(ownerid+"xxx", UwsJobOwner.ROLE_USER);
		try{
			manager.tryLoadJob(jobid, currentUser2);
			Assert.fail("Non valid user: exception expected.");
		}catch(UwsException e){
			//expected
		}
		
		currentUser2 = new UwsJobOwner(UwsUtils.ANONYMOUS_USER, UwsJobOwner.ROLE_USER);
		try{
			manager.tryLoadJob(jobid, currentUser2);
			Assert.fail("Non valid user: exception expected.");
		}catch(UwsException e){
			//expected
		}

		currentUser2 = new UwsJobOwner(UwsUtils.ANONYMOUS_USER, UwsJobOwner.ROLE_ADMIN);
		jobRet = manager.tryLoadJob(jobid, currentUser2);
		Assert.assertEquals(job, jobRet);
	}
	
	@Test
	public void testRestartJobs() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		args.setEventsManager(factory.getEventsManager());
		UwsJob job = new UwsJob(args);
		
		String jobid = job.getJobId();
		
		boolean ret = manager.restartJob(job);
		UwsJobThread jobThread = listManager.getInMemoryJob(jobid);
		Assert.assertNotNull("Job added", jobThread);
		Assert.assertEquals("Job id", jobid, jobThread.getJob().getJobId());
		Assert.assertEquals("job stored", job, storage.getJob());
		Assert.assertFalse("Job not started", ret);
		Assert.assertNull("Job not scheduled", scheduler.getJobThread());
		
		//put parameter phase=run
		UwsJobParameters params = new UwsJobParameters();
		params.setParameter("phase", "run");
		job.setParameters(params);
		
		listManager.reset();
		ret = manager.restartJob(job);
		jobThread = listManager.getInMemoryJob(jobid);
		Assert.assertNotNull("Job added", jobThread);
		Assert.assertEquals("Job id", jobid, jobThread.getJob().getJobId());
		Assert.assertEquals("job stored", job, storage.getJob());
		Assert.assertTrue("Job started", ret);
		Assert.assertEquals("Job scheduled", jobThread, scheduler.getJobThread());
		
		
		factory.reset();
		storage.raiseExceptionAt(0);
		try{
			manager.restartJob(job);
			Assert.fail("Exception expected");
		}catch(UwsException e){
			//expected
		}
	}
	
	@Test
	public void testAddJob() throws UwsException {
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		args.setEventsManager(factory.getEventsManager());
		UwsJob job = new UwsJob(args);
		
		String jobid = job.getJobId();
		
		manager.addJob(job);
		UwsJobThread jobThread = listManager.getInMemoryJob(jobid);
		Assert.assertNotNull("Job added", jobThread);
		Assert.assertEquals("Job id", jobid, jobThread.getJob().getJobId());
		Assert.assertEquals("job stored", job, storage.getJob());
		
		factory.reset();
		storage.raiseExceptionAt(0);
		try{
			manager.addJob(job);
			Assert.fail("Exception expected when adding a job");
		}catch(UwsException e){
			//expected
		}

		factory.reset();
		storage.raiseExceptionAt(1);
		try{
			manager.addJob(job);
			Assert.fail("Exception expected when adding a job + changing phase");
		}catch(UwsException e){
			//expected
		}
	}
	
	
	@Test
	public void testStartJob() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String jobid = "1234" + appid;
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		try{
			manager.startJob(jobid, currentUser);
			Assert.fail("Exception expected: jobid not found in execution list");
		}catch(UwsException e){
			//expected
		}
		
		
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		UwsJob job = new UwsJob(args);
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		
		factory.reset();
		storage.setJob(job);
		try{
			manager.startJob(jobid, currentUser);
			Assert.fail("Exception expected: job not found in execution list but already stored");
		}catch(UwsException e){
			//expected
		}

		factory.reset();
		listManager.addInMemoryJob(jobid, jobThread);
		Assert.assertNull(scheduler.getJobThread());
		boolean ret = manager.startJob(jobid, currentUser);
		Assert.assertTrue("Job started", ret);
		Assert.assertEquals("Job scheduled", jobThread, scheduler.getJobThread());
		
		factory.reset();
		listManager.addInMemoryJob(jobid, jobThread);
		storage.setJob(job);
		jobThread.setStarted();
		ret = manager.startJob(jobid, currentUser);
		Assert.assertFalse("Cannot start job: already started",ret);
		Assert.assertNull(scheduler.getJobThread());
	}
	
	@Test
	public void testAbortJob() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		String jobid = "1234" + appid;
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		try{
			manager.abortJob(jobid, currentUser);
			Assert.fail("Exception expected: jobid not found in execution list");
		}catch(UwsException e){
			//expected
		}
		
		
		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		args.setEventsManager(factory.getEventsManager());
		UwsJob job = new UwsJob(args);
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		
		factory.reset();
		storage.setJob(job);
		try{
			manager.abortJob(jobid, currentUser);
			Assert.fail("Exception expected: job not found in execution list but already stored");
		}catch(UwsException e){
			//expected
		}

		factory.reset();
		listManager.addInMemoryJob(jobid, jobThread);
		manager.abortJob(jobid, currentUser);
		
		Assert.assertEquals("Phase aborted", UwsJobPhase.ABORTED, job.getPhase());
	}
	
	@Test
	public void testTryUpdateJobAttribute() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		configuration.clear();
		storage.reset();
		UwsJobsListManager.resetLists();
		executor.reset();
		
		String jobid = "1234" + appid;
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		UwsJobPhase phase = UwsJobPhase.ERROR;
		UwsJobAttribute attributeName = UwsJobAttribute.Phase;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		try{
			manager.tryUpdateJobAttribute(jobid, currentUser, attributeName, phase);
			Assert.fail("Exception expected: jobid not found in execution list");
		}catch(UwsException e){
			//expected
		}

		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		UwsJob job = new UwsJob(args);
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		
		factory.reset();
		listManager.addInMemoryJob(jobid, jobThread);
		try{
			manager.tryUpdateJobAttribute(jobid, currentUser, attributeName, phase);
			Assert.fail("Exception expected: phase cannot be updated by calling tryUpdateJobAttribute");
		}catch(Exception e){
			//exepcted
		}
		
		Long l = new Long(2);
		manager.tryUpdateJobAttribute(jobid, currentUser, UwsJobAttribute.Quote, l);
		
		//non valid user
		UwsJobOwner currentUser2 = new UwsJobOwner(ownerid+"xxx", UwsJobOwner.ROLE_USER);
		try{
			manager.tryUpdateJobAttribute(jobid, currentUser2, attributeName, phase);
			Assert.fail("Non valid user: exception expected.");
		}catch(UwsException e){
			//expected
		}
	}

	
	@Test
	public void testTryUpdateJobParameters() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		configuration.clear();
		storage.reset();
		UwsJobsListManager.resetLists();
		executor.reset();
		
		String jobid = "1234" + appid;
		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		Map<String,Object> parameters = null;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		try{
			manager.tryUpdateJobParameters(jobid, currentUser, parameters);
			Assert.fail("Exception expected: jobid not found in execution list");
		}catch(UwsException e){
			//expected
		}

		UwsUploadResourceLoader[] uploadResources = null;
		DummyHttpResponse response = new DummyHttpResponse();

		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		UwsJob job = new UwsJob(args);
		
		UwsJobThread jobThread = new UwsJobThread(job, executor, scheduler);
		
		factory.reset();
		listManager.addInMemoryJob(jobid, jobThread);
		manager.tryUpdateJobParameters(jobid, currentUser, parameters);
		
		//non valid user
		UwsJobOwner currentUser2 = new UwsJobOwner(ownerid+"xxx", UwsJobOwner.ROLE_USER);
		try{
			manager.tryUpdateJobParameters(jobid, currentUser2, parameters);
			Assert.fail("Non valid user: exception expected.");
		}catch(UwsException e){
			//expected
		}
	}
	
	@Test
	public void testGetJobList(){
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();
		
		String listName = "async";
		String ownerid = "test";
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		List<UwsJob> jobs = null;
		List<UwsOwnerSessionFilter> ownersFilters;
		UwsOwnerSessionFilter ownerFilter;

		boolean metadataOnly = false;
		String order = null;
		Integer limit = null;
		Integer offset = null;
		UwsJobsFilter filter = null;
		
		//no jobs at all
		storage.raiseExceptionAt(0);
		try {
			//jobs = manager.getJobsMetaList(listName, currentUser);
			jobs = manager.getJobsMetaList(listName, filter, limit, offset, order, metadataOnly);
			Assert.fail("Expected exception.");
		} catch (UwsException e) {
		}

		factory.reset();
		try {
			//jobs = manager.getJobsMetaList(listName, currentUser);
			jobs = manager.getJobsMetaList(listName, filter, limit, offset, order, metadataOnly);
		} catch (UwsException e) {
			Assert.fail("Unexpected exception.");
		}
		
		Assert.assertNull("No jobs expected.", jobs);
		Assert.assertNull("Filter", storage.getJobsFilter());
		Assert.assertEquals("Limit", -1, storage.getLimit());
		Assert.assertEquals("Offset", -1, storage.getOffset());
		Assert.assertEquals("list name", listName, storage.getListName());
		Assert.assertEquals("Appid", appid, storage.getAppId());
		//ownersFilters = storage.getOwnersFilters();
		//Assert.assertEquals("Owners filter size", 1, ownersFilters.size());
		//ownerFilter = ownersFilters.get(0);
		//Assert.assertEquals("Onwer filter", ownerid, ownerFilter.getOwnerid());
		//Assert.assertEquals("list name", listName, storage.getListName());
		//Assert.assertEquals("list name", appid, storage.getAppId());
	}
	
	@Test
	public void testExecuteSyncJob() throws UwsException{
		UwsTestUtils.initDirectory(fStorageDir);
		factory.clear();

		String listName = "async";
		String ownerid = "test";
		int priority = DummyUwsScheduler.TEST_DEFAULT_PRIORITY;
		
		UwsJobOwner currentUser = new UwsJobOwner(ownerid, UwsJobOwner.ROLE_USER);
		
		DummyHttpResponse response = new DummyHttpResponse();
		
		UwsUploadResourceLoader[] uploadResources = null;
		
		UwsJobsListManager listManager = UwsJobsListManager.getInstance(appid, listName);
		UwsJobInitArgs args = new UwsJobInitArgs();
		args.setAppid(appid);
		args.setListid(listName);
		args.setJobsListManager(listManager);
		args.setOwner(currentUser);
		args.setPriority(priority);
		args.setResponse(response);
		args.setStorage(storage);
		args.setUploadResources(uploadResources);
		args.setEventsManager(events);
		UwsJob job = new UwsJob(args);
		
		executor.setGenerateUwsException(true);
		
		
		//user context checks are not longer available.

//		factory.reset();
//		response.reset();
//		job = new UwsJob(args);
//		scheduler.setStartThread(true);
//		System.out.println(events);
//	
//		try{
//			manager.executeSyncJob(job);
//			Assert.fail("Exception expected: no user context available");
//		}catch(UwsException e){
//			//expected
//		}
//		Assert.assertEquals("No user context available", UwsJobPhase.ERROR, job.getPhase());

		
//		factory.getSecurityManager().setUser(job.getOwner());
//		try{
//			manager.executeSyncJob(job);
//			Assert.fail("Exception expected");
//		}catch(UwsException e){
//			//expected
//		}
//		Assert.assertEquals("Status after error", UwsJobPhase.ERROR, job.getPhase());

		
		factory.reset();
		response.reset();
		job = new UwsJob(args);
		scheduler.setStartThread(true);
		
		executor.setGenerateInterruptedException(true);
		manager.executeSyncJob(job);
//		try{
//			manager.executeSyncJob(job);
//			Assert.fail("Exception expected");
//		}catch(UwsException e){
//			//expected
//			
//		}
//		Assert.assertEquals("Status after interrupted", UwsJobPhase.ERROR, job.getPhase());
		Assert.assertEquals("Status after interrupted", UwsJobPhase.ABORTED, job.getPhase());
		

//		factory.reset();
//		response.reset();
//		job = new UwsJob(args);
//		factory.getSecurityManager().setUser(new UwsJobOwner("otheruser", UwsJobOwner.ROLE_USER));
//		scheduler.setStartThread(true);
//		
//		try{
//			manager.executeSyncJob(job);
//			Assert.fail("Exception expected");
//		}catch(UwsException e){
//			//expected
//		}
//		Assert.assertEquals("User context and job owner inconsistent", UwsJobPhase.ERROR, job.getPhase());


		factory.reset();
		response.reset();
		job = new UwsJob(args);
		factory.getSecurityManager().setUser(job.getOwner());
		scheduler.setStartThread(true);
		
		manager.executeSyncJob(job);
		Assert.assertEquals("OK", UwsJobPhase.COMPLETED, job.getPhase());
		
		
		//check max queued jobs
		listManager.setMaxQueuedJobs(0);
		factory.reset();
		response.reset();
		job = new UwsJob(args);
		factory.getSecurityManager().setUser(job.getOwner());
		scheduler.setStartThread(true);
		
		try{
			manager.executeSyncJob(job);
			Assert.fail("Exception expected: max queued jobs reached.");
		}catch(UwsException e){
			//expected
			Assert.assertEquals("Http code expected", 503, e.getCode());
		}
		
		//disable queue limit
		listManager.setMaxQueuedJobs(-1);

	}

}
