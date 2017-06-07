package esavo.uws.actions.handlers.admin;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All UWS action handlers admin tests");
		
		//submodules
		suite.addTest(esavo.uws.actions.handlers.admin.handlers.AllTests.suite());
		suite.addTest(suite(TemplatesTest.class));
		//suite.addTest(suite(AdminTest.class));

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
