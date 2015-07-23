package esavo.tap;

import java.util.List;

import esavo.tap.metadata.TAPMetadataLoader;

/**
 * Utilities
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Utils {
	
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
	
	/**
	 * Returns 'true' if the schema name is 'public' or 'tap_schema' (case insensitive)
	 * @param schemaName
	 * @return
	 */
	public static boolean isSchemaPublic(String schemaName){
		return TAPMetadataLoader.PUBLIC_SCHEMA.equalsIgnoreCase(schemaName) || 
				TAPMetadataLoader.TAP_SCHEMA.equalsIgnoreCase(schemaName);
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


}
