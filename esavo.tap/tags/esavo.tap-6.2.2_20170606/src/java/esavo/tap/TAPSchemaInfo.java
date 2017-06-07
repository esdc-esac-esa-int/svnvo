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
package esavo.tap;

/**
 * This class returns the real tables or the views associated to TAP_SCHEMA tables.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TAPSchemaInfo {

	private String tapSchemaName;

	private String tapSchemasTableName;
	private String tapTablesTableName;
	private String tapColumnsTableName;
	private String tapKeysTableName;
	private String tapKeyColumnsTableName;
	private String tapFunctionsTableName;
	private String tapFunctionsArgumentsTableName;

	private String tapSchemasViewName;
	private String tapTablesViewName;
	private String tapColumnsViewName;
	private String tapKeysViewName;
	private String tapKeyColumnsViewName;
	private String tapFunctionsViewName;
	private String tapFunctionsArgumentsViewName;

	/**
	 * Assigns default values for schema, tables and views.<br/>
	 * <ul>
	 * <li>Schema name: <code>tap_schema</code></li>
	 * </ul>
	 * Tables<br/>
	 * <ul>
	 * <li>'schemas' table name: <code>schemas</code></li>
	 * <li>'tables' table name: <code>tables</code></li>
	 * <li>'columns' table name: <code>columns</code></li>
	 * <li>'keys' table name: <code>keys</code></li>
	 * <li>'key_column' table name: <code>key_column</code></li>
	 * </ul>
	 * Views<br/>
	 * <ul>
	 * <li>'schemas' view name: <code>schemas</code></li>
	 * <li>'tables' view name: <code>tables</code></li>
	 * <li>'columns' view name: <code>columns</code></li>
	 * <li>'keys' view name: <code>keys</code></li>
	 * <li>'key_column' view name: <code>key_column</code></li>
	 * </ul>
	 * 
	 */
	public TAPSchemaInfo() {
		// Schema name
		tapSchemaName = "tap_schema";
		
		// Tables
		tapSchemasTableName = "schemas";
		tapTablesTableName = "tables";
		tapColumnsTableName = "columns";
		tapKeysTableName = "keys";
		tapKeyColumnsTableName = "key_columns";
		tapFunctionsTableName = "functions";
		tapFunctionsArgumentsTableName = "functions_arguments";

		//Views
		tapSchemasViewName = tapSchemasTableName;
		tapTablesViewName = tapTablesTableName;
		tapColumnsViewName = tapColumnsTableName;
		tapKeysViewName = tapKeysTableName;
		tapKeyColumnsViewName = tapKeyColumnsTableName;
		tapFunctionsViewName = tapFunctionsTableName;
		tapFunctionsArgumentsViewName = tapFunctionsArgumentsTableName;
	}
	
	public TAPSchemaInfo(TAPSchemaInfo clone){
		// Schema name
		this.tapSchemaName = clone.tapSchemaName;
		
		// Tables
		this.tapSchemasTableName = clone.tapSchemasTableName;
		this.tapTablesTableName = clone.tapTablesTableName;
		this.tapColumnsTableName = clone.tapColumnsTableName;
		this.tapKeysTableName = clone.tapKeysTableName;
		this.tapKeyColumnsTableName = clone.tapKeyColumnsTableName;
		this.tapFunctionsTableName = clone.tapFunctionsTableName;
		this.tapFunctionsArgumentsTableName = clone.tapFunctionsArgumentsTableName;

		//Views
		this.tapSchemasViewName = clone.tapSchemasViewName;
		this.tapTablesViewName = clone.tapTablesViewName;
		this.tapColumnsViewName = clone.tapColumnsViewName;
		this.tapKeysViewName = clone.tapKeysViewName;
		this.tapKeyColumnsViewName = clone.tapKeyColumnsViewName;
		this.tapFunctionsViewName = clone.tapFunctionsViewName;
		this.tapFunctionsArgumentsViewName = clone.tapFunctionsArgumentsViewName;
	}

	/**
	 * @return the tapSchemaName
	 */
	public String getTapSchemaName() {
		return tapSchemaName;
	}

	/**
	 * @param tapSchemaName
	 *            the tapSchemaName to set
	 */
	public void setTapSchemaName(String tapSchemaName) {
		this.tapSchemaName = tapSchemaName;
	}

	/**
	 * @return the tapSchemasTableName
	 */
	public String getTapSchemasTableName() {
		return tapSchemasTableName;
	}

	/**
	 * @return tapSchemaName + '.' + schemasName (e.g. tap_schema.schemas)
	 */
	public String getFullQualifiedTapSchemasTable(){
		return tapSchemaName + "." + tapSchemasTableName;
	}

	/**
	 * @param tapSchemasTableName
	 *            the tapSchemasTableName to set
	 */
	public void setTapSchemasTableName(String tapSchemasTableName) {
		this.tapSchemasTableName = tapSchemasTableName;
	}

	/**
	 * @return the tapTablesTableName
	 */
	public String getTapTablesTableName() {
		return tapTablesTableName;
	}
	
	/**
	 * @return tapSchemaName + '.' + tableName (e.g. tap_schema.tables)
	 */
	public String getFullQualifiedTapTablesTable(){
		return tapSchemaName + "." + tapTablesTableName;
	}

	/**
	 * @param tapTablesTableName
	 *            the tapTablesTableName to set
	 */
	public void setTapTablesTableName(String tapTablesTableName) {
		this.tapTablesTableName = tapTablesTableName;
	}

	/**
	 * @return the tapColumnsTableName
	 */
	public String getTapColumnsTableName() {
		return tapColumnsTableName;
	}

	/**
	 * @return tapSchemaName + '.' + columnsName (e.g. tap_schema.columns)
	 */
	public String getFullQualifiedTapColumnsTable(){
		return tapSchemaName + "." + tapColumnsTableName;
	}


	/**
	 * @param tapColumnsTableName
	 *            the tapColumnsTableName to set
	 */
	public void setTapColumnsTableName(String tapColumnsTableName) {
		this.tapColumnsTableName = tapColumnsTableName;
	}

	/**
	 * @return the tapKeysTableName
	 */
	public String getTapKeysTableName() {
		return tapKeysTableName;
	}

	/**
	 * @return tapSchemaName + '.' + keysTable (e.g. tap_schema.keys)
	 */
	public String getFullQualifiedTapKeysTable(){
		return tapSchemaName + "." + tapKeysTableName;
	}


	/**
	 * @param tapKeysTableName
	 *            the tapKeysTableName to set
	 */
	public void setTapKeysTableName(String tapKeysTableName) {
		this.tapKeysTableName = tapKeysTableName;
	}

	/**
	 * @return the tapKeyColumnsTableName
	 */
	public String getTapKeyColumnsTableName() {
		return tapKeyColumnsTableName;
	}

	/**
	 * @return tapSchemaName + '.' + keyColumnsName (e.g. tap_schema.key_columns)
	 */
	public String getFullQualifiedTapKeyColumnsTable(){
		return tapSchemaName + "." + tapKeyColumnsTableName;
	}


	/**
	 * @param tapKeyColumnsTableName
	 *            the tapKeyColumnsTableName to set
	 */
	public void setTapKeyColumnsTableName(String tapKeyColumnsTableName) {
		this.tapKeyColumnsTableName = tapKeyColumnsTableName;
	}

	/**
	 * @return the tapSchemasViewName
	 */
	public String getTapSchemasViewName() {
		return tapSchemasViewName;
	}

	/**
	 * @param tapSchemasViewName
	 *            the tapSchemasViewName to set
	 */
	public void setTapSchemasViewName(String tapSchemasViewName) {
		this.tapSchemasViewName = tapSchemasViewName;
	}

	/**
	 * @return the tapTablesViewName
	 */
	public String getTapTablesViewName() {
		return tapTablesViewName;
	}

	/**
	 * @param tapTablesViewName
	 *            the tapTablesViewName to set
	 */
	public void setTapTablesViewName(String tapTablesViewName) {
		this.tapTablesViewName = tapTablesViewName;
	}

	/**
	 * @return the tapColumnsViewName
	 */
	public String getTapColumnsViewName() {
		return tapColumnsViewName;
	}

	/**
	 * @param tapColumnsViewName
	 *            the tapColumnsViewName to set
	 */
	public void setTapColumnsViewName(String tapColumnsViewName) {
		this.tapColumnsViewName = tapColumnsViewName;
	}

	/**
	 * @return the tapKeysViewName
	 */
	public String getTapKeysViewName() {
		return tapKeysViewName;
	}

	/**
	 * @param tapKeysViewName
	 *            the tapKeysViewName to set
	 */
	public void setTapKeysViewName(String tapKeysViewName) {
		this.tapKeysViewName = tapKeysViewName;
	}

	/**
	 * @return the tapKeyColumnsViewName
	 */
	public String getTapKeyColumnsViewName() {
		return tapKeyColumnsViewName;
	}

	/**
	 * @param tapKeyColumnsViewName
	 *            the tapKeyColumnsViewName to set
	 */
	public void setTapKeyColumnsViewName(String tapKeyColumnsViewName) {
		this.tapKeyColumnsViewName = tapKeyColumnsViewName;
	}

	/**
	 * @return the tapFunctionsTableName
	 */
	public String getTapFunctionsTableName() {
		return tapFunctionsTableName;
	}

	/**
	 * @return tapSchemaName + '.' + fuctionsName (e.g. tap_schema.functions)
	 */
	public String getFullQualifiedTapFunctionsTable(){
		return tapSchemaName + "." + tapFunctionsTableName;
	}


	/**
	 * @param tapFunctionsTableName the tapFunctionsTableName to set
	 */
	public void setTapFunctionsTableName(String tapFunctionsTableName) {
		this.tapFunctionsTableName = tapFunctionsTableName;
	}

	/**
	 * @return the tapFunctionsArgumentsTableName
	 */
	public String getTapFunctionsArgumentsTableName() {
		return tapFunctionsArgumentsTableName;
	}
	
	/**
	 * @return tapSchemaName + '.' + functionsArgumentsName (e.g. tap_schema.functions_arguments)
	 */
	public String getFullQualifiedTapFunctionsArgumentsTable(){
		return tapSchemaName + "." + tapFunctionsArgumentsTableName;
	}



	/**
	 * @param tapFunctionsArgumentsTableName the tapFunctionsArgumentsTableName to set
	 */
	public void setTapFunctionsArgumentsTableName(
			String tapFunctionsArgumentsTableName) {
		this.tapFunctionsArgumentsTableName = tapFunctionsArgumentsTableName;
	}

	/**
	 * @return the tapFunctionsViewName
	 */
	public String getTapFunctionsViewName() {
		return tapFunctionsViewName;
	}

	/**
	 * @param tapFunctionsViewName the tapFunctionsViewName to set
	 */
	public void setTapFunctionsViewName(String tapFunctionsViewName) {
		this.tapFunctionsViewName = tapFunctionsViewName;
	}

	/**
	 * @return the tapFunctionsArgumentsViewName
	 */
	public String getTapFunctionsArgumentsViewName() {
		return tapFunctionsArgumentsViewName;
	}

	/**
	 * @param tapFunctionsArgumentsViewName the tapFunctionsArgumentsViewName to set
	 */
	public void setTapFunctionsArgumentsViewName(
			String tapFunctionsArgumentsViewName) {
		this.tapFunctionsArgumentsViewName = tapFunctionsArgumentsViewName;
	}

}
