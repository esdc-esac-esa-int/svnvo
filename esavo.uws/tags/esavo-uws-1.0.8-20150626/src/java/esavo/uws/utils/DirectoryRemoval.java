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
