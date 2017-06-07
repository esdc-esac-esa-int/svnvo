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
package esavo.tap.publicgroup;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.uws.owner.UwsJobOwner;

public interface PublicGroupHandler {
	
	public static final String JDBC_PUBLIC_GROUP_CONNECTION = "PublicGroupConnection";
	
	public static final String PARAM_ACTION = "ACTION";
	public static final String PARAM_TABLES = "TABLES";
	public static final String PARAM_TITLE_PATTERN = "PATTERN";

	//public static final String PARAM_PUBLIC_GROUP_ID = "public.group.id";
	//public static final String PARAM_PUBLIC_GROUP_OWNER_ID = "public.group.owner";

	public String getAction();
	public boolean canHandle(String action);
	public void handle(UwsJobOwner user, HttpServletResponse response, TAPService service, Map<String,String> parameters) throws IOException;

}
