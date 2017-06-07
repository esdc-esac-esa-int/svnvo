package esavo.uws.owner.utils;

public class UwsJobsOwnersFilter {
	
	private String idFilter;
	
	public UwsJobsOwnersFilter(){
		
	}
	
	public void setIdFilter(String id){
		this.idFilter = id;
	}
	
	public boolean hasIdFilter(){
		return idFilter != null && !"".equals(idFilter);
	}
	
	public String getIdFilter(){
		return idFilter;
	}

}
