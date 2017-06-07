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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;

/**
 * Processing:
 * 
 * <pre><tt>
 *   cmd=CREATE&name=schema_name
 *   cmd=DELETE&name=schema_name
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class Schemas implements TAPResource {

	public static final String RESOURCE_NAME = "schemas";
	public static final String[] RESOURCE_ITEMS = {RESOURCE_NAME};
	
	private static final String CMD_CREATE = "create";
	private static final String CMD_DELETE = "delete";
	
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
		//check is admin
		if(!UwsUtils.checkAdminUser(user)){
			throw new IOException("User is not allowed to modify tap schema");
		}

		UwsOutputResponseHandler uwsOutput = service.getFactory().getOutputHandler();
		
		String cmd;
		if(request.getParameterMap().containsKey(CMD_PARAM)){
			cmd = request.getParameter(CMD_PARAM);
		}else{
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.BAD_REQUEST, "Missing required parameter: '"+CMD_PARAM+"'");
			return true;
		}
		String schemaName;
		if(request.getParameterMap().containsKey(NAME_PARAM)){
			schemaName = request.getParameter(NAME_PARAM);
		}else{
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.BAD_REQUEST, "Missing required parameter: '"+NAME_PARAM+"'");
			return true;
		}
		
		//In case of creating/removing tap schema 'tap_schema' check 'force=true' param is present
		if("tap_schema".equalsIgnoreCase(schemaName)){
			if(!checkForceParam(request)){
				uwsOutput.writeTextPlainResponse(response, 
						UwsOutputResponseHandler.BAD_REQUEST, "Missing required parameter: '"+
								FORCE_PARAM+"=true' when working with tap schema 'tap_schema'");
				return true;
			}
		}
		
		//handle command
		if(cmd.equalsIgnoreCase(CMD_CREATE)){
			createSchema(response, schemaName, uwsOutput);
		}else if (cmd.equalsIgnoreCase(CMD_DELETE)){
			deleteSchema(response, schemaName, uwsOutput);
		}else{
			//Cmd not valid
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.BAD_REQUEST, "Unknown command: '"+cmd+"'");
		}
		
		return true;
	}
	
	private boolean checkForceParam(HttpServletRequest request){
		if(request.getParameterMap().containsKey(FORCE_PARAM)){
			String value = request.getParameter(FORCE_PARAM);
			if(value == null){
				return false;
			}
			boolean b = Boolean.parseBoolean(value);
			return b;
		}else{
			return false;
		}
	}
	
	private void createSchema(HttpServletResponse response, String schemaName, UwsOutputResponseHandler uwsOutput) throws UwsException{
		TapJDBCPooledFunctions dbConn = null;
		try {
			dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
					"SchemasConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			dbConn.createTapSchema(schemaName);
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.OK, "Created tap schema '"+schemaName+"'");
		}catch (Exception e){
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.BAD_REQUEST, 
					"Error creating tap schema '"+schemaName+"' due to: " + e.getMessage());
		}finally{
			if(dbConn != null){
				try {
					dbConn.close();
				} catch (DBException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void deleteSchema(HttpServletResponse response, String schemaName, UwsOutputResponseHandler uwsOutput) throws UwsException{
		TapJDBCPooledFunctions dbConn = null;
		try {
			dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
					"SchemasConnection", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			dbConn.deleteTapSchema(schemaName);
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.OK, "Removed tap schema '"+schemaName+"'");
		}catch (Exception e){
			uwsOutput.writeTextPlainResponse(response, UwsOutputResponseHandler.BAD_REQUEST, 
					"Error removing tap schema '"+schemaName+"' due to: " + e.getMessage());
		}finally{
			if(dbConn != null){
				try {
					dbConn.close();
				} catch (DBException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	@Override
	public boolean canHandle(String action) {
		return RESOURCE_NAME.equals(action);
	}

}
