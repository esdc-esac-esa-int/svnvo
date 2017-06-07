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
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.TAPException;
import esavo.tap.TAPService;

public interface TAPResource {
	public static final String SHARE_INFO_PARAM = "share_info";
	public static final String TABLE_PARAM = "tables";
	public static final String ACCESSIBLE_SHARED_TABLES_PARAM = "share_accessible";
	public static final String ONLY_SCHEMAS_PARAM = "only_schemas";
	public static final String ONLY_TABLES_PARAM = "only_tables";
	public static final String SCHEMA_PARAM = "schemas";
	public static final String TAP_PUBLISH_PARAM = "tap_publish";
	public static final String TAP_SCHEMA_NAME_PARAM = "tap_schema";
	public static final String NAME_PARAM = "name";
	public static final String CMD_PARAM = "cmd";
	public static final String FORCE_PARAM = "force";
	public static final String CALCULATE_ARRAY_DIMS = "calculate_arrays_dims";
	public static final String FUNCTION_NAME_PARAM = "function_name";
	
	public void init(TAPService service);

	public boolean canHandle(String action);

	public String getName();
	
	public String[] getResourceItems();

	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException, TAPException, UwsException;

}
