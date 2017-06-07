package esavo.tap.sql;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import esavo.tap.TapTestUtils;

public class TapSqlTest {
	
	/**
	 * Unique id
	 */
	public static final String TEST_APP_ID = "__TEST__" + TapSqlTest.class.getName();

	@Test
	public void testCreate() throws IOException{
		String createSql = TapSql.getCreateTapSchemaSql("test_schema");
		String testData = TapTestUtils.readDataFromResource(TapTestUtils.DATA_DIR + "sql_create_test_data.txt");
		Assert.assertEquals("Schema creation", testData, createSql);
	}

	@Test
	public void testDrop() throws IOException{
		String createSql = TapSql.getDeleteTapSchemaSql("test_schema");
		String testData = TapTestUtils.readDataFromResource(TapTestUtils.DATA_DIR + "sql_drop_test_data.txt");
		Assert.assertEquals("Schema removal", testData, createSql);
	}

}
