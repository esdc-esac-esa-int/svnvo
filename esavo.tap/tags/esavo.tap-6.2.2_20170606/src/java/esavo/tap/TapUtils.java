package esavo.tap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.votable.VOStarTable;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TAPTypes;
import esavo.tap.metadata.TapTableInfo;
import esavo.tap.metadata.VotType;

/**
 * Utilities
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TapUtils {

	//Binary flags, 0,1,2,4,8,16...
	public static final int TAP_TABLE_TYPE_NORMAL = 0;
	public static final int TAP_TABLE_TYPE_RADEC  = 1;
	public static final int TAP_TABLE_TYPE_XMATCH = 2;
	
	//Binary flags, 0,1,2,4,8,16...
	public static final int TAP_COLUMN_TABLE_FLAG_RA   = 1;
	public static final int TAP_COLUMN_TABLE_FLAG_DEC  = 2;
	public static final int TAP_COLUMN_TABLE_FLAG_FLUX = 4;
	public static final int TAP_COLUMN_TABLE_FLAG_MAG  = 8;
	public static final int TAP_COLUMN_TABLE_FLAG_PK   = 16;
	
	public static final String TAP_COLUMN_TABLE_FLAG_ID_RA = "ra";
	public static final String TAP_COLUMN_TABLE_FLAG_ID_DEC = "dec";
	public static final String TAP_COLUMN_TABLE_FLAG_ID_FLUX = "flux";
	public static final String TAP_COLUMN_TABLE_FLAG_ID_MAG = "mag";
	public static final String TAP_COLUMN_TABLE_FLAG_ID_PK = "pk";

	public static final String TAP_COLUMNS_TABLE_UCD = "ucd";
	public static final String TAP_COLUMNS_TABLE_UTYPE = "utype";
	public static final String TAP_COLUMNS_TABLE_FLAGS = "flags";
	public static final String TAP_COLUMNS_TABLE_INDEXED = "indexed";
	

	/**
	 * Returns a table name only. I.e., in case the table contains the schema name, it is removed.
	 * @param s a table name or fully qualified name (schema+table)
	 * @return a table name only.
	 */
	public static String getTableNameOnly(String s){
		if(s == null){
			return null;
		}
		int p = s.lastIndexOf('.');
		if(p < 0){
			return s;
		}
		return s.substring(p+1);
	}
	
	/**
	 * Returns a schema name only.
	 * @param s a table name or fully qualified name (schema+table)
	 * @return a schema name only.
	 */
	public static String getSchemaNameOnly(String s){
		if(s == null){
			return null;
		}
		int p = s.lastIndexOf('.');
		if(p < 0){
			return null;
		}
		return s.substring(0,p);
	}
	
	/**
	 * Returns the user name from a full qualified table name or table name
	 * @param s
	 * @return
	 */
	public static String getUserNameFromSchema(String s){
		String schema = getSchemaNameOnly(s);
		if(schema == null){
			return null;
		}
		int p = schema.lastIndexOf('_');
		if(p < 0){
			return null;
		}
		return schema.substring(0, p);
	}

	
	/**
	 * Returns 'true' if the provided userSchema is the same the the schema specified in fullQualifiedTableName
	 * @param userSchema
	 * @param fullQualifiedTableName
	 * @return
	 */
	public static boolean checkUserSchema(String userSchema, String fullQualifiedTableName) {
		if (fullQualifiedTableName == null) {
			return userSchema == null;
		} else {
			String schema = getSchemaNameOnly(fullQualifiedTableName);
			if (schema == null) {
				return userSchema == null;
			} else {
				return schema.equals(userSchema);
			}
		}
	}
	
	public static boolean isSchemaPublic(TAPSchemaInfo tapSchemaInfo, String schemaName, DBConnection dbConn) throws DBException {
		if(TAPMetadataLoader.TAP_UPLOAD.equalsIgnoreCase(schemaName)){
			return true;
		}else{
			return dbConn.isSchemaPublic(tapSchemaInfo, schemaName);
		}
	}
	
	/**
	 * Returns 'true' if the schema from full qualified table name is found in 'availableSchemas' list
	 * @param availableSchemas
	 * @param fullQualifiedTableName
	 * @return
	 */
	public static boolean checkValidSchema(List<String> availableSchemas, String fullQualifiedTableName){
		if(fullQualifiedTableName == null){
			return false;
		}
		
		String schema = getSchemaNameOnly(fullQualifiedTableName);
		if(schema == null){
			return false;
		}

		if(availableSchemas == null){
			return false;
		}
		
		for(String sch: availableSchemas){
			if(schema.equals(sch)){
				return true;
			}
		}
		
		return false;
	}

	
	/** Get the list of all allowed coordinate systems. */
	public static ArrayList<String> getCoordinateSystems(){
		ArrayList<String> coordSys = new ArrayList<String>(2);
		coordSys.add("ICRS");
		coordSys.add("ICRS BARYCENTER");
		return coordSys;
	}
	
	public static String getDbType(TAPColumn tc){
		if(TAPTypes.checkVarBinaryRequired(tc.getDatatype(), tc.getArraySize())){
			//TODO function to obtain the array data type required
			return "bytea";
		}else{
			return TAPTypes.getDBType(tc.getDatatype());
		}
	}
	
	public static int getArraySize(ColumnInfo field){
		int arraysize = TAPTypes.NO_SIZE;

		if(field.getContentClass().equals(String.class)){
			arraysize = TAPTypes.STAR_SIZE;
		}else if(!field.isArray())
			arraysize = 1;
		else if (field.isArray()&&field.getShape()[0]<=0)
			arraysize = TAPTypes.STAR_SIZE;
		else{
			try{
				arraysize = field.getShape()[0];
			}catch(Exception nfe){
				throw new IllegalArgumentException("Invalid array-size for the field \""+field.getName()+"\": \""+arraysize+"\"");
			}
		}
		return arraysize;
	}
	
	
	public static TAPColumn addColumnToTable(TAPTable tapTable, ColumnInfo field, int arraysize, int flags, int pos){
		VotType votType;
		if(field.getAuxData().size()>0){
			votType= new VotType((String)field.getAuxDatumValue(VOStarTable.DATATYPE_INFO, String.class), 
								arraysize, 
								(String)field.getAuxDatumValue(VOStarTable.XTYPE_INFO, String.class)); 
		}else{
			votType = TAPTypes.getVotType(field.getContentClass());
			votType.arraysize=arraysize;
			
		}
		
		return tapTable.addColumn(field.getName().toLowerCase(), 
				field.getDescription(), 
				field.getUnitString(), 
				field.getUCD(), 
				field.getUtype(), 
				votType, 
				false, 
				false, 
				false,
				flags,
				pos);

	}
	
	public static boolean hasParam(Map map, String key){
		if(map == null || key == null){
			return false;
		}
		if(map.containsKey(key)){
			return true;
		}
		if(map.containsKey(key.toLowerCase())){
			return true;
		}
		if(map.containsKey(key.toUpperCase())){
			return true;
		}
		return false;
	}
	
	public static String getParam(HttpServletRequest request, String key){
		if(request == null || key == null){
			return null;
		}
		String o;
		o = request.getParameter(key);
		if(o != null){
			return o;
		}
		o = request.getParameter(key.toLowerCase());
		if(o != null){
			return o;
		}
		return request.getParameter(key.toUpperCase());
	}

	public static double getDoubleParameter(String parameter, HttpServletRequest request) throws IOException{
		return getDoubleParameter(parameter, request, null);
	}
	
	public static double getDoubleParameter(String parameter, HttpServletRequest request, String defaultValue) throws IOException{
		String v = getParameter(parameter, request, defaultValue);
		try{
			return Double.parseDouble(v);
		}catch(NumberFormatException nfe){
			throw new IOException("Invalid " + parameter + " value: '"+v+"'");
		}
	}

	public static int getIntegerParameter(String parameter, HttpServletRequest request) throws IOException{
		return getIntegerParameter(parameter, request, null);
	}

	public static int getIntegerParameter(String parameter, HttpServletRequest request, String defaultValue) throws IOException{
		String v = getParameter(parameter, request, defaultValue);
		try{
			return Integer.parseInt(v);
		}catch(NumberFormatException nfe){
			throw new IOException("Invalid " + parameter + " value: '"+v+"'");
		}
	}
	

	public static long getLongParameter(String parameter, HttpServletRequest request) throws IOException{
		return getLongParameter(parameter, request, null);
	}

	public static long getLongParameter(String parameter, HttpServletRequest request, String defaultValue) throws IOException{
		String v = getParameter(parameter, request, defaultValue);
		try{
			return Long.parseLong(v);
		}catch(NumberFormatException nfe){
			throw new IOException("Invalid " + parameter + " value: '"+v+"'");
		}
	}
	
	public static boolean getBooleanParameter(String parameter, HttpServletRequest request) throws IOException{
		return getBooleanParameter(parameter, request, null);
	}

	public static boolean getBooleanParameter(String parameter, HttpServletRequest request, Boolean defaultValue) throws IOException{
		String v = getParameter(parameter, request, defaultValue == null ? (String)null : defaultValue.toString());
		return Boolean.parseBoolean(v);
//		try{
//			return Boolean.parseBoolean(v);
//		}catch(NumberFormatException nfe){
//			throw new IOException("Invalid " + parameter + " value: '"+v+"'");
//		}
	}

	public static String getParameter(String parameter, HttpServletRequest request) throws IOException{
		return getParameter(parameter, request, null);
	}
	
	public static String getParameter(String parameter, HttpServletRequest request, String defaultValue) throws IOException{
		String value = request.getParameter(parameter);
		if(value == null){
			if(defaultValue == null){
				String warnMsg = "Param [" + parameter + "] must be specified in request";
				throw new IOException(warnMsg);
			}else{
				value = defaultValue;
			}
		}
		return value;
	}

	public static int convertTapTableFlag(String flag){
		if(flag == null){
			return 0;
		}
		try{
			//If the string is a number, return it
			return Integer.parseInt(flag);
		}catch(NumberFormatException nfe){
		}
		int fValue = 0;
		if(flag.indexOf(',') >= 0){
			String[] flags = flag.split(",");
			for(String f: flags){
				fValue += convertTapTableFlag(f);
			}
		}else{
			String t = flag.trim();
			if(TAP_COLUMN_TABLE_FLAG_ID_RA.equalsIgnoreCase(t)){
				return TAP_COLUMN_TABLE_FLAG_RA;
			} else if(TAP_COLUMN_TABLE_FLAG_ID_DEC.equalsIgnoreCase(t)){
				return TAP_COLUMN_TABLE_FLAG_DEC;
			} else if(TAP_COLUMN_TABLE_FLAG_ID_FLUX.equalsIgnoreCase(t)){
				return TAP_COLUMN_TABLE_FLAG_FLUX;
			} else if(TAP_COLUMN_TABLE_FLAG_ID_MAG.equalsIgnoreCase(t)){
				return TAP_COLUMN_TABLE_FLAG_MAG;
			} else if(TAP_COLUMN_TABLE_FLAG_ID_PK.equalsIgnoreCase(t)){
				return TAP_COLUMN_TABLE_FLAG_PK;
			}
		}
		return fValue;
	}
	
	/**
	 * Tests whether 'flags' is 'ra' or 'dec' (binary comparison)
	 * @param flags flags to test.
	 * @return 'true' if 'flags' is 'ra' or 'dec'.
	 */
	public static boolean isRaOrDec(int flags){
		if((flags & TAP_COLUMN_TABLE_FLAG_RA) > 0){
			return true;
		}
		if((flags & TAP_COLUMN_TABLE_FLAG_DEC) > 0){
			return true;
		}
		return false;
	}

	/**
	 * Return 'true' if the RA flag is active.
	 * @param flags
	 * @return
	 */
	public static boolean isRa(int flags){
		return ((flags & TAP_COLUMN_TABLE_FLAG_RA) > 0);
	}
	
	/**
	 * Return 'true' if the DEC flag is active.
	 * @param flags
	 * @return
	 */
	public static boolean isDec(int flags){
		return ((flags & TAP_COLUMN_TABLE_FLAG_DEC) > 0);
	}
	
	/**
	 * Returns the first column name that has the specified flag set.<br/>
	 * Use this method for unique flags.
	 * @param tapTableInfo
	 * @param flag
	 * @return
	 */
	public static String findParameterByFlag(TapTableInfo tapTableInfo, int flag){
		for(String tableColumnName: tapTableInfo.getTableColumnNames()){
			int i = tapTableInfo.getInteger(tableColumnName, "flags");
			if((i & flag) > 0){
				return tableColumnName;
			}
		}
		return null;
	}

	/**
	 * Returns the flags associated to a column.<br/>
	 * Use this method for unique flags.
	 * @param tapTableInfo
	 * @param columnName
	 * @return
	 */
	public static int findFlagsFor(TapTableInfo tapTableInfo, String columnName){
		return tapTableInfo.getInteger(columnName, "flags");
	}

	/**
	 * Returns an string representation of the provided flags argument.
	 * @param flags flags
	 * @return an string representation of the provided flags argument.
	 */
	public static String getFlagIds(int flags){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		if((flags & TAP_COLUMN_TABLE_FLAG_RA) > 0){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(",");
			}
			sb.append(TAP_COLUMN_TABLE_FLAG_ID_RA);
		}
		if((flags & TAP_COLUMN_TABLE_FLAG_DEC) > 0){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(",");
			}
			sb.append(TAP_COLUMN_TABLE_FLAG_ID_DEC);
		}
		if((flags & TAP_COLUMN_TABLE_FLAG_FLUX) > 0){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(",");
			}
			sb.append(TAP_COLUMN_TABLE_FLAG_ID_FLUX);
		}
		if((flags & TAP_COLUMN_TABLE_FLAG_MAG) > 0){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(",");
			}
			sb.append(TAP_COLUMN_TABLE_FLAG_ID_MAG);
		}
		return sb.toString();
	}

	/**
	 * Checks whether ra and dec are already indexed.<br/>
	 * raColumn and decColumn are the columns to be indexed. tapTableInfo contains the current database information.
	 * @param tapTableInfo current database information.
	 * @param raColumn ra column name.
	 * @param decColumn dec column name.
	 * @return 'true' if the ra and dec are already indexed.
	 */
	public static boolean areAlreadyIndexedRaDec(TapTableInfo tapTableInfo, String raColumn, String decColumn){
		boolean ra = (tapTableInfo.getInteger(raColumn, TAP_COLUMNS_TABLE_FLAGS) & TAP_COLUMN_TABLE_FLAG_RA) > 0;
		boolean dec = (tapTableInfo.getInteger(decColumn, TAP_COLUMNS_TABLE_FLAGS) & TAP_COLUMN_TABLE_FLAG_DEC) > 0;
		return (ra && dec);
	}
	
	/**
	 * Compares current database values with the new ones. If a new value is different than the old one, 'true' is returned.
	 * @param tapTableInfo current database information.
	 * @param tableColumnName tap table column name to test.
	 * @param ucd new ucd value
	 * @param uType new utype value
	 * @param flags new flags value
	 * @param indexed new index value
	 * @return 'true' if a new value is different than the old one.
	 */
	public static boolean requireUpdate(TapTableInfo tapTableInfo, String tableColumnName, String ucd, String uType, int flags, int indexed){
		String testDbData = tapTableInfo.getString(tableColumnName, TAP_COLUMNS_TABLE_UCD);
		if(!compareDataBaseDataWithValue(testDbData, ucd)){
			return true;
		}
		testDbData = tapTableInfo.getString(tableColumnName, TAP_COLUMNS_TABLE_UTYPE);
		if(!compareDataBaseDataWithValue(testDbData, uType)){
			return true;
		}
		int dbFlags = getFlagsFromTapTable(tapTableInfo, tableColumnName);
		if (flags != dbFlags){
			return true;
		}
		int dbIndexed = getIndexedFromTapTable(tapTableInfo, tableColumnName);
		return dbIndexed != indexed;
	}
	
	/**
	 * Compares a database data with a value.<br/>
	 * If database data is null, returns 'true' if value is not null nor empty string.<br/>
	 * If database data is not null, returns the result of an 'equals' java comparison.<br/>
	 * @param dataBaseData
	 * @param value
	 * @return
	 */
	public static boolean compareDataBaseDataWithValue(String dataBaseData, String value) {
		if (dataBaseData == null) {
			return (value == null || "".equals(value));
		} else {
			return dataBaseData.equals(value);
		}
	}
	
	/**
	 * Returns 'true' if the specified column indexed field contains '1' or 'true'
	 * @param tapTableInfo
	 * @param tableColumnName
	 * @return 'true' if the specified column indexed field contains '1' or 'true'
	 */
	public static boolean isTrueFromTapTableIndexed(TapTableInfo tapTableInfo, String tableColumnName){
		return isTrueFromTapTableIndexed(tapTableInfo.getColumn(tableColumnName, TAP_COLUMNS_TABLE_INDEXED));
	}
	
	public static boolean isTrueFromTapTableIndexed(Object value){
		if(value == null){
			return false;
		}
		String s = value.toString();
		if("1".equals(s)){
			return true;
		}
		return Boolean.parseBoolean(s);
	}
	
	/**
	 * Returns the integer value of the specified column flags value. If the value is null, '0' is returned.
	 * @param tapTableInfo
	 * @param tableColumnName
	 * @return
	 */
	public static int getFlagsFromTapTable(TapTableInfo tapTableInfo, String tableColumnName){
		Object o = tapTableInfo.getColumn(tableColumnName, TAP_COLUMNS_TABLE_FLAGS);
		if(o == null){
			return 0;
		}
		try{
			return ((Integer)o);
		}catch(ClassCastException e){
			try{
				return Integer.parseInt(o.toString());
			}catch(NumberFormatException nfe){
				return 0;
			}
		}
	}
	
	/**
	 * Returns the integer value of the specified column flags value. If the value is null, '0' is returned.
	 * @param tapTableInfo
	 * @param tableColumnName
	 * @return
	 */
	public static int getIndexedFromTapTable(TapTableInfo tapTableInfo, String tableColumnName){
		Object o = tapTableInfo.getColumn(tableColumnName, TAP_COLUMNS_TABLE_INDEXED);
		if(o == null){
			return 0;
		}
		try{
			return ((Integer)o);
		}catch(ClassCastException e){
			try{
				return Integer.parseInt(o.toString());
			}catch(NumberFormatException nfe){
				return 0;
			}
		}
	}
	
	/**
	 * Returns 'true' if the specified column is indexed (1) and the 'flags' field contains 'ra' or 'dec' values.
	 * @param tapTableInfo
	 * @param tableColumnName
	 * @return
	 */
	public static boolean isRaDecIndexed(TapTableInfo tapTableInfo, String tableColumnName){
		boolean indexed = isTrueFromTapTableIndexed(tapTableInfo, tableColumnName);
		if(indexed){
			int flags = getFlagsFromTapTable(tapTableInfo, tableColumnName);
			return isRaOrDec(flags);
		} else {
			return false;
		}
	}
	
	/**
	 * Returns 'true' if the specified column is indexed (1) and the 'flags' field does not contain 'ra' nor 'dec' values. 
	 * @param tapTableInfo
	 * @param tableColumnName
	 * @return
	 */
	public static boolean isNormalIndexed(TapTableInfo tapTableInfo, String tableColumnName) {
		boolean indexed = isTrueFromTapTableIndexed(tapTableInfo, tableColumnName);
		if(indexed){
			int flags = getFlagsFromTapTable(tapTableInfo, tableColumnName);
			return !isRaOrDec(flags);
		} else {
			return false;
		}
	}
}
