package esavo.tap.test.uws;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

public class DummyUwsSecurityManager implements UwsSecurity {
	
	private UwsJobOwner user;
	
	public DummyUwsSecurityManager(){
		
	}

	@Override
	public UwsJobOwner getUser() throws UwsException {
		return user;
	}

	@Override
	public void setUser(UwsJobOwner user) {
		this.user = user;
	}

}
