package esavo.sl.dd.util;

public enum DDContentDisposition {
	
	Inline("inline"), Attachment("attachment");
	
	private String value;
	
	private DDContentDisposition(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}

}
