package esavo.uws.actions.handlers.jobs;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.actions.handlers.UwsHandlersUtils;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

/**
 * TAP+: Allows sorting (see {@link UwsListJobsHandler} also)<br/>
 * This TAP+ extension contains parameters (differs from UwsListJobsHandler)
 * 
 * GET: jobs/list?[&session=][&limit=][&offset=][&order=][&metadata_only=true|false]
 * 
 * e.g. 
 * <pre><tt>
 * jobs/async
 * jobs/async?offset=100&limit=20
 * </tt></pre>
 * 
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobsHandler implements UwsActionHandler {
	
	private static final String ID = "jobs";
	public static final String ACTION_NAME = "jobs";
	private static final boolean IS_JOB_ACTION = false;

	
	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}
	
	@Override
	public boolean isJobAction() {
		return IS_JOB_ACTION;
	};


	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet()){
			return false;
		}
		if(actionRequest.hasJobList()){
			return false;
		}
//		if(actionRequest.hasJobId()){
//			return false;
//		}
		if(actionRequest.hasAction()){
			return false;
		}
		if(actionRequest.isMultipartContent()){
			return false;
		}
		if(!actionRequest.hasHandlerAction(ACTION_NAME)){
			return false;
		}
		if(!actionRequest.hasJobId()){
			//jobid contains in this case the list name
			throw new UwsException("Missing jobs list (e.g. 'jobs/async'.");
		}
		
//		//it is jobs list (sort capabilities)
//		if(!actionRequest.hasHttpParameter(PARAMETER_LIST)){
//			throw new UwsException("Missing '" + PARAMETER_LIST + "' required parameter.");
//		}

		return true;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		
		boolean isAdmin = false;
		String role = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ROLES);
		if(role != null){
			if("admin".equalsIgnoreCase(role)){
				//user wants to act like admin: 
				//check whether the user is a real admin:
				isAdmin = UwsUtils.checkAdminUser(currentUser);
			}
		}
		
//		String ownerIdFilter = null;
//		String session = null;
//		if(!isAdmin){
//			ownerIdFilter = currentUser.getId();
//			session = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_SESSION);
//			if(session != null){
//				//to filter by session
//				currentUser.setSession(session);
//			}
//		}
		String ownerIdFilter = null;
		if(!isAdmin){
			ownerIdFilter = currentUser.getId();
		}
//		if(UwsUtils.isAnonymous(currentUser)){
//			//Only anonymous requries to filter by session id when retrieving jobs
//			//for anonymous: check flag filter by session
//			
//			session = currentUser.getSession();
//		} else {
//			session = null;
//		}
		
		String session = UwsUtils.getSessionFilterForUser(actionRequest, currentUser);

		UwsJobsFilter filter = UwsHandlersUtils.createFilter(actionRequest, ownerIdFilter, session);

//		//Check user is admin
//		String ownerIdFilter = null;
//		if(!UwsUtils.checkAdminUser(currentUser)){
//			//user is not admin: search is restricted to his/her jobs
//			ownerIdFilter = currentUser.getId();
//		}
		
		//jobid contains in this case, the list name
		String listName = actionRequest.getJobid();
		
		
		String limitStr = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_LIMIT);
		String offsetStr = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_OFFSET);
		String order = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_ORDER);
		
		Integer limit = null;
		Integer offset = null;
		if(limitStr!=null && !limitStr.trim().isEmpty()){
			limit=new Integer(limitStr);
		}
		if(offsetStr!=null && !offsetStr.trim().isEmpty()){
			offset=new Integer(offsetStr);
		}
		
		boolean metadataOnly = true;
		String metadataOnlyArg = actionRequest.getHttpParameter(UwsHandlersUtils.PARAMETER_METADATA_ONLY);
		if(metadataOnlyArg != null && !"".equals(metadataOnlyArg)){
			metadataOnly = Boolean.parseBoolean(metadataOnlyArg);
		}
		
		List<UwsJob> jobs = uwsManager.getJobsMetaList(listName, filter, limit, offset, order, metadataOnly);
		String baseUrl = actionRequest.getBaseUrl();
		Integer total = uwsManager.getJobsMetaListNumber(listName, filter);
		if (total == null) {
			if (jobs != null) {
				total = jobs.size();
			} else {
				total = 0;
			}
		}
		
		outputHandler.writeJobListResponseExtended(response, baseUrl, listName, jobs, limit, offset, total, order);

		
////		UwsJobsFilter filter = new UwsJobsFilter();
////		filter.setFilterByOwnerId(currentUser.getId(), false);
//		
//		//This method returns public (anonymous) jobs and the user jobs.
//		//Integer total = uwsManager.getJobListTotal(listName, currentUser);
//		List<UwsJob> jobs = uwsManager.getJobsMetaList(listName, currentUser, limit, offset, order);
//		//List<UwsJob> jobs = uwsManager.getJobsMetaList(listName, filter, limit, offset, order, metadataOnly);
//		String baseUrl = actionRequest.getBaseUrl();
//		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
//		int total = 0;
//		if(jobs != null){
//			total = jobs.size();
//		}
//		outputHandler.writeJobListResponse(response, baseUrl, listName, jobs, limit, offset, total, order );
	}

	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
}
