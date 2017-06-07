package esavo.sl.dd.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.ice.tar.TarEntry;
import com.ice.tar.TarOutputStream;

import esavo.sl.dd.requests.DDProperties;
import esavo.uws.config.UwsConfiguration;

public class DDFileUtils {
	
	static Logger logger = Logger.getLogger(DDFileUtils.class);
	
	public static String readEmailFile(UwsConfiguration configuration, String file){
		String emailConf = configuration.getProperty(DDProperties.PROP_EMAIL_CONF);
		return readFile(emailConf + file);
	}
	
	public static String readFile(String file){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				sb.append(line).append('\n');
			}
			return sb.toString();
		} catch(Exception e){
            e.printStackTrace();
      		logger.error("Reading file '"+file+"': " + e.getMessage());
      		logger.error(e);
			return "";
		} finally{
			if(reader != null){
				try{
					reader.close();
				}catch(IOException ioe){
					
				}
			}
		}

	}
	
	public static String getFilename(String url) {
		if(url == null){
			return null;
		}
		int filenameLocation = url.lastIndexOf("/");
		return url.substring(filenameLocation + 1);

	}


    public static void createStreamedTar(OutputStream output, List<String[]> filesToBeTared) throws Exception {
		TarOutputStream tos = new TarOutputStream(output);

		String entryName;
		String entryFile;
		String[] fileRecord;
        for(int i=0 ; i < filesToBeTared.size(); i++) {
        	fileRecord = filesToBeTared.get(i);
        	entryName = fileRecord[0];
        	entryFile = fileRecord[1];
            boolean hasLongLink = entryName.length() > 99; //safer 100 chars max
            if (hasLongLink) {
                TarEntry te = new TarEntry("././@LongLink"); //by hand
                te.getHeader().linkFlag = (byte) 'L'; //longlink, SONAR messed up
                byte[] data = entryName.getBytes();
                te.setSize(data.length);
                //te.setUserName(System.getProperty("user.name")); should be root
                tos.putNextEntry(te);
                tos.write(data);
                tos.closeEntry();
            }
            File f = new File(entryFile); //new TarEntry(f);
            TarEntry te = new TarEntry(f); //new File(name)); //getSafeName(name, f.isFile()));
            //te.setUserName(System.getProperty("user.name"));
            if (hasLongLink) {
                te.setName(""); //getSafeName(name, f.isFile())); this avoids te.isDirectory() but there is more ...
            } else {
                te.setName(entryName); //Use unixfied relative path name
            }
            te.setSize(f.length());
            tos.putNextEntry(te);
            if (f.isFile()) {
            	if(!f.exists()){
            		throw new IOException("File not found: " + f.getAbsolutePath());
            	}
            	
    			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
    			byte[] data = new byte[65536];
    			int byteCount;

    			while ((byteCount = bis.read(data)) > -1){
    				tos.write(data, 0, byteCount);
    			}
    			bis.close();
            }
            tos.closeEntry();
 
		}
        
        tos.finish();
        tos.close();


	}
    
    public static String finalPathTarEntryString(String reqID, String pathString, String repoTopLevel) {        
        logger.debug("Input path in finalPathTarEntryString: "+pathString);
        if(pathString == null) {
        	return "";
        }
        String finalPathString;
        if(repoTopLevel != null){
        	finalPathString = pathString.substring(repoTopLevel.length()+1);
        }else{
        	finalPathString = pathString;
        }
        finalPathString = finalPathString.substring(finalPathString.indexOf(File.separator));
        logger.debug("Final path in finalPathTarEntryString: "+finalPathString);
        return finalPathString;
    }
}
