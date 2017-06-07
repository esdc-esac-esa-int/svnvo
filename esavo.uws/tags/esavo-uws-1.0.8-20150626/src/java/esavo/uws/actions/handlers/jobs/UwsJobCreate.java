/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.uws.actions.handlers.jobs;


import java.io.File;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.actions.handlers.UwsActionHandler;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.QuotaException;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

/**
 * Handles <code>{job_list}?job_param1=param1...[phase=RUN]</code></br>
 * POST/GET: creates a new job to be launched.<br/>
 * 'sync': see Rec. 1.0, section 5.
 * 
 * <p>{job_list} is not 'sync':</br>
 * POST/GET: Response: 303 (see other). Location header must point to job: i.e. <code>{job_list}/{job_id}</code></br> 
 * 
 * <p>{job_list} is 'sync':</br>
 * POST/GET: (phase=RUN is ignored, the job will be launched)</br> 
 * POST/GET: Response: 303 (see other). Location header must point to job: i.e. <code>sync/{job_id}</code></br>
 * A results request will block the client until the job finish. 
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobCreate implements UwsActionHandler {
	
	public static final String ID = "job_create";
	
	public static final String PARAM_PRIORITY = "UWS_PRIORITY";
	public static final String SYNC_LIST = "sync";
	public static final String PARAM_PHASE = "PHASE";
	public static final String PHASE_RUN = "RUN"; 
	
	class Parameters {
		int priority;
		boolean hasPhaseRun;
	}

	@Override
	public String getActionHandlerIdentifer() {
		return ID;
	}

	@Override
	public String getActionName() {
		//No action associated to this handler
		return null;
	}

	@Override
	public boolean canHandle(String appid, UwsJobOwner owner, UwsActionRequest actionRequest) throws UwsException {
		if(!actionRequest.isGet() && !actionRequest.isPost()){
			return false;
		}
		if(!actionRequest.hasJobList()){
			return false;
		}
		if(actionRequest.hasJobId()){
			return false;
		}
		if(actionRequest.hasAction()){
			return false;
		}
		
		//Multipart can have no parameters at all (they are part of the multipart-form
		if(!actionRequest.hasHttpParameters() && !actionRequest.isMultipartContent()){
			return false;
		}
		
		
		
		return true;
	}
	
//	/**
//	 * package: test-harness
//	 * @return
//	 */
//	int getPriority(){
//		return priority;
//	}
//	
//	/**
//	 * package: test-harness
//	 * @return
//	 */
//	boolean hasPhaseRun(){
//		return hasPhaseRun;
//	}
	
	private Parameters getParameters(UwsActionRequest actionRequest) throws UwsException {
		Parameters actionParameters = new Parameters();
		if(actionRequest.hasHttpParameter(PARAM_PRIORITY)){
			String sPriority = actionRequest.getHttpParameter(PARAM_PRIORITY);
			try{
				actionParameters.priority = Integer.parseInt(sPriority);
			}catch(NumberFormatException nfe){
				throw new UwsException(
						UwsOutputResponseHandler.BAD_REQUEST, 
						"Wrong priority value: '"+sPriority+"'");
			}
		}else{
			actionParameters.priority = -1;
		}
		
		if(actionRequest.hasHttpParameter(PARAM_PHASE)){
			String sPhase = actionRequest.getHttpParameter(PARAM_PHASE);
			if(PHASE_RUN.equalsIgnoreCase(sPhase)){
				actionParameters.hasPhaseRun = true;
			}else{
				throw new UwsException(
						UwsOutputResponseHandler.BAD_REQUEST, 
						"Wrong phase value: '"+sPhase+"'. Only PHASE=RUN is allowed.");
			}
		}else{
			actionParameters.hasPhaseRun = false;
		}
		return actionParameters;
	}

	@Override
	public void handle(UwsManager uwsManager, UwsJobOwner currentUser, UwsActionRequest actionRequest, HttpServletResponse response) throws UwsException {
		Parameters actionParameters = getParameters(actionRequest);
		String listid = actionRequest.getJobListName();
		if(actionParameters.priority < 0){
			actionParameters.priority = uwsManager.getFactory().getScheduler().getDefaultPriority();
		}
		
		UwsStorage storage = uwsManager.getFactory().getStorageManager();
		UwsOutputResponseHandler outputHandler = uwsManager.getFactory().getOutputHandler();
		
		UwsJob job = uwsManager.getFactory().getCreator().createJob(currentUser, listid, actionParameters.priority);
		
		//Set exec limit if available.
		//By default, job has a '0' exec duration limit, which means: 'allowed to run for ever'
		UwsConfiguration config = uwsManager.getFactory().getConfiguration();
		String sExecDurationLimit = config.getProperty(UwsConfiguration.CONFIG_EXEC_DURATION_LIMIT);
		if(sExecDurationLimit != null && !"".equals(sExecDurationLimit)){
			job.setExecutionDuration(UwsUtils.getLong(sExecDurationLimit));
		}

		//check quota
		UwsQuota quota = null;
		int maxConfigurationFileSize = config.getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
		int maxFileSize = 0;
		quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(currentUser);
		maxFileSize = quota.getMinFileQuotaAvailable(maxConfigurationFileSize);

		checkQuota(quota);

		//get upload directory (sync: generic upload dir, async: user space)
		File uploadDir;
		if(SYNC_LIST.equals(listid)){
			//there is no job_id, use generic user upload directory (storage/user/__UWS__upload)
			uploadDir = storage.getUploadDir(currentUser);
		}else{
			//there is a job_id, use job directory
			//checkQuota(currentUser);
			uploadDir = storage.getRealJobDir(job.getLocationId());
		}
		
		if(!uploadDir.exists()){
			uploadDir.mkdirs();
		}
		
		//create parameters (multipart is parsed if required)
		job.setEnableUpdates(false);
		UwsJobParameters parameters= actionRequest.createJobParametersAsStrings(config, uploadDir, maxFileSize);
		job.setParameters(parameters);
		job.setEnableUpdates(true);

		job.getArgs().setUploadResources(actionRequest.getUploadResourceLoaders());

		String jobid = job.getJobId();
		if(SYNC_LIST.equals(listid)){
			actionParameters.hasPhaseRun = true;
			//Rec 1.0 section 5 describes the response must be a redirect:
			//TODO not implemented. The job is started
			//executeSyncJob(uwsManager, job, outputHandler, response);
			
			//The output is written to response directly
			job.getArgs().setResponse(response);
			
			//This method blocks this thread until the job is finished.
			uwsManager.executeSyncJob(job);
			
			//Remove output data just in case... 
			//when a job is finished, multipart loaded files are not removed: because notification is disabled for sync jobs.
			storage.removeJobOutputData(job);
			
			job.getStatusManager().deleteTmpFilesIfRequired();
		}else{
			//async.
			//any possible multipart upload will be saved in job results directory.
			//These files will be removed as part of the 'destroy' operation (as long as notifications are enabled)
			uwsManager.addJob(job);
			if(actionParameters.hasPhaseRun){
				uwsManager.startJob(jobid, currentUser);
			}
			String urlLocation = UwsOutputUtils.getJobHref(actionRequest.getBaseUrl(), listid, jobid);
			outputHandler.redirectResponse(response, urlLocation, null);
		}
	}


//	private void executeSyncJob(UwsManager uwsManager, UwsJob job, UwsOutputResponseHandler outputHandler, HttpServletResponse response) throws UwsException{
//		job.getArgs().setResponse(response);
//		//This method blocks this thread until the job is finished.
//		uwsManager.executeSyncJob(job);
//		UwsStorage storage = uwsManager.getFactory().getStorageManager();
//		InputStream source = null;
//		if(job.getErrorSummary() != null){
//			//error found
//			source = storage.getErrorDetailsDataInputSource(job);
//			try{
//				outputHandler.writeJobErrorDataResponse(response, job.getJobId(), job.getErrorSummary(), source);
//			}finally{
//				try{
//					source.close();
//				}catch(Exception e){
//					
//				}
//			}
//		}else{
//			//No error: get first result
//			UwsJobResultMeta result = job.getResults().get(0);
//			source = storage.getResultDataInputSource(job, result.getId());
//			try{
//				outputHandler.writeJobResultDataResponse(response, result, source);
//			}finally{
//				try{
//					source.close();
//				}catch(Exception e){
//					
//				}
//			}
//		}
//		//Delete tmp data
//		//FIXME In case the job is stored (implementation of redirect for sync),
//		//the right method should be: storage.removeJobMetaDataAndOutputData(job)
//		storage.removeJobOutputData(job);
//	}
	
	@Override
	public String toString(){
		return "Action handler: " + this.getClass().getName();
	}
	
//	private void checkQuota(UwsJobOwner owner) throws UwsException{
//		UwsQuota quota = null;
//		
//		try {
//			quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
//			quota.checkQuota();
//		} catch (QuotaException e) {
//			throw new UwsException(UwsOutputResponseHandler.QUOTA_EXCEEDED,e.getMessage(),e);
//		}
//
//	}
	
	
	private void checkQuota(UwsQuota quota) throws UwsException{
		try {
			quota.checkQuota();
		} catch (QuotaException e) {
			throw new UwsException(UwsOutputResponseHandler.QUOTA_EXCEEDED,e.getMessage(),e);
		}
	}

}
