package esavo.uws.utils.status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import esavo.uws.owner.UwsJobOwner;

/**
 * Class to store information associated to a user client.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsUserInfo {
	
	private Map<String, String> infoMap;
	private String ipAddress;
	private UwsJobOwner owner;
	private long startTime;
	
	public UwsUserInfo(UwsJobOwner owner){
		this.owner=owner;
		infoMap = new HashMap<String, String>();
		startTime = System.currentTimeMillis();
	}
	
	public void setip(String ipAddress){
		this.ipAddress = ipAddress;
	}
	
	public String getip(){
		return this.ipAddress;
	}
	
	public void setOwner(UwsJobOwner owner){
		this.owner = owner;
	}
	
	public UwsJobOwner getOwner(){
		return this.owner;
	}
	
	public long getStartTime(){
		return this.startTime;
	}
	
	public String getFormattedStartTime(){
		//SimpleDateFormat is not thread safe
		return new SimpleDateFormat("yyyy-MMM-dd'T'HH:mm:ss.SSS").format(new Date(this.startTime));
	}

	public void add(String key, String value){
		infoMap.put(key, value);
	}
	
	public String get(String key){
		return infoMap.get(key);
	}
	
	@Override
	public String toString(){
		return "Username: " + this.owner.getAuthUsername() + ", IP: " + this.ipAddress + ", created at: " + getFormattedStartTime();
	}

}
