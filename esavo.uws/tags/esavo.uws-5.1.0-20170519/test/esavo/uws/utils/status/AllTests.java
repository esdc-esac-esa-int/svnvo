package esavo.uws.utils.status;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All UWS utils status tests");
		
		//submodules
		suite.addTest(suite(UwsStatusManagerTest.class));
		suite.addTest(suite(UwsUserInfoTest.class));
		suite.addTest(suite(UwsUserStatusDataTest.class));

		return suite;
	}

	/**
	 * Allows you to run all tests as an application.
	 */
	public static void main(String[] arguments) {
		TestRunner.run(suite());
	}

	private static Test suite(Class<?> c) {
		return new JUnit4TestAdapter(c);
	}

}