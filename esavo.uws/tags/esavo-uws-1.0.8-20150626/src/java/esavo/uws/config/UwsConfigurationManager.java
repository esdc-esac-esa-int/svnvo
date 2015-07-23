package esavo.uws.config;

public class UwsConfigurationManager {
	
	//private static Map<String, UwsConfiguration> configurations = new HashMap<String, UwsConfiguration>();
	private static UwsConfiguration config;
	
	public static synchronized UwsConfiguration getConfiguration(String appid){
		//UwsConfiguration config = configurations.get(appid);
		if(config == null){
			config = new UwsDefaultConfiguration(appid);
			//configurations.put(appid, config);
		}
		return config;
	}

}
