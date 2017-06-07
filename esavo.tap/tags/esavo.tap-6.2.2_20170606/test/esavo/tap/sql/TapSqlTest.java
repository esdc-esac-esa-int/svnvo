/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
