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
package esavo.uws.utils.test.uws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsAbstractStorage;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class DummyUwsStorageManager implements UwsStorage {
	
	private UwsJob uwsJob;
	private String jobid;
	private String jobName;
	
	private UwsJobOwner jobOwner;
	private String ownerid;
	
	private boolean addJobMetaIfNeededFlag;
	
	private List<UwsJob> jobsList;
	private List<UwsJobOwner> ownersList;
	private List<UwsJobDetails> jobsToDestroy;
	private List<UwsJobDetails> olderJobs;
	//private List<UwsJobDetails> jobDetails;
	
	private String parameterId;
	private UwsJobAttribute attribute;
	private UwsJobResultMeta result;
	private String resultid;
	private UwsJobErrorSummaryMeta error;
	private boolean actionResult;
	private boolean jobExist;
	
	private int numJobs;
	private int numOwners;
	
	private String listName;
	private List<UwsOwnerSessionFilter> ownersFilters;
	private String appid;
	
	private File storageDir;
	//private File uploadDir;
	
	private UwsJobsOwnersFilter ownersFilter;
	private long offset;
	private long limit;
	private long currentTime;
	private String order;
	private boolean onlyMeta;
	private long deltaDestructionTime;
	
	private UwsJobsFilter jobsFilter;
	
	private int raiseExceptionCounter;
	
	private UwsNotificationItem uwsNotificationItem;
	private List<UwsNotificationItem> uwsNotificationItems;
	private List<String> notificationsList;
	private boolean changeJobName;

	public DummyUwsStorageManager(File storageDir){
		this.storageDir = storageDir;
		//this.uploadDir = uploadDir;
		reset();
	}
	
	public void reset(){
		jobsList = null;
		ownersList = null;
		jobsToDestroy = null;
//		jobDetails = null;
		uwsJob = null;
		jobid = null;
		jobOwner = null;
		ownerid = null;
		parameterId = null;
		attribute = null;
		error = null;
		actionResult = false;
		result = null;
		resultid = null;
		addJobMetaIfNeededFlag = false;
		numJobs = 0;
		numOwners = 0;
		listName = null;
		ownersFilters = null;
		appid = null;
		ownersFilter = null;
		offset = -1;
		limit = -1;
		currentTime = -1;
		jobsFilter = null;
		raiseExceptionCounter = -1;
		order = null;
		onlyMeta = false;
		deltaDestructionTime = -1;
		uwsNotificationItem = null;
		uwsNotificationItems = null;
		notificationsList = null;
		deltaDestructionTime = -1;
		jobName = null;
		changeJobName = false;
	}
	
	public void setJobsList(List<UwsJob> jobs){
		jobsList = jobs;
	}
	
	public void setJob(UwsJob job){
		this.uwsJob = job;
	}
	
	public UwsJob getJob(){
		return this.uwsJob;
	}
	
	public String getJobId(){
		return jobid;
	}
	
	public void setJobId(UwsJob job){
		if(job == null){
			jobid = null;
		}else{
			jobid = job.getJobId();
		}
	}
	
	public void setJobId(String jobid){
		this.jobid = jobid;
	}
	
	public void setJobOwner(UwsJobOwner owner){
		this.jobOwner = owner;
	}
	
	public void setOwnerId(String ownerid){
		this.ownerid = ownerid;
	}
	
	public void setOwnerId(UwsJobOwner owner){
		if(owner == null){
			ownerid = null;
		}else{
			ownerid = owner.getId();
		}
	}
	
	public String getOwnerId(){
		return this.ownerid;
	}

	public String getParameterId(){
		return parameterId;
	}
	
	public UwsJobAttribute getAttribute(){
		return attribute;
	}
	
	public void setActionResultFlag(boolean actionResult) {
		this.actionResult = actionResult;
	}

	public void setAddJobMetaIfNeeded(boolean addJobMetaIfNeededFlag){
		this.addJobMetaIfNeededFlag = addJobMetaIfNeededFlag;
	}
	
	public UwsJobResultMeta getJobResultMeta(){
		return result;
	}
	
	public String getResultId(){
		return resultid;
	}
	
	public UwsJobErrorSummaryMeta getJobError(){
		return error;
	}
	
	public void setNumJobs(int numJobs){
		this.numJobs = numJobs;
	}
	
	public void setNumOwners(int numOwners){
		this.numOwners = numOwners;
	}
	
	public String getListName() {
		return listName;
	}

	public List<UwsOwnerSessionFilter> getOwnersFilters() {
		return ownersFilters;
	}

	public String getAppId() {
		return appid;
	}
	
	public void setOwnersList(List<UwsJobOwner> ownersList){
		this.ownersList = ownersList;
	}
	
	public void setJobsToDestroy(List<UwsJobDetails> jobsToDestroy){
		this.jobsToDestroy = jobsToDestroy;
	}

	public void setOlderJobs(List<UwsJobDetails> olderJobs){
		this.olderJobs = olderJobs;
	}

//	public void setJobDetails(List<UwsJobDetails> jobDetails){
//		this.jobDetails = jobDetails;
//	}
	
	public void setJobsFilter(UwsJobsFilter jobsFilter){
		this.jobsFilter = jobsFilter; 
	}

	public File getErrorFile(UwsJob job){
		File f = getRealJobDir(job.getLocationId());
		f.mkdirs();
		File fr = new File(f, "error");
		return fr;
	}
	
	public File getResultFile(UwsJob job, String resultid){
		File f = getRealJobDir(job.getLocationId());
		f.mkdirs();
		File fr = new File(f, resultid);
		return fr;
	}
	
	public UwsJobsOwnersFilter getOwnersFilter(){
		return ownersFilter;
	}
	
	public long getOffset(){
		return offset;
	}
	
	public long getLimit(){
		return limit;
	}
	
	public long getCurrentTime(){
		return currentTime;
	}

	/**
	 * -1 disabled. 0 next action. n = counter (exception is raised when counter = 0)
	 * @param counter
	 */
	public void raiseExceptionAt(int counter){
		raiseExceptionCounter = counter;
	}

	private void raiseExceptionIfRequired() throws UwsException{
		if(raiseExceptionCounter < 0){
			//disabled
			return;
		}
		if(raiseExceptionCounter == 0){
			throw new UwsException("Requested");
		}
		raiseExceptionCounter--;
	}


	@Override
	public UwsJob getJobMeta(String jobid) throws UwsException {
		raiseExceptionIfRequired();
		setJobId(jobid);
		return this.uwsJob;
	}

	@Override
	public UwsJobOwner getOwner(String ownerid) throws UwsException {
		raiseExceptionIfRequired();
		setOwnerId(ownerid);
		return jobOwner;
	}

	@Override
	public UwsJobOwner getOwnerIfAvailable(String ownerid) throws UwsException {
		raiseExceptionIfRequired();
		return jobOwner;
	}

	@Override
	public void addOwner(UwsJobOwner owner) throws UwsException {
		raiseExceptionIfRequired();
		setJobOwner(owner);
		setOwnerId(owner);
	}

	@Override
	public boolean addNewJobMetaIfNeeded(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		setJob(job);
		setJobId(job);
		return addJobMetaIfNeededFlag;
	}

	@Override
	public List<UwsJob> searchByParameter(String parameterName, String value) throws UwsException {
		raiseExceptionIfRequired();
		return jobsList;
	}

	@Override
	public boolean createParameter(UwsJob job, String parameterid) throws UwsException {
		raiseExceptionIfRequired();
		this.parameterId = parameterid;
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public boolean createOrUpdateParameter(UwsJob job, String parameterid) throws UwsException {
		raiseExceptionIfRequired();
		this.parameterId = parameterid;
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public boolean updateParameter(UwsJob job, String parameterid) throws UwsException {
		raiseExceptionIfRequired();
		this.parameterId = parameterid;
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public void updateAllParameters(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
	}

	@Override
	public boolean removeParameter(UwsJob job, String parameterid) throws UwsException {
		raiseExceptionIfRequired();
		this.parameterId = parameterid;
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public boolean updateJobAttribute(UwsJob job, UwsJobAttribute attributeid) throws UwsException {
		raiseExceptionIfRequired();
		this.attribute = attributeid;
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public boolean addJobResultMeta(String jobid, UwsJobResultMeta res) throws UwsException {
		raiseExceptionIfRequired();
		result = res;
		setJobId(jobid);
		return actionResult;
	}

	@Override
	public boolean addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws UwsException {
		raiseExceptionIfRequired();
		error = errorSummary;
		setJobId(jobid);
		return actionResult;
	}

	@Override
	public int getNumJobs() throws UwsException {
		raiseExceptionIfRequired();
		return numJobs;
	}

	@Override
	public int getNumOwners() throws UwsException {
		raiseExceptionIfRequired();
		return numOwners;
	}

	@Override
	public Integer getNumberOfJobsByList(String listName,
			List<UwsOwnerSessionFilter> ownersFilter, String appid)
			throws UwsException {
		raiseExceptionIfRequired();
		this.listName = listName;
		this.ownersFilters = ownersFilter;
		this.appid = appid;
		return jobsList == null ? 0:jobsList.size();
	}

	@Override
	public List<UwsJob> getPendingJobs(String appid) throws UwsException {
		raiseExceptionIfRequired();
		return jobsList;
	}

	@Override
	public boolean removeJobOutputData(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public boolean removeJobMetaDataAndOutputData(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		setJob(job);
		setJobId(job);
		return actionResult;
	}

	@Override
	public File getStorageDir() {
		return storageDir;
	}

	@Override
	public File getRealJobDir(String joblocation) {
		return new File(storageDir, joblocation);
	}

	@Override
	public boolean createJobOutputDataDirIfNecessary(File fJobDir) {
		if(fJobDir.exists()){
			return false;
		}else{
			return fJobDir.mkdirs();
		}
	}

	@Override
	public OutputStream getJobResultsDataOutputStream(UwsJob job, String resultid) throws UwsException {
		raiseExceptionIfRequired();
		File fr = getResultFile(job, resultid);
		try {
			return new FileOutputStream(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new UwsException("cannot create output file", e);
		}
	}

	@Override
	public OutputStream getJobErrorDetailsDataOutputStream(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		File fr = getErrorFile(job);
		try {
			return new FileOutputStream(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new UwsException("cannot create output file", e);
		}
	}

	@Override
	public InputStream getJobResultDataInputSource(UwsJob job, String resultid) throws UwsException {
		raiseExceptionIfRequired();
		File fr = getResultFile(job, resultid);
		try {
			return new FileInputStream(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new UwsException("cannot create output file", e);
		}
	}

	@Override
	public InputStream getJobErrorDetailsDataInputSource(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		File fr = getErrorFile(job);
		try {
			return new FileInputStream(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new UwsException("cannot create output file", e);
		}
	}
	
	@Override
	public long getJobResultDataSize(UwsJob job, String resultid) throws UwsException {
		raiseExceptionIfRequired();
		return getResultFile(job, resultid).length();
	}

	@Override
	public long getJobErrorDetailsDataSize(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		return getErrorFile(job).length();
	}

	@Override
	public File getJobResultDataFile(UwsJob job, String resultid) throws UwsException {
		raiseExceptionIfRequired();
		return getResultFile(job, resultid);
	}

	@Override
	public File getJobErrorDetailsFile(UwsJob job) throws UwsException {
		raiseExceptionIfRequired();
		return getErrorFile(job);
	}

	@Override
	public File getUploadDir(UwsJobOwner owner) {
		return new File(getOwnerDir(owner), UwsAbstractStorage.UPLOAD_DIR_NAME);
	}

	@Override
	public File getOwnerDir(UwsJobOwner owner) {
		return new File(getStorageDir(), UwsUtils.getOwnerSubDir(owner.getId()));
	}

	@Override
	public void updateOwner(UwsJobOwner owner) throws UwsException {
		raiseExceptionIfRequired();
		this.jobOwner = owner;
		setOwnerId(owner);
		
	}

	@Override
	public void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException {
		raiseExceptionIfRequired();
		this.jobOwner = owner;
		this.parameterId = parameterName;
		setOwnerId(owner);
	}

	@Override
	public void updateOwnerRoles(UwsJobOwner owner) throws UwsException {
		raiseExceptionIfRequired();
		this.jobOwner = owner;
		setOwnerId(owner);
	}

	@Override
	public List<UwsJobOwner> retrieveOwners(UwsJobsOwnersFilter filter, long offset, long limit) throws UwsException {
		raiseExceptionIfRequired();
		this.ownersFilter = filter;
		this.offset = offset;
		this.limit = limit;
		return ownersList;
	}

	@Override
	public List<UwsJobDetails> getJobsToDestroy(String appid, long currentTime) throws UwsException {
		raiseExceptionIfRequired();
		this.appid = appid;
		this.currentTime = currentTime;
		return jobsToDestroy;
	}
	
	@Override
	public List<UwsJobDetails> getOlderJobs(String appid, UwsJobPhase phaseId,
			long time) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long calculateDbSize(String ownerid) throws UwsException {
		raiseExceptionIfRequired();
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long calculateFileSize(String ownerid) throws UwsException {
		raiseExceptionIfRequired();
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean checkJobExists(String jobid) throws UwsException {
		return jobExist;
	}
	
	public void setJobExists(boolean jobExist){
		this.jobExist = jobExist;
	}

	@Override
	public void createNotification(UwsNotificationItem uwsNotificationItem) throws UwsException {
		raiseExceptionIfRequired();
		this.uwsNotificationItem = uwsNotificationItem;
	}

	@Override
	public List<UwsNotificationItem> getNotificationsForUser(String userid) throws UwsException {
		raiseExceptionIfRequired();
		this.ownerid = userid;
		return uwsNotificationItems;
	}

	@Override
	public void deleteNotificationRelation(String userid, List<String> notificationid) throws UwsException {
		raiseExceptionIfRequired();
		this.ownerid = userid;
		this.notificationsList = notificationid;
	}

	@Override
	public int deleteNotifications(long currentTime, long deltaDestructionTime) throws UwsException {
		raiseExceptionIfRequired();
		this.currentTime = currentTime;
		this.deltaDestructionTime = deltaDestructionTime;
		return 0;
	}

	@Override
	public List<UwsJob> getJobsMetaByOwner(String ownerid) throws UwsException {
		this.ownerid = ownerid;
		return jobsList;
	}

	@Override
	public List<UwsJob> getJobsByFilter(String listName, UwsJobsFilter filter,
			String appid, Integer limit, Integer offset, String order,
			boolean onlyMeta) throws UwsException {
		raiseExceptionIfRequired();
		this.listName = listName;
		this.jobsFilter = filter;
		this.appid = appid;
		if(limit == null){
			this.limit = -1;
		}else{
			this.limit = limit;
		}
		if(offset == null){
			this.offset = -1;
		}else{
			this.offset = offset;
		}
		this.order = order;
		this.onlyMeta = onlyMeta;
		return jobsList;
	}
	
	@Override
	public boolean changeJobName(String jobid, String jobName)
			throws UwsException {
		raiseExceptionIfRequired();
		this.jobid = jobid;
		this.jobName = jobName;
		return changeJobName;
	}

	@Override
	public int updateParameterToJson() throws UwsException {
		raiseExceptionIfRequired();
		return 0;
	}

	@Override
	public void updateParameterToJson(String jobid) throws UwsException {
		raiseExceptionIfRequired();
	}

	/**
	 * @return the order
	 */
	public String getOrder() {
		return order;
	}
	
	/**
	 * @return the uwsNotificationItem
	 */
	public UwsNotificationItem getUwsNotificationItem() {
		return uwsNotificationItem;
	}

	/**
	 * @return the notificationsList
	 */
	public List<String> getNotificationsList() {
		return notificationsList;
	}

	public boolean getOnlyMeta(){
		return onlyMeta;
	}

	/**
	 * @return the deltaDestructionTime
	 */
	public long getDeltaDestructionTime() {
		return deltaDestructionTime;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @return the changeJobName
	 */
	public boolean getChangeJobName() {
		return changeJobName;
	}

	/**
	 * @param changeJobName the changeJobName to set
	 */
	public void setChangeJobName(boolean changeJobName) {
		this.changeJobName = changeJobName;
	}

	/**
	 * @return the jobsFilter
	 */
	public UwsJobsFilter getJobsFilter() {
		return jobsFilter;
	}

	@Override
	public Integer getJobsNumberByFilter(String listName, UwsJobsFilter filter, String appid) throws UwsException {
		this.listName = listName;
		this.jobsFilter = filter;
		this.appid = appid;
		return numJobs;
	}

	@Override
	public int deleteSystemWarningNotifications(List<String> notificationid)
			throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
