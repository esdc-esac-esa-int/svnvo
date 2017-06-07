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
