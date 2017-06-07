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
