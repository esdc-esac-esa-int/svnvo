package esavo.tap.publicgroup;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.uws.owner.UwsJobOwner;

public interface PublicGroupHandler {
	
	public static final String JDBC_PUBLIC_GROUP_CONNECTION = "PublicGroupConnection";
	
	public static final String PARAM_ACTION = "ACTION";
	public static final String PARAM_TABLES = "TABLES";
	public static final String PARAM_TITLE_PATTERN = "PATTERN";

	//public static final String PARAM_PUBLIC_GROUP_ID = "public.group.id";
	//public static final String PARAM_PUBLIC_GROUP_OWNER_ID = "public.group.owner";

	public String getAction();
	public boolean canHandle(String action);
	public void handle(UwsJobOwner user, HttpServletResponse response, TAPService service, Map<String,String> parameters) throws IOException;

}
