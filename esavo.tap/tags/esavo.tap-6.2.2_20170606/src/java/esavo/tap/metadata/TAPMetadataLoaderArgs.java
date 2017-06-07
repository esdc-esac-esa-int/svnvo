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
package esavo.tap.metadata;

import java.util.List;

import esavo.tap.metadata.TapPublish.TapPublishCommand;


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
	private List<Boolean> isPublic;
	private TapPublishCommand tapPublishCommand;
	private String tapSchemaName;
	private boolean calculateArrayDims;
	private String fullQualifiedFunctionName;
	
	
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

	/**
	 * @return the isPublic
	 */
	public List<Boolean> getIsPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setIsPublic(List<Boolean> isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the tapPublishCommand
	 */
	public TapPublishCommand getTapPublishCommand() {
		return tapPublishCommand;
	}

	/**
	 * @param tapPublishCommand the tapPublishCommand to set
	 */
	public void setTapPublishCommand(TapPublishCommand tapPublishCommand) {
		this.tapPublishCommand = tapPublishCommand;
	}

	/**
	 * @return 'true' if a tapPublisCommand is available
	 */
	public boolean hasTapPublishCommand(){
		return this.tapPublishCommand != null;
	}

	/**
	 * @return the tapSchema
	 */
	public String getTapSchemaName() {
		return tapSchemaName;
	}

	/**
	 * @param tapSchema the tapSchema to set
	 */
	public void setTapSchemaName(String tapSchemaName) {
		this.tapSchemaName = tapSchemaName;
	}

	/**
	 * @return the calculateArrayDims
	 */
	public boolean isCalculateArrayDims() {
		return calculateArrayDims;
	}

	/**
	 * @param calculateArrayDims the calculateArrayDims to set
	 */
	public void setCalculateArrayDims(boolean calculateArrayDims) {
		this.calculateArrayDims = calculateArrayDims;
	}

	/**
	 * @return the fullQualifiedFunctionName
	 */
	public String getFullQualifiedFunctionName() {
		return fullQualifiedFunctionName;
	}

	/**
	 * @param fullQualifiedFunctionName the fullQualifiedFunctionName to set
	 */
	public void setFullQualifiedFunctionName(String fullQualifiedFunctionName) {
		this.fullQualifiedFunctionName = fullQualifiedFunctionName;
	}

}
