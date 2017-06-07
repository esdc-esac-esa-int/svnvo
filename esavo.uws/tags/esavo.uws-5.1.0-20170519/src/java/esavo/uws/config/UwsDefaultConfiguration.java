package esavo.uws.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UwsDefaultConfiguration implements UwsConfiguration {
	
	private Properties properties = new Properties();
	private String appid;
	
	public UwsDefaultConfiguration(String appid){
		this.appid = appid;
		// Load uws config.properties
		try {
			loadPropertiesFile("uws.properties");
		} catch (IOException e) {
			properties = new Properties();
		}
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
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
	public boolean hasValidPropertyValue(String propertyName){
		if(!hasProperty(propertyName)){
			return false;
		}
		String value = properties.getProperty(propertyName);
		if(value == null){
			return false;
		}
		if(value.startsWith("$") || value.startsWith("@")){
			return false;
		}
		return true;
	}
	
	@Override
	public long getLongProperty(String propertyName) throws NumberFormatException{
		return Long.parseLong(properties.getProperty(propertyName));
	}

	@Override
	public int getIntProperty(String propertyName) throws NumberFormatException{
		return Integer.parseInt(properties.getProperty(propertyName));
	}

	@Override
	public String toString(){
		return "Configuration properties: " + properties.size();
	}

	@Override
	public void clear() {
		properties.clear();
	}

	@Override
	public boolean getBooleanProperty(String propertyName) throws NumberFormatException {
		return Boolean.parseBoolean(properties.getProperty(propertyName));
	}

	@Override
	public String getUwsVersion() {
		String version = getProperty(UwsConfiguration.VERSION_PROP);
		if(version==null){
			return "n/a";
		}
		
		return version;
	}
	
	protected void loadPropertiesFile(String propFileName) throws IOException{
		InputStream inputStream = null;
		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		} finally {
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
}
