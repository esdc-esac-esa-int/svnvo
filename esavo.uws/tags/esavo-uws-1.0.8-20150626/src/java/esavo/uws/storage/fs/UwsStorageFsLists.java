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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.utils.UwsUtils;

/**
 * Each record:
 * <pre><tt>
 * jobid#owner
 * </tt></pre>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStorageFsLists {
	
	public static final String LIST_FILE_NAME = "list_jobs";
	private File baseListsDir;
	 

	public UwsStorageFsLists(File baseListsDir){
		this.baseListsDir = baseListsDir;
	}
	
	public synchronized void addJobToList(String jobid, String ownerid, String ownersession, String listid) throws IOException{
		File fList = new File(baseListsDir, getListFileName(listid));
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(fList, true));
			bw.append(createRecord(jobid, ownerid, ownersession)+'\n');
		}finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public synchronized List<String> getJobsForList(String listid, List<UwsOwnerSessionFilter> ownersFilter, String appid) throws IOException {
		File fList = new File(baseListsDir, getListFileName(listid));
		if(fList.exists()){
			BufferedReader br = null;
			List<String> jobs = new ArrayList<String>();
			String line;
			try{
				br = new BufferedReader(new FileReader(fList));
				while((line = br.readLine()) != null){
					if(validJobToAdd(line, ownersFilter, appid)){
						jobs.add(getJobFromJobListFile(line.trim()));
					}
				}
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return jobs;
		}else{
			return null;
		}
	}
	
	public synchronized boolean removeJobFromList(String jobid, String listid) throws IOException{
		File fList = new File(baseListsDir, getListFileName(listid));
		if(!fList.exists()){
			return false;
		}
		boolean removed = false;
		List<String> records = new ArrayList<String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(fList));
			String record;
			String jobidTmp;
			while((record = br.readLine()) != null){
				jobidTmp = getJobFromJobListFile(record);
				if(jobidTmp.equals(jobid)){
					removed = true;
				}else{
					records.add(record);
				}
			}
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException ioe){
					
				}
			}
		}
		if(removed){
			PrintWriter pw = null;
			try{
				pw = new PrintWriter(fList);
				for(String record: records){
					pw.println(record);
				}
			}finally{
				if(pw != null){
					try{
						pw.close();
					}catch(Exception ioe){
						
					}
				}
			}
		}
		return true;
	}

	
	private boolean validJobToAdd(String record, List<UwsOwnerSessionFilter> ownersFilter, String appid){
		//1. check owners filter
		if(!validJobToAdd(record, ownersFilter)){
			return false;
		}
		
		//2. check appid
		if(appid == null){
			return true;
		}else{
			String[] items = record.split("#");
			return UwsUtils.isJobValidForAppid(items[0].trim(), appid);
		}
	}

	private boolean validJobToAdd(String record, List<UwsOwnerSessionFilter> ownersFilter){
		String[] items = record.split("#");
		//return ownerids.contains(items[1].trim());
		String ownerid = items[1].trim();
		String sessionid = items[2].trim();
		for(UwsOwnerSessionFilter filter: ownersFilter){
			if(ownerid.equals(filter.getOwnerid())){
				//valid owner.
				//check session if available
				if(filter.hasSession()){
					//check session value
					return sessionid.equals(filter.getSessionid());
				}else{
					//valid owner, no session to compare, return true
					return true;
				}
			}
		}
		return false;
	}

	private String createRecord(String jobid, String ownerid, String session){
		if(session == null){
			session = "";
		}
		return jobid + '#' + ownerid + '#' + session;
	}
	
	private String getJobFromJobListFile(String record){
		String[] items = record.split("#");
		return items[0].trim();
	}
	
	
	private String getListFileName(String listid){
		return listid + "_" + LIST_FILE_NAME;
	}

}
