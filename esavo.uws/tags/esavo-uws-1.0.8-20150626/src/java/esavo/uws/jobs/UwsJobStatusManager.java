package esavo.uws.jobs;

import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsJobsListManager;
import esavo.uws.actions.UwsUploadResourceLoader;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.utils.UwsJobChangeType;
import esavo.uws.jobs.utils.UwsJobUpdateParameterType;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class UwsJobStatusManager {
	
	private static final Logger LOG = Logger.getLogger(UwsJobStatusManager.class.getName());
	
	private static final boolean INCREASE = true;
	private static final boolean DECREASE = false;
	
	private UwsJob job;
	private boolean enableUpdates;
	private UwsStorage storageManager;
	private UwsJobsListManager jobsListManager;
	private UwsEventsManager eventsManager;
	private boolean addedToRunningJobs;
	
	public UwsJobStatusManager(UwsJob job, UwsStorage storageManager, UwsJobsListManager jobsListManager, UwsEventsManager eventsManager){
		this.job = job;
		this.storageManager = storageManager;
		this.jobsListManager = jobsListManager;
		this.eventsManager = eventsManager;
		enableUpdates = true;
		addedToRunningJobs = false;
	}
	
	public void setEnableUpdates(boolean enable){
		this.enableUpdates = enable;
	}
	
	public boolean getEnableUpdates(){
		return enableUpdates;
	}
	
	public void updateStatusPhase(UwsJobChangeType type, UwsJobPhase oldPhase) throws UwsException{
		if (!enableUpdates) {
			return;
		}
		if(job.isPhaseFinished()){
			updateRunningJob(DECREASE);
			jobsListManager.removeRunningJob(job.getJobId());
			deleteTmpFilesIfRequired();
		}
		if(UwsJobPhase.EXECUTING == job.getPhase()){
			updateRunningJob(INCREASE);
		}
		storageManager.updateJobAttribute(job, UwsJobAttribute.Phase);
		eventsManager.setEventTime(job.getOwner(), UwsEventType.JOB_UPDATED_EVENT);
	}
	
	public void updateStatus(UwsJobChangeType updateType) throws UwsException {
		if (!enableUpdates) {
			return;
		}
		switch(updateType){
		case AddedErrorSummary:
			storageManager.addErrorSummaryMeta(job.getJobId(), job.getErrorSummary());
			break;
		case DestructionTime:
			storageManager.updateJobAttribute(job, UwsJobAttribute.DestructionTime);
			break;
		case EndTime:
			storageManager.updateJobAttribute(job, UwsJobAttribute.EndTime);
			break;
		case ExecDuration:
			storageManager.updateJobAttribute(job, UwsJobAttribute.ExecutionDuration);
			break;
		case SetQuote:
			storageManager.updateJobAttribute(job, UwsJobAttribute.Quote);
			break;
		case SetRunIdentifier:
			storageManager.updateJobAttribute(job, UwsJobAttribute.RunId);
			break;
		case StarTime:
			storageManager.updateJobAttribute(job, UwsJobAttribute.StarTime);
			break;
		case UpdatedAllParameters:
			storageManager.updateAllParameters(job);
			break;
		default:
			throw new UwsException("Invalid status to update: " + updateType.name());
		}
	}
	
	public boolean updateStatusResult(UwsJobResultMeta res) throws UwsException{
		if (!enableUpdates) {
			return false;
		}
		return storageManager.addJobResultMeta(job.getJobId(), res);
	}

	public boolean updateStatusParameter(String parameterName, UwsJobUpdateParameterType type) throws UwsException{
		if (!enableUpdates) {
			return false;
		}
		switch(type){
		case NewParameter:
			return storageManager.createParameter(job, parameterName);
		case RemovedParameter:
			return storageManager.removeParameter(job, parameterName);
		case UpdatedParameter:
			return storageManager.updateParameter(job, parameterName);
		case CreateOrUpdateParameter:
			return storageManager.createOrUpdateParameter(job, parameterName);
		default:
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unknown update parameter action: " + type.name());
		}
	}

	public void updateStatusError(String error, Exception e) {
		if (!enableUpdates) {
			return;
		}
		// Called when the thread was running and it was not able to execute some code.
		// Finish the job: avoid notifications (something went really wrong, we have no way to save the status)
		LOG.severe("Error when processing job: " + job + 
				" (This job will be terminated.)\n:" + error +
				(e == null ? "" : "\n"+e.getMessage()+"\n"+UwsUtils.dumpStackTrace(e)));
		job.setEnableUpdates(false);
		updateRunningJob(DECREASE);
		jobsListManager.stopAndRemoveRunningJob(job.getJobId(), storageManager);
	}
	
	/**
	 * This method will increase or decrease running jobs just once.
	 * @param increase
	 */
	private synchronized void updateRunningJob(boolean increase){
		if(increase){
			//increase
			if(!addedToRunningJobs){
				addedToRunningJobs = true;
				jobsListManager.increaseJobsCounter();
			}
		}else{
			//decrease
			if(addedToRunningJobs){
				addedToRunningJobs = false;
				jobsListManager.decreaseJobsCounter();
			}
		}
	}
	
	public synchronized void deleteTmpFilesIfRequired(){
		//currently, only upload resouces should be removed
		UwsUploadResourceLoader[] uploadLoaders = job.getArgs().getUploadResources();
		if(uploadLoaders != null){
			for(UwsUploadResourceLoader loader: uploadLoaders){
				loader.deleteFile();
			}
		}
	}

}
