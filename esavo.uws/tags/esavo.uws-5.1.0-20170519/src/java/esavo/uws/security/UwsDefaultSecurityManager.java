package esavo.uws.security;

import javax.servlet.http.HttpServletRequest;

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
	public UwsJobOwner getUser(HttpServletRequest request) {
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