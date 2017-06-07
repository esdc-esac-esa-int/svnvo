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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import esavo.sl.dd.requests.DDProperties;
import esavo.uws.config.UwsConfiguration;


/** A class with many utilities used to access the system.
 *   Will be modified to use more elegant code for copying
 *   in version 1.0
 **/
public class DDSystemUtils {
    
    static Logger logger = Logger.getLogger(DDSystemUtils.class);
    
        /*Avoid instantiation
         */
    private DDSystemUtils(){}
    
    
    /** The following method find files according to a pattern. The input parameters
     * are the dir where to start to find and a string with wildcards representing
     * the file name(s) to be find
     **/
    public static List<DDFilePath> findFiles(String parentDir, DDFilePath parentFilePath) throws Exception {
    	List<DDFilePath> resultSet = null;
        try {
        	String patternString = parentFilePath.getPath();
        	
            /** Variables created to prevent too many files problems **/
            Iterator<File> itfo;
            File currDir;
            DDRpFilterName filter;
            File[] filesOldArray;
            
            patternString = DDUtils.wildcardSubst(patternString);
            StringTokenizer st = new StringTokenizer(patternString,File.separator);
            String[] pattern = new String[st.countTokens()];
            
            int i=0;
            while(st.hasMoreTokens()) {
                pattern[i]=st.nextToken();
                i++;
                
            }
            
            File initialDir = new File(parentDir);
            
            List<File> filesOld = new ArrayList<File>();
            List<File> filesNew = new ArrayList<File>();
            
            filesOld.add(initialDir);
            
            for(int level = 0; level < pattern.length; level++) {
            
            	List<File> filesOldClone = new ArrayList<File>(filesOld);
                //itfo = ((Set<File>) filesOld.clone()).iterator();
            	itfo = filesOldClone.iterator();
                filesNew.clear();
                
                while(itfo.hasNext()) {
                    currDir = (File) itfo.next();
                    filter = new DDRpFilterName(pattern[level]);
                    filesOldArray = currDir.listFiles(filter);
                    int tryInt = 0;
                    while(filesOldArray == null && tryInt < 3) {
                        Runtime.getRuntime().gc();
                        DDUtils.sleepThread(250);
                        filesOldArray = currDir.listFiles(filter);
                        tryInt ++;
                    }
                    filesNew = DDUtils.addArrayToHashSet(filesNew,filesOldArray);
                }
                filesOld = new ArrayList<File>();
                filesOld = filesNew;
            }
            
            resultSet = DDUtils.filesSetToStringSetNoDirectories(parentFilePath, filesNew);
            
            itfo			= null;
            currDir			= null;
            filter			= null;
            filesOldArray	= null;
            filesOld		= null;
            filesNew		= null;
            
            
        } catch(Exception e) {            
            logger.error("Finding files problem",e);
        }
        
        return resultSet;
    }
    
    
    
        /*
         * Copies a vector of files (given as pathnames) to the FTP
         * directory (at the destinationRoot level) and returns a HashSet with the copied files
         */
    public static List<String> copyToFtp(String destinationRoot, List<String> filesHashSet, String repoTopLevel) {
        
    	List<String> resultsSet = new ArrayList<String>();
        
        Iterator<String> it = filesHashSet.iterator();
        String thisFileString = "";
        while(it.hasNext()) {
            try {
                thisFileString = (String) it.next();
                File inputFile = new File(thisFileString);
                
                String ap = inputFile.getAbsolutePath();
                String name = inputFile.getName();

                int iname = ap.indexOf(name);
                
                String dir = ap.substring(repoTopLevel.length() + 1, iname);
                
                String destinationFilename = destinationRoot + File.separator + dir + name;
                copyCreatePath(thisFileString, destinationFilename);
                
                resultsSet.add(destinationFilename);
            } catch(Exception e) {
                logger.error("Problems copying " + thisFileString);
                e.printStackTrace();
            }
        }
        
        return resultsSet;
    }
    
    
    public static void copyCreatePath(String from, String to) throws Exception {
        
        from = DDUtils.fileConversion(from);
        to = DDUtils.fileConversion(to);
        
        if(! (new File(from)).exists()) return;
        if((new File(from)).isDirectory()) return;
        
        File currentFile = new File(to);
        File currentParentFile = (new File(to)).getParentFile();
        
        if(!currentParentFile.exists()) {
            currentParentFile.mkdirs();
        }
        
        currentFile.createNewFile();
        
        try {
            copy(from,to);
            
        } catch (Exception e) {
            logger.error("The problem is inside copy for file: " + to);
            e.printStackTrace();
        }
    }
    
    
    public static List<String> copyOneFileToFtp(String destinationRoot,
            String filename,
            boolean calculateSizeFlag,
            String fileType) {
        
        List<String> resultsVector = new ArrayList<String>();
        
        logger.debug("FileList:***:"+filename);
        File inputFile 	= new File(filename);
        String name 	= inputFile.getName();
        
        logger.debug("Name:+++++++"+name);
        String destinationDir      = destinationRoot
                + File.separatorChar
                + fileType
                + File.separator;
        
        String destinationFilename = destinationDir
                + File.separatorChar
                + name;
        
        File destinationDirFile=new File(destinationDir);
        if (!calculateSizeFlag){
            if (!destinationDirFile.exists()) {destinationDirFile.mkdir();}
        }
        if (!(new File(destinationFilename)).exists()) {
            try {
                if (!calculateSizeFlag){
                    copy(filename, destinationFilename);
                    resultsVector.add(destinationFilename);
                } else {
                    resultsVector.add(filename);
                }
            } catch (Exception e) {
                logger.error("Exception when copying file:"
                        + filename
                        + " to "
                        + destinationFilename);
            }
        }
        
        return resultsVector;
    }
    
    
    
    
    /** Just a copy "from" path "to" path
     */
    public static void copy(String from, String to) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            while (true) {
            	byte[] buffer = new byte[65536];
                int amountRead = in.read(buffer);
                if (amountRead == -1) {
                    break;
                }
                out.write(buffer, 0, amountRead);
            }
            in.close();
            out.close();
            
        } catch (FileNotFoundException e) {
            logger.error("File: " + from + "  or  "+ to + " not found");
            return;
            
        } catch (Exception e) {
            logger.error("Exception: " + e);
            return;
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FIN");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FOUT");
                }
            }
        }
    }
    
    /** Just a imitation of mv "from" path "to" path, as renameTo doesn't work
     * in Unix...
     */
    public static void mv(String from, String to) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            while (true) {
            	byte[] buffer = new byte[65536];
                int amountRead = in.read(buffer);
                if (amountRead == -1) {
                    break;
                }
                out.write(buffer, 0, amountRead);
            }
            in.close();
            out.close();
            
            new File(from).delete();

        } catch (FileNotFoundException e) {
            logger.error("File: " + from + "  or  "+ to + " not found",e);
            e.printStackTrace();
            return;
        } catch (Exception e) {
            logger.error("Exception",e);
            e.printStackTrace();
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FIN");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FOUT");
                }
            }
        }
    }
    
    /** Just a move of a vector of files to a dir
     */
    public static List<String> move(List<String> files, String dir) throws Exception {
        List<String> filesMovedTo = new ArrayList<String>();
        
        for (int i=0; i<files.size();i++){
            String fromFilename = files.get(i);
            try {
                File   fromFile = new File(fromFilename);
                String toFilename = dir+File.separator+fromFile.getName();
                
                mv(fromFilename,toFilename);
                filesMovedTo.add(toFilename);
                
            } catch (Exception e) {
                logger.error("Couldn't move file" +fromFilename,e);
                e.printStackTrace();
            }
        }
        return filesMovedTo;
        
    }
    
    public static void cleanDir(File dir){
        
        String[] filelist = dir.list();
        File tmpFile = null;
        
        for (int i = 0; i < filelist.length; i++) {
            tmpFile = new File(dir.getAbsolutePath(), filelist[i]);
            if (tmpFile.isDirectory()) {
                tmpFile.delete();
            } else if (tmpFile.isFile()) {
                if(tmpFile.toString().indexOf("tar")==-1) {
                    tmpFile.delete();
                } else if (tmpFile.toString().indexOf("tar.gz")!=-1){
                    tmpFile.delete();
                }
            }
        }
        
    }
    
    
    
    public static void copyGZIP(String from, String to) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        GZIPOutputStream zout = null;
        to=to+".gz";
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            zout=new GZIPOutputStream(out);
            
            while (true) {
            	byte[] buffer = new byte[65536];
                int amountRead = in.read(buffer);
                if (amountRead == -1) {
                    break;
                }
                zout.write(buffer, 0, amountRead);
            }
            zout.finish();
            in.close();
            out.close();
            zout.close();
            
        } catch (FileNotFoundException e) {
            logger.error("File: " + from + " not found" + e);
            
        } catch (Exception e) {
            logger.error("Exception: " + e);
            e.printStackTrace();
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FIN");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.error("Couldn't close FOUT");
                }
            }
            if (zout != null) {
                try {
                    zout.close();
                } catch (Exception e) {
                    logger.error("Couldn't close ZOUT");
                }
            }
            
        }
    }
    
    /** Calculates the size in bytes of a file represented by a FilePath object
     */
    public static long calculateFileSize(DDFilePath filePath) {
        long size = 0;
        
        String fileName = (String)filePath.getPath();
        File inputFile  = new File(fileName);
        size            = inputFile.length();
        
        return size;
    }
    
    /** Calculates the size in bytes of a file represented by a FilePath object
     */
    public static void updateFileSize(DDFilePath filePath) {
    	long size = calculateFileSize(filePath);
    	filePath.setSize(size);
    }
    
    /** Calculates the total size of a vector of files
     */
    public static long calculateFilesSize(List<String> fileNames) {
        List<String> unrepeatedFiles = new ArrayList<String>();
        long partialSize = 0;
        for (String fileName: fileNames) {
            if(!unrepeatedFiles.contains(fileName)){
            	unrepeatedFiles.add(fileName);
                File inputFile = new File(fileName);
                partialSize += inputFile.length();//in bytes
            }
        }
        return partialSize;//in bytes
    }
    
    public static long calculateFilePathSize(List<DDFilePath> filePaths) {
        long size = 0;
        
        for (DDFilePath filePath: filePaths) {
            long sizeToAdd = filePath.getSize();//-1 if the file size has still not been set
            if(sizeToAdd <= 0) {
                sizeToAdd = calculateFileSize(filePath);	//using length method of the File class
                logger.debug(filePath.getPath() + " size=" + sizeToAdd);
                filePath.setSize(sizeToAdd);				//bytes
            }
            size += sizeToAdd;
        }
        return size;
    }

    
    /** Notifies the user about the request finished
     */
    public static void emailNotify(URL retrievalURL, String userName, String email, String reqID, List<DDFilePath> copiedFiles, boolean obsIsProp, long size, UwsConfiguration configuration) throws Exception{
		List<String> to = new ArrayList<String>();
		List<String> cc = new ArrayList<String>();
        to.add(email);
       
        String missionName = configuration.getProperty(DDProperties.PROP_MISSION_NAME);
		String smtpHost = configuration.getProperty(DDProperties.PROP_SMTP_HOST);
		String helpdeskCustodian = configuration.getProperty(DDProperties.PROP_HELPDESK_CUSTODIAN);

		String subject="Your "+ missionName + " Science Archive Data request " + reqID + " has been processed";
        
        String thisMessageText = buildMessageText(retrievalURL, reqID, copiedFiles, obsIsProp, size, configuration);
        DDEmailMessage thisMessage=new DDEmailMessage(smtpHost,
                helpdeskCustodian,
                to,
                cc,
                subject,
                thisMessageText);
        
        
        thisMessage.send();
    }
    
    
    
    /** Notifies the user about an error in the request
     */
    public static void emailError(String email, String reqID, UwsConfiguration configuration) throws Exception{        
		List<String> to = new ArrayList<String>();
		List<String> cc = new ArrayList<String>();
		String smtpHost = configuration.getProperty(DDProperties.PROP_SMTP_HOST);
		String helpdeskCustodian = configuration.getProperty(DDProperties.PROP_HELPDESK_CUSTODIAN);
        String masterEmail = configuration.getProperty(DDProperties.PROP_MASTER_EMAIL);
        
        StringTokenizer st=new StringTokenizer(helpdeskCustodian,",");
        while (st.hasMoreElements()) {
            to.add(st.nextElement().toString());
        }
               
        st=new StringTokenizer(masterEmail,",");
        while (st.hasMoreElements()) {
            cc.add(st.nextElement().toString());
        }
        
//        email = QueryBeanFactory.getEmail(userName,rmiWrapper);        

        st   = new StringTokenizer(email,",");
        while (st.hasMoreElements()) {
            to.add(st.nextElement().toString());
        }
        
        String thisMessageText = buildErrorMessage(configuration);
        String subject="PRIVATE: Error in request "+reqID;
        DDEmailMessage thisMessage=new DDEmailMessage(smtpHost,
                helpdeskCustodian,
                to,
                cc,
                subject,
                thisMessageText);
        
        thisMessage.send();
        
    }
    
    
    /** Builds a message text from a text header and footer including
     *   data specific for each user
     */
    public static String buildMessageText(URL retrievalURL, String reqID, List<DDFilePath> copiedFiles, boolean obsIsProp, long size, UwsConfiguration configuration) {
    	StringBuilder text   = new StringBuilder();

    	String header=DDFileUtils.readEmailFile(configuration, "email_header.txt");
    	String footer=DDFileUtils.readEmailFile(configuration, "email_footer.txt");

    	try{
    		text.append(header);

    		double sizeMB 	= size/1024/1024;//input size must be in bytes.
    		
    		text.append("You can access your files online from the browser at:\n");
			text.append(retrievalURL.toString()).append("\n");
			logger.debug("Email Shopping Basket adress: " + retrievalURL.toString());
 
    		text.append("\n");
    		text.append("The total size of the requested files is ").append(rounding(sizeMB,2)).append(" MB (").append(size).append(" bytes).\n");

    		text.append(footer);

    		//In this part we add the list of found files in the mail text (only first 10000)

    		if(copiedFiles.size() > 10000) {
    			text.append("More than 10000 files. HashSet not attached\n");
    		} else {
    			String thisFile;
    			for(DDFilePath filePath: copiedFiles) {
    				thisFile = filePath.getPath();                        
    				text.append(thisFile).append("\n");
    			}
    		}

    	} catch (Exception e){
    		logger.error("Problems creating mail text",e);
    	}

    	return text.toString();
    }

    
    

    /** Notifies the user about the request finished
    */ 
      public static void emailNoProductsNotify(String email, String reqID, UwsConfiguration configuration) throws Exception{
      	List<String> to=new ArrayList<String>();
      	to.add(email);
      	List<String> cc=new ArrayList<String>();
    	
      	String missionName = configuration.getProperty(DDProperties.PROP_MISSION_NAME);
      	String smtpHost = configuration.getProperty(DDProperties.PROP_SMTP_HOST);
      	String helpdeskCustondian = configuration.getProperty(DDProperties.PROP_HELPDESK_CUSTODIAN);
    	
      	 String subject="Your "+ missionName+ " Science Archive Data request " + reqID +" has not produced products";
         String thisMessageText = buildNoProductsMessage(reqID, configuration);
         DDEmailMessage thisMessage=new DDEmailMessage(smtpHost,
                 helpdeskCustondian,
                 to,
                 cc,
                 subject,
                 thisMessageText);
                 
         thisMessage.send();      
      }
        
      /** Builds the specific text to be sent to the user when
       *  request has not products associated
       */
      public static String buildNoProductsMessage(String reqID, UwsConfiguration configuration) {
    	  String header = DDFileUtils.readEmailFile(configuration, "email_noData_header.txt");
    	  String footer = DDFileUtils.readEmailFile(configuration, "email_noData_footer.txt");
    	  return header+footer;
      }      
      
    
    /** Builds the specific text to be sent to the user when
     *   a proprietary request was asked
     */
    public static String buildProprietaryMessage(String reqID, UwsConfiguration configuration) {
    	return DDFileUtils.readEmailFile(configuration, "proprietary.txt");
    }
    
    
    public static String buildErrorMessage(UwsConfiguration configuration) {
    	return DDFileUtils.readEmailFile(configuration, "email_error.txt");
    }
    
     
    public static String createFtpDir(String reqID,String userName,String obsno,boolean obsIsProp,UwsConfiguration configuration){
        String dir="";
        
        if (obsIsProp){
            String base = configuration.getProperty(DDProperties.PROP_FTP_SECURE);
            dir 	=  base +
                    File.separator +
                    userName.toLowerCase() +
                    File.separator +
                    reqID+
                    File.separator+
                    obsno;
        } else {
            String base = configuration.getProperty(DDProperties.PROP_FTP_PUBLIC);
            dir 	=  base +
                    File.separator +
                    reqID+
                    File.separator+
                    obsno;
        }
        
        File destinationDirFile=new File(dir);
        if (!destinationDirFile.exists()) {
            destinationDirFile.mkdirs();
        }
        return dir;
    }
    
    public static double rounding(double nD, int nDec) {
        return Math.round(nD*Math.pow(10,nDec))/Math.pow(10,nDec);
    }
}
