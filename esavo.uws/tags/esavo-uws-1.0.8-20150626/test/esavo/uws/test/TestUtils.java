package esavo.uws.test;

import java.io.File;

import esavo.uws.jobs.UwsJob;

public class TestUtils {
	
	public static void initDirectory(File f){
		if(f == null){
			return;
		}
		removeDirectory(f);
		f.mkdirs();
	}
	
	public static void removeDirectory(File f){
		if(f == null){
			return;
		}
		if(f.isDirectory()){
			File[] fContent = f.listFiles();
			if(fContent != null){
				for(File fTmp: fContent){
					removeDirectory(fTmp);
				}
			}
		}
		f.delete();
	}
	
	public static void waitForJobFinished(UwsJob job){
		while(!job.isPhaseFinished()){
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				throw new RuntimeException("Wait interrupted", e);
			}
		}
	}


}
