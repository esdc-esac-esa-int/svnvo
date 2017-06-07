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
package esavo.sl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
//TODO enable this when new stil library is available.
//import uk.ac.starlink.table.Tables;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.votable.VOStarTable;
import uk.ac.starlink.votable.VOTableBuilder;
import esavo.sl.test.TestUtils;
import esavo.sl.test.data.ReadData;
import esavo.tap.TapUtils;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TAPTypes;


public class TestVotTypes {
	
	public static String getHeaderNames(){
		return("ColumnInfo - Name, isArray, Shape, elementSize, Class, contentClass, auxDatumUByte, auxDatumXtype, "
				+ "TAPColumn - Name, DataType, arraySize, TAPSCH_arraysize, VotType, "
				+ "dbtype, dbSqlType");
	}


	class CheckRecord implements Comparator<CheckRecord>{
		String stilColumnInfoName;
		boolean stilIsArray;
		String stilShape;
		int stilElementsSize;
		String stilClass;
		String stilContentClass;
		String stilAuxDatumUnsignedByte;
		String stilAuxDatumXtype;
		String tapColumnName;
		String tapDataType;
		int tapArraySize;
		String tapSchArraySize;
		String tapVotType;
		String dbType;
		String dbSqlType;
		
		@Override
		public String toString(){
			return toString(true);
		}
		
		public String toString(boolean withHeaders){
			if(withHeaders){
				return("ColumnInfo - Name: " + stilColumnInfoName + ", isArray: " + stilIsArray + ", Shape: " + stilShape + 
					", elementSize: " + stilElementsSize + ", Class: " + stilClass + ", contentClass: " + stilContentClass +
					", auxDatumUbyte: " + stilAuxDatumUnsignedByte + ", auxDatumXtype" + stilAuxDatumXtype + 
					", TAPColumn - Name: " + tapColumnName + ", DataType: " + tapDataType + 
					", arraySize: " + tapArraySize + ", TAPSCH_arraysize: " + tapSchArraySize + ", VotType: " + tapVotType + 
					", dbtype: " + dbType + ", dbSqlType: " + dbSqlType);
			}else{
				return(stilColumnInfoName + ", " + stilIsArray + ", " + stilShape + ", " + stilElementsSize + ", " + stilClass + 
						", " + stilContentClass + ", " + stilAuxDatumUnsignedByte + ", " + stilAuxDatumXtype + 
						", " + tapColumnName + ", " + tapDataType + 
						", " + tapArraySize + ", " + tapSchArraySize + ", " + tapVotType + 
						", " + dbType + ", " + dbSqlType);
			}
		}
		
		@Override
		public int compare(CheckRecord o1, CheckRecord o2) {
			if(o1 == null && o2 == null){
				return 0;
			}
			if(o1 == null && o2 != null){
				return -1;
			}
			if(o2 == null && o1 != null){
				return 1;
			}
			int c;
			c = compareStrings(o1.stilColumnInfoName, o2.stilColumnInfoName); 
			if(c != 0){
				return c;
			}
			c = compareBooleans(o1.stilIsArray, o2.stilIsArray);
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.stilShape, o2.stilShape); 
			if(c != 0){
				return c;
			}
			c = compareInts(o1.stilElementsSize, o2.stilElementsSize);
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.stilClass, o2.stilClass); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.stilContentClass, o2.stilContentClass); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.tapColumnName, o2.tapColumnName); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.tapDataType, o2.tapDataType); 
			if(c != 0){
				return c;
			}
			c = compareInts(o1.tapArraySize, o2.tapArraySize);
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.tapSchArraySize, o2.tapSchArraySize); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.tapVotType, o2.tapVotType); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.dbType, o2.dbType); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.dbSqlType, o2.dbSqlType); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.stilAuxDatumUnsignedByte, o2.stilAuxDatumUnsignedByte); 
			if(c != 0){
				return c;
			}
			c = compareStrings(o1.stilAuxDatumXtype, o2.stilAuxDatumXtype); 
			if(c != 0){
				return c;
			}
			
			return 0;
		}
		
		private int compareStrings(String a, String b){
			if(a == null && b == null){
				return 0;
			}
			if(a == null && b != null){
				return -1;
			}
			if(b == null && a != null){
				return 1;
			}
			return a.compareTo(b);
		}
		
		private int compareBooleans(boolean a, boolean b){
			if(a == b){
				return 0;
			}
			if(a == true){
				return 1;
			}
			return -1;
		}
		
		private int compareInts(int a, int b){
			if(a == b){
				return 0;
			}
			if(a > b){
				return 1;
			}
			return -1;
		}
		
		@Override 
		public boolean equals(Object o){
			if(o == null){
				return false;
			}
			if(o instanceof CheckRecord){
				int r = compare(this, (CheckRecord)o);
				return (r == 0);
			}else{
				return false;
			}
		}
	}
	
	public TestVotTypes(){
		
	}
	
	@Test
	public void testTypes() throws Exception {
		InputStream is = ReadData.findResource(this.getClass(), TestUtils.DATA_DIR + "vottypes/test_all_types.vot");
		List<CheckRecord> data = dumpCheck(is);
		is.close();
		
		InputStream isReference = ReadData.findResource(this.getClass(), TestUtils.DATA_DIR + "vottypes/expected_results.txt");
		List<CheckRecord> reference = parseReference(isReference);
		isReference.close();
		
		check(data, reference);
	}
	
	private void check(List<CheckRecord> data, List<CheckRecord> expected){
		if(data == null){
			if(expected != null){
				Assert.fail("Expected results: found null");
			}
			//both null: OK
			return;
		}
		//data not null;
		if(expected == null){
			Assert.fail("Expected no results. Found: " + data.size() + " result(s).");
		}
		//check same size
		Assert.assertEquals("Size", expected.size(), data.size());
		for(int i = 0; i < expected.size(); i++){
			CheckRecord exp = expected.get(i);
			CheckRecord act = data.get(i);
			Assert.assertEquals(exp, act);
		}
	}
	
	private List<CheckRecord> parseReference(InputStream is) throws Exception {
		List<CheckRecord> checkRecords = new ArrayList<TestVotTypes.CheckRecord>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String data;
		String[] items;
		while((line = br.readLine()) != null){
			data = line.trim();
			if("".equals(data)){
				continue;
			}
			items = line.split(",");
			CheckRecord cr = new CheckRecord();
			cr.stilColumnInfoName 		= getValueFromReferenceAsString(items[0]);
			cr.stilIsArray 				= getValueFromReferenceAsBoolean(items[1]);
			cr.stilShape 				= getValueFromReferenceAsString(items[2]);
			cr.stilElementsSize 		= getValueFromReferenceAsInteger(items[3]);
			cr.stilClass 				= getValueFromReferenceAsString(items[4]);
			cr.stilContentClass 		= getValueFromReferenceAsString(items[5]);
			cr.stilAuxDatumUnsignedByte	= getValueFromReferenceAsString(items[6]);
			cr.stilAuxDatumXtype		= getValueFromReferenceAsString(items[7]);
			cr.tapColumnName 			= getValueFromReferenceAsString(items[8]);
			cr.tapDataType 				= getValueFromReferenceAsString(items[9]);
			cr.tapArraySize 			= getValueFromReferenceAsInteger(items[10]);
			cr.tapSchArraySize 			= getValueFromReferenceAsString(items[11]);
			cr.tapVotType 				= getValueFromReferenceAsString(items[12]);
			cr.dbType 					= getValueFromReferenceAsString(items[13]);
			cr.dbSqlType				= getValueFromReferenceAsString(items[14]);
			checkRecords.add(cr);
		}
		br.close();
		return checkRecords;
	}
	
	private String getValueFromReferenceAsString(String value){
		if(value == null){
			return null;
		}
		if("null".equals(value.trim())){
			return null;
		}
		return value.trim();
	}
	
	private boolean getValueFromReferenceAsBoolean(String value){
		if(value == null){
			return false;
		}
		return Boolean.parseBoolean(value.trim());
	}
	
	private int getValueFromReferenceAsInteger(String value){
		if(value == null){
			return 0;
		}
		return Integer.parseInt(value.trim());
	}
	
	public List<CheckRecord> dumpCheck(String path) throws Exception {
		File file = new File(path);
		FileInputStream fins = new FileInputStream(file);
		List<CheckRecord> checkRecords = dumpCheck(fins);
		fins.close();
		return checkRecords;
	}
	
	private List<CheckRecord> dumpCheck(InputStream fins) throws Exception{
		//Upload.upload
		StarTableFactory factory = new StarTableFactory();
		StarTable table = factory.makeStarTable( fins, new VOTableBuilder() );
		List<CheckRecord> checkRecords = fetchTableMeta(table);
		return checkRecords;
	}
	
	private List<CheckRecord> fetchTableMeta(final StarTable votable) throws Exception {
		Map<Integer, String> sqlTypes = getSqlTypes();
		String fieldNameLowerCase;
		List<CheckRecord> checkRecords = new ArrayList<TestVotTypes.CheckRecord>();
		printHeader();
		TAPTable table = new TAPTable("test", TAPTable.PUBLIC_TABLE);
		for(int col=0 ; col<votable.getColumnCount(); col++){
			ColumnInfo field = votable.getColumnInfo(col);
			
			
			fieldNameLowerCase = field.getName().toLowerCase();
			
			int arraysize = 0;
			try{
				arraysize = TapUtils.getArraySize(field);
			}catch(IllegalArgumentException iae){
				System.out.println("Invalid array-size in the uploaded table \""+votable.getName()+"\": "+iae.getMessage()+". It will be considered as \"*\" !");
			}

			int flags = 0;
			
			TAPColumn tc = TapUtils.addColumnToTable(table, field, arraysize, flags, col);
			
			CheckRecord cr = create(field, tc, sqlTypes);
			System.out.println(cr.toString(false));
			checkRecords.add(cr);

		}
		
		return checkRecords;
	}
	
	private void printHeader(){
//		System.out.println("ColumnInfo - Name, isArray, Shape, elementSize, Class, contentClass"
//				+ ", TAPColumn - Name, DataType, arraySize, TAPSCHarraysize, VotType, DBtype");
		System.out.println(getHeaderNames());
	}
	
	private CheckRecord create(ColumnInfo ci, TAPColumn tc, Map<Integer, String> sqlTypes){
		CheckRecord cr = new CheckRecord();
		cr.stilColumnInfoName = ci.getName();
		cr.stilIsArray = ci.isArray();
		cr.stilShape = getShape(ci);
		cr.stilElementsSize = ci.getElementSize();
		cr.stilClass = ci.getClass().getName();
		cr.stilContentClass = ci.getContentClass().getName();
		cr.stilAuxDatumUnsignedByte = getSuitableAuxDatumUnsignedByte(ci);
		cr.stilAuxDatumXtype = getSuitableAuxDatumXtype(ci);
		cr.tapColumnName = tc.getName();
		cr.tapDataType = tc.getDatatype();
		cr.tapArraySize = tc.getArraySize();
		cr.tapSchArraySize = getTapSchemaArraySizeFormated(tc);
		cr.tapVotType = tc.getVotType().toString();
		cr.dbType = TapUtils.getDbType(tc);
		int sqlType = TAPTypes.getEffectiveSQLType(tc);
		cr.dbSqlType = "" + sqlType + " ("+ sqlTypes.get(sqlType)+")";
		return cr;
	}
	
	private String getSuitableAuxDatumUnsignedByte(ColumnInfo ci){
		//TODO enable this when new stil library is available
		//return getSuitableAuxDatum(ci, Tables.UBYTE_FLAG_INFO);
		return null;
	}
	
	private String getSuitableAuxDatumXtype(ColumnInfo ci){
		return getSuitableAuxDatum(ci, VOStarTable.XTYPE_INFO);
	}
	
	private String getSuitableAuxDatum(ColumnInfo ci, ValueInfo vi){
		DescribedValue dv = ci.getAuxDatum(vi);
		if(dv == null){
			return null;
		}else{
			return dv.toString();
		}
	}
	
	private String getTapSchemaArraySizeFormated(TAPColumn col){
		Integer arraySize = TAPTypes.getColumnArraySize(col.getDatatype(), col.getArraySize());
		if(arraySize == null){
			return null;
		}else{
			return ""+arraySize;
		}
	}
	
	private String getShape(ColumnInfo ci){
		int[] s = ci.getShape();
		if(s == null){
			return null;
		}else{
			StringBuilder sb = new StringBuilder("[");
			for(int i = 0; i < s.length; i++){
				if(i != 0){
					sb.append(' ');
				}
				sb.append(i);
			}
			sb.append("]");
			return sb.toString();
		}
	}
	
	private static Map<Integer, String> getSqlTypes() throws Exception {
		Map<Integer, String> map = new HashMap<Integer, String>();
		for(Field field: java.sql.Types.class.getFields()){
			map.put((Integer)field.get(null), field.getName());
		}
		return map;
	}

	
	public static void main(String[] args) throws Exception{
		TestVotTypes t = new TestVotTypes();
		t.dumpCheck("/home/jsegovia/Downloads/test_all_types.vot");
	}

}
