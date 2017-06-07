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
package esavo.uws.security;

import esavo.uws.owner.UwsJobOwner;

public class UwsDefaultSecurityManager implements UwsSecurity {
	
	private String appid;
	
	public UwsDefaultSecurityManager(String appid){
		this.appid = appid;
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public UwsJobOwner getUser() {
		return null;
	}

	@Override
	public void setUser(UwsJobOwner user) {
	}
	
	@Override
	public String toString(){
		return "Default Security Manager for application '"+appid+"'";
	}

}
