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
