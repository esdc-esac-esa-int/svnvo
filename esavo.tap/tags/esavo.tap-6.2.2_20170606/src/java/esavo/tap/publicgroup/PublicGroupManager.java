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
