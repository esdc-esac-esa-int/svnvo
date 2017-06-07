package esavo.sl.services.upload;

import esavo.sl.services.upload.UploadTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All ESAVO TAP SL upload services tests");
		
		//submodules
		suite.addTest(suite(UploadTest.class));
		suite.addTest(suite(UploadProgressListenerTest.class));

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
