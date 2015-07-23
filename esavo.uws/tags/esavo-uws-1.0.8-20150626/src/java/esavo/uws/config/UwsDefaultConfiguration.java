package esavo.uws.config;

import java.util.HashMap;
import java.util.Map;

public class UwsDefaultConfiguration implements UwsConfiguration {
	
	private Map<String, String> properties;
	private String appid;
	
	public UwsDefaultConfiguration(String appid){
		this.appid = appid;
		properties = new HashMap<String, String>();
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return properties.containsKey(propertyName);
	}
	
	@Override
	public long getLongProperty(String propertyName) throws NumberFormatException{
		return Long.parseLong(properties.get(propertyName));
	}

	@Override
	public int getIntProperty(String propertyName) throws NumberFormatException{
		return Integer.parseInt(properties.get(propertyName));
	}

	@Override
	public String toString(){
		return "Configuration properties: " + properties.size();
	}

	@Override
	public void clear() {
		properties.clear();
	}
}
