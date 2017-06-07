package esavo.tap.resource;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.TapUtils;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPMetadataLoaderArgs;
import esavo.tap.metadata.TAPMetadata.OutputType;
import esavo.tap.metadata.TapPublish;

/**
 * Processing:
 * 
 * <pre><tt>
 *     1. if tap_publish (=true|false|status|remove) is present:
 *        tables is required and no more checks are performed
 *     2. tables=full_qualified_table_name
 *         can be a list
 *         Return all columns of the specified table
 *         share_info / share_accessible are used if present
 *         No more checks/actions are performed
 * 
 *     3. only_schemas = TRUE
 *         any other flags are ignored. Except share_info / share_accessible
 * 
 *     4. only_schemas = FALSE (default)
 * 
 *         4.1. if schema_names != null, the following (2.2, 2.3) applies only to the specified schemas
 *         schema_names can be a list of names separated by ','
 * 
 *         4.2. only_tables = TRUE
 *         any other flag is ignored. Except share_info / share_accessible
 * 
 *         4.3. only_tables = FALSE (default)
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Tables implements TAPResource {

	public static final String RESOURCE_NAME = "tables";
	public static final String[] RESOURCE_ITEMS = {RESOURCE_NAME};
	
	private TAPService service;
	
	@Override
	public void init(TAPService service) {
		this.service = service;
	}

	@Override
	public String getName() {
		return RESOURCE_NAME;
	}
	
	@Override
	public String[] getResourceItems(){
		return RESOURCE_ITEMS;
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException, TAPException, UwsException {
		TAPMetadataLoaderArgs args = createArguments(request);
		if(args.hasTapPublishCommand()){
			return executeTapPublish(request, response, user, args);
		} else {
			return executeTapMetadata(request, response, user, args);
		}
	}
	
	private boolean executeTapPublish(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user, TAPMetadataLoaderArgs args) throws ServletException, IOException{
		//check is admin
		if(!UwsUtils.checkAdminUser(user)){
			throw new IOException("User is not allowed to modify tap schema");
		}
		TapPublish tapPublish = new TapPublish(service, user, args.getFullQualifiedTableNames(), args.getTapSchemaName(), args.isCalculateArrayDims());
		try{
			tapPublish.executeCommand(args.getTapPublishCommand());
			tapPublish.writeResponse(response);		
			return false;
		}catch(Exception e){
			throw new IOException(e);
		}
	}
	
	private boolean executeTapMetadata(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user, TAPMetadataLoaderArgs args) throws ServletException, IOException{
		TAPMetadata tapMetadata;
		try {
			if(args.hasFullQualifiedTableNames()){
				//Req 2
				tapMetadata = TAPMetadataLoader.getSingleTable(service, user, args);
			}else{
				//Req 3, 4
				tapMetadata = TAPMetadataLoader.getTAPMetadata(service, user, args);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		boolean shareInfoFlag = args.isIncludeShareInfo();
		return tapMetadata.executeResource(request, response, OutputType.OnlyTables, shareInfoFlag);
	}
	
	private TAPMetadataLoaderArgs createArguments(HttpServletRequest request) throws TAPException{
		boolean shareInfoFlag = false;
		Map paramMap = request.getParameterMap();
		if(TapUtils.hasParam(paramMap,SHARE_INFO_PARAM)){
			shareInfoFlag = Boolean.parseBoolean(TapUtils.getParam(request,SHARE_INFO_PARAM));
		}
		List<String> fullQualifiedTableNameRequestedTableList = null;
		String fullQualifiedTableNameRequestedTable = null;
		if(TapUtils.hasParam(paramMap,TABLE_PARAM)){
			fullQualifiedTableNameRequestedTable = TapUtils.getParam(request,TABLE_PARAM);
			fullQualifiedTableNameRequestedTableList = parseList(fullQualifiedTableNameRequestedTable);
		}
		boolean accessibleSharedItems = false;
		if(TapUtils.hasParam(paramMap,ACCESSIBLE_SHARED_TABLES_PARAM)){
			accessibleSharedItems = Boolean.parseBoolean(TapUtils.getParam(request,ACCESSIBLE_SHARED_TABLES_PARAM));
		}
		boolean onlySchemas = false;
		if(TapUtils.hasParam(paramMap,ONLY_SCHEMAS_PARAM)){
			onlySchemas = Boolean.parseBoolean(TapUtils.getParam(request,ONLY_SCHEMAS_PARAM));
		}
		boolean onlyTables = false;
		if(TapUtils.hasParam(paramMap,ONLY_TABLES_PARAM)){
			onlyTables = Boolean.parseBoolean(TapUtils.getParam(request,ONLY_TABLES_PARAM));
		}
		List<String> schemaNamesList = null;
		String schemaNames = null;
		if(TapUtils.hasParam(paramMap,SCHEMA_PARAM)){
			schemaNames = TapUtils.getParam(request,SCHEMA_PARAM);
			schemaNamesList = parseList(schemaNames);
		}
		esavo.tap.metadata.TapPublish.TapPublishCommand tapPublishCommand = null;
		if(TapUtils.hasParam(paramMap,TAP_PUBLISH_PARAM)){
			String tapPublishCommandArg = TapUtils.getParam(request,TAP_PUBLISH_PARAM);
			try{
				tapPublishCommand = TapPublish.getTapPublishCommand(tapPublishCommandArg);
			}catch(IllegalArgumentException e){
				//raise exception
				throw new TAPException("Unknown tap_publish command '"+tapPublishCommandArg+"'. Valid commands are: " + TapPublish.getValidCommands());
			}
		}
		String tapSchemaName = null;
		if(TapUtils.hasParam(paramMap, TAP_SCHEMA_NAME_PARAM)){
			tapSchemaName = TapUtils.getParam(request, TAP_SCHEMA_NAME_PARAM);
		}
		boolean calculateArrayDims = false;
		if(TapUtils.hasParam(paramMap, CALCULATE_ARRAY_DIMS)){
			calculateArrayDims = Boolean.parseBoolean(TapUtils.getParam(request,CALCULATE_ARRAY_DIMS));
		}
		String fullQualifiedFunctionName = null;
		if(TapUtils.hasParam(paramMap, FUNCTION_NAME_PARAM)){
			fullQualifiedFunctionName = TapUtils.getParam(request, FUNCTION_NAME_PARAM);
		}

		TAPMetadataLoaderArgs args = new TAPMetadataLoaderArgs();
		args.setFullQualifiedTableNames(fullQualifiedTableNameRequestedTableList);
		args.setIncludeShareInfo(shareInfoFlag);
		args.setIncludeAccessibleSharedItems(accessibleSharedItems);
		args.setOnlySchemas(onlySchemas);
		args.setOnlyTables(onlyTables);
		args.setSchemaNames(schemaNamesList);
		args.setTapPublishCommand(tapPublishCommand);
		args.setTapSchemaName(tapSchemaName);
		args.setCalculateArrayDims(calculateArrayDims);
		args.setFullQualifiedFunctionName(fullQualifiedFunctionName);
		
		return args;
	}
	
	private List<String> parseList(String param){
		if(param == null){
			return null;
		}
		String[] items = param.split(",");
		List<String> list = new ArrayList<String>();
		for(String i: items){
			list.add(i);
		}
		return list;
	}
	
	@Override
	public boolean canHandle(String action) {
		return RESOURCE_NAME.equals(action);
	}

}
