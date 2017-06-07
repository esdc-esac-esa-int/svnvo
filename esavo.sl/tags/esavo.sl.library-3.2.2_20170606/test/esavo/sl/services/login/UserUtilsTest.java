package esavo.sl.services.login;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import esavo.sl.test.TestUtils;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.test.UwsTestUtils;
import esavo.uws.utils.test.http.DummyHttpResponse;
import esavo.uws.utils.test.uws.DummyUwsExecutor;
import esavo.uws.utils.test.uws.DummyUwsFactory;
import esavo.uws.utils.test.uws.DummyUwsFactory.StorageType;
import esavo.uws.utils.test.uws.DummyUwsScheduler;
import esavo.uws.utils.test.uws.DummyUwsStorageManager;

public class UserUtilsTest {
	
	public static final String TEST_ID = UserUtilsTest.class.getName();
	
	private static File fStorageDir;
	private static String appid = TEST_ID;
	private static UwsManager manager;
	private static DummyUwsFactory factory;
	private static UwsConfiguration configuration;
	private static DummyUwsStorageManager storage;
	//private static UwsJobsListManager listManager;
	private static DummyUwsExecutor executor;
	private static DummyUwsScheduler scheduler;

	@BeforeClass
	public static void beforeClass() throws UwsException{
		fStorageDir = new File(".", TEST_ID);
		fStorageDir.mkdirs();
		configuration = UwsConfigurationManager.getConfiguration(appid);
		factory = new DummyUwsFactory(appid, fStorageDir, configuration, StorageType.fake); 
		manager = UwsManager.getInstance();
		storage = (DummyUwsStorageManager)factory.getStorageManager();
		executor = (DummyUwsExecutor)factory.getExecutor();
		scheduler = (DummyUwsScheduler)factory.getScheduler();
		//listManager = UwsJobsListManager.getInstance(appid);
	}
	
	@AfterClass
	public static void afterClass(){
		UwsTestUtils.removeDirectory(fStorageDir);
	}
	

	
	@Test
	public void testSendLogInRequiredResponse() throws IOException{
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		UserUtils.sendLogInRequiredResponse(response);
		
		String expected = "User must be logged in to perform this action\n";
		String msg = response.getOutputAsString();
		Assert.assertEquals("Expected login message", expected, msg);
	}
	
	
	@Test
	public void testCreateXmlFromUser() throws ParserConfigurationException, UwsException{
		//UwsJobOwner user = new UwsJobOwner(UwsUtils.ANONYMOUS_USER, UwsJobOwner.ROLE_ADMIN);
		//user.setAuthUsername("anonymous");
		UwsJobOwner user = UwsUtils.createDefaultOwner(UwsUtils.ANONYMOUS_USER, appid);
		user.setRoles(UwsJobOwner.ROLE_ADMIN);
		user.setAuthUsername("anonymous");
		UwsJobOwnerParameters parameters = new UwsJobOwnerParameters();
		
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA, new Long(100));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE, new Long(100));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA, new Long(100));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME, new Long(1800));
		parameters.setParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME, new Long(60));
		user.setParameters(parameters);

		Document doc = UserUtils.createXmlFromUser(user);
		Assert.assertNotNull("Expected xml document", doc);
		
		checkXmlFromUser(doc, user);
	}
	
	private void checkXmlFromUser(Document doc, UwsJobOwner user){
		Element eRoot = doc.getDocumentElement();
		NodeList nl = eRoot.getChildNodes();
		
		int numParameters = 0;
		if(user.getParameters()!=null) {
			numParameters=user.getParameters().getNumParameters();
		}
		int extraArgs = 2; //userid and username
		Assert.assertEquals("Number of children", extraArgs+numParameters, nl.getLength());
		Element e;
		for(int i = 0; i < nl.getLength(); i++){
			e = (Element)nl.item(i);
			checkXmlFromUser(e, user);
		}
	}
	
	private void checkXmlFromUser(Element e, UwsJobOwner user){
		String id = e.getNodeName();
		String expected = getSuitableValueFromUser(user, id);
		String text = null;
		Text t = (Text)e.getChildNodes().item(0);
		if(t != null){
			text = t.getTextContent();
		}
		Assert.assertEquals("Testing xml node name: " + id, expected, text);
	}
	
	private String getSuitableValueFromUser(UwsJobOwner user, String id) {
		if (UserUtils.XML_TAG_VALUE_USER_USERNAME.equals(id)) {
			return user.getAuthUsername();
		}
		if ("user_name_details".equals(id)){
			return user.getName();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_DB_QUOTA).toString();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_DB_SIZE).toString();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_FILES_QUOTA).toString();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_CURRENT_FILES_SIZE).toString();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME).toString();
		}
		if (UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME.equals(id)) {
			return user.getParameters().getLongParameter(UwsJobsOwnersManager.OWNER_PARAMETER_SYNC_MAX_EXEC_TIME).toString();
		}
		throw new IllegalArgumentException(id + " not found");

	}
	
	@Test
	public void testSendResponseWithUserDetails() throws Exception {
		String username = "test";
		//UwsJobOwner user = new UwsJobOwner(username, UwsJobOwner.ROLE_USER);
		UwsJobOwner user = UwsUtils.createDefaultOwner(username, appid);
		DummyHttpResponse response = TestUtils.createSimpleHttpResponse();
		
		UserUtils.sendResponseWithUserDetails(user, response);
		
		String msg = response.getOutputAsString();
		
		InputSource is = new InputSource(new StringReader(msg));

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		doc.getDocumentElement().normalize();

		checkXmlFromUser(doc, user);
	}
	

}
