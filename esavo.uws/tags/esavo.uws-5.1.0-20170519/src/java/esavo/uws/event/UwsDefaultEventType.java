package esavo.uws.event;

public class UwsDefaultEventType implements UwsEventType {

	private int code;
	private String description;
	
	public UwsDefaultEventType(int code){
		this.code = code;
	}

	public UwsDefaultEventType(int code, String description){
		this.code = code;
		this.description = description;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString(){
		return "Event type: " + code + ": " + description;
	}

}
