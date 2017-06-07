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
package esavo.uws.utils;

import java.io.File;

public class DirectoryRemoval extends Thread {
	
	private static final int MAX_NUM_TRIES = 10;
	private static final long WAIT_TIME = 500;
	
	private File base;
	private int maxNumTries;
	private long waitTime;
	
	public DirectoryRemoval(File f){
		this(f, MAX_NUM_TRIES, WAIT_TIME);
	}

	public DirectoryRemoval(File f, int maxRemovalTries, long waitTime){
		base = f;
		this.maxNumTries = maxRemovalTries;
		this.waitTime = waitTime;
	}

	public void run(){
		deleteDirs(base);
	}
	
	private boolean deleteDirs(File f){
		if(isInterrupted()){
			return false;
		}
		if(!f.exists()){
			return true;
		}
		if(f.isDirectory()){
			for(File ftmp: f.listFiles()){
				if(!deleteDirs(ftmp)){
					return false;
				}
			}
		}
		if(!f.delete()){
			//sometimes, you have a '.nfsxxxxx' file that appears after you remove a file 
			retryRemoval(f);
		}
		return true;
	}
	
	private void retryRemoval(File f){
		for(int i = 0; i < maxNumTries; i++){
			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				break;
			}
			if(f.delete()){
				return;
			}
		}
		System.out.println("Cannot remove: " + f.getAbsolutePath());
	}

}
