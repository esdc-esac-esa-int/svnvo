package esavo.uws.utils;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All UWS utils tests");
		
		//submodules
		suite.addTest(esavo.uws.utils.status.AllTests.suite());

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
