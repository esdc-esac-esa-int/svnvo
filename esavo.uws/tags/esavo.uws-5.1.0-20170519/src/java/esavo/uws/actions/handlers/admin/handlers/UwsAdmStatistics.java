package esavo.uws.actions.handlers.admin.handlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.actions.handlers.UwsFunctionsHandler;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.jdbc.UwsJdbcManager;

/**
 * <pre><tt>
 * admin?ACTION=stats
 * </tt></pre>
 * 
 * @author jsegovia
 *
 */
public class UwsAdmStatistics implements UwsFunctionsHandler {

	public static final String ACTION = "stats";
	
	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsActionRequest actionRequest, HttpServletResponse response, UwsManager uwsManager, UwsJobOwner currentUser) throws UwsException  {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		//Check user is admin
		if(!UwsHandlersUtils.checkAdminUser(currentUser, outputHandler, response)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "Not allowed.");
		}
		
		String poolInfo = UwsJdbcManager.info();
		Map<String, Long> jobsInMemory = uwsManager.getNumberOfJobsInMemory();
		String jobsInMemoryReport = getJobsInMemoryReport(jobsInMemory);

		UwsOutputResponseHandler uwsOutput = uwsManager.getFactory().getOutputHandler();
		UwsConfiguration uwsConfiguration = uwsManager.getFactory().getConfiguration();
		StringBuilder data = new StringBuilder();
		String currenTime = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
		data.append("Date: ").append(currenTime).append('\n');
		data.append("UWS: ").append(uwsConfiguration.getUwsVersion()).append('\n');
		data.append("Num Jobs in memory: ").append('\n').append(jobsInMemoryReport).append('\n'); 
		data.append("PoolInfo:\n").append(poolInfo);
		data.append("Scheduler:\n").append(uwsManager.getFactory().getScheduler().toString()).append('\n');
		uwsOutput.writeTextPlainResponse(response, data.toString());
		return;
	}
	
	@Override
	public String getActionIdentifier() {
		return ACTION;
	}
	
	private String getJobsInMemoryReport(Map<String,Long> jobsInMemory){
		if(jobsInMemory == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(Entry<String,Long> e: jobsInMemory.entrySet()){
			sb.append("\t").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
		}
		return sb.toString();
	}

}