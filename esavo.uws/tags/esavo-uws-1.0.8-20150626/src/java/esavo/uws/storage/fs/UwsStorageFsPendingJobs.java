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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class UwsStorageFsPendingJobs {
	
	private File pendingJobsFile;

	public UwsStorageFsPendingJobs(File baseDir){
		pendingJobsFile = new File(baseDir, "pending_jobs");
	}
	
	public synchronized void addPendingJobIfRequired(String jobid) throws IOException{
		List<String> pendingJobs = getPendingJobs();
		if(pendingJobs == null){
			pendingJobs = new ArrayList<String>();
		} else {
			if(pendingJobs.contains(jobid)){
				return;
			}
		}
		pendingJobs.add(jobid);
		savePendingJobs(pendingJobs);
//		BufferedWriter bw = null;
//		try{
//			bw = new BufferedWriter(new FileWriter(pendingJobsFile, true));
//			bw.append(jobid+'\n');
//		}finally {
//			if (bw != null) {
//				try {
//					bw.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	public synchronized boolean removePendingJob(String jobid) throws IOException{
		List<String> pendingJobs = getPendingJobs();
		if(pendingJobs == null){
			return false;
		}
		if(pendingJobs.remove(jobid)){
			//pending job removed => update list
			savePendingJobs(pendingJobs);
			return true;
		}else{
			return false;
		}
	}
	
	public synchronized List<String> getPendingJobs() throws IOException {
		if(!pendingJobsFile.exists()){
			return null;
		}
		BufferedReader br = null;
		try{
			List<String> pendingJobs = new ArrayList<String>();
			String line;
			br = new BufferedReader(new FileReader(pendingJobsFile));
			while((line = br.readLine()) != null){
				pendingJobs.add(line);
			}
			return pendingJobs;
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void savePendingJobs(List<String> jobs) throws IOException {
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(pendingJobsFile);
			for(String job: jobs){
				pw.println(job);
			}
		}finally {
			if (pw != null) {
				pw.close();
			}
		}

	}

}
