package esavo.uws.utils.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.uws.config.UwsConfiguration;

public class UwsJdbcManager {
	
	private static Map<String, UwsJdbcSingleton> handlers = new HashMap<String, UwsJdbcSingleton>();
	
	/*
	public static synchronized long getMinTimeout(List<String> poolIds){

		long minTimeout = -1;
		for(String id:poolIds){
			UwsJdbcSingleton poolManager = UwsJdbcManager.getInstance(id);
			UwsDatabaseProperties databaseProperties = poolManager.getDatabaseProperties();
			
			long timeout = databaseProperties.getTimeOut();
			
			if(minTimeout<0 || minTimeout>timeout){
				minTimeout = timeout;
			}
			
		}
		
		return minTimeout;
	}
	*/
	
	public static synchronized UwsJdbcSingleton createInstance(String appid, String poolId, UwsDatabaseProperties databaseProperties){
		UwsJdbcSingleton manager = handlers.get(poolId);
		if(manager == null){
			manager = new UwsJdbcSingleton(appid, databaseProperties);
			handlers.put(poolId, manager);
		}
		return manager;
	}
	
	public static synchronized UwsJdbcSingleton getInstance(String poolid){
		return handlers.get(poolid);
	}
	
	public static synchronized void removeInstance(String poolId){
		handlers.remove(poolId);
	}
	
	public static synchronized void setDummyConnection(String appid, String poolId, Connection dummyConnection){
		UwsJdbcSingleton manager = handlers.get(poolId);
		if(manager == null){
			manager = new UwsJdbcSingleton(appid, dummyConnection);
			handlers.put(poolId, manager);
		}else{
			manager.setDummyConnection(dummyConnection);
		}
	}
	
	public static synchronized String dump(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(Entry<String, UwsJdbcSingleton> e: handlers.entrySet()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append('\n');
			}
			sb.append(e.getKey()).append(": ").append(e.getValue());
		}
		return sb.toString();
	}
	
	public static synchronized String info(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(Entry<String, UwsJdbcSingleton> e: handlers.entrySet()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append('\n');
			}
			sb.append(e.getKey()).append(":\n").append(e.getValue().info());
		}
		return sb.toString();
	}

}
