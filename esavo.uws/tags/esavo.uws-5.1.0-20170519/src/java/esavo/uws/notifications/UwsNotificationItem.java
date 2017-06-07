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
