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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.TapUtils;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.metadata.TAPFunctionArgument.DataType;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.utils.UwsUtils;

public class TAPMetadataLoader {
	
	/**
	 * Flag to include share information.
	 */
	public static final boolean INCLUDE_SHARE_INFO = true;
	
	/**
	 * Flag to not include share information.
	 */
	public static final boolean DO_NOT_INCLUDE_SHARE_INFO = false;
	
	
	/**
	 * Flag to include shared items the user can access to.
	 */
	public static final boolean INCLUDE_ACCESSIBLE_SHARED_ITEMS = true;
	
	/**
	 * Flag to not include shared items the user can access to.
	 */
	public static final boolean DO_NOT_INCLUDE_ACCESSIBLE_SHARED_ITEMS = false;
	
	/**
	 * Flag to use real tables (that contain all user tables).<br/>
	 * To obtain public tables only, use {@link #USE_VIEWS}.
	 */
	public static final boolean USE_REAL_TABLES = true;
	
	/**
	 * Flag to use views (that contain public tables only).<br/>
	 * To obtain all tables (public+private), use {@link #USE_REAL_TABLES}.
	 */
	public static final boolean USE_VIEWS = false;
	
	/**
	 * 'Table' shared resource type.
	 */
	public static final int SHARED_RESOURCE_TYPE_TABLE = 0;
	
	
	public static final String PUBLIC_SCHEMA = "public";
	public static final String TAP_SCHEMA = "tap_schema";
	public static final String TAP_UPLOAD = "TAP_UPLOAD";
	
	
	/**
	 * Creates a metadata object associated to a user from the DB TAP schema. It contains both general 
	 * tables (catalogs) and user tables.
	 * <pre><tt>
	 *     2. only_schemas = TRUE
	 *         any other flags are ignored. Except share_info / share_accessible
	 * 
	 *     3. only_schemas = FALSE (default)
	 * 
	 *         3.1. if schema_names != null, the following (2.2, 2.3) applies only to the specified schemas
	 *         schema_names can be a list of names separated by ','
	 * 
	 *         3.2. only_tables = TRUE
	 *         any other flag is ignored. Except share_info / share_accessible
	 * 
	 *         3.3. only_tables = FALSE (default)
	 * </tt></pre>
	 * @param request
	 * @param response
	 * @param includeShareInfo 'true' to include share information.
	 * @param includeAccessibleSahredItems 'true' to include shared items the user can access to.
	 * @return
	 * @throws Exception
	 */
	public static TAPMetadata getTAPMetadata(TAPService service, UwsJobOwner owner, TAPMetadataLoaderArgs args) throws Exception{
		TAPMetadata tapMetadata=null;
		DBConnection dbConn = null;
		TAPFactory factory = service.getFactory();
		try {
			dbConn = factory.createDBConnection("TAP(ServiceConnection)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(owner);
			
			// List all public schemas, tables and columns available:
			tapMetadata = new TAPMetadata(service);

			TAPSchemaLoaderArgs schemaLoaderArgs = new TAPSchemaLoaderArgs(dbConn, tapSchemaInfo, owner, args);

			//PUBLIC SCHEMAS section
			if(!args.hasSchemaNames()){
				//if no schema is specified: load public schemas always
				//tapMetadata.addSchema(loadSchema(PUBLIC_SCHEMA, schemaLoaderArgs, USE_VIEWS));
				//tapMetadata.addSchema(loadSchema(TAP_SCHEMA, schemaLoaderArgs, USE_VIEWS));
				List<String> publicSchemas = dbConn.getPublicSchemas(tapSchemaInfo);
				if(publicSchemas != null){
					for(String sch: publicSchemas){
						tapMetadata.addSchema(loadSchema(sch, schemaLoaderArgs, USE_VIEWS));
					}
				}
			} else {
				//schema names are specified. Public schemas must be loaded using views.
				List<String> specifiedSchemas = args.getSchemaNames();
				for(String sch: specifiedSchemas){
					if(TapUtils.isSchemaPublic(tapSchemaInfo, sch, dbConn)){
						tapMetadata.addSchema(loadSchema(sch, schemaLoaderArgs, USE_VIEWS));
					}
				}
			}

			//USER SCHEMA section
			List<String> availableSchemas = service.getAvailableSchemas(owner);
			loadAvailableSchemas(tapMetadata, availableSchemas, schemaLoaderArgs);
			
			if(schemaLoaderArgs.areAccessibleSharedItemsRequired()){
				includeAccessibleSharedItems(dbConn, tapSchemaInfo, owner.getId(), tapMetadata, args.isOnlyTables());
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			throw new TAPException(e, "Error while creating the accessible schema for user: " + owner.getId());
			
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
		return tapMetadata;
	}
	
	private static void loadAvailableSchemas(TAPMetadata tapMetadata, List<String> availableSchemas, TAPSchemaLoaderArgs schemaLoaderArgs) throws Exception {
		if(availableSchemas == null){
			return;
		}else{
			TAPMetadataLoaderArgs args = schemaLoaderArgs.getMetadataArgs(); 
			if(args.hasSchemaNames()){
				//user specify schemas: load the specified ones
				List<String> specifiedSchemas = args.getSchemaNames();
				for(String schemaName: availableSchemas){
					if(specifiedSchemas.contains(schemaName)){
						loadAndAddSchema(schemaName, schemaLoaderArgs, tapMetadata);
					}
				}
			}else{
				//no schemas specified: load user schemas
				for(String schemaName: availableSchemas){
					loadAndAddSchema(schemaName, schemaLoaderArgs, tapMetadata);
				}
			}
		}
	}
	
	private static void loadAndAddSchema(String schemaName, TAPSchemaLoaderArgs schemaLoaderArgs, TAPMetadata tapMetadata) throws Exception{
		String ownerid = schemaLoaderArgs.getOwnerId();
		boolean includeShareInfo = schemaLoaderArgs.getMetadataArgs().isIncludeShareInfo();
		TAPSchema schema = loadSchema(schemaName, schemaLoaderArgs);
		tapMetadata.addSchema(schema);
		if(includeShareInfo){
			includeShareInfo(schema, ownerid);
		}
	}
	
	public static TAPMetadata getSingleTable(TAPService service, UwsJobOwner owner, TAPMetadataLoaderArgs args) throws Exception {
		TAPMetadata tapMetadata=null;
		DBConnection dbConn = null;
		TAPFactory factory = service.getFactory();
		try {
			dbConn = factory.createDBConnection("TAP(ServiceConnection)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(owner);
			
			TAPSchemaLoaderArgs schemaLoaderArgs = new TAPSchemaLoaderArgs(dbConn, tapSchemaInfo, owner, args);
			List<String> fullQualifiedTableNames = args.getFullQualifiedTableNames();
			
			// List all schemas, tables and columns available:
			tapMetadata = new TAPMetadata(service);
			
			String schemaName;
			//String userSchemaName = null;
			List<String> availableSchemas = null;
			List<String> accessibleSchemas = null;
			if(schemaLoaderArgs.isAuthenticatedUser()){
				//userSchemaName = TAPMetadata.getUserSchema(owner);
				availableSchemas = service.getAvailableSchemas(owner);
				accessibleSchemas = getAccessibleSchemas(owner.getId());
			}
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
				if(TapUtils.isSchemaPublic(tapSchemaInfo, schemaName, dbConn)){
					loadSingleTable(tapMetadata, schemaLoaderArgs, USE_VIEWS, fullQualifiedTableName);
				}else{
					//Schema is not public: only schemas that belong to the user can be loaded
					if(!TapUtils.checkValidSchema(availableSchemas, fullQualifiedTableName) && 
							!TapUtils.checkValidSchema(accessibleSchemas, fullQualifiedTableName)){
						throw new Exception("Invalid access to table '"+fullQualifiedTableName+"' from user: " + owner.getId());
					}else{
						loadSingleTable(tapMetadata, schemaLoaderArgs, USE_REAL_TABLES, fullQualifiedTableName);
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			throw new TAPException(e, "Error while obtaining table metadata. ");
			
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
		return tapMetadata;
	}
	
	private static void loadSingleTable(TAPMetadata tapMetadata, TAPSchemaLoaderArgs schemaLoaderArgs, boolean useRealTables, String fullQualifiedTableName) throws Exception {
		DBConnection dbConn = schemaLoaderArgs.getDbConn();
		TAPSchemaInfo tapSchemaInfo = schemaLoaderArgs.getTapSchemaInfo();
		String schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
		TAPSchema schema = tapMetadata.getSchema(schemaName);
		if(schema == null){
			//schema = new TAPSchema(schemaName);
			schema = dbConn.loadSchema(tapSchemaInfo, schemaName);
			tapMetadata.addSchema(schema);
		}
		String tapSchemaName = tapSchemaInfo.getTapSchemaName();
		TAPValidTables tapValidTables = getValidTables(tapSchemaInfo, useRealTables);
		TAPMetadataLoaderArgs args = schemaLoaderArgs.getMetadataArgs();
		
		String userid = schemaLoaderArgs.getOwnerId();
		//if(!TapUtils.isSchemaPublic(schemaName, dbConn)){
		if(!schema.isPublic()){
			if(!hasAccessTo(dbConn, userid, fullQualifiedTableName, args.isIncludeAccessibleSharedItems())){
				throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Table '"+fullQualifiedTableName+"' not found.");
			}
		}

		TAPTable tapTable = getSingleTableFromSchema(dbConn, schema, tapSchemaName, tapValidTables.getTapTables(), fullQualifiedTableName);
		if(tapTable == null){
			throw new UwsException(UwsOutputResponseHandler.NOT_FOUND, "Table '"+fullQualifiedTableName+"' not found.");
		}
		populateTableColumns(dbConn, tapTable, tapSchemaName, tapValidTables.getTapColumns(), schema.getADQLName());
		schema.addTable(tapTable);

		if(args.isIncludeShareInfo()){
			//search for share info associated to the user schema (public tables are already shared).
			includeShareInfo(schema, schemaLoaderArgs.getOwnerId());
		}

	}
	
	private static TAPSchema loadSchema(String schemaName, TAPSchemaLoaderArgs schemaLoaderArgs) throws Exception{
		return loadSchema(schemaName, schemaLoaderArgs, schemaLoaderArgs.isAuthenticatedUser());
	}

	private static TAPSchema loadSchema(String schemaName, TAPSchemaLoaderArgs schemaLoaderArgs, boolean useRealTables) throws Exception{
		DBConnection dbConn = schemaLoaderArgs.getDbConn();
		TAPSchemaInfo tapSchemaInfo = schemaLoaderArgs.getTapSchemaInfo();
		//TAPSchema schema = new TAPSchema(schemaName);
		TAPSchema schema = dbConn.loadSchema(tapSchemaInfo, schemaName);
		TAPMetadataLoaderArgs args = schemaLoaderArgs.getMetadataArgs();
		if(args.isOnlySchemas()){
			return schema;
		}
		String tapSchemaName = tapSchemaInfo.getTapSchemaName();
		TAPValidTables tapValidTables = getValidTables(tapSchemaInfo, useRealTables);
		boolean onlyTables = args.isOnlyTables();

		loadTables(dbConn, schema, tapSchemaName, tapValidTables.getTapTables(), tapValidTables.getTapColumns(), onlyTables);
		loadFunctions(dbConn, schema, tapSchemaName, tapValidTables.getTapFunctions(), tapValidTables.getTapFunctionsArgs());

		return schema;
	}

	
	private static void loadTables(DBConnection dbConn, TAPSchema schema, String tapSchemaName, String tablesTable, String columnsTable, boolean onlyTables) throws Exception {
		//Get all the tables from the TAP_SCHEMA
		List<TAPTable> tables = getTablesFromSchema(dbConn, schema, tapSchemaName, tablesTable);

		//Get all the columns for every table
		for(TAPTable tapTable: tables){
			if(!onlyTables){
				populateTableColumns(dbConn, tapTable, tapSchemaName, columnsTable, schema.getADQLName());
			}
			schema.addTable(tapTable);
		}
	}
	
	private static TAPTable getSingleTableFromSchema(DBConnection dbConn, TAPSchema schema, String schemaName, String tablesTable, String fullQualifiedTableName) throws Exception {
		String tableName = TapUtils.getTableNameOnly(fullQualifiedTableName).toLowerCase();
		String sch = schemaName.toLowerCase();
		String tbl = tablesTable.toLowerCase();
		String schDb = schema.getADQLName().toLowerCase();
		String query = "SELECT table_name,description,size,flags,public,db_table_name,hierarchy FROM "+sch+"."+tbl+" " +
				" WHERE schema_name='"+schDb+"' AND table_name = '"+tableName+"'";
		
		List<TAPTable> tables = getTablesFromSchema(dbConn, query);
		
		if (tables != null && tables.size() > 0) {
			return tables.get(0);
		} else {
			return null;
		}
	}
	
	private static List<TAPTable> getTablesFromSchema(DBConnection dbConn, TAPSchema schema, String tapSchemaName, String tablesTable) throws Exception {
		String tapSchema = tapSchemaName.toLowerCase();
		String tables = tablesTable.toLowerCase();
		String query = "SELECT table_name,description,size,flags,public,db_table_name,hierarchy FROM "+tapSchema+"."+tables+" " +
				" WHERE schema_name='"+schema.getADQLName()+"'";
		return getTablesFromSchema(dbConn, query);
	}
	
	private static List<TAPTable> getTablesFromSchema(DBConnection dbConn, String query) throws Exception {
		ResultSet tableResultSet = (ResultSet) dbConn.executeQuery(query, null);

		//Get all the tables from the TAP_SCHEMA
		List<TAPTable> tables = new ArrayList<TAPTable>();

		while (tableResultSet.next()) {
			String tableName = tableResultSet.getString("table_name");
			String tableDesc = tableResultSet.getString("description");
			String tableHierarchy = tableResultSet.getString("hierarchy");

			Long tableSize = tableResultSet.getLong("size");
			if (tableResultSet.wasNull()) {
				tableSize = null;
			}
			
			Integer tableFlags = tableResultSet.getInt("flags");
			Boolean tableIsPublic = tableResultSet.getBoolean("public");
			String dbTableName = tableResultSet.getString("db_table_name");

			TAPTable tapTable = new TAPTable(tableName, tableIsPublic);
			tapTable.setDescription(tableDesc);
			tapTable.setHierarchy(tableHierarchy);
			tapTable.setSize(tableSize);
			tapTable.setFlags(tableFlags);
			if(dbTableName != null && !dbTableName.isEmpty()){
				tapTable.setDBName(dbTableName);
			}
			tables.add(tapTable);
		}
		
		return tables;

	}
	
	
	private static void populateTableColumns(DBConnection dbConn, TAPTable tapTable, String tapSchemaName, String columnsTable, String schemaName) throws Exception {
		String tapSchema = tapSchemaName.toLowerCase();
		String columns = columnsTable.toLowerCase();
		String schema = schemaName.toLowerCase();
		String query = "SELECT c.column_name,c.description,c.ucd," + 
				"c.utype,c.datatype,c.unit,c.std,c.indexed,c.principal,c.flags,c.db_column_name," + 
				"c.array_type,c.array_dims " +
				"FROM "+tapSchema+"."+columns+" c " +
				"where c.schema_name= '" +schema+"' "+
				"AND c.table_name='"+tapTable.getADQLName() + "' " +
				"ORDER BY c.pos, column_name ";
		
		ResultSet columnResultSet = (ResultSet) dbConn.executeQuery(query, null);
		
		while (columnResultSet.next()) {
			String columnName = columnResultSet.getString("column_name");
			String description = columnResultSet.getString("description");
			String ucd = columnResultSet.getString("ucd");
			String utype = columnResultSet.getString("utype");
			String type = columnResultSet.getString("datatype");
			String unit = columnResultSet.getString("unit");
			boolean std = (columnResultSet.getInt("std") == 1) ? true : false;
			boolean indexed = (columnResultSet.getInt("indexed") == 1) ? true : false;
			boolean principal = (columnResultSet.getInt("principal") == 1) ? true : false;
			int flags = columnResultSet.getInt("flags");
			String dbColumnName = columnResultSet.getString("db_column_name");
			String arrayType = columnResultSet.getString("array_type");
			String arrayDims = columnResultSet.getString("array_dims");

			TAPColumn tapColumn = new TAPColumn(columnName, description, unit, ucd, utype);
			tapColumn.setDatatype(type, TAPTypes.NO_SIZE);
			if (std) {
				tapColumn.setStd(true);
			}
			if (indexed) {
				tapColumn.setIndexed(true);
			}
			if (principal) {
				tapColumn.setPrincipal(true);
			}
			tapColumn.setFlags(flags);
			tapTable.addColumn(tapColumn);
			if(dbColumnName != null && !dbColumnName.isEmpty()){
				tapColumn.setDBName(dbColumnName);
			}
			tapColumn.setArrayType(arrayType);
			tapColumn.setArrayDims(arrayDims);
		}
	}
	
	public static TAPFunction getTAPSingleFunction(TAPService service, UwsJobOwner owner, TAPMetadataLoaderArgs args) throws Exception{
		DBConnection dbConn = null;
		TAPFactory factory = service.getFactory();
		try {
			dbConn = factory.createDBConnection("TAP(ServiceConnection)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(owner);
			TAPSchemaLoaderArgs schemaLoaderArgs = new TAPSchemaLoaderArgs(dbConn, tapSchemaInfo, owner, args);
			String fullQualifiedFunctionName = args.getFullQualifiedFunctionName();
			String functionSchemaName = TapUtils.getSchemaNameOnly(fullQualifiedFunctionName);
			String functionName = TapUtils.getTableNameOnly(fullQualifiedFunctionName);
			return getSingleFunction(schemaLoaderArgs, functionSchemaName, functionName);
			
		} catch (Exception e) {
			//e.printStackTrace();
			throw new TAPException(e, "Error while creating the accessible schema for user: " + owner.getId());
			
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}

	}
	
	private static TAPFunction getSingleFunction(TAPSchemaLoaderArgs schemaLoaderArgs, String functionSchemaName, String functionName) throws Exception {
		DBConnection dbConn = schemaLoaderArgs.getDbConn();
		TAPSchemaInfo tapSchemaInfo = schemaLoaderArgs.getTapSchemaInfo();

		boolean useRealTables = schemaLoaderArgs.isAuthenticatedUser();
		TAPValidTables tapValidTables = getValidTables(tapSchemaInfo, useRealTables);
		
		String funcs = tapValidTables.getTapFunctions();
		String tapSchema = tapSchemaInfo.getTapSchemaName();

		String query = "SELECT function_name,description,return_type, array_dims, array_type FROM "+tapSchema+"."+funcs+" " +
				" WHERE schema_name='"+functionSchemaName+"' and function_name='"+functionName+"'"; 
		
		ResultSet rs = (ResultSet) dbConn.executeQuery(query, null);

		//Get all the functions from the TAP_SCHEMA
		String rsDescription;
		String rsArgDims;
		String rsArgDimType;
		DataType rsReturnType;
		if(rs.next()) {
			rsDescription = rs.getString("description");
			rsReturnType = DataType.valueOf(rs.getString("return_type"));
			rsArgDims = rs.getString("array_dims");
			rsArgDimType = rs.getString("array_type");
			TAPFunction f = new TAPFunction(functionName, rsDescription, rsReturnType, rsArgDims, rsArgDimType);
			return f;
		}else{
			throw new TAPException("Function '"+functionSchemaName+"."+functionName+"' not found.");
		}

	}
	
	private static void loadFunctions(DBConnection dbConn, TAPSchema schema, String tapSchemaName, String functionsTable, String functionsArgsTable) throws Exception {
		//Get all functions from the TAP_SCHEMA
		List<TAPFunction> functions = getFunctionsFromSchema(dbConn, schema, tapSchemaName, functionsTable);
		//Get all the arguments for every function
		for(TAPFunction f: functions) {
			populateFunction(dbConn, f, tapSchemaName, functionsArgsTable, schema.getADQLName());
			schema.addFunction(f);
		}
	}
	
	private static List<TAPFunction> getFunctionsFromSchema(DBConnection dbConn, TAPSchema schema, String tapSchemaName, String functionsTable) throws Exception {
		String tapSchema = tapSchemaName.toLowerCase();
		String funcs = functionsTable.toLowerCase();
		String sch = schema.getADQLName().toLowerCase();
		String query = "SELECT function_name,description,return_type,array_dims,array_type FROM "+tapSchema+"."+funcs+" " +
				" WHERE schema_name='"+sch+"'"; 
		
		ResultSet rs = (ResultSet) dbConn.executeQuery(query, null);

		//Get all the functions from the TAP_SCHEMA
		List<TAPFunction> functions = new ArrayList<TAPFunction>();

		String functionName;
		String description;
		String argDims;
		String argDimType;
		DataType returnType;
		while(rs.next()) {
			functionName = rs.getString("function_name");
			description = rs.getString("description");
			returnType = DataType.valueOf(rs.getString("return_type"));
			argDims = rs.getString("array_dims");
			argDimType = rs.getString("array_type");
			TAPFunction f = new TAPFunction(functionName, description, returnType, argDims, argDimType);
			functions.add(f);
		}
		
		return functions;
	}
	
	
	private static void populateFunction(DBConnection dbConn, TAPFunction function, String tapSchemaName, String functionsArgsTable, String schemaName) throws Exception {
		String tapSchema = tapSchemaName.toLowerCase();
		String schema = schemaName.toLowerCase();
		String query = "SELECT argument_name, description, arg_type," + 
				" default_value, max_value, min_value "	+ 
				" FROM "+tapSchema+"."+functionsArgsTable +
				" where schema_name='"+schema+"'" +
				" AND function_name='"+function.getADQLName() + "' ORDER BY argument_name ASC";
		
		ResultSet rsArgs = (ResultSet) dbConn.executeQuery(query, null);
		
		String argName;
		String argDesc;
		DataType argType;
		String s;
		while(rsArgs.next()) {
			argName = rsArgs.getString("argument_name");
			argDesc = rsArgs.getString("description");
			argType = DataType.valueOf(rsArgs.getString("arg_type"));
			
			TAPFunctionArgument arg = new TAPFunctionArgument(argName, argDesc, argType);
			
			s = rsArgs.getString("default_value");
			if(s == null){
				arg.setDefaultValue(null);
			}else{
				arg.setDefaultValue(TAPFunctionsUtils.getValueFromDB(argType, s));
			}
			
			s = rsArgs.getString("max_value");
			if(s == null){
				arg.setMaxValue(null);
			}else{
				arg.setMaxValue(TAPFunctionsUtils.getValueFromDB(argType, s));
			}
			
			s = rsArgs.getString("min_value");
			if(s == null){
				arg.setMinValue(null);
			}else{
				arg.setMinValue(TAPFunctionsUtils.getValueFromDB(argType, s));
			}
			
			function.addArgument(arg);
		}
	}
	
	
	private static void includeShareInfo(TAPSchema userSchema, String userid) throws UwsException {
		UwsShareManager shareManager = UwsManager.getInstance().getFactory().getShareManager();
		String resourceId;
		for(TAPTable table: userSchema.getTables()){
			resourceId = userSchema.getADQLName()+'.'+table.getADQLName();
			//List<UwsShareItem> shareInfo = shareManager.getUserSharedItemsInfo(userid, resourceId, SHARED_RESOURCE_TYPE_TABLE);
			List<UwsShareItem> shareInfo = shareManager.getUserSharedItem(userid, resourceId, SHARED_RESOURCE_TYPE_TABLE);
			table.setShareInfo(shareInfo);
		}
	}

	/**
	 * Returns a set of tables depending on useRealTables parameter.<br/>
	 * For authenticated users (useRealTables = true), 'all_[table]' are returned (to be able to access to private tables).
	 * If the user is anonymous (useRealTables = false), views are returned (access to public data only).
	 * @param tapSchemaInfo
	 * @param useRealTables 'true' to access ot real tables (that contains private tables). 'false' to access to views (public data only)
	 * @return
	 */
	private static TAPValidTables getValidTables(TAPSchemaInfo tapSchemaInfo, boolean useRealTables){
		TAPValidTables tapValidTables = new TAPValidTables();
		if(useRealTables){
			//authenticated user: use real tables (contain all the tables)
			tapValidTables.setTapTables(tapSchemaInfo.getTapTablesTableName());
			tapValidTables.setTapColumns(tapSchemaInfo.getTapColumnsTableName());
			tapValidTables.setTapFunctions(tapSchemaInfo.getTapFunctionsTableName());
			tapValidTables.setTapFunctionsArgs(tapSchemaInfo.getTapFunctionsArgumentsTableName());
		} else {
			//use views: non authenticated user (show public tables only)
			tapValidTables.setTapTables(tapSchemaInfo.getTapTablesViewName());
			tapValidTables.setTapColumns(tapSchemaInfo.getTapColumnsViewName());
			tapValidTables.setTapFunctions(tapSchemaInfo.getTapFunctionsViewName());
			tapValidTables.setTapFunctionsArgs(tapSchemaInfo.getTapFunctionsArgumentsViewName());
		}
		return tapValidTables;
	}
	
	/**
	 * Obtains the schema/tables the user can access to (shared by other users)
	 * @param dbConn
	 * @param tapSchemaInfo
	 * @param userid
	 * @param tapMetadata
	 * @throws Exception
	 */
	private static void includeAccessibleSharedItems(DBConnection dbConn, TAPSchemaInfo tapSchemaInfo, String userid, TAPMetadata tapMetadata, boolean onlyTables) throws Exception {
		Map<String, List<UwsShareItemBase>> schemasByItems = getAccessibleItems(userid);

		//Get tables info:
		String tapSchemaName = tapSchemaInfo.getTapSchemaName();
		TAPValidTables tapValidTables = getValidTables(tapSchemaInfo, USE_REAL_TABLES);

		String fullQualifiedTableName;
		String schemaName;
		List<UwsShareItemBase> items;
		for(Entry<String, List<UwsShareItemBase>> e: schemasByItems.entrySet()){
			schemaName = e.getKey();
			
			//Create a schema for the shared resources (the user can access to) associated to the user that export the resources
			//TAPSchema schema = new TAPSchema(schemaName);
			TAPSchema schema = tapMetadata.getSchema(schemaName);
			if(schema == null){
				schema = dbConn.loadSchema(tapSchemaInfo, schemaName);
			}
			items = e.getValue();
			
			//Add tables to the schema
			for(UwsShareItemBase item: items){
				fullQualifiedTableName = item.getTitle();
				TAPTable tapTable = getSingleTableFromSchema(dbConn, schema, tapSchemaName, tapValidTables.getTapTables(), fullQualifiedTableName);
				if(tapTable == null){
					throw new UwsException("Resource '"+fullQualifiedTableName+"' not available");
				}
				if(!onlyTables){
					populateTableColumns(dbConn, tapTable, tapSchemaName, tapValidTables.getTapColumns(), schema.getADQLName());
				}
				schema.addTable(tapTable);
			}

			//Add schema
			tapMetadata.addSchema(schema);
		}
	}
	
	private static Map<String, List<UwsShareItemBase>> getAccessibleItems(String userid) throws Exception {
		UwsShareManager shareManager = UwsManager.getInstance().getFactory().getShareManager();

		//Returns the shared items the user can access to:
		List<UwsShareItemBase> accessibleItems = shareManager.getMaxAccessibilityAccessibleSharedItems(
				userid, SHARED_RESOURCE_TYPE_TABLE, UwsShareManager.UNSPECIFIED_SHARE_TYPE, UwsShareManager.UNSPECIFIED_SHARE_MODE);

		//Sort shared items by schemas
		Map<String, List<UwsShareItemBase>> schemasByItems = getSchemasForSharedItems(accessibleItems);
		
		return schemasByItems;
	}
	
	private static boolean hasAccessTo(DBConnection dbConn, String userid, String fullQualifiedTableName, boolean includeSharedItems) throws Exception {
		//check user is owner
		String userSchema = TAPMetadata.getUserSchema(userid);
		if(TapUtils.checkUserSchema(userSchema, fullQualifiedTableName)){
			return true;
		}
		
		//user is not owner, check shared items if required
		if(includeSharedItems){
			UwsShareManager shareManager = UwsManager.getInstance().getFactory().getShareManager();
	
			String query = "SELECT resource_id FROM share_schema.share" +
					" WHERE resource_type="+ SHARED_RESOURCE_TYPE_TABLE+" AND title = '"+fullQualifiedTableName.toLowerCase()+"'";
	
			//there should be only one result
			ResultSet tableResultSet = (ResultSet) dbConn.executeQuery(query, null);
			if(!tableResultSet.next()){
				return false;
			}
	
			String resourceId = tableResultSet.getString(1);
			return shareManager.hasAccess(userid, resourceId, SHARED_RESOURCE_TYPE_TABLE);
		} else {
			return false;
		}
	}
	
	private static Map<String, List<UwsShareItemBase>> getSchemasForSharedItems(List<UwsShareItemBase> accessibleItems) {
		Map<String, List<UwsShareItemBase>> schemasByItems = new HashMap<String, List<UwsShareItemBase>>();
		String schemaName;
		String id;
		List<UwsShareItemBase> list;
		for (UwsShareItemBase item : accessibleItems) {
			id = item.getTitle();
			schemaName = TapUtils.getSchemaNameOnly(id);
			list = schemasByItems.get(schemaName);
			if (list == null) {
				list = new ArrayList<UwsShareItemBase>();
				schemasByItems.put(schemaName, list);
			}
			list.add(item);
		}
		return schemasByItems;
	}
	
	private static List<String> getAccessibleSchemas(String userid) throws Exception {
		UwsShareManager shareManager = UwsManager.getInstance().getFactory().getShareManager();

		//Returns the shared items the user can access to:
		List<UwsShareItemBase> accessibleItems = shareManager.getMaxAccessibilityAccessibleSharedItems(
				userid, SHARED_RESOURCE_TYPE_TABLE, UwsShareManager.UNSPECIFIED_SHARE_TYPE, UwsShareManager.UNSPECIFIED_SHARE_MODE);

		//Sort shared items by schemas
		List<String> accessibleSchemas = getAccessibleSchemas(accessibleItems);
		
		return accessibleSchemas;
	}

	
	private static List<String> getAccessibleSchemas(List<UwsShareItemBase> accessibleItems){
		List<String> accessibleSchemas = new ArrayList<String>();
		String id;
		String schemaName;
		for (UwsShareItemBase item : accessibleItems) {
			id = item.getTitle();
			schemaName = TapUtils.getSchemaNameOnly(id);
			if(accessibleSchemas.contains(schemaName)){
				continue;
			}else{
				accessibleSchemas.add(schemaName);
			}
		}
		return accessibleSchemas;
	}

	/**
	 * Tries to load tables.<br/>
	 * If schema+name is provided, if the table is not found an exception is raised.<br/>
 	 * If schema is not provided, a search by table name is performed in order to
	 * obtain the schemas: the resulting schema+table items are loaded and no exception is raised if a schema+name item
	 * is not accessible.
	 * @param service
	 * @param owner
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static TAPMetadata getMatchingTables(TAPService service, UwsJobOwner owner, TAPMetadataLoaderArgs args) throws TAPException {
		TAPMetadata tapMetadata=null;
		DBConnection dbConn = null;
		TAPFactory factory = service.getFactory();
		try {
			dbConn = factory.createDBConnection("TAP(ServiceConnection)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			TAPSchemaInfo tapSchemaInfo = service.getTapSchemaInfo(owner);
			
			TAPSchemaLoaderArgs schemaLoaderArgs = new TAPSchemaLoaderArgs(dbConn, tapSchemaInfo, owner, args);
			List<String> fullQualifiedTableNames = args.getFullQualifiedTableNames();
			
			// List all schemas, tables and columns available:
			tapMetadata = new TAPMetadata(service);
			
			boolean isAnonymous = UwsUtils.isAnonymous(owner.getId());
			
			//String userSchemaName = null;
			for(String fullQualifiedTableName: fullQualifiedTableNames){
				if(isAnonymous){
					loadMatchesTables(tapMetadata, schemaLoaderArgs, USE_VIEWS, fullQualifiedTableName);
				}else{
					loadMatchesTables(tapMetadata, schemaLoaderArgs, USE_REAL_TABLES, fullQualifiedTableName);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			throw new TAPException(e);
			
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
		return tapMetadata;
	}
	
	/**
	 * If schema+name is provided, if the table is not found an exception is raised.<br/>
	 * When no schema is provided, a search by pattern (by table name) is launched and then, every schema+table is loaded:
	 * if a table cannot be loaded (not accessible), no exception is raised and the next table is tried.
	 * @param tapMetadata
	 * @param schemaLoaderArgs
	 * @param useRealTables
	 * @param fullQualifiedTableName
	 * @throws Exception
	 */
	private static void loadMatchesTables(TAPMetadata tapMetadata, TAPSchemaLoaderArgs schemaLoaderArgs, boolean useRealTables, String fullQualifiedTableName) throws Exception {
		String schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTableName);
		if(schemaName != null && !"".equals(schemaName)){
			loadSingleTable(tapMetadata, schemaLoaderArgs, useRealTables, fullQualifiedTableName);
		} else {
			TAPSchemaInfo tapSchemaInfo = schemaLoaderArgs.getTapSchemaInfo();
			DBConnection dbConn = schemaLoaderArgs.getDbConn();
			String tapSchemaName = tapSchemaInfo.getTapSchemaName();
			TAPValidTables tapValidTables = getValidTables(tapSchemaInfo, useRealTables);
	
			String tableName = TapUtils.getTableNameOnly(fullQualifiedTableName);
			List<String> possibleFullQualifedTableNames = getMatchesFromTap(dbConn, tapSchemaName, tapValidTables.getTapTables(), tableName);
			
			for(String f: possibleFullQualifedTableNames){
				try{
					loadSingleTable(tapMetadata, schemaLoaderArgs, useRealTables, f);
				}catch(Exception e){
					//ignore, try next match
				}
			}
		}
	}
	
	private static List<String> getMatchesFromTap(DBConnection dbConn, String tapSchemaName, String tapTableName, String tableName) throws Exception {
		String tapSchema = tapSchemaName.toLowerCase();
		String tapTable = tapTableName.toLowerCase();
		String table = tableName.toLowerCase();
		String query = "SELECT table_name,schema_name,description,size,flags FROM "+tapSchema+"."+tapTable+" " +
				" WHERE table_name = '"+table+"'";
		
		ResultSet tableResultSet = (ResultSet) dbConn.executeQuery(query, null);

		//Get all the tables from the TAP_SCHEMA
		List<String> tables = new ArrayList<String>();
		String fullQualifiedTableName;
		while (tableResultSet.next()) {
			fullQualifiedTableName = tableResultSet.getString("schema_name") + "." + tableResultSet.getString("table_name");

			tables.add(fullQualifiedTableName);
		}
		
		return tables;
	}

}
