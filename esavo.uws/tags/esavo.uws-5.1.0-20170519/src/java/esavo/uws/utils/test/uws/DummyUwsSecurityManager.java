package esavo.uws.utils.test.uws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;

public class DummyUwsSecurityManager implements UwsSecurity {
	
	private UwsJobOwner user;
	
	public DummyUwsSecurityManager(){
		
	}

	@Override
	public UwsJobOwner getUser(HttpServletRequest request) throws UwsException {
		if(request != null){
			HttpSession session = request.getSession();
			if(session != null){
				user.setSession(session.getId());
			}
		}
		return user;
	}

	@Override
	public void setUser(UwsJobOwner user) {
		this.user = user;
	}
	
	public void reset(){
		this.user = null;
	}

}
