package esavo.sl.services.transform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import esavo.sl.services.transform.handlers.TransformHandler;
import esavo.sl.services.transform.handlers.TransformHandlerFactory;
import esavo.sl.services.transform.handlers.TransformHandlerFactory.TransformHandlerType;
import esavo.sl.test.TestUtils;
import esavo.sl.test.data.ReadData;

public class TransformToJsonTest {
	
	private static String expectedJson;
	private static String expectedJsonFromCsv;
	
	@BeforeClass
	public static void oneTimeSetup() throws IOException{
		//expectedJson = ReadData.readDataTextAsResource(TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		expectedJson = ReadData.readDataTextAsResource(TransformToJsonTest.class, TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		expectedJsonFromCsv = ReadData.readDataTextAsResource(TransformToJsonTest.class, TestUtils.DATA_DIR + "transform/transformed_from_csv_into_json.json");
	}

	@Test
	public void testFromJson() throws IOException{
		//String expectedJson = ReadData.readDataTextAsResource(TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		//InputStream isr = this.getClass().getClassLoader().getResourceAsStream(TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		//String expectedJson = ReadData.readDataTextAsResource(isr, false);
		TransformHandlerType type = TransformHandlerType.JSON;
		
		long resultsOffset = 0;
		long pageSize = -1; //no limits
		boolean allStrings = true;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);

		TransformHandler handler = TransformHandlerFactory.createHandler(type, out, resultsOffset, pageSize, null, allStrings);
		
		//InputStream is = this.getClass().getResourceAsStream(TestUtils.DATA_DIR + "transform/result.json");
		InputStream is = ReadData.findResource(this.getClass(), TestUtils.DATA_DIR + "transform/result.json");
		handler.parse(is);
		
		is.close();
		out.close();
		String output = new String(baos.toByteArray(), "UTF-8");
		//System.out.println(output);
		
		Assert.assertEquals(expectedJson, output);
	}

	@Test
	public void testFromVoTable() throws IOException{
		//String expectedJson = ReadData.readDataTextAsResource(TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		TransformHandlerType type = TransformHandlerType.VOTABLE;
		
		long resultsOffset = 0;
		long pageSize = -1; //no limits
		boolean allStrings = true;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);

		TransformHandler handler = TransformHandlerFactory.createHandler(type, out, resultsOffset, pageSize, null, allStrings);
		
		//InputStream is = this.getClass().getResourceAsStream(TestUtils.DATA_DIR + "transform/result.vot");
		InputStream is = ReadData.findResource(this.getClass(), TestUtils.DATA_DIR + "transform/result.vot");
		handler.parse(is);
		
		is.close();
		out.close();
		String output = new String(baos.toByteArray(), "UTF-8");
		//System.out.println(output);
		
		Assert.assertEquals(expectedJson, output);
	}

	@Test
	public void testFromVoCsv() throws IOException{
		//String expectedJson = ReadData.readDataTextAsResource(TestUtils.DATA_DIR + "transform/transformed_into_json.json");
		TransformHandlerType type = TransformHandlerType.CSV;
		
		long resultsOffset = 0;
		long pageSize = -1; //no limits
		boolean allStrings = true;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		
		File tmpDir = new File("./transform_tmpdir");
		tmpDir.mkdirs();
		tmpDir.deleteOnExit();

		TransformHandler handler = TransformHandlerFactory.createHandler(type, out, resultsOffset, pageSize, tmpDir, allStrings);
		
		//InputStream is = this.getClass().getResourceAsStream(TestUtils.DATA_DIR + "transform/result.csv");
		InputStream is = ReadData.findResource(this.getClass(), TestUtils.DATA_DIR + "transform/result.csv");
		handler.parse(is);
		
		is.close();
		out.close();
		String output = new String(baos.toByteArray(), "UTF-8");
		//System.out.println(output);
		
		Assert.assertEquals(expectedJsonFromCsv, output);
	}

}
