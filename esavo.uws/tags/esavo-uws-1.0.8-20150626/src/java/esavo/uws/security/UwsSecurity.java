package esavo.uws.security;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

public interface UwsSecurity {
	
	/**
	 * Returns the user in the current communication.
	 * @return
	 */
	public UwsJobOwner getUser() throws UwsException;
	
	/**
	 * Sets the user object associated to a the current communication.<br/>
	 * (This method can be empty if the security is handled by a subsystem like spring security)
	 * @param user
	 */
	public void setUser(UwsJobOwner user);

}
