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
package esavo.uws.storage.fs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.creator.UwsCreator;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsAbstractStorage;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

/**
 * Storage manager based on files.<br/>
 * Metadata directory contains owners (a subdirectory) and each owner contains its data and the jobs (identifiers) that belong to that user.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStorageFileSystem extends UwsAbstractStorage implements UwsStorage {
	
	private File metadataDir;
	private UwsStorageFsOwners owners;
	private UwsStorageFsJobs jobsMeta;
	private UwsStorageFsLists lists;
	private UwsStorageFsPendingJobs pendingJobs;
	private String appid;

	public UwsStorageFileSystem(String appid, File storageDir, UwsCreator creator) {
		super(storageDir, appid);
		metadataDir = new File(getStorageDir(), "_metadata");
		if(!metadataDir.exists()){
			metadataDir.mkdirs();
		}
		File fLists = new File(metadataDir, "lists");
		if(!fLists.exists()){
			fLists.mkdirs();
		}
		lists = new UwsStorageFsLists(fLists);
		File fOwners = new File(metadataDir, "owners");
		if(!fOwners.exists()){
			fOwners.mkdirs();
		}
		owners = new UwsStorageFsOwners(fOwners);
		File fJobs = new File(metadataDir, "jobs");
		if(!fJobs.exists()){
			fJobs.mkdirs();
		}
		jobsMeta = new UwsStorageFsJobs(fJobs, owners, creator);
		pendingJobs = new UwsStorageFsPendingJobs(metadataDir);
	}
	
	public String getAppId(){
		return appid;
	}

	@Override
	public UwsJob getJobMeta(String jobid) throws UwsException {
		try {
			return jobsMeta.loadJobMeta(jobid);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}
	
	@Override
	public boolean checkJobExists(String jobid) throws UwsException {
		try {
			return jobsMeta.checkJobExists(jobid);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}


	@Override
	public UwsJobOwner getOwner(String ownerid) throws UwsException {
		try {
			UwsJobOwner owner = owners.loadOwner(ownerid);
			if(owner == null){
				throw new UwsException("Owner '"+ownerid+"' not found.");
			}
			return owner;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}
	
	@Override
	public UwsJobOwner getOwnerIfAvailable(String ownerid) throws UwsException {
		try {
			return owners.loadOwner(ownerid);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	
	@Override
	public List<UwsJobOwner> retrieveOwners(UwsJobsOwnersFilter filter, long offset, long limit) throws UwsException {
		try {
			return owners.retrieveOwners(filter, offset, limit);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public void addOwner(UwsJobOwner owner) throws UwsException {
		try {
			owners.addOwner(owner);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public void updateOwner(UwsJobOwner owner) throws UwsException {
		try {
			owners.updateOwner(owner);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}
	
	@Override
	public void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException {
		try {
			owners.updateOwnerParameter(owner, parameterName);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public void updateOwnerRoles(UwsJobOwner owner) throws UwsException {
		try {
			owners.updateOwnerRoles(owner);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}



//	@Override
//	public UwsJobParameters getParameters(String jobid) throws UwsException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<UwsJobResult> getResults(String jobid) throws UwsException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public UwsJobError getErrorSummary(String jobid) throws UwsException {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public String getJobLocation(UwsJob jobid) throws UwsException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean addNewJobMetaIfNeeded(UwsJob job) throws UwsException {
		try {
			boolean ret = jobsMeta.addNewJobMetaIfNeeded(job);
			if(ret){
				//Update jobs list file.
				lists.addJobToList(job.getJobId(), job.getOwner().getId(), job.getOwner().getSession(), job.getListid());
			}
			return ret;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

//	@Override
//	public boolean updateJobPhase(UwsJob job) throws UwsException {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public List<UwsJob> searchByParameter(String parameterName, String value) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateAllParameters(UwsJob job) throws UwsException {
		try {
			jobsMeta.updateAllParameters(job);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean removeParameter(UwsJob job, String parameterid) throws UwsException {
		try {
			jobsMeta.updateAllParameters(job);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean updateParameter(UwsJob job, String parameterid) throws UwsException {
		try {
			jobsMeta.updateAllParameters(job);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean createParameter(UwsJob job, String parameterid) throws UwsException {
		try {
			jobsMeta.updateAllParameters(job);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean createOrUpdateParameter(UwsJob job, String parameterid) throws UwsException {
		try {
			jobsMeta.updateAllParameters(job);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean updateJobAttribute(UwsJob job, UwsJobAttribute attributeid) throws UwsException {
		try {
			jobsMeta.updateJobAttribute(job);
			if(attributeid == UwsJobAttribute.Phase){
				if(UwsUtils.isJobPending(job.getPhase())){
					pendingJobs.addPendingJobIfRequired(job.getJobId());
				} else {
					pendingJobs.removePendingJob(job.getJobId());
				}
			}
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean addJobResultMeta(String jobid, UwsJobResultMeta res) throws UwsException {
		try {
			jobsMeta.addJobResultMeta(jobid, res);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public boolean addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws UwsException {
		try {
			jobsMeta.addErrorSummaryMeta(jobid, errorSummary);
			return true;
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public int getNumJobs() throws UwsException {
		return jobsMeta.getNumJobs();
	}

	@Override
	public int getNumOwners() throws UwsException {
		return owners.getNumOwners();
	}

	@Override
	public List<UwsJob> getJobsByOwner(String ownerid) throws UwsException {
		try {
			List<String> jobIds = owners.getJobs(ownerid);
			List<UwsJob> jobs = new ArrayList<UwsJob>();
			for (String id : jobIds) {
				jobs.add(jobsMeta.loadJobMeta(id));
			}
			return jobs;
		} catch (IOException ioe) {
			throw new UwsException("Input/Output error", ioe);
		}
	}

	@Override
	public List<UwsJob> getJobsByList(String listName, List<UwsOwnerSessionFilter> ownersFilter, String appid) throws UwsException {
		try {
			List<String> jobIds = lists.getJobsForList(listName, ownersFilter, appid);
			if(jobIds == null){
				return null;
			}
			List<UwsJob> jobs = new ArrayList<UwsJob>();
			for (String id : jobIds) {
				jobs.add(jobsMeta.loadJobMeta(id));
			}
			return jobs;
		} catch (IOException ioe) {
			throw new UwsException("Input/Output error", ioe);
		}
	}

	@Override
	public List<UwsJob> getPendingJobs() throws UwsException {
		try{
			List<String> jobids = pendingJobs.getPendingJobs();
			if(jobids == null){
				return null;
			}
			List<UwsJob> jobs = new ArrayList<UwsJob>();
			for(String jobid: jobids){
				UwsJob job = jobsMeta.loadJobMeta(jobid);
				jobs.add(job);
			}
			return jobs;
		}catch(IOException ioe){
			throw new UwsException("Input/Output error", ioe);
		}
	}

	@Override
	public boolean removeJobOutputData(UwsJob job) throws UwsException {
		String jobid = job.getJobId();
		try{
			pendingJobs.removePendingJob(jobid);
			lists.removeJobFromList(jobid, job.getListid());
			jobsMeta.removeJobOutputMeta(jobid);
			super.removeJobOutputData(job);
			return true;
		}catch(IOException ioe){
			throw new UwsException("Input/Output error", ioe);
		}
	}

	@Override
	public boolean removeJobMetaDataAndOutputData(UwsJob job) throws UwsException {
		String jobid = job.getJobId();
		try{
			pendingJobs.removePendingJob(jobid);
			lists.removeJobFromList(jobid, job.getListid());
			jobsMeta.removeJobAndAssociatedMeta(jobid);
			super.removeJobOutputData(job);
			return true;
		}catch(IOException ioe){
			throw new UwsException("Input/Output error", ioe);
		}
	}

	@Override
	public String toString(){
		return "File storage system:"+
				"\n" + super.toString();
	}

	@Override
	public List<UwsJobDetails> retrieveJobsByFilter(UwsJobsFilter filter, long offset, long limit) throws UwsException {
		try {
			return jobsMeta.retrieveJobsByFilter(filter, offset, limit);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public List<String> getOldJobs(String appid, long currentTime) throws UwsException {
		try {
			return jobsMeta.retrieveOldJobs(appid, currentTime);
		} catch (IOException e) {
			throw new UwsException("Input/Output error", e);
		}
	}

	@Override
	public long calculateDbSize(String ownerid) throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long calculateFileSize(String ownerid) throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void createNotification(UwsNotificationItem uwsNotificationItem) throws UwsException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<UwsNotificationItem> getNotificationsForUser(String userid) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteNotificationRelation(String userid, List<String> notificationids) throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int deleteNotifications(long currentTime, long deltaDestructionTime)	throws UwsException {
		// TODO Auto-generated method stub
		return 0;
	}






	
}
