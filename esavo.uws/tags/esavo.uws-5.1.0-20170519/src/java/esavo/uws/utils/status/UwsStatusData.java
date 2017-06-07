package esavo.uws.utils.status;

public class UwsStatusData {
	
	public static final String TYPE_INGESTION = "INGESTION";
	public static final String TYPE_UPLOAD = "UPLOAD";
	public static final String TYPE_PARSE = "PARSE";
	
	private String statusData;
	private String statusType;
	
	public UwsStatusData(String statusType, String initialInfo){
		this.statusType = statusType;
		this.statusData = initialInfo;
	}

	public void setData(String data) {
		this.statusData = data;
	}

	public String getData() {
		return this.statusData;
	}
	
	public String getType(){
		return this.statusType;
	}
	
	@Override
	public String toString(){
		return "Status type: " + getType() + ", data: " + getData();
	}

}
