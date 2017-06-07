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
package esavo.uws.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esavo.uws.UwsException;
import esavo.uws.executor.UwsExecutorJobHandler;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobChangeType;
import esavo.uws.jobs.utils.UwsJobInitArgs;
import esavo.uws.jobs.utils.UwsJobRestartArgs;
import esavo.uws.jobs.utils.UwsJobUpdateParameterType;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public class UwsJob {
	
	public static final UwsJobPhase INITAL_PHASE = UwsJobPhase.PENDING;
	
	private String jobid;
	private String runid;
	private UwsJobOwner owner;
	private String listid;
	private UwsJobPhase phase;
	private long quote;
	private Date creationTime;
	private Date startTime;
	private Date endTime;
	private int priority;
	
	/**
	 * Max. running time in seconds
	 */
	private long executionDuration;
	
	/**
	 * Job real removal time (date-time when the job is removed from the system)
	 */
	private Date destructionTime;
	
	
	private String locationid;
	private String name;
	
	private UwsJobParameters parameters;
	private UwsJobErrorSummaryMeta errorSummary;
	private List<UwsJobResultMeta> results;
	
	private UwsJobStatusManager jobStatusManager;
	
	private String appid;
	
	private transient UwsJobInitArgs args;
	
	private transient UwsExecutorJobHandler executorJobHandler;
	
	/**
	 * New job
	 * @param args initial arguments
	 */
	public UwsJob(UwsJobInitArgs args){
		this.args = args;
		this.appid = args.getAppid();
		if(appid == null){
			throw new IllegalArgumentException("Application identifier cannot be null");
		}
		this.owner = args.getOwner();
		this.listid = args.getListid();
		this.priority = args.getPriority();
		this.jobid = UwsUtils.getUniqueJobIdentifier(appid);
		this.phase = INITAL_PHASE;
		this.locationid = UwsUtils.getJobResultSubDir(this, true);
		this.jobStatusManager = new UwsJobStatusManager(this, args.getStorage(), args.getJobsListManager(), args.getEventsManager());
		this.quote = -1;
		this.creationTime = new Date(System.currentTimeMillis());
		executorJobHandler = new UwsExecutorJobHandler();
	}
	
	/**
	 * Job from storage
	 * @param args job arguments (from storage)
	 */
	public UwsJob(UwsJobRestartArgs args){
		this.args = args;
		this.appid = args.getAppid();
		if(appid == null){
			throw new IllegalArgumentException("Application identifier cannot be null");
		}
		this.jobid = args.getJobid();
		if(jobid == null){
			throw new IllegalArgumentException("Job identifier cannot be null");
		}
		this.phase = INITAL_PHASE;
		this.owner = args.getOwner();
		this.listid = args.getListid();
		this.priority = args.getPriority();
		this.locationid = args.getLocationid();
		this.results = args.getResults();
		this.jobStatusManager = new UwsJobStatusManager(this, args.getStorage(), args.getJobsListManager(), args.getEventsManager());
		this.creationTime = new Date(args.getCreationTime());
		executorJobHandler = new UwsExecutorJobHandler();
	}
	
	public UwsJobStatusManager getStatusManager(){
		return jobStatusManager;
	}
	
	public void setEnableUpdates(boolean enable){
		jobStatusManager.setEnableUpdates(enable);
	}
	
	/**
	 * @return the id
	 */
	public String getJobId() {
		return jobid;
	}
	
	/**
	 * @return the runid
	 */
	public String getRunid() {
		return runid;
	}
	
	/**
	 * 
	 * @return the job name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets the job name
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * @param runid the runid to set
	 * @throws UwsException 
	 */
	public void setRunid(String runid) throws UwsException {
		this.runid = runid;
		jobStatusManager.updateStatus(UwsJobChangeType.SetRunIdentifier);
	}
	
	/**
	 * @return the ownerid
	 */
	public UwsJobOwner getOwner() {
		return owner;
	}
	
	/**
	 * @return the listid
	 */
	public String getListid() {
		return listid;
	}
	
	/**
	 * @return the phase
	 */
	public synchronized UwsJobPhase getPhase() {
		return phase;
	}
	
	/**
	 * Sets the job phase.<br/>
	 * This method is <b>not executed if</b> the job is <b>finished</b>. See {@link #isPhaseFinished()}
	 * @param phase the phase to set
	 * @throws UwsException 
	 */
	public synchronized void setPhase(UwsJobPhase phase) throws UwsException {
		if(isPhaseFinished()){
			//ignore
			return;
		}
		UwsJobPhase oldPhase = phase;
		this.phase = phase;
		jobStatusManager.updateStatusPhase(UwsJobChangeType.ExecutionPhase, oldPhase);
	}
	
	/**
	 * @return the quote
	 */
	public long getQuote() {
		return quote;
	}
	
	/**
	 * @param quote the quote to set
	 * @throws UwsException 
	 */
	public void setQuote(long quote) throws UwsException {
		this.quote = quote;
		jobStatusManager.updateStatus(UwsJobChangeType.SetQuote);
	}
	
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * @param startTime the startTime to set
	 * @throws UwsException 
	 */
	public void setStartTime(Date startTime) throws UwsException {
		this.startTime = startTime;
		jobStatusManager.updateStatus(UwsJobChangeType.StarTime);
	}
	
	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	
	/**
	 * @param endTime the endTime to set
	 * @throws UwsException 
	 */
	public void setEndTime(Date endTime) throws UwsException {
		this.endTime = endTime;
		jobStatusManager.updateStatus(UwsJobChangeType.EndTime);
	}
	
	/**
	 * @return the creation date
	 */
	public Date getCreationTime() {
		return creationTime;
	}
	
	/**
	 * @return the executionDuration
	 */
	public long getExecutionDuration() {
		return executionDuration;
	}
	
	/**
	 * @param executionDuration the executionDuration to set
	 * @throws UwsException 
	 */
	public void setExecutionDuration(long executionDuration) throws UwsException {
		this.executionDuration = executionDuration;
		jobStatusManager.updateStatus(UwsJobChangeType.ExecDuration);
	}
	
	/**
	 * @return the destructionTime
	 */
	public Date getDestructionTime() {
		return destructionTime;
	}
	
	/**
	 * @param destructionTime the destructionTime to set
	 * @throws UwsException 
	 */
	public void setDestructionTime(Date destructionTime) throws UwsException {
		this.destructionTime = destructionTime;
		jobStatusManager.updateStatus(UwsJobChangeType.DestructionTime);
	}
	
//	/**
//	 * @return the waitForStop
//	 */
//	public long getWaitForStop() {
//		return waitForStop;
//	}
//	
//	/**
//	 * @param waitForStop the waitForStop to set
//	 * @throws UwsException 
//	 */
//	public void setWaitForStop(long waitForStop) throws UwsException {
//		this.waitForStop = waitForStop;
//		jobStatusManager.notifyListeners(UwsJobChangeType.TimeToWait);
//	}
	
	/**
	 * @return the locationid
	 */
	public String getLocationId() {
		return locationid;
	}
	
	/**
	 * @return the parameters
	 */
	public UwsJobParameters getParameters() {
		return parameters;
	}
	
	/**
	 * @param parameters the parameters to set
	 * @throws UwsException 
	 */
	public void setParameters(UwsJobParameters parameters) throws UwsException {
		this.parameters = parameters;
		jobStatusManager.updateStatus(UwsJobChangeType.UpdatedAllParameters);
		parameters.setUwsJobStatusManager(jobStatusManager);
	}
	/**
	 * @return the errorSummary
	 */
	public UwsJobErrorSummaryMeta getErrorSummary() {
		return errorSummary;
	}
	
	/**
	 * @param errorSummary the errorSummary to set
	 * @throws UwsException 
	 */
	public void setErrorSummary(UwsJobErrorSummaryMeta errorSummary) throws UwsException {
		this.errorSummary = errorSummary;
		jobStatusManager.updateStatus(UwsJobChangeType.AddedErrorSummary);
	}
	
	/**
	 * @return the results
	 */
	public List<UwsJobResultMeta> getResults() {
		return results;
	}
	
	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}

	/**
	 * Returns 'true' if the current job phase is: 'COMPLETED', 'ABORTED' or 'ERROR'
	 * @return
	 */
	public boolean isPhaseFinished(){
		return phase == UwsJobPhase.COMPLETED || phase == UwsJobPhase.ABORTED || phase == UwsJobPhase.ERROR;
	}
	
	/**
	 * Returns 'true' if the job phase is 'aborted'
	 * @return
	 */
	public boolean isPhaseAborted(){
		return phase == UwsJobPhase.ABORTED;
	}
	
	/**
	 * Aborts the job
	 * @throws UwsException 
	 */
	public void cancel() throws UwsException{
		setEndTime(new Date());
		setPhase(UwsJobPhase.ABORTED);
	}
	
	/**
	 * Updates a job attribute. No phase status checks are performed.<br/>
	 * Only destruction time, execution duration and quote can be updated.<br/>
	 * @param attribute
	 * @param value
	 * @throws UwsException 
	 */
	public void updateAttribute(UwsJobAttribute attribute, Object value) throws UwsException{
		switch(attribute){
		case DestructionTime:
			setDestructionTime((Date)value);
			break;
		case Quote:
			setQuote((Long)value);
			break;
		case ExecutionDuration:
			setExecutionDuration((Long)value);
			break;
		default:
			throw new UwsException("Attribute '"+attribute.name()+"' (job '"+jobid+"') cannot be changed.");
		}
	}

	/**
	 * Adds parameters to this job. No phase status checks are performed.
	 * @param parameters
	 * @throws UwsException
	 */
	public void addOrUpdateParameters(Map<String,Object> parameters) throws UwsException{
		if(parameters == null){
			return;
		}
		for(Entry<String,Object> e: parameters.entrySet()){
			addOrUpdateParameter(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Adds or updates a parameter. No phase status checks are performed.
	 * @param parameterid
	 * @param value
	 * @throws UwsException
	 */
	public void addOrUpdateParameter(String parameterName, Object value) throws UwsException{
		UwsJobUpdateParameterType type;
		if(parameters.containsParameter(parameterName)){
			type = UwsJobUpdateParameterType.UpdatedParameter;
		} else {
			type = UwsJobUpdateParameterType.NewParameter;
		}
		parameters.setParameter(parameterName, value);
		jobStatusManager.updateStatusParameter(parameterName, type);
	}
	
	public Object removeParameter(String parameterName) throws UwsException{
		Object oldValue = parameters.removeParameter(parameterName);
		jobStatusManager.updateStatusParameter(parameterName, UwsJobUpdateParameterType.RemovedParameter);
		return oldValue;
	}
	
	public synchronized void addResult(UwsJobResultMeta result) throws UwsException{
		if(results == null){
			results = new ArrayList<UwsJobResultMeta>();
		}
		results.add(result);
		jobStatusManager.updateStatusResult(result);
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the args
	 */
	public UwsJobInitArgs getArgs() {
		return args;
	}
	
	/**
	 * @return the executorJobHandler
	 */
	public UwsExecutorJobHandler getExecutorJobHandler() {
		return executorJobHandler;
	}

	/**
	 * @param executorJobHandler the executorJobHandler to set
	 */
	public void setExecutorJobHandler(UwsExecutorJobHandler executorJobHandler) {
		this.executorJobHandler = executorJobHandler;
	}

	@Override
	public String toString(){
		return "jobid: " + jobid + " (runid: '"+runid+"')" +
				"\nCreation time: " + UwsUtils.formatDate(creationTime) + " ("+(creationTime == null ? "Not defined yet" : creationTime.getTime())+")" +
				"\nPriority: " + priority + 
				"\nPhase: " + phase.name() +
				"\nStart time: " + UwsUtils.formatDate(startTime) + " ("+(startTime == null ? "Not defined yet" : startTime.getTime())+")" +
				"\nEnd time: " + UwsUtils.formatDate(endTime) + " ("+(endTime == null ? "Not defined yet" : endTime.getTime())+")" +
				"\nDestruction time: " + UwsUtils.formatDate(destructionTime) + " ("+(destructionTime == null ? "Not defined yet" : destructionTime.getTime())+")" +
				"\nExecution duration: " + executionDuration + 
				"\nQuote: " + quote +
				"\nList: " + listid +
				"\nLocation identifier: " + locationid +
				"\nOwner: " + owner.toString() +
				"\nName: " + name +
				"\nParameters: " + (parameters != null ? parameters.toString() : " none" )+
				"\nResults: " + (results == null ? "None" : results.size()) +
				"\nError: " + (errorSummary == null ? "None" : errorSummary.toString());
	}

}
