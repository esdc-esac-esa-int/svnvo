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
package esavo.tap.publicgroup.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.publicgroup.PublicGroupHandler;
import esavo.tap.publicgroup.PublicGroupItem;
import esavo.tap.publicgroup.PublicGroupUtils;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsEventType;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

/**
 * Removes access to one or more public group tables.<br/>
 * User is extracted from the security context.<br/>
 * <p>Example
 * <pre><tt>
 * server/PublicGroup?ACTION=remove&TABLES=schema1.table1,schema2.table2
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class RemoveTableFromUserHandler implements PublicGroupHandler {
	
	public static final String ACTION = "remove";

	@Override
	public String getAction(){
		return ACTION;
	}

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsJobOwner user, HttpServletResponse response, TAPService service, Map<String,String> parameters) throws IOException {
		String tablesParam = parameters.get(PublicGroupHandler.PARAM_TABLES);
		List<String> tables = PublicGroupUtils.getTables(tablesParam);
		
		//Will raise an exception
		PublicGroupUtils.checkValidTables(tables);
		
		List<PublicGroupItem> items = PublicGroupUtils.getTableItemsForUser(tables, user.getId());
		
		try {
			TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
					JDBC_PUBLIC_GROUP_CONNECTION, UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			dbConn.removeAccessToPublicGroupTables(items);
			service.getFactory().getEventsManager().setEventTime(user, UwsEventType.PUBLIC_GROUP_HIDE_TABLE);
			PublicGroupUtils.writeMsg(response, UwsOutputResponseHandler.OK, UwsOutputResponseHandler.CONTENT_TYPE_TEXT_PLAIN, "OK");
		} catch (Exception e) {
			PublicGroupUtils.writeError(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, 
					"Cannot remove access to group tables for user '"+user.getId()+"'", e.getMessage());
		}
	}

}
