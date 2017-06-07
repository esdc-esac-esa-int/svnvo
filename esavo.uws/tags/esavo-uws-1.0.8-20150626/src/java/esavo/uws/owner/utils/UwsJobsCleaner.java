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
package esavo.uws.owner.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.actions.handlers.jobs.UwsJobCreate;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsAbstractStorage;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

/**
 * This class removes old empty directories and orphans jobs (jobs that exist in files and do not exist in database)<br/>
 * Empty directories removal:<br/>
 * <ul>
 * <li>A</li>
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobsCleaner {
	
	private static final Logger LOG = Logger.getLogger(UwsJobsCleaner.class.getName());
	
	
	/**
	 * Clean orphan jobs and old empty directories of the specified user.
	 * @param owner
	 * @param appid
	 * @param deltaTime amount of time in order to determine whether a directory is old (i.e. 3 days) in ms.
	 * @return true if something is removed.
	 * @throws UwsException
	 */
	public static boolean cleanOwner(UwsStorage storage, UwsJobOwner owner, String appid, long deltaTime) throws UwsException{
		//update owner quota (if not already updated)
		UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
		//get user dir
		//File ownerDir = new File(storage.getStorageDir(), UwsUtils.getOwnerSubDir(owner.getId()));
		File ownerDir = storage.getOwnerDir(owner);
		//Remove orphans
		boolean removed = cleanOrphans(owner, storage, ownerDir, appid);
		//Remove empty dirs
		if(cleanOldEmptyDirectories(owner, storage, ownerDir, appid, deltaTime)){
			removed = true;
		}
		return removed;
	}
	
	/**
	 * Checks for jobs that do not exist in database.<br/>
	 * A job is created in storage before the directory is created 
	 * (see {@link UwsJobCreate#handle(UwsManager, UwsJobOwner, esavo.uws.actions.UwsActionRequest, javax.servlet.http.HttpServletResponse)}} and
	 * {@link UwsManager#addJob(esavo.uws.jobs.UwsJob)}).
	 * So, if a job directory exists and the job does not exist in storage, the directory can be removed.
	 * @param owner
	 * @param storage
	 * @param ownerDir
	 * @throws UwsException 
	 */
	private static boolean cleanOrphans(UwsJobOwner owner, UwsStorage storage, File ownerDir, String appid) throws UwsException{
		List<File> files = findJobsDirs(ownerDir, appid);
		if(files == null){
			return false;
		}
		String id;
		boolean exists;
		long size;
		boolean somethingRemoved = false;
		for(File f: files){
			id = f.getName();
			if(id.endsWith(appid)){
				//it is a job
				exists = storage.checkJobExists(id);
				if(!exists){
					//the job does not exist in database => orphan
					size = UwsUtils.clearDirectory(f);
					restoreUserQuota(owner, size);
					somethingRemoved = true;
				}
			}
		}
		return somethingRemoved;
	}
	
	private static List<File> findJobsDirs(File ownerDir, String appid){
		List<File> jobsDirs = new ArrayList<File>();
		checkJobsDir(jobsDirs, ownerDir, appid);
		return jobsDirs;
	}
	
	private static List<File> findEmptyDirs(File ownerDir){
		List<File> emptyDirs = new ArrayList<File>();
		checkEmptyDir(emptyDirs, ownerDir);
		return emptyDirs;
	}
	
	private static void checkEmptyDir(List<File> emptyDirs, File f){
		if(!f.exists()){
			return;
		}
		if(f.isDirectory()){
			if(f.getName().equals(UwsAbstractStorage.UPLOAD_DIR_NAME)){
				return;
			}
			File[] files = f.listFiles();
			if(files == null){
				emptyDirs.add(f);
				return;
			}
			if(files.length == 0){
				emptyDirs.add(f);
				return;
			}
			for(File fTmp: files){
				checkEmptyDir(emptyDirs, fTmp);
			}
		}
	}

	private static void checkJobsDir(List<File> jobsDirs, File f, String appid) {
		if (!f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			if(f.getName().equals(UwsAbstractStorage.UPLOAD_DIR_NAME)){
				return;
			}
			if (f.getName().endsWith(appid)) {
				jobsDirs.add(f);
				return;
			} else {
				File[] files = f.listFiles();
				if (files == null) {
					return;
				}
				if (files.length == 0) {
					return;
				}
				for (File fTmp : files) {
					checkJobsDir(jobsDirs, fTmp, appid);
				}
			}
		}
	}

	/**
	 * 
	 * @param owner
	 * @param storage
	 * @param ownerDir
	 * @throws UwsException 
	 */
	private static boolean cleanOldEmptyDirectories(UwsJobOwner owner, UwsStorage storage, File ownerDir, String appid, long deltaTime) throws UwsException{
		List<File> files = findEmptyDirs(ownerDir);
		if(files == null){
			return false;
		}
		String id;
		boolean exists;
		long size;
		boolean somethingRemoved = false;
		for(File f: files){
			id = f.getName();
			if(id.endsWith(appid)){
				//it is a job
				exists = storage.checkJobExists(id);
				if(!exists){
					//job does not exist in storage. It is empty
					//update quota
					size = UwsUtils.clearDirectory(f);
					somethingRemoved = true;
					restoreUserQuota(owner, size);
					removeOldEmptyDir(f.getParentFile(), ownerDir, deltaTime, appid);
				}else{
					//Ignore: it can be removed using GUI
				}
			}else{
				removeOldEmptyDir(f, ownerDir, deltaTime, appid);
			}
		}
		return somethingRemoved;
	}
	
	private static void removeOldEmptyDir(File dir, File baseDir, long deltaTime, String appid){
		if(dir.getPath().equals(baseDir.getAbsolutePath())){
			//top: do not remove anyway
			return;
		}
		//if dir name is a job: remove it
		if(dir.getName().endsWith(appid)){
			dir.delete();
			return;
		}
		//it is a directory date, like 2014/06/25
		//dir = 2014/06/25
		long date = UwsUtils.getDirectoryTime(dir);
		if(date < 0){
			//wrong format
			return;
		}
		long currentDate = System.currentTimeMillis();
		long offset = currentDate - date;
		if(offset > 0){
			if(offset > deltaTime){
				//remove it
				dir.delete();
				//check parent: 2014/06
				File parent = dir.getParentFile();
				if(isEmpty(parent)){
					parent.delete();
				}
				//check parent: 2014
				parent = parent.getParentFile();
				if(isEmpty(parent)){
					parent.delete();
				}
			}
		}
	}
	
	
	private static boolean isEmpty(File dir){
		if(dir == null){
			return false;
		}
		File[] files = dir.listFiles();
		if(files == null){
			return true;
		}
		return files.length == 0;
	}
	
	
	
	private static void restoreUserQuota(UwsJobOwner owner, long size) throws UwsException{
		UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
		quota.reduceFileSize(size);
	}


}
