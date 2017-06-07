package esavo.uws.notifications;

import java.util.Set;

public class UwsNotificationItem {
	
	private String id;
	private int type;
	private int subtype;
	private String msg;
	private long creationTime;
	private Set<String> users;
	
	public UwsNotificationItem(String id, int type, int subtype, String msg){
		this.id = id;
		this.type = type;
		this.subtype = subtype;
		this.msg = msg;
		this.creationTime = System.currentTimeMillis();
	}
	
	public UwsNotificationItem(String id, int type, int subtype, String msg, Set<String> users){
		this(id, type, subtype, msg);
		setUsers(users);
	}
	
	public String getId(){
		return id;
	}
	
	public String getMsg(){
		return msg;
	}
	
	public int getType(){
		return type;
	}
	
	public int getSubtype(){
		return subtype;
	}
	
	public void setUsers(Set<String> users){
		this.users = users;
	}
	
	public Set<String> getUsers(){
		return users;
	}
	
	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	public void setCreationTime(long creationTime){
		this.creationTime = creationTime;
	}

	@Override
	public String toString(){
		return id + " (type "+type+", subtype "+subtype+"): " + msg;
	}

}
