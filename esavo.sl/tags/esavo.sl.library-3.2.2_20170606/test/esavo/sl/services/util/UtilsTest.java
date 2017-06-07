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
package esavo.sl.services.util;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import esavo.tap.TapUtils;
import esavo.tap.metadata.TapTableInfo;
import esavo.uws.utils.test.http.DummyHttpRequest;

public class UtilsTest {
	
	@Test
	public void testIsRaDec(){
		Assert.assertFalse("No ra/dec", TapUtils.isRaOrDec(0));
		Assert.assertTrue("ra", TapUtils.isRaOrDec(TapUtils.TAP_COLUMN_TABLE_FLAG_RA));
		Assert.assertTrue("dec", TapUtils.isRaOrDec(TapUtils.TAP_COLUMN_TABLE_FLAG_DEC));
	}
	
	@Test
	public void testConvertTapTableFlag(){
		Assert.assertEquals("null flag", 0, TapUtils.convertTapTableFlag(null));
		Assert.assertEquals("0 flag", 0, TapUtils.convertTapTableFlag("0"));
		checkTapTableFlag(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA, TapUtils.TAP_COLUMN_TABLE_FLAG_RA, "ra");
		checkTapTableFlag(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC, TapUtils.TAP_COLUMN_TABLE_FLAG_DEC, "dec");
		checkTapTableFlag(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_MAG, TapUtils.TAP_COLUMN_TABLE_FLAG_MAG, "mag");
		checkTapTableFlag(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_FLUX, TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX, "flux");
		
		int flags = TapUtils.convertTapTableFlag(TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA + ", " + TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC);
		Assert.assertTrue("ra found", (flags & TapUtils.TAP_COLUMN_TABLE_FLAG_RA) > 0);
		Assert.assertTrue("dec found", (flags & TapUtils.TAP_COLUMN_TABLE_FLAG_DEC) > 0);
		Assert.assertFalse("mag should not be found", (flags & TapUtils.TAP_COLUMN_TABLE_FLAG_MAG) > 0);
		Assert.assertFalse("flux should not be found", (flags & TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX) > 0);
		
		flags = TapUtils.convertTapTableFlag("100");
		Assert.assertEquals("Number direct conversion", 100, flags);
	}
	
	private void checkTapTableFlag(String flag, int expected, String msg){
		Assert.assertEquals(msg + " 1", expected, TapUtils.convertTapTableFlag(flag));
		Assert.assertEquals(msg + " 2", expected, TapUtils.convertTapTableFlag(" \n"+flag+" \n"));
		Assert.assertEquals(msg + " 3", expected, TapUtils.convertTapTableFlag(flag.toUpperCase()));
		Assert.assertEquals(msg + " 4", expected, TapUtils.convertTapTableFlag(flag.toLowerCase()));
		String tmp = "" + Character.toUpperCase(flag.charAt(0)) + flag.substring(1);
		Assert.assertEquals(msg + " 5", expected, TapUtils.convertTapTableFlag(tmp));
	}
	
	@Test
	public void testGetFlagIds(){
		Assert.assertEquals("No flags", "", TapUtils.getFlagIds(0));
		Assert.assertEquals("Flag ra", TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA, TapUtils.getFlagIds(TapUtils.TAP_COLUMN_TABLE_FLAG_RA));
		Assert.assertEquals("Flag dec", TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC, TapUtils.getFlagIds(TapUtils.TAP_COLUMN_TABLE_FLAG_DEC));
		Assert.assertEquals("Flag mag", TapUtils.TAP_COLUMN_TABLE_FLAG_ID_MAG, TapUtils.getFlagIds(TapUtils.TAP_COLUMN_TABLE_FLAG_MAG));
		Assert.assertEquals("Flag flux", TapUtils.TAP_COLUMN_TABLE_FLAG_ID_FLUX, TapUtils.getFlagIds(TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX));
		int flags = TapUtils.TAP_COLUMN_TABLE_FLAG_RA | TapUtils.TAP_COLUMN_TABLE_FLAG_DEC | TapUtils.TAP_COLUMN_TABLE_FLAG_MAG | TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX;
		String allFlags = TapUtils.TAP_COLUMN_TABLE_FLAG_ID_RA + "," + TapUtils.TAP_COLUMN_TABLE_FLAG_ID_DEC + "," +
				TapUtils.TAP_COLUMN_TABLE_FLAG_ID_FLUX + "," + TapUtils.TAP_COLUMN_TABLE_FLAG_ID_MAG;
		Assert.assertEquals("All flags", allFlags, TapUtils.getFlagIds(flags));
	}
	
	@Test
	public void testAreRaDecAlreadyIndexed(){
		String schemaName = "schema";
		String tableName = "table";
		String raColumn = "col1";
		String decColumn = "col2";
		
		TapTableInfo tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "a");
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "b");
		try{
			TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn);
			Assert.fail("Exception expected I (string instead of integer)");
		}catch(ClassCastException e){
		}
		
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "b");
		try{
			TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn);
			Assert.fail("Exception expected II (string instead of integer)");
		}catch(ClassCastException e){
		}
		
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		Assert.assertTrue("Index expected", TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn));
		
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_MAG);
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_DEC);
		Assert.assertFalse("Only dec must be found => not indexed", TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn));

		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_RA);
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_MAG);
		Assert.assertFalse("Only ra must be found => not indexed", TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn));

		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.putColumn(raColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_FLUX);
		tti.putColumn(decColumn, TapUtils.TAP_COLUMNS_TABLE_FLAGS, TapUtils.TAP_COLUMN_TABLE_FLAG_MAG);
		Assert.assertFalse("No ra nor dec must be found => not indexed", TapUtils.areAlreadyIndexedRaDec(tti, raColumn, decColumn));
	}
	
	@Test
	public void testRaDecRequireUpdate(){
		String ucd;
		String uType;
		int flags;
		int indexed;
		String schemaName = "schema";
		String tableName = "table";
		String tableColumnName = "col";
		TapTableInfo tti = null;
		
		ucd = "ucd";
		uType = "utype";
		flags = 0;
		indexed = 0;
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "c"); //wrong class
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertFalse("All the same", TapUtils.requireUpdate(tti, tableColumnName, ucd, uType, flags, indexed));
		
		//everything is the same => no change
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertFalse("All the same", TapUtils.requireUpdate(tti, tableColumnName, ucd, uType, flags, indexed));
		
		//Different flags
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags+1);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertTrue("Different flags", TapUtils.requireUpdate(tti, tableColumnName, ucd, uType, flags, indexed));
		
		//Different ucd
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd+"x");
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertTrue("Different ucd", TapUtils.requireUpdate(tti, tableColumnName, ucd, uType, flags, indexed));
		
		//Different utype
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType+"z");
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertTrue("Different utype", TapUtils.requireUpdate(tti, tableColumnName, ucd, uType, flags, indexed));
		
		//Nulls
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertTrue("Different ucd (null)", TapUtils.requireUpdate(tti, tableColumnName, null, uType, flags, indexed));
		Assert.assertTrue("Different utype (null)", TapUtils.requireUpdate(tti, tableColumnName, ucd, null, flags, indexed));
		
		//Null ucd (same)
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, null);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, uType);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertFalse("Same ucd (null)", TapUtils.requireUpdate(tti, tableColumnName, null, uType, flags, indexed));

		//Null utype (same)
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UCD, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_UTYPE, String.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UCD, ucd);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_UTYPE, null);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_FLAGS, flags);
		tti.putColumn(tableColumnName, TapUtils.TAP_COLUMNS_TABLE_INDEXED, indexed);
		Assert.assertFalse("Same utype (null)", TapUtils.requireUpdate(tti, tableColumnName, ucd, null, flags, indexed));
	}
	
	@Test
	public void testCheckIndexType(){
		String schemaName = "schema";
		String tableName = "table";
		String tableColumnName1 = "col1";
		String tableColumnName2 = "col2";
		String tableColumnName3 = "col3";
		TapTableInfo tti = null;
		
		tti = new TapTableInfo(schemaName, tableName);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_INDEXED, Integer.class);
		tti.addColumnDataType(TapUtils.TAP_COLUMNS_TABLE_FLAGS, String.class);
		tti.putColumn(tableColumnName1, TapUtils.TAP_COLUMNS_TABLE_INDEXED, "c"); //wrong class
		tti.putColumn(tableColumnName2, TapUtils.TAP_COLUMNS_TABLE_INDEXED, 1); //True
		tti.putColumn(tableColumnName3, TapUtils.TAP_COLUMNS_TABLE_INDEXED, 0); //False

		tti.putColumn(tableColumnName1, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "c"); //wrong class
		tti.putColumn(tableColumnName2, TapUtils.TAP_COLUMNS_TABLE_FLAGS, 1); //wrong class
		tti.putColumn(tableColumnName3, TapUtils.TAP_COLUMNS_TABLE_FLAGS, "2"); //wrong class

		Assert.assertFalse("Indexed false, null value", TapUtils.isTrueFromTapTableIndexed(null));
		Assert.assertFalse("Integer 0", TapUtils.isTrueFromTapTableIndexed(new Integer(0)));
		Assert.assertFalse("String 0", TapUtils.isTrueFromTapTableIndexed("0"));
		Assert.assertFalse("Boolean false", TapUtils.isTrueFromTapTableIndexed(false));
		Assert.assertFalse("String false", TapUtils.isTrueFromTapTableIndexed("false"));
		Assert.assertFalse("String False", TapUtils.isTrueFromTapTableIndexed("False"));
		Assert.assertTrue("Integer 1", TapUtils.isTrueFromTapTableIndexed(new Integer(1)));
		Assert.assertTrue("String 1", TapUtils.isTrueFromTapTableIndexed("1"));
		Assert.assertTrue("Boolean true", TapUtils.isTrueFromTapTableIndexed(true));
		Assert.assertTrue("String true", TapUtils.isTrueFromTapTableIndexed("true"));
		Assert.assertTrue("String True", TapUtils.isTrueFromTapTableIndexed("True"));
		Assert.assertFalse("Wrong class", TapUtils.isTrueFromTapTableIndexed(tti, tableColumnName1));
		Assert.assertTrue("Integer 1", TapUtils.isTrueFromTapTableIndexed(tti, tableColumnName2));
		Assert.assertFalse("Integer 0", TapUtils.isTrueFromTapTableIndexed(tti, tableColumnName3));
		
		Assert.assertEquals("Wrong class", 0, TapUtils.getFlagsFromTapTable(tti, tableColumnName1));
		Assert.assertEquals("Integer 1", 1, TapUtils.getFlagsFromTapTable(tti, tableColumnName2));
		Assert.assertEquals("String 2", 2, TapUtils.getFlagsFromTapTable(tti, tableColumnName3));
		Assert.assertEquals("Not found", 0, TapUtils.getFlagsFromTapTable(tti, tableColumnName1+"xxx"));

		tti.putColumn(tableColumnName1, TapUtils.TAP_COLUMNS_TABLE_INDEXED, 0); //Not indexed
		tti.putColumn(tableColumnName2, TapUtils.TAP_COLUMNS_TABLE_INDEXED, 1); //Indexed Ra
		tti.putColumn(tableColumnName3, TapUtils.TAP_COLUMNS_TABLE_INDEXED, 1); //Indexed no Ra/Dec

		tti.putColumn(tableColumnName1, TapUtils.TAP_COLUMNS_TABLE_FLAGS, 0); //No flags
		tti.putColumn(tableColumnName2, TapUtils.TAP_COLUMNS_TABLE_FLAGS, 1); //flags Ra
		tti.putColumn(tableColumnName3, TapUtils.TAP_COLUMNS_TABLE_FLAGS, 0); //No ra/dec flags
		
		Assert.assertFalse("col1: No normal index", TapUtils.isNormalIndexed(tti, tableColumnName1));
		Assert.assertFalse("col1: No ra/dec index", TapUtils.isRaDecIndexed(tti, tableColumnName1));
		Assert.assertFalse("col2: No Normal index", TapUtils.isNormalIndexed(tti, tableColumnName2));
		Assert.assertTrue("col2: ra/dec index", TapUtils.isRaDecIndexed(tti, tableColumnName2));
		Assert.assertTrue("col3: Normal index", TapUtils.isNormalIndexed(tti, tableColumnName3));
		Assert.assertFalse("col3: No ra/dec index", TapUtils.isRaDecIndexed(tti, tableColumnName3));
	}

	@Test
	public void getTableNameOnly(){
		String s = "schema.table";
		Assert.assertEquals("table", TapUtils.getTableNameOnly(s));
		s = "table";
		Assert.assertEquals("table", TapUtils.getTableNameOnly(s));
		Assert.assertNull(TapUtils.getTableNameOnly(null));
	}
	
	@Test
	public void testParameters() throws IOException{
		DummyHttpRequest request = new DummyHttpRequest();
		String paramNotFoundKey = "notFound";
		String paramInvalidNumber = "InvalidNumber";
		String paramDoubleKey = "doublekey";
		double doubleValue = 2.3;
		String paramIntKey = "intKey";
		int intValue = 5;
		String paramLongKey = "longKey";
		long longValue = 27L;
		String paramBoolKey = "boolKey";
		boolean boolValue  = true;
		request.setParameter(paramInvalidNumber, "a");
		request.setParameter(paramDoubleKey, ""+doubleValue);
		request.setParameter(paramIntKey, ""+intValue);
		request.setParameter(paramLongKey, ""+longValue);
		request.setParameter(paramBoolKey, ""+boolValue);
		
		//Double
		try{
			TapUtils.getDoubleParameter(paramNotFoundKey, request);
			Assert.fail("Double: Exception expected: parameter not found");
		}catch(IOException e){
		}
		try{
			TapUtils.getDoubleParameter(paramInvalidNumber, request);
			Assert.fail("Double: Exception expected: invalid value");
		}catch(IOException e){
		}
		Assert.assertEquals("Valid double", doubleValue, TapUtils.getDoubleParameter(paramDoubleKey, request));
		Assert.assertEquals("Default double", doubleValue, TapUtils.getDoubleParameter(paramNotFoundKey, request, ""+doubleValue));
		
		//Int
		try{
			TapUtils.getIntegerParameter(paramNotFoundKey, request);
			Assert.fail("Integer: Exception expected: parameter not found");
		}catch(IOException e){
		}
		try{
			TapUtils.getIntegerParameter(paramInvalidNumber, request);
			Assert.fail("Integer: Exception expected: invalid value");
		}catch(IOException e){
		}
		Assert.assertEquals("Valid int", intValue, TapUtils.getIntegerParameter(paramIntKey, request));
		Assert.assertEquals("Default int", intValue, TapUtils.getIntegerParameter(paramNotFoundKey, request, ""+intValue));

		//Long
		try{
			TapUtils.getLongParameter(paramNotFoundKey, request);
			Assert.fail("Long: Exception expected: parameter not found");
		}catch(IOException e){
		}
		try{
			TapUtils.getLongParameter(paramInvalidNumber, request);
			Assert.fail("Long: Exception expected: invalid value");
		}catch(IOException e){
		}
		Assert.assertEquals("Valid long", longValue, TapUtils.getLongParameter(paramLongKey, request));
		Assert.assertEquals("Default long", longValue, TapUtils.getLongParameter(paramNotFoundKey, request, ""+longValue));

		//Boolean
		try{
			TapUtils.getBooleanParameter(paramNotFoundKey, request);
			Assert.fail("Boolean: Exception expected: parameter not found");
		}catch(IOException e){
		}
		Assert.assertFalse("No boolean", TapUtils.getBooleanParameter(paramInvalidNumber, request));
		Assert.assertEquals("Valid bool", boolValue, TapUtils.getBooleanParameter(paramBoolKey, request));
		Assert.assertEquals("Default bool", boolValue, TapUtils.getBooleanParameter(paramNotFoundKey, request, boolValue));
		
		try{
			TapUtils.getParameter(paramNotFoundKey, request);
			Assert.fail("Exception expected: parameter not found");
		}catch(IOException e){
		}
		Assert.assertEquals("Parameter found", ""+intValue, TapUtils.getParameter(paramIntKey, request));
	}
	
//	@Test
//	public void testCheckAuthentication(){
//		try{
//			TapUtils.checkAuthentication(null);
//			Assert.fail("Exception expected: null owner");
//		}catch(InvalidParameterException e){
//		}
//		DefaultJobOwner owner = new DefaultJobOwner(null);
//		try{
//			TapUtils.checkAuthentication(owner);
//			Assert.fail("Exception expected: null id");
//		}catch(InvalidParameterException e){
//		}
//		String id = "id";
//		String authUsername = "auth";
//		String pseudo = "p";
//		owner = new DefaultJobOwner(id, authUsername, pseudo);
//		TapUtils.checkAuthentication(owner);
//	}
	
//	@Test
//	public void testGetTapSchemaInfo(){
//		DummyServletContext servletContext = new DummyServletContext();
//		DummyServletConfig servletConfig = new DummyServletConfig(servletContext);
//		
//		TAPSchemaInfo tapSchemaInfo = TapUtils.getTapSchemaInfo(servletConfig);
//		Assert.assertEquals("Default schemas table name", "schemas", tapSchemaInfo.getTapSchemasTableName());
//		Assert.assertEquals("Default tables table name", "tables", tapSchemaInfo.getTapTablesTableName());
//		Assert.assertEquals("Default columns table name", "columns", tapSchemaInfo.getTapColumnsTableName());
//		Assert.assertEquals("Default keys table name", "keys", tapSchemaInfo.getTapKeysTableName());
//		
//		servletContext.setInitParameter(TapServlet.TAP_SCHEMAS_USE_VIEWS, "true");
//		
//		tapSchemaInfo = TapUtils.getTapSchemaInfo(servletConfig);
//		Assert.assertEquals("Normal (with views) schemas table name", "all_schemas", tapSchemaInfo.getTapSchemasTableName());
//		Assert.assertEquals("Normal (with views) tables table name", "all_tables", tapSchemaInfo.getTapTablesTableName());
//		Assert.assertEquals("Normal (with views) columns table name", "all_columns", tapSchemaInfo.getTapColumnsTableName());
//		Assert.assertEquals("Normal (with views) keys table name", "all_keys", tapSchemaInfo.getTapKeysTableName());
//	}
}
