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

import java.util.ArrayList;
import java.util.List;

import esavo.tap.publicgroup.handlers.AddTableToUserHandler;
import esavo.tap.publicgroup.handlers.ListAllTables;
import esavo.tap.publicgroup.handlers.ListUserTables;
import esavo.tap.publicgroup.handlers.RemoveTableFromUserHandler;

public class PublicGroupManager {
	
	private List<PublicGroupHandler> handlers;
	
	private static PublicGroupManager manager = null;
	
	public static synchronized PublicGroupManager getInstance() {
		if(manager == null){
			manager = new PublicGroupManager();
		}
		return manager;
	}
	
	private PublicGroupManager() {
		handlers = new ArrayList<PublicGroupHandler>();
		handlers.add(new AddTableToUserHandler());
		handlers.add(new RemoveTableFromUserHandler());
		handlers.add(new ListUserTables());
		handlers.add(new ListAllTables());
	}
	
	public PublicGroupHandler getSuitableHandler(String action){
		for(PublicGroupHandler handler: handlers){
			if(handler.canHandle(action)){
				return handler;
			}
		}
		return null;
	}
	
	public String getValidActions(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(PublicGroupHandler handler: handlers){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append(handler.getAction());
		}
		return sb.toString();
	}

}
