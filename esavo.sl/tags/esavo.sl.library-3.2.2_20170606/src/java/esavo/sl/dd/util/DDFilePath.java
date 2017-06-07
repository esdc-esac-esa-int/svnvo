package esavo.sl.dd.util;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DDFilePath implements Comparable<DDFilePath>{
    
    static Logger logger = Logger.getLogger(DDFilePath.class.getName());
    
	public static int PATHPATTERN = 0;
	public static int FILELOCATION = 1;
    
	private String path = "";
	private int type = 0;
	private long size = -1;// ------>>>>>TOBeModified: Hastable with properties to be used by all archives!!!!
	private String retrievalId;
	private String retrievalInfo;
	private String distributionPath;

	private Map<String, Object> fileProperties = new HashMap<String, Object>();
    
    private String contentType;
    private boolean compressed;
    private boolean accessible;
    private DDContentDisposition contentDisposition;
    
	public DDFilePath(String retrievalId, String path, int type) {
		this.retrievalId = retrievalId;
		this.path = path;
		this.type = type;
		setCompressed();
		this.contentDisposition = DDContentDisposition.Inline;
	}

	public DDFilePath(String retrievalId, String path, int type, Map<String, Object> fileProperties) {
		this.retrievalId = retrievalId;
		this.path = path;
		this.type = type;
		this.fileProperties = fileProperties;
		setCompressed();
		this.contentDisposition = DDContentDisposition.Inline;
	}
    
    public DDFilePath clone() {
    	DDFilePath filePath = new DDFilePath(retrievalId,path,type);
    	filePath.setProperties(fileProperties);
    	filePath.setSize(size);
    	return filePath;
    }
    
    public String getPath() {
        return path.trim();
    }
    public int getType() {
        return type;
    }
    
    public long getSize() {
        return size;
    }
    
    
    public void setPath(String path) {
        this.path = path;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public boolean equals(Object anotherFilePath) {

    	if(anotherFilePath == null) {
    		return false;
    	}
    		
    	String thisPath		= this.path;
    	String anotherPath 	= (String) ((DDFilePath)anotherFilePath).getPath();

    	//Two nulls; true
    	if(thisPath== null && anotherPath == null) 
    		return true;

    	//Only one null; false
    	if(thisPath== null || anotherPath == null) 
    		return false;

    	//No nulls; who knows? It depends on the value...
    	boolean equals = thisPath.equals(anotherPath);  

    	if(equals) {
    		String anotherDistributionPath 	= ((DDFilePath)anotherFilePath).getDistributionPath();
    		String thisDistributionPath 	= this.getDistributionPath();

    		//Two nulls; true
    		if(thisDistributionPath== null && anotherDistributionPath == null) {
    			return true;
    		}

    		//Only one null; false
    		if(thisDistributionPath== null || anotherDistributionPath == null) { 
    			return false;
    		}

    		//No nulls; who knows? It depends on the value...
    		return (thisDistributionPath.equals(anotherDistributionPath));

    	} else {
    		return false;   
    	}
    }
    
    public int hashCode(){
        return this.path.hashCode();
    }
    
    public Map<String,Object> getProperties(){
        return this.fileProperties;
    }
    
    public void setProperties(Map<String,Object> fileProperties){
        this.fileProperties = fileProperties;
    }
    
    
    public Object getProperty(String key){
        return this.fileProperties.get(key);
    }
    
    public void setProperty(String key, Object value){
        this.fileProperties.put(key,value);
    }
    
    
    // Comparable Implementation
    public int compareTo(DDFilePath anotherFilePath) {
        try {
            
            if(this.equals((DDFilePath)anotherFilePath)){
                return 0;
            }
            
            //if the paths are different, we compare the dates
            Date date           = (Date)     ((DDFilePath)anotherFilePath).getProperties().get("observation_date");
            Date thisDate       = (Date)     this.getProperties().get("observation_date");
            
            if(date != null && thisDate != null){
//                logger.debug("Comparing Observation dates");
                int compareDates = thisDate.compareTo(date);       
                
                //Warning! We must avoid this situation: two files with the same date are equals (!)
                if(compareDates != 0){
                    return  compareDates;//ordering two different files by date
                }else{
                    return 1;//different files with the same date. The order does not mind in this case.
                }
            }else{//no dates related to FilePath objects--> Order given by String applied to paths
                return this.getPath().compareToIgnoreCase(((DDFilePath)anotherFilePath).getPath());
            }
            
        } catch (Exception e) {
            logger.severe("Problems comparing two FilePath objects");
            return 1;
        }
    }

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the compressed
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * @param compressed the compressed to set
	 */
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}
	
	/**
	 * Sets compressed based on 'path' file name
	 */
	public void setCompressed(){
		if(path == null){
			setCompressed(false);
		}else{
			boolean isCompressed = path.toLowerCase().endsWith(".gz");
			setCompressed(isCompressed);
		}
	}

	/**
	 * @return the accessible
	 */
	public boolean isAccessible() {
		return accessible;
	}

	/**
	 * @param accessible the accessible to set
	 */
	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	/**
	 * @return the retrievalId
	 */
	public String getRetrievalId() {
		return retrievalId;
	}

	/**
	 * @param retrievalId the retrievalId to set
	 */
	public void setRetrievalId(String retrievalId) {
		this.retrievalId = retrievalId;
	}

	/**
	 * @return the retrievalInfo
	 */
	public String getRetrievalInfo() {
		return retrievalInfo;
	}

	/**
	 * @param retrievalInfo the retrievalInfo to set
	 */
	public void setRetrievalInfo(String retrievalInfo) {
		this.retrievalInfo = retrievalInfo;
	}

	/**
	 * @return the distributionPath
	 */
	public String getDistributionPath() {
		return distributionPath;
	}

	/**
	 * @param distributionPath the distributionPath to set
	 */
	public void setDistributionPath(String distributionPath) {
		this.distributionPath = distributionPath;
	}

	/**
	 * @return the contentDisposition
	 */
	public DDContentDisposition getContentDisposition() {
		return contentDisposition;
	}

	/**
	 * @param contentDisposition the contentDisposition to set
	 */
	public void setContentDisposition(DDContentDisposition contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	
}
