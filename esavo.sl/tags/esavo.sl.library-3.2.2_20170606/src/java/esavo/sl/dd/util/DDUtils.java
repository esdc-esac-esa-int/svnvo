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
package esavo.sl.dd.util;

// Import from Java packages
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esac.archive.absi.common.countryresolver.CountryResolver;
import esavo.sl.dd.requests.DDProperties;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputUtils;
import esavo.uws.utils.UwsUtils;




//log4j classes
import org.apache.log4j.Logger;

public class DDUtils {
    
    static Logger logger = Logger.getLogger(DDUtils.class);
    
    private static String ASTERIX      = "*";
    private static String QUESTIONMARK = "?";
    
    private DDUtils() {}
    
    
    
    public static List<DDFilePath> findFiles(String parent, List<DDFilePath> pathsFiles) throws Exception{
    	if(pathsFiles == null){
    		return null;
    	}
        
        Iterator<DDFilePath> it = pathsFiles.iterator();

        List<DDFilePath> outputFilesSet = new ArrayList<DDFilePath>();
        List<DDFilePath> tempFilesSet   = new ArrayList<DDFilePath>();
        
        while(it.hasNext()) {
            
            DDFilePath filePath = (DDFilePath) it.next();
            
            
            if(filePath.getType() == DDFilePath.FILELOCATION) {
                
                outputFilesSet.add(filePath);
                
            } else if(filePath.getType() == DDFilePath.PATHPATTERN) {
                
            	tempFilesSet	= DDSystemUtils.findFiles(parent, filePath);
                outputFilesSet 	= addHashSetToHashSet(outputFilesSet,tempFilesSet);
                
            }
        }
        
        return outputFilesSet;
    }
    
    /** Removes the ../../ from a path going to upper directories **/
    public static String fileConversion(String fileString) {
        
        StringTokenizer st = new StringTokenizer(fileString, File.separator);
        int ii = 0;
        List<String> stVector = new ArrayList<String>();
        
        while(st.hasMoreTokens()) {
            String thisToken = (String) st.nextToken();
            
            if(! thisToken.equals("..")) {
                stVector.add(ii,(String) thisToken);
                ii++;
            } else {
                ii--;
            }
        }
        
        String outputString = "";
        
        for(int index=0 ; index < ii ; index++) {
            outputString = outputString + File.separator + stVector.get(index);
        }
        
        return outputString;
    }
    
    /** Removes the ../../ from a path going to upper directories **/
    public static DDFilePath fileConversion(DDFilePath filePath) {
        
        StringTokenizer st = new StringTokenizer(filePath.getPath(), File.separator);
        int ii = 0;
        List<String> stVector = new ArrayList<String>();
        
        while(st.hasMoreTokens()) {
            String thisToken = (String) st.nextToken();
            
            if(! thisToken.equals("..")) {
                stVector.add(ii, thisToken);
                ii++;
            } else {
                ii--;
            }
        }
        
        
        String outputFilePath_path = "";
        
        for(int index=0 ; index < ii ; index++) {
            outputFilePath_path = outputFilePath_path + File.separator + stVector.get(index);
        }
        DDFilePath outputFilePath = new DDFilePath(filePath.getRetrievalId(),outputFilePath_path,DDFilePath.FILELOCATION);
        
        return outputFilePath;
    }
    
    /** This method removes the aux files from the hashset when the user
     * does not want them
     **/
    public static List<String> removeAuxFiles(List<String> oldFilesSet) {
        
        String auxPath = File.separator + "aux" + File.separator;
        List<String> newFilesSet = new ArrayList<String>();
        
        Iterator<String> it = oldFilesSet.iterator();
        
        while(it.hasNext()) {
            
            String currentFile = it.next();
            if(currentFile.indexOf(auxPath) == -1){
                newFilesSet.add(currentFile);
            }
        }
        
        return newFilesSet;
    }
    
    
    /** This method add the content of the tempSet HashSet to a
     * finalSet HashSet
     **/
    public static List<DDFilePath> addHashSetToHashSet(List<DDFilePath> finalSet, List<DDFilePath> tempSet) {
        if(tempSet.isEmpty()){
        	return finalSet;
        } else {
	        finalSet.addAll(tempSet);
	        return finalSet;
        }
    }
    
    /** This method add the content of the tempList HashSet to a
     * finalList HashSet
     **/
    public static List<String> addListToList(List<String> finalList, List<String> tempList) {
        if(tempList.isEmpty()){
        	return finalList;
        }
        finalList.addAll(tempList);
        return finalList;
    }
    
    /** This method add the content of a File Array to a Hashset
     * Used by findfiles **/
    public static List<File> addArrayToHashSet(List<File> output, File[] inputArray) {
		if (inputArray == null) {
			return output;
		}
		for (File f: inputArray) {
			output.add(f);
		}
		return output;
    }
    
    /** This method substitute a Unix-like pattern string to a
     * perl-like pattern string to be recognised by the regex packet **/
    public static String wildcardSubst_old(String inputString) {
        
        String outputString = "";
        
        if(inputString == null){
        	return outputString;
        }
        if(inputString.equals("")){
        	return outputString;
        }
        
        for(int i=0; i<inputString.length() ; i++) {
            if(inputString.substring(i,i+1).equals(ASTERIX)) {
                if(i == 0				&&
                        inputString.length()>1		&&
                        inputString.substring(1,2).equals(".")) {
                    outputString = outputString + ".+";
                } else {
                	outputString = outputString + ".*";
                }
            } else if(inputString.substring(i,i+1).equals(QUESTIONMARK)) {
                outputString = outputString + ".";
            } else if(inputString.substring(i,i+1).equals(".")) {
                outputString = outputString + "\\.";
            } else if(inputString.substring(i,i+1).equals("+")) {
                outputString = outputString + "\\+";
            } else if(inputString.substring(i,i+1).equals("_")) {
                outputString = outputString + "\\_";
            } else if(inputString.substring(i,i+1).equals("-")) {
                outputString = outputString + "\\-";
            } else {
                outputString = outputString + inputString.substring(i,i+1);
            }
        }
        return outputString;
    }
    
    
    /**
     * Converts a windows wildcard pattern to a regex pattern
     *
     * @param wildcard - Wildcard pattern containing * and ?
     *
     * @return - a regex pattern that is equivalent to the windows wildcard pattern
     */
    public static String wildcardSubst(String wildcard) {
        if (wildcard == null) {
        	return null;
        }
        
        StringBuffer buffer = new StringBuffer();
        
        char [] chars = wildcard.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '*') {
                buffer.append(".*");
            } else if (chars[i] == '?') {
                buffer.append(".");
            } else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1) {
                buffer.append('\\').append(chars[i]); // prefix all metacharacters with backslash
            } else {
                buffer.append(chars[i]);
            }
        }
        
        return buffer.toString();
    }
    
    /** This method converts a HashSet of Files to a HashSet of FilePath objects
     */
    public static List<DDFilePath> filesSetToStringSetNoDirectories(DDFilePath parentFilePath, List<File> inputSet) {
    	List<DDFilePath> outputSet = new ArrayList<DDFilePath>();
        if(inputSet.isEmpty()){
        	return outputSet;
        }
        for(File thisFile: inputSet){
            if(! thisFile.isDirectory()) {
                // outputSet.add((String) thisFile.toString());
                String path = (String) thisFile.toString();
                DDFilePath filePath = parentFilePath.clone();
                filePath.setPath(path);
                filePath.setType(DDFilePath.FILELOCATION);
                outputSet.add(filePath);
            }
        }
        return outputSet;
    }
    
    
//    public static List<Object> convertToHashSet(List<Object> vector){
//        return new ArrayList<Object>(vector);
//    }
    
    
    /** This procedure sleeps the current Thread for a delay from 0 to miliSeconds
     * miliseconds **/
    
    public static void sleepThread(int miliSeconds) throws InterruptedException {
        Random rnd = new Random((new Date()).getTime());
        int timeToSleep = rnd.nextInt(miliSeconds);
        
        Thread.sleep(timeToSleep);
        return;
    }
    
    /** This method returns a String value that id the input string when
     * this is different than null or an empty string. In other case it
     * return the second argument
     **/
    public static String setIfValue(String inputString, String wildcardString) {
        if(inputString == null){
        	return wildcardString;
        }
        if(inputString.equals("")){
        	return wildcardString;
        }
        return inputString;
    }
    
	public static String getCountryFromIp(String ip){
		try {
			return CountryResolver.resolveCountryFromIp(ip);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int getAproxUniqueId(){
		String longUniqueId = UwsUtils.getUniqueIdentifier();
		long l = Long.parseLong(longUniqueId);
		int aproxId = (int)(l / 1000L);
		return aproxId;
	}
	
	public static int getOriginOid(String username){
		if("AIO".equalsIgnoreCase(username)){
			return 1;
		} else {
			return 2;
		}
	}
	
	public static List<String> getListFromParam(String param){
		if (param == null){
			return null;
		}
		String[] items = param.split(",");
		List<String> result = new ArrayList<String>();
		for(String item: items){
			result.add(item.trim());
		}
		return result;
	}
	
	/**
	 * Clone HttpServletRequest parameters map
	 * @param request
	 * @return
	 */
	public static Map<String, String[]> getPropertiesFromRequest(HttpServletRequest request){
		Map<String,String[]> properties = new HashMap<String, String[]>();
		
		Map<String,String[]> params = request.getParameterMap();
		String[] values;
		String[] tmpValues;
		for(Entry<String, String[]> e: params.entrySet()){
			tmpValues = e.getValue();
			if(tmpValues == null){
				values = null;
			}else{
				values = new String[tmpValues.length];
				for(int i = 0; i < values.length; i++){
					values[i] = new String(tmpValues[i]);
				}
			}
			properties.put(e.getKey().toLowerCase(), values);
		}
		
		return properties;
	}
	
	public static String getDistributionPath(String distributionPathBase, String completePath){
		int pos = completePath.lastIndexOf(File.separator);
		String finalFile = distributionPathBase + completePath.substring(pos);
		return finalFile;
	}
	
	public static void dumpTarToStream(List<DDFilePath> urlItemsList, String reqId, String repoTopLevel, HttpServletResponse response, boolean compressed, String fileNameBase) throws IOException {
		List<String[]> filesToBeTared = new ArrayList<String[]>();
//		String finalFile;
		String distributionPath;
		String path;
		for(DDFilePath fpItem: urlItemsList){
			String[] entry = new String[2];
			path = fpItem.getPath();
			distributionPath = fpItem.getDistributionPath();
			if(distributionPath == null)    {
				distributionPath = DDFileUtils.finalPathTarEntryString(reqId, path, repoTopLevel);
				if(distributionPath.charAt(0) == File.separatorChar) {
					distributionPath = distributionPath.substring(1);
				}
			} 
			entry[0] = distributionPath;
			entry[1] = path;
			//System.out.println("TAR entry: " + finalFile + " - PATH: " + path);
			filesToBeTared.add(entry);
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(DDProperties.CONTENT_TYPE_BINARY);
		
		String fileName;
		if(compressed){
			fileName = fileNameBase + ".tgz";
		}else{
			fileName = fileNameBase + ".tar";
		}

		String cdFileName = getSuitableContentDispistionFileName(fileName);
		response.setHeader("Content-disposition", "attachment; filename=\"" + cdFileName+ "\n");

		OutputStream output = null;
		if(compressed){
			//avoid browser auto unzip
			//response.addHeader("Content-Encoding", "gzip");
			output = new GZIPOutputStream(response.getOutputStream());
		}else{
			output = response.getOutputStream();
		}
		try {
			DDFileUtils.createStreamedTar(output, filesToBeTared);
			if(output instanceof GZIPOutputStream){
				((GZIPOutputStream)output).finish();
			}
		} catch (Exception e) {
			throw new IOException("Cannot create TAR file: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param fpItem
	 * @param response
	 * @param compress 'true' if the user wants to compress the file. If the file is already compressed, it is ignored.
	 * @param uncompress 'true' if the user wants to uncompress the file. If the file is already uncompressed, it is ignored.
	 * @throws IOException
	 */
	public static void dumpToStream(DDFilePath fpItem, HttpServletResponse response, boolean compress, boolean uncompress) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			boolean fileAlreadyCompressed = fpItem.isCompressed();
			String file = fpItem.getPath();
			File f = new File(file);
			if(!f.exists()){
				throw new IOException("File not found: " + f.getAbsolutePath());
			}
			long size = -1;
			if(uncompress && fileAlreadyCompressed){
				is = new GZIPInputStream(new FileInputStream(file));
			}else{
				size = fpItem.getSize();
				is = new FileInputStream(new File(file));
			}
			response.setStatus(HttpServletResponse.SC_OK);
			String contentType = fpItem.getContentType();
			if(contentType != null){
				response.setContentType(contentType);
			}
			
			//response.setHeader("Connection", "close");
			
			//Commented to avoid browsers to auto-unzip
//			if(fileAlreadyCompressed){
//				response.addHeader("Content-Encoding", "gzip");
//			}
			if(compress && !fileAlreadyCompressed){
				//Content-Disposition
				setContentDispositionIfRequired(fpItem, response, true);
				//gzip
				//avoid browser auto unzip
				//response.addHeader("Content-Encoding", "gzip");
				size = -1;
				os = new GZIPOutputStream(response.getOutputStream());
			}else{
				//Content-Disposition
				setContentDispositionIfRequired(fpItem, response, false);
				os = response.getOutputStream();
			}
			
			if(size >= 0){
				response.setContentLength((int)size);
				//Tomcat 8
				//response.setContentLengthLong(size);
			}

			UwsOutputUtils.dumpToStream(is, os);
			if(os instanceof GZIPOutputStream){
				((GZIPOutputStream)os).finish();
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {

				}
			}
		}
	}
	
	private static void setContentDispositionIfRequired(DDFilePath fpItem, HttpServletResponse response, boolean appendGzipExt){
		DDContentDisposition cd = fpItem.getContentDisposition();
		if(cd != null){
			//"Content-Disposition", "attachment;filename=somefile.ext"
			File f = new File(fpItem.getPath());
			String fileName = f.getName();
			if(appendGzipExt){
				fileName += ".gz";
			}
			String value = cd.getValue() + ";filename=\""+fileName + "\"";
			response.setHeader("Content-Disposition", value);
		}
	}

	
//	public static void dumpUrls(List<Filepa> urls, HttpServletResponse response) throws IOException{
//		response.setStatus(HttpServletResponse.SC_OK);
//		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_JSON);
//		PrintWriter pw = response.getWriter();
//		pw.print('{');
//		if(urls != null){
//			boolean firstTime = true;
//			String url;
//			String contentType;
//			long size;
//			boolean compressed;
//			for(UrlItem urlItem: urls){
//				if(firstTime){
//					firstTime = false;
//				}else{
//					pw.println(',');
//				}
//				url = urlItem.getUrl().toString();
//				contentType = urlItem.getContentType();
//				compressed = urlItem.isCompressed();
//				size = urlItem.getSize();
//				pw.print('{');
//				pw.print("{url: \'" + url + "'");
//				pw.print(", content-type: '"+contentType+"'");
//				pw.print(", compressed: '"+compressed+"'");
//				pw.print(", size: "+size+"");
//				pw.print('}');
//			}
//		}
//		pw.print('}');
//		pw.flush();
//	}
	
	public static String getCompleteUrl(HttpServletRequest request){
		if(request == null){
			return null;
		}
		String queryString = request.getQueryString();
		if(queryString == null || queryString.isEmpty()){
			return request.getRequestURL().toString();
		}else{
			return request.getRequestURL().toString() + "?" + queryString;
		}
	}
	
	public static String getSuitableContentDispistionFileName(String fileName) throws IOException {
		String fileNameEncoded = URLEncoder.encode(fileName, "UTF-8");
        return URLDecoder.decode(fileNameEncoded, "ISO8859_1");
	}
	
	
	public static int getPropStatus(List<DDFilePath> files){
		if(files == null){
			return DDProperties.PROPRIETARY_STATUS_PUBLIC;
		}
		for(DDFilePath fp: files){
			if(!fp.isAccessible()){
				return DDProperties.PROPRIETARY_STATUS_PRIVATE;
			}
		}
		return DDProperties.PROPRIETARY_STATUS_PUBLIC;
	}
	
	/**
	 * Different ids for different debug files:
	 * <pre><tt>
	 * dd.file_path_debug=id1=path_to_debug_file_1|id2=path_to_debug_file_2[|...]
	 * </tt></pre>
	 * Unique debug file:
	 * <pre><tt>
	 * dd.file_path_debug=path_to_debug_file
	 * </tt></pre>
	 * @param configuration
	 * @param id
	 * @return
	 */
	public static String getFilePathDebugById(UwsConfiguration configuration, String id){
		if(configuration.hasValidPropertyValue(DDProperties.PROP_FILE_PATH_DEBUG)){
			String filePathDebug = configuration.getProperty(DDProperties.PROP_FILE_PATH_DEBUG);
			String[] items = filePathDebug.split("\\|");
			String[] keyValue;
			String defaultValue = null;
			for(String item: items){
				keyValue = item.split("=");
				if(keyValue.length == 2){
					if(id.equalsIgnoreCase(keyValue[0])){
						return keyValue[1];
					}
				}else{
					defaultValue = item;
				}
			}
			return defaultValue;
		}
		return null;
	}
	
	public static String joinData(String[] data, boolean isString){
		if(data == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < data.length; i++){
			if(i != 0){
				sb.append(',');
			}
			if(isString){
				sb.append('\'').append(data[i]).append('\'');
			}else{
				sb.append(data[i]);
			}
		}
		return sb.toString();
	}
	
	public static String[] convertToString(int[] data){
		if(data == null){
			return null;
		}
		String[] results = new String[data.length];
		for(int i = 0; i < data.length; i++){
			results[i] = ""+data[i];
		}
		return results;
	}
	
}
