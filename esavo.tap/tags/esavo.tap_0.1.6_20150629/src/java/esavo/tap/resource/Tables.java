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
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPMetadataLoaderArgs;
import esavo.tap.metadata.TAPMetadata.OutputType;

/**
 * Processing:
 * 
 * <pre><tt>
 *     1. tables=full_qualified_table_name
 *         can be a list
 *         Return all columns of the specified table
 *         share_info / share_accessible are used if present
 *         No more checks/actions are performed
 * 
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
		TAPMetadata tapMetadata;
		try {
			//String fullQualifiedTableNameRequestedTable = args.getFullQualifiedTableName();
			//if(fullQualifiedTableNameRequestedTable != null && !"".equals(fullQualifiedTableNameRequestedTable)){
			if(args.hasFullQualifiedTableNames()){
				//Req 1
//				if(args.isIncludeAccessibleSharedItems()){
//					throw new IOException("Cannot access to share information of a non proprietary table.");
//				}
				//tapMetadata = TAPMetadataLoader.getMatchingTables(service, user, args);
				tapMetadata = TAPMetadataLoader.getSingleTable(service, user, args);
			}else{
				//Req 2, 3
				tapMetadata = TAPMetadataLoader.getTAPMetadata(service, user, args);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		boolean shareInfoFlag = args.isIncludeShareInfo();
		return tapMetadata.executeResource(request, response, OutputType.OnlyTables, shareInfoFlag);
	}
	
	private TAPMetadataLoaderArgs createArguments(HttpServletRequest request){
		boolean shareInfoFlag = false;
		Map paramMap = request.getParameterMap();
		if(paramMap.containsKey(SHARE_INFO_PARAM)){
			shareInfoFlag = Boolean.parseBoolean(request.getParameter(SHARE_INFO_PARAM));
		}
		List<String> fullQualifiedTableNameRequestedTableList = null;
		String fullQualifiedTableNameRequestedTable = null;
		if(paramMap.containsKey(TABLE_PARAM)){
			fullQualifiedTableNameRequestedTable = request.getParameter(TABLE_PARAM);
			fullQualifiedTableNameRequestedTableList = parseList(fullQualifiedTableNameRequestedTable);
		}
		boolean accessibleSharedItems = false;
		if(paramMap.containsKey(ACCESSIBLE_SHARED_TABLES_PARAM)){
			accessibleSharedItems = Boolean.parseBoolean(request.getParameter(ACCESSIBLE_SHARED_TABLES_PARAM));
		}
		boolean onlySchemas = false;
		if(paramMap.containsKey(ONLY_SCHEMAS_PARAM)){
			onlySchemas = Boolean.parseBoolean(request.getParameter(ONLY_SCHEMAS_PARAM));
		}
		boolean onlyTables = false;
		if(paramMap.containsKey(ONLY_TABLES_PARAM)){
			onlyTables = Boolean.parseBoolean(request.getParameter(ONLY_TABLES_PARAM));
		}
		List<String> schemaNamesList = null;
		String schemaNames = null;
		if(paramMap.containsKey(SCHEMA_PARAM)){
			schemaNames = request.getParameter(SCHEMA_PARAM);
			schemaNamesList = parseList(schemaNames);
		}

		TAPMetadataLoaderArgs args = new TAPMetadataLoaderArgs();
		args.setFullQualifiedTableNames(fullQualifiedTableNameRequestedTableList);
		args.setIncludeShareInfo(shareInfoFlag);
		args.setIncludeAccessibleSharedItems(accessibleSharedItems);
		args.setOnlySchemas(onlySchemas);
		args.setOnlyTables(onlyTables);
		args.setSchemaNames(schemaNamesList);
		
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
