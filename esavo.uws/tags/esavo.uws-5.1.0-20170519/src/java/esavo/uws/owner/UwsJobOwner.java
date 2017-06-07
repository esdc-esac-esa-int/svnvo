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
package esavo.uws.owner;

import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.utils.UwsUtils;

public class UwsJobOwner {

	
	public static final int ROLE_USER  = 0;
	public static final int ROLE_ADMIN = 1;

	public static UwsJobOwner ANONYMOUS_OWNER = new UwsJobOwner(UwsUtils.ANONYMOUS_USER, ROLE_USER);
	public static UwsJobOwner ALL_USERS_OWNER = new UwsJobOwner(UwsUtils.ALL_USERS, ROLE_USER);

	private String id;
	private String name;
	private String authUsername;
	private String pseudo;
	private String session;
	private transient String ip;
	private int roles;
	private UwsJobOwnerParameters parameters;
	
	public UwsJobOwner(String id, int roles){
		this.id = id;
		this.roles = roles;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the authUsername
	 */
	public String getAuthUsername() {
		return authUsername;
	}

	/**
	 * @return the pseudo
	 */
	public String getPseudo() {
		return pseudo;
	}

	/**
	 * @return the session
	 */
	public String getSession() {
		return session;
	}

	/**
	 * @return the parameters
	 */
	public UwsJobOwnerParameters getParameters() {
		return parameters;
	}

	/**
	 * @param authUsername the authUsername to set
	 */
	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}

	/**
	 * @param pseudo the pseudo to set
	 */
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(String session) {
		this.session = session;
	}

	/**
	 * @param parameters owner parameters
	 */
	public void setParameters(UwsJobOwnerParameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Returns true if the user is admin
	 * @return
	 */
	public boolean isAdmin(){
		return ((roles & ROLE_ADMIN) > 0);
	}
	
	/**
	 * @return the roles
	 */
	public int getRoles() {
		return roles;
	}
	
	/**
	 * Sets the roles
	 * @param roles
	 */
	public void setRoles(int roles){
		this.roles = roles;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString(){
		return "owner id: " + id + ", roles: " + roles + ", session: " + session + ", ip: " + ip;
	}

}
