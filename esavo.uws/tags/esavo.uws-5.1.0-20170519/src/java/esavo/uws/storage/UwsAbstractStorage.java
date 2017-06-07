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
package esavo.uws.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.event.UwsEventType;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsUtils;

public abstract class UwsAbstractStorage implements UwsStorage {
	
	private File storageDir;
	//private File uploadDir;
	private String appid;
	
	public static final String UPLOAD_DIR_NAME = "__UWS__upload__";
	
	public UwsAbstractStorage(File storageDir, String appid){
		this.appid = appid;
		this.storageDir = storageDir;
		if(!storageDir.exists()){
			if(!storageDir.mkdirs()){
				throw new IllegalArgumentException("Cannot create storage directory: " + storageDir.getAbsolutePath());
			}
		}
//		uploadDir = new File(storageDir, UPLOAD_DIR_NAME);
//		if(!uploadDir.exists()){
//			if(!uploadDir.mkdirs()){
//				throw new IllegalArgumentException("Cannot create upload directory: " + uploadDir.getAbsolutePath());
//			}
//		}
	}
	
	@Override
	public String getAppId(){
		return appid;
	}
	
	public File getStorageDir(){
		return storageDir;
	}
	
	@Override
	public File getRealJobDir(String joblocation){
		return new File(storageDir, joblocation);
	}

	@Override
	public boolean removeJobOutputData (UwsJob job) throws UwsException {
		File f = getRealJobDir(job.getLocationId());
		boolean somethingRemoved = false;
		if(f.exists()){
			File[] files = f.listFiles();
			for(File fTmp: files){
				//TODO ONLY "result" file is being counted for quota reduction
				if(fTmp.getName().equals(UwsJobResultMeta.DEFAULT_IDENTIFIER)){
					somethingRemoved = true;
					reduceUserQuota(job,fTmp.length());
				}
				fTmp.delete();
			}
			f.delete();
		}
		if(somethingRemoved){
			UwsManager.getInstance().getFactory().getEventsManager().setEventTime(job.getOwner(), UwsEventType.QUOTA_FILE_UPDATED_EVENT);
		}
		return true;
	}

	@Override
	public OutputStream getJobResultsDataOutputStream(UwsJob job, String resultid) throws UwsException {
		String jobLocation = job.getLocationId();
		File f = getRealJobDir(jobLocation);
		createJobOutputDataDirIfNecessary(f);
		File fData = new File(f, resultid);
		//FileOutputStream os;
		OutputStream os;
		try{
			//os = new FileOutputStream(fData);
			os = new BufferedOutputStream(new FileOutputStream(fData));
		}catch(IOException ioe){
			throw new UwsException("Cannot open results data file to write at: '" + fData.getAbsolutePath() + "'", ioe);
		}
		return os;
	}

	@Override
	public OutputStream getJobErrorDetailsDataOutputStream(UwsJob job) throws UwsException {
		String jobLocation = job.getLocationId();
		File f = getRealJobDir(jobLocation);
		createJobOutputDataDirIfNecessary(f);
		String errorid = getErrorId(job.getJobId());
		File fData = new File(f, errorid);
		FileOutputStream os;
		try{
			os = new FileOutputStream(fData);
		}catch(IOException ioe){
			throw new UwsException("Cannot open error details data file to write at: '" + fData.getAbsolutePath() + "'", ioe);
		}
		return os;
	}

	@Override
	public synchronized boolean createJobOutputDataDirIfNecessary(File fJobDir){
		if(fJobDir.exists()){
			return true;
		}
		return fJobDir.mkdirs();
	}


	@Override
	public synchronized InputStream getJobResultDataInputSource(UwsJob job, String resultid) throws UwsException {
		File fData = getJobResultDataFile(job, resultid);
		try {
			return new FileInputStream(fData);
		} catch (FileNotFoundException e) {
			throw new UwsException("Cannot find result '" + resultid
					+ "' for job '" + job.getJobId()
					+ "'. Path does not exists: " + fData.getAbsolutePath());
		}
	}
	
	@Override
	public synchronized InputStream getJobErrorDetailsDataInputSource(UwsJob job) throws UwsException {
		File fData = getJobErrorDetailsFile(job);
		try {
			return new FileInputStream(fData);
		} catch (FileNotFoundException e) {
			String errorid = getErrorId(job.getJobId());
			throw new UwsException("Cannot find error '" + errorid
					+ "' for job '" + job.getJobId()
					+ "'. Path does not exists: " + fData.getAbsolutePath());
		}
	}
	
	public String getErrorId(String jobid){
		return jobid + "_error";
	}
	
	@Override
	public long getJobResultDataSize(UwsJob job, String resultid) throws UwsException {
		return getJobResultDataFile(job, resultid).length();
	}
	
	@Override
	public long getJobErrorDetailsDataSize(UwsJob job) throws UwsException {
		return getJobErrorDetailsFile(job).length();
	}
	
	@Override
	public File getJobResultDataFile(UwsJob job, String resultid) throws UwsException{
		String jobLocation = job.getLocationId();
		File f = getRealJobDir(jobLocation);
		File fData = new File(f, resultid);
		return fData;
	}

	@Override
	public File getJobErrorDetailsFile(UwsJob job) throws UwsException{
		String jobLocation = job.getLocationId();
		File f = getRealJobDir(jobLocation);
		String errorid = getErrorId(job.getJobId());
		File fData = new File(f, errorid);
		return fData;
	}

	
	@Override
	public File getOwnerDir(UwsJobOwner owner){
		String ownerdir = UwsUtils.getOwnerSubDir(owner.getId());
		File f = new File(storageDir, ownerdir);
		return f;
	}
	
	@Override
	public File getUploadDir(UwsJobOwner owner){
		File ownerDir = getOwnerDir(owner);
		File f = new File(ownerDir, UPLOAD_DIR_NAME);
		return f;
		//return uploadDir;
	}
	
	private void reduceUserQuota(UwsJob job, long size) throws UwsException{
		UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(job.getOwner());
		quota.reduceFileSize(size);
	}
	
	@Override
	public String toString(){
		return "Storage directory: " + storageDir.getAbsolutePath();
	}
	
}
