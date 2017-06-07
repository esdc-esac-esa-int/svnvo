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
package esavo.uws.storage;

public class UwsOwnerSessionFilter {
	
	private String ownerid;
	private String sessionid;
	
	public UwsOwnerSessionFilter(){
		
	}
	
	public UwsOwnerSessionFilter(String ownerid, String sessionid){
		this.ownerid = ownerid;
		this.sessionid = sessionid;
	}
	
	/**
	 * @return the ownerid
	 */
	public String getOwnerid() {
		return ownerid;
	}
	/**
	 * @param ownerid the ownerid to set
	 */
	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
	}
	/**
	 * @return the sessionid
	 */
	public String getSessionid() {
		return sessionid;
	}
	/**
	 * @param sessionid the sessionid to set
	 */
	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
	
	/**
	 * Returns 'true' if session is not null nor empty string
	 * @return
	 */
	public boolean hasSession(){
		return sessionid != null && !"".equals(sessionid);
	}
	
	@Override
	public String toString(){
		return "Filter ownerid: '"+ownerid+"', session: '"+sessionid+"'";
	}

}
