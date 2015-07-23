package esavo.tap.metadata;

import java.util.List;

import esavo.tap.Utils;


/**
 * This class provides the required arguments to be used by {@link TAPMetadataLoader}
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TAPMetadataLoaderArgs {
	
	private boolean includeShareInfo;
	private boolean includeAccessibleSharedItems;
	private boolean onlySchemas;
	private boolean onlyTables;
	private List<String> schemaNames;
	private List<String> fullQualifiedTableNames;
	
	
	/**
	 * Constructor
	 */
	public TAPMetadataLoaderArgs() {

	}	
	
	/**
	 * @return the includeShareInfo
	 */
	public boolean isIncludeShareInfo() {
		return includeShareInfo;
	}
	
	/**
	 * @param includeShareInfo the includeShareInfo to set
	 */
	public void setIncludeShareInfo(boolean includeShareInfo) {
		this.includeShareInfo = includeShareInfo;
	}
	
	/**
	 * @return the includeAccessibleSharedItems
	 */
	public boolean isIncludeAccessibleSharedItems() {
		return includeAccessibleSharedItems;
	}
	
	/**
	 * @param includeAccessibleSharedItems the includeAccessibleSharedItems to set
	 */
	public void setIncludeAccessibleSharedItems(boolean includeAccessibleSharedItems) {
		this.includeAccessibleSharedItems = includeAccessibleSharedItems;
	}
	
	/**
	 * @return the onlySchemas
	 */
	public boolean isOnlySchemas() {
		return onlySchemas;
	}
	
	/**
	 * @param onlySchemas the onlySchemas to set
	 */
	public void setOnlySchemas(boolean onlySchemas) {
		this.onlySchemas = onlySchemas;
	}
	
	/**
	 * @return the onlyTables
	 */
	public boolean isOnlyTables() {
		return onlyTables;
	}
	
	/**
	 * @param onlyTables the onlyTables to set
	 */
	public void setOnlyTables(boolean onlyTables) {
		this.onlyTables = onlyTables;
	}
	
	/**
	 * @return the schemaName
	 */
	public List<String> getSchemaNames() {
		return schemaNames;
	}
	
	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaNames(List<String> schemaNames) {
		this.schemaNames = schemaNames;
	}
	
	/**
	 * @return the full qualified table name (schema+'.'+table)
	 */
	public List<String> getFullQualifiedTableNames(){
		return fullQualifiedTableNames;
	}
	
	/**
	 * Sets the full qualified table name. This method does not check {@link #getSchemaName()}
	 * can be used.
	 * @param fullQualifiedTableName the full qualified table name.
	 */
	public void setFullQualifiedTableNames(List<String> fullQualifiedTableNames){
		this.fullQualifiedTableNames = fullQualifiedTableNames;
	}
	
	/**
	 * Returns 'true' if a full qualified table name is available.
	 * @return
	 */
	public boolean hasFullQualifiedTableNames(){
		return fullQualifiedTableNames != null && fullQualifiedTableNames.size() > 0;
	}
	
	/**
	 * Returns 'true' if a schema name is available.
	 * @return
	 */
	public boolean hasSchemaNames(){
		return schemaNames != null && schemaNames.size() > 0;
	}
	
//	/**
//	 * Return 'true' if all schemas are public
//	 * @return
//	 */
//	public boolean areSchemasPublic(){
//		if(schemaNames == null){
//			return false;
//		}
//		for(String sch: schemaNames){
//			if (!Utils.isSchemaPublic(sch)){
//				return false;
//			}
//		}
//		return true;
//	}
	

}
