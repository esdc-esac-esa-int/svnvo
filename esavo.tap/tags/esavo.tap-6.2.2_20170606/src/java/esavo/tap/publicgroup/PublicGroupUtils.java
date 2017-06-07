package esavo.tap.publicgroup;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TapUtils;
import esavo.uws.output.UwsOutputResponseHandler;

public class PublicGroupUtils {
	
	private static String GENERIC_ERROR_MSG = "Manager error";

	/**
	 * Returns a list of table names (full qualified tables names) from the argument.
	 * If the argument is null, the returned list is null.
	 * @param tablesArg
	 * @return
	 */
	public static List<String> getTables(String tablesArg) {
		if (tablesArg == null) {
			return null;
		}
		List<String> tables = new ArrayList<String>();
		String[] items = tablesArg.split(",");
		for (String i: items) {
			tables.add(i.trim());
		}
		return tables;
	}

	/**
	 * Raises an exception if tables is not valid (null or empty size)
	 * @param tables
	 */
	public static void checkValidTables(List<String> tables) {
		if (tables == null) {
			throw new IllegalArgumentException("Null tables not allowed");
		}
		if (tables.size() < 1) {
			throw new IllegalArgumentException("No tables found");
		}
	}
	
	/**
	 * Writes an error message (HTML).
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param error error message.
	 * @param extraMsg extra message.
	 * @throws IOException
	 */
	public static void writeError(HttpServletResponse response, int httpErrorCode, String error, String extraMsg) throws IOException{
		response.setStatus(httpErrorCode);
		response.setContentType(UwsOutputResponseHandler.CONTENT_TYPE_HTML);
		PrintWriter pw = response.getWriter();
		pw.println(Templates.getErrorMessage(GENERIC_ERROR_MSG, error, extraMsg));
		pw.flush();
	}
	
	public static void writeTableList(HttpServletResponse response, List<PublicGroupItem> items) throws IOException {
		response.setStatus(UwsOutputResponseHandler.OK);
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.println("[");
		boolean firstTime = true;
		String row;
		if(items != null){
			for(PublicGroupItem item: items){
				if(firstTime){
					firstTime = false;
				}else{
					pw.println(",");
				}
				row = Templates.getTableListRow(item);
				pw.print(row);
			}
		}
		pw.println("]");

		pw.flush();
	}
	
	
	/**
	 * Writes a simple message
	 * @param response communication handler.
	 * @param httpErrorCode http status code.
	 * @param contentType content type.
	 * @param msg message.
	 * @throws IOException
	 */
	public static void writeMsg(HttpServletResponse response, int httpErrorCode, String contentType, String msg) throws IOException{
		response.setStatus(httpErrorCode);
		response.setContentType(contentType);
		PrintWriter pw = response.getWriter();
		pw.println(msg);
		pw.flush();
	}
	

	
	public static List<PublicGroupItem> getTableItemsForUser(List<String> tables, String userid){
		if (tables == null) {
			return null;
		}
		List<PublicGroupItem> items = new ArrayList<PublicGroupItem>();
		String schemaName;
		String tableName;
		String owner;
		for (String fullQualifiedTable: tables) {
			schemaName = TapUtils.getSchemaNameOnly(fullQualifiedTable);
			tableName = TapUtils.getTableNameOnly(fullQualifiedTable);
			owner = TapUtils.getUserNameFromSchema(schemaName);
			PublicGroupItem item = new PublicGroupItem();
			item.setUser(userid);
			item.setTableSchemaName(schemaName);
			item.setTableName(tableName);
			item.setOwner(owner);
			items.add(item);
		}
		return items;
	}
	
	/**
	 * Returns a list of the tables that are not found in public group shared items
	 * @param items
	 * @param publicGroupSharedItems
	 * @return
	 */
	public static List<String> getNonPublicTables(List<PublicGroupItem> items, List<String> publicGroupSharedItems){
		List<String> nonAccessibleTables = new ArrayList<String>();
		String fullQualifiedTableName;
		for(PublicGroupItem item: items){
			fullQualifiedTableName = item.getTableSchemaName() + "." + item.getTableName();
			if(!publicGroupSharedItems.contains(fullQualifiedTableName)){
				nonAccessibleTables.add(fullQualifiedTableName);
			}
		}
		return nonAccessibleTables;
	}

}
