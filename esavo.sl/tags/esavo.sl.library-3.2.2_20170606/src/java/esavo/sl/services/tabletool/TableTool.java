package esavo.sl.services.tabletool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.sl.services.tabletool.handlers.EditTableHandler;
import esavo.sl.services.tabletool.handlers.RaDecHandler;
import esavo.sl.services.util.Utils;
import esavo.tap.TAPService;
import esavo.uws.UwsException;
import esavo.uws.output.UwsExceptionOutputFormat;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.security.UwsSecurity;


/**
 * This class is able to change some table columns and to perform an index operation.<br/>
 * Format:
 * <pre><tt>
 * 
 * Complete edition: NUMTABLES required (TABLE_NAME ignored)
 * 
 * NUMTABLES=nt
 * TABLEnt=table
 * TABLEnt_NUMCOLS=nc
 * TABLEnt_COLn=column_name_nc
 * TABLEnt_COLn_UCD=column_nc UCD
 * TABLEnt_COLn_UTYPE=column_nc utype
 * TABLEnt_COLn_FLAGS=column_nc flags (it is expected a single string value, like 'ra'/'dec'/'mag'/'flux' ...)
 * TABLEnt_COLn_INDEXED=column_nc indexed yes/no (true/false)
 * 
 * 
 * Assign Ra/Dec: TABLE_NAME required (NUMTABLES ignored)
 * TABLE_NAME=schema.name
 * RA=ra_column
 * DEC=dec_column
 * 
 * If TABLE_NAME and NUMTABLES are present at the same time, an error is raised.
 * </tt></pre>
 * <p><b>NOTE</b>: In case table contains a schema name, the schema is removed, in order to use the authenticated user schema.
 * All user schemas are protected because the schema is set by the security context.
 * @param <R>
 */
public class TableTool {
	
	public static final String PARAM_TASKID = "TASKID";
	public static final String PARAM_ACTION = "ACTION";
	
	private static final List<TableToolHandler> HANDLERS = new ArrayList<TableToolHandler>();
	static {
		HANDLERS.add(new EditTableHandler());
		HANDLERS.add(new RaDecHandler());
	}
	
	protected final TAPService service;

	public TableTool(TAPService serviceConnection) {
		service = serviceConnection;
	}

	public void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		Map<String,String> parameters = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> e = request.getParameterNames();
		String paramName;
		while(e.hasMoreElements()){
			paramName = e.nextElement();
			parameters.put(paramName, request.getParameter(paramName));
		}
		
		UwsSecurity security = service.getFactory().getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e1) {
			throw new ServletException("Cannot obtain current user: " + e1.getMessage(), e1);
		}

		try {
			//DENY ACCESS TO UNAUTHENTICATED/UNAUTHORIZED USERS
			Utils.checkAuthentication(user);

			String action = parameters.get(PARAM_ACTION);
			if(action == null){
				service.getFactory().getOutputHandler().writeServerErrorResponse(
						response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "TableTool", null, PARAM_ACTION + " parameter not found.");
				return;
			}

			long taskIdentifier = -1;
			String taskid = parameters.get(PARAM_TASKID);
			if(taskid != null && !"".equals(taskid)){
				try{
					taskIdentifier = Long.parseLong(taskid);
				}catch(NumberFormatException nfe){
					service.getFactory().getOutputHandler().writeServerErrorResponse(
							response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "TableTool", null, "Cannot get task identifier");
					return;
				}
			}

			for(TableToolHandler handler: HANDLERS){
				if(handler.getAction().equalsIgnoreCase(action)){
					handler.handle(parameters, taskIdentifier, user, response, service);
					response.flushBuffer();
					return;
				}
			}
			
			//handler not found
			service.getFactory().getOutputHandler().writeServerErrorResponse(
					response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "TableTool", null, 
					"Cannot find a suitable handler for action '"+action+"'.\nAvailable actions are: " + getAvailableActions());
			response.flushBuffer();
			
		}catch(Throwable t){
			try {
				service.getFactory().getOutputHandler().writeServerErrorResponse(
						response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Updating tables", t, UwsExceptionOutputFormat.HTML);
			} catch (UwsException e1) {
				throw new ServletException(e1);
			}
		}

	}
	
	private String getAvailableActions(){
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(TableToolHandler handler: HANDLERS){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append(handler.getAction());
		}
		return sb.toString();
	}
	
}
