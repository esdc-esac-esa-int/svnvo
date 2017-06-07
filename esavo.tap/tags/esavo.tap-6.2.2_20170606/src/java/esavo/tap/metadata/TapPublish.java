package esavo.tap.metadata;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.TAPSchemaInfo;
import esavo.tap.TAPService;
import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;

public class TapPublish {

	public enum TapPublishCommand{
		Create,
		Public,
		Private,
		Status,
		Remove
	}
	
	public static TapPublishCommand getTapPublishCommand(String command){
		if(command == null){
			return null;
		}
		try{
			return TapPublishCommand.valueOf(command);
		}catch(IllegalArgumentException e){
			
		}
		char c = command.charAt(0);
		String newcommnad = Character.toUpperCase(c) + command.toLowerCase().substring(1);
		return TapPublishCommand.valueOf(newcommnad);
	}
	
	public static String getValidCommands(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(TapPublishCommand c: TapPublishCommand.values()){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append('\'').append(c.name()).append('\'');
		}
		return sb.toString();
	}
	

	private TAPService service;
	private UwsJobOwner owner;
	private List<String> fullQualifiedTableNames;
	private Map<String,String> results;
	private TapPublishCommand lastCommand;
	private String tapSchemaName;
	private boolean calculateArrayDims;
	
	public TapPublish(TAPService service, UwsJobOwner owner, List<String> fullQualifiedTableNames, String tapSchemaName, boolean calculateArrayDims){
		this.service = service;
		this.owner = owner;
		this.fullQualifiedTableNames = fullQualifiedTableNames;
		this.tapSchemaName = tapSchemaName;
		this.calculateArrayDims = calculateArrayDims;
	}
	
	public void executeCommand(TapPublishCommand command) throws DBException{
		lastCommand = command;
		DBConnection dbConn = null;
		TAPFactory factory = service.getFactory();
		TAPSchemaInfo tapSchemaInfo;
		try {
			tapSchemaInfo = service.getTapSchemaInfo(owner);
		} catch (TAPException e) {
			throw new DBException("Cannot obtain tap schema info for user '"+owner.getId()+"': " + e.getMessage(), e);
		}
		if(tapSchemaName != null && !tapSchemaName.isEmpty()){
			tapSchemaInfo.setTapSchemaName(tapSchemaName);
		}
		try {
			dbConn = factory.createDBConnection("TAP(ServiceConnection)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			switch(command){
			case Create:
				results = dbConn.publishTablesInTapSchema(tapSchemaInfo, fullQualifiedTableNames, calculateArrayDims);
				break;
			case Public:
				results = dbConn.setPublicStatusForTablesInTapSchema(tapSchemaInfo, fullQualifiedTableNames, true);
				break;
			case Private:
				results = dbConn.setPublicStatusForTablesInTapSchema(tapSchemaInfo, fullQualifiedTableNames, false);
				break;
			case Remove:
				results = dbConn.removeTablesFromTapSchema(tapSchemaInfo, fullQualifiedTableNames);
				break;
			case Status:
				results = dbConn.getPublicStatusForTablesInTapSchema(tapSchemaInfo, fullQualifiedTableNames);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(e, "Error while publishing tables");
			
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public void writeResponse(HttpServletResponse response) throws IOException{
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);
		response.setStatus(UwsOutputResponseHandler.OK);
		PrintWriter writer = response.getWriter();
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<tables>");
		if(results != null){
			for(Entry<String, String> table: results.entrySet()){
				writer.println("<table>");
				writer.println("\t<name>"+table.getKey()+"</name>");
				switch(lastCommand){
				case Create:
				case Public:
				case Private:
				case Remove:
					writer.println("\t<status>"+table.getValue()+"</status>");
					break;
				default:
					writer.println("\t<public>"+table.getValue()+"</public>");
					break;
				}
				writer.println("</table>");
			}
		}
		writer.println("</tables>");
		writer.flush();
	}


}
