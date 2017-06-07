package esavo.sl.services;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All ESAVO TAP SL services tests");
		
		//submodules
		suite.addTest(esavo.sl.services.status.AllTests.suite());
		suite.addTest(esavo.sl.services.transform.AllTests.suite());
		suite.addTest(esavo.sl.services.upload.AllTests.suite());
		suite.addTest(esavo.sl.services.login.AllTests.suite());
		suite.addTest(esavo.sl.services.nameresolution.AllTests.suite());
		suite.addTest(esavo.sl.services.tabletool.AllTests.suite());
		suite.addTest(esavo.sl.services.util.AllTests.suite());

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
