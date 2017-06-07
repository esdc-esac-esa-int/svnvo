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
import java.util.Date;

import esavo.uws.UwsException;
import esavo.uws.utils.UwsUtils;

public class UwsStorageFsUtils {

	public static long getLong(String s, String msg) throws IOException{
		try {
			return UwsUtils.getLong(s);
		} catch (UwsException e) {
			throw new IOException("Invalid value '"+s+"': " + msg);
		}
	}
	
	public static int getInteger(String s, String msg) throws IOException{
		try {
			return UwsUtils.getInt(s);
		} catch (UwsException e) {
			throw new IOException("Invalid value '"+s+"': " + msg);
		}
	}

	public static String getDateToSave(Date d){
		if(d == null){
			return "0";
		}else{
			return ""+d.getTime();
		}
	}
	
	public static Date getDateFromStorage(long milliSeconds){
		if(milliSeconds == 0){
			return null;
		}else{
			return new Date(milliSeconds);
		}
	}
	
	public static String getStringForProperty(String value){
		if(value == null){
			return "__null__";
		}else{
			return value;
		}
	}
	
	public static String getStringFromProperty(String value){
		if("__null__".equals(value)){
			return null;
		}else{
			return value;
		}
	}
	
	public static void deleteDirectory(File f){
		if(!f.exists()){
			return;
		}
		if(f.isDirectory()){
			File[] fItems = f.listFiles();
			for(File fTmp: fItems){
				deleteDirectory(fTmp);
			}
		}
		f.delete();
	}
	
}
