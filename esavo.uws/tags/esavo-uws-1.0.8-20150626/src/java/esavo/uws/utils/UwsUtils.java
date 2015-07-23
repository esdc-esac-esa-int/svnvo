package esavo.uws.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsJobThread;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;

public class UwsUtils {
	
	public static final String ANONYMOUS_USER = "anonymous";
	
	/**
	 * Default date format pattern.
	 */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	/**
	 * Job phases that require a job restart procedure.<br/>
	 * <ul>
	 * <li>{@link UwsJobPhase#PENDING}</li>
	 * <li>{@link UwsJobPhase#QUEUED}</li>
	 * <li>{@link UwsJobPhase#EXECUTING}</li>
	 * </ul>
	 */
	public static final Set<UwsJobPhase> TO_BE_RELAUNCHED_JOBS_PHASES = new HashSet<UwsJobPhase>(
			Arrays.asList(new UwsJobPhase[]{UwsJobPhase.PENDING, UwsJobPhase.QUEUED, UwsJobPhase.EXECUTING}));

	public static final String[][] mimeTypesFileExtensions = {
		{"application/octet-stream", ""}
		,{"application/x-votable+xml", "vot"}
		,{"application/json", "json"}
		,{"text/json", "json"}
		,{"text/csv", "csv"}
		,{"text/tab-separated-values", "tsv"}
		,{"text/plain", "txt"}
		,{"application/xml", "xml"}
		,{"text/xml", "xml"}
		,{"application/pdf", "pdf"}
		,{"application/postscript", "ai"}
		,{"application/postscript", "eps"}
		,{"application/postscript", "ps"}
		,{"text/html", "html"}
		,{"application/zip", "zip"}
		,{"application/x-gzip", "gzip"}
		,{"application/x-tar", "gz"}
		,{"image/gif", "tar"}
		,{"image/jpeg", "gif"}
		,{"image/jpeg", "jpeg"}
		,{"image/png", "jpg"}
		,{"image/x-portable-bitmap", "png"}
	};
	
	public static final Map<String,String> MIME_TYPES_FILE_EXTENSIONS = new HashMap<String, String>();
	
	static{
		for(int i = 0; i < mimeTypesFileExtensions.length; i++){
			MIME_TYPES_FILE_EXTENSIONS.put(mimeTypesFileExtensions[i][0], mimeTypesFileExtensions[i][1]);
		}
	}
	
	public static String formatDate(Date d){
		if(d == null){
			return null;
		}else{
			//SimpleDateFormat is not thread safe.
			return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(d);
		}
	}
	
	
	public static Date formatDate(String d) throws ParseException{
		if(d == null){
			return null;
		}else{
			//SimpleDateFormat is not thread safe.
			return new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(d);
		}
	}
	
	public static String getJobResultSubDir(UwsJob job, boolean useOwner){
		String ownerSubDir;
		if(useOwner){
			ownerSubDir = getOwnerSubDir(job.getOwner().getId());
		}else{
			ownerSubDir = "";
		}
		String subDirByDate = job.getLocationId();
		if(subDirByDate == null){
			subDirByDate = getSubDirByDate(System.currentTimeMillis());
		}
		return ownerSubDir + "/" + subDirByDate + "/" + job.getJobId();
	}
	
	public static String getOwnerSubDir(String ownerid){
		if (ownerid == null || ownerid.trim().isEmpty()) {
			return "";
		} else {
			return ownerid.replaceAll(File.separator, "_");
		}
		
	}

	
    /**
    * Returns a directory name based on the specified time.
    * The directory is composed of 'YYYY/MM/DD'.
    * @return a subdirectory composed of 'YYYY/MM/DD'
    */
   public static String getSubDirByDate(long ltime) {
       GregorianCalendar gc = new GregorianCalendar();
       gc.setTimeInMillis(ltime);
       int y = gc.get(GregorianCalendar.YEAR);
       int m = gc.get(GregorianCalendar.MONTH) + 1;
       String strSubDir = null;
       if (m < 10) {
           strSubDir = "" + y + "/0" + m + "/";
       } else {
           strSubDir = "" + y + "/" + m + "/";
       }
       int d = gc.get(GregorianCalendar.DAY_OF_MONTH);
       if (d < 10) {
           return strSubDir + "0" + d;
       }
       return strSubDir + d;
   }
   

	/**
	 * Returns the time based on a directory path (i.e. 2014/01/25)
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirectoryTime(File dir) {
		if (dir == null) {
			return -1;
		}
		String[] items = dir.getAbsolutePath().split("/");
		int day;
		int month;
		int year;
		try{
			day = Integer.parseInt(items[items.length - 1]);
			month = Integer.parseInt(items[items.length - 2]);
			year = Integer.parseInt(items[items.length - 3]);
		}catch(NumberFormatException nfe){
			return -1;
		}
		GregorianCalendar gc = new GregorianCalendar(year, month - 1, day);
		return gc.getTimeInMillis();
	}

	/**
	 * Returns an string with the stack trace.
	 * @param t
	 * @return an string with the stack trace.
	 */
	public static String dumpStackTrace (Throwable t){
		if(t == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(StackTraceElement ste: t.getStackTrace()){
			sb.append(ste).append("\n");
		}
		return sb.toString();
	}
	
	
	public static long getLong(String s) throws UwsException {
		try{
			return Long.parseLong(s);
		}catch(NumberFormatException nfe){
			throw new UwsException("Error parsing long from value: '" + s +"'");
		}
	}

	public static int getInt(String s) throws UwsException {
		try{
			return Integer.parseInt(s);
		}catch(NumberFormatException nfe){
			throw new UwsException("Error parsing integer from value: '" + s +"'");
		}
	}

	public static boolean isJobPending(UwsJobPhase jobPhase){
		return TO_BE_RELAUNCHED_JOBS_PHASES.contains(jobPhase);
	}
	
	
	public static boolean isAnonymous(String userid) {
		if (userid == null) {
			return false;
		} else {
			return ANONYMOUS_USER.equals(userid);
		}
	}

	/**
	 * Checks <code>currentUser</code> permissions.<br/>
	 * If currentUser is admin, the user has enough privileges (returns 'true').<br/>
	 * If currentUser is the same as jobOwner, the user has enough privileges (returns 'true').<br/>
	 * Otherwise, 'false' is returned.
	 * @param jobOwner
	 * @param currentUser
	 * @throws UwsException
	 */
	public static boolean checkValidAccess(UwsJobOwner jobOwner, UwsJobOwner currentUser) {
		if(jobOwner == null){
			//this should not happen
			return false;
		}
		
		//Anonymous: access allowed
		if(isAnonymous(jobOwner.getId())){
			return true;
		}
		
		//Job not anonymous.
		
		//If current user is null (anonymous) deny access.
		if(currentUser == null){
			return false;
		}
		//If current user is Admin: allow access
		if(currentUser.isAdmin()){
			return true;
		}
		//Normal (non anonymous) job: check currentUser is the job owner
		return currentUser.getId().equals(jobOwner.getId());
	}
	
	/**
	 * Tests whether the jobid id belongs to the provided application.<br/>
	 * See {@link #getUniqueJobIdentifier(String)}
	 * <p>A job is valid if its jobid ends with the provided appid.
	 * <p>If appid is null, the jobid is valid.
	 * @param jobid
	 * @param appid
	 * @return
	 */
	public static boolean isJobValidForAppid(String jobid, String appid){
		if(appid == null){
			return true;
		}
		if(jobid == null){
			return false;
		}
		return jobid.endsWith(appid);
	}

	/**
	 * Returns a unique identifier.
	 * @return a unique identifier.
	 */
	public static String getUniqueJobIdentifier(String appid){
		return getUniqueIdentifier(appid);
	}
	
	/**
	 * Returns a unique identifier.
	 * @return a unique identifier.
	 */
	public static String getUniqueIdentifier(String appid){
		return getUniqueIdentifier() + appid;
	}
	
	/**
	 * Returns a unique identifier.
	 * @return a unique identifier.
	 */
	public static synchronized String getUniqueIdentifier(){
		long l = System.currentTimeMillis();
		long t;
		while((t = System.currentTimeMillis()) == l);
		return ""+t;
	}
	
	/**
	 * <p>
	 * Normalizes the given URI.
	 * </p>
	 * <p>
	 * <i><u>Note:</u> A normalized URI always starts with a / and ends with no /.</i>
	 * </p>
	 * 
	 * @param uri
	 *            The URI to normalize.
	 * @return The normalized URI.
	 */
	public static String normalizeURI(String uri) {
		if (uri == null) {
			return null;
		}
		String newUri = uri.trim();
		while (newUri.length() >= 1 && newUri.charAt(newUri.length() - 1) == '/') {
			newUri = newUri.substring(0, newUri.length() - 1).trim();
		}
		if (!newUri.startsWith("/")) {
			return "/" + newUri;
		} else {
			return newUri;
		}
	}

	/**
	 * Returns the suitable file extension based on the provided mime type.<br/>
	 * This method does not contain all the valid mime types. 
	 * It contains the required extensions for the types specified in {@link #MIME_TYPES_FILE_EXTENSIONS}
	 * @param mimeType
	 * @return
	 */
	public static String getFileExtensionForMimeType(String mimeType){
		return MIME_TYPES_FILE_EXTENSIONS.get(mimeType);
	}
	
	/**
	 * Searches for the application identifier {@link UwsConfiguration#CONFIG_APP_ID} in (by order):
	 * <ul>
	 * <li>ServletContext</li>
	 * <li>ServletConfig</li>
	 * </ul>
	 * @param context
	 * @param config
	 * @return
	 */
	public static String getAppIdFromContext(ServletContext context, ServletConfig config){
		String appid = context.getInitParameter(UwsConfiguration.CONFIG_APP_ID);
		if(appid == null || "".equals(appid)){
			appid = config.getInitParameter(UwsConfiguration.CONFIG_APP_ID);
		}
		return appid;
	}
	
	/**
	 * Updates configuration object by copying servlet context configuration parameters. 
	 * @param configuration
	 * @param config
	 */
	public synchronized static void updateConfiguration(UwsConfiguration configuration, ServletContext context){
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>)context.getInitParameterNames();
		String name;
		while(e.hasMoreElements()){
			name = e.nextElement();
			configuration.setProperty(name, context.getInitParameter(name));
		}
	}
	
	/**
	 * Updates configuration object by copying servlet config configuration parameters. 
	 * @param configuration
	 * @param config
	 */
	public synchronized static void updateConfiguration(UwsConfiguration configuration, ServletConfig config){
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>)config.getInitParameterNames();
		String name;
		while(e.hasMoreElements()){
			name = e.nextElement();
			configuration.setProperty(name, config.getInitParameter(name));
		}
	}
	
	/**
	 * Creates a CDATA xml item.
	 * @param data
	 * @return
	 */
	public static String escapeXmlData(String data){
		if(data == null){
			data = "";
		}
		return "<![CDATA["+data+"]]>";
	}
	
	/**
	 * Unescapes a CDATA xml item.<br/>
	 * if the argument does not start with '&lt;![CDATA[' or does not end with ']]&gt;', the argument is returned.
	 * @param cdata
	 * @return
	 */
	public static String unescapeXmlData(String cdata){
		if(cdata == null){
			return null;
		}
		int pIni = cdata.indexOf("<![CDATA[");
		if(pIni < 0){
			return cdata;
		}
		int pEnd = cdata.lastIndexOf("]]>");
		if(pEnd < 0){
			return cdata;
		}
		return cdata.substring(pIni+9, pEnd);
	}
	
	/**
	 * Escapes the value so it can be used as an XML item attribute.
	 * @param value
	 * @return
	 */
	public static String escapeXmlAttribute(final String value) {
		if(value == null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i = 0; i < value.length(); i++) {
			c = value.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&#039;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Unescapes an XML attribute value.
	 * @param xmlAttr
	 * @return
	 */
	public static String unescapeXmlAttribute(final String xmlAttr){
		if(xmlAttr == null){
			return null;
		}
		String value = xmlAttr.replaceAll("&amp;", "&");
		value = value.replaceAll("&lt;", "<");
		value = value.replaceAll("&gt;", ">");
		value = value.replaceAll("&quot;", "\"");
		value = value.replaceAll("&#39;", "\\");
		return value;
	}
	
    /**
     * Only the characters "<" and "&" are strictly illegal in XML. Those characters will be escaped.
     * @param data
     * @return
     */
    public static String escapeText(String data){
    	String replaced = data.replaceAll("&", "&amp;");
    	return replaced.replaceAll("<", "&lt;");
    }
    
    /**
     * All "&lt;" are replaced by "<" and then, all "&amp;" are replaced by "&" 
     * @param data
     * @return
     */
    public static String unescapeText(String data){
    	String replaced = data.replaceAll("&lt;", "<");
    	return replaced.replaceAll("&amp;", "&");
    }

	/**
	 * Encodes a HREF value.
	 * @param ref
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeUrl(String ref) throws UnsupportedEncodingException{
		return URLEncoder.encode(ref, "UTF-8");
	}

	/**
	 * Parses destruction time. If null or invalid, returns {@link UwsConfiguration#DEFAULT_DESTRUCTION_TIME}.
	 * @param destructionTime
	 * @return
	 */
	public static long parseDestructionTime(String destructionTime){
		long lOlderThanTime;
		if(destructionTime == null){
			lOlderThanTime = UwsConfiguration.DEFAULT_DELTA_DESTRUCTION_TIME;
		}else{
			try{
				lOlderThanTime = Long.parseLong(destructionTime);
			}catch(NumberFormatException nfe){
				lOlderThanTime = UwsConfiguration.DEFAULT_DELTA_DESTRUCTION_TIME;
			}
		}
		return lOlderThanTime;
	}
	
	
	/**
	 * Waits until a job thread is terminated (Thread state 'TERMINATED')
	 * @param jobThread
	 * @throws UwsException
	 */
	public static void waitUntilThreadIsTerminated(UwsJobThread jobThread) throws InterruptedException {
		if(jobThread == null){
			return;
		}
		while(true){
			//wait while the thread is running.
			//if(jobThread.getState() == Thread.State.TERMINATED || jobThread.getState() == Thread.State.BLOCKED){
			if(jobThread.getState() != Thread.State.RUNNABLE){
				//Can be: NEW, TERMINATED, BLOCKED, WAITING, TIME_WAITING
				break;
			}
			Thread.sleep(100);
		}
	}
	
	/**
	 * Removes all files recursivelly
	 * @param f
	 */
	public static long clearDirectory(File f){
		if(f == null){
			return 0;
		}
		long size = 0;
		if(f.isDirectory()){
			File[] files = f.listFiles();
			if(files != null){
				for(File fTmp: files){
					size += clearDirectory(fTmp);
				}
			}
		}
		size += f.length();
		f.delete();
		return size;
	}
	

	/**
	 * Returns a manager. Waits until the manager is ready.
	 * @return
	 */
	public static  UwsManager getManager(){
		UwsManager manager = null;
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			try{
				manager = UwsManager.getInstance();
				break;
			}catch(Exception e){
				//ignore: the manager is not ready yet.
			}
		}
		return manager;
	}
	
	
	public static void updateSharedItemsIfRequired(UwsShareItemBase sharedItem){
		if(sharedItem == null){
			return;
		}
		List<UwsShareItem> sharedItems = sharedItem.getShareToItems();
		if(sharedItems == null){
			return;
		}
		for(UwsShareItem si: sharedItems){
			si.setResourceId(sharedItem.getResourceId());
			si.setResourceType(sharedItem.getResourceType());
			si.setOwnerId(sharedItem.getOwnerid());
		}
	}
	
	public static Set<String> getUniqueGroupsFromSharedToItems(List<UwsShareItem> sharedToItems){
		if(sharedToItems == null){
			return null;
		}
		Set<String> uniqueIds = new TreeSet<String>();
		for(UwsShareItem si: sharedToItems){
			if(si.getShareType() == UwsShareType.Group){
				uniqueIds.add(si.getShareToId());
			}
		}
		return uniqueIds;
	}
	
	public static Set<String> getUniqueUsersFromSharedToItems(List<UwsShareItem> sharedToItems){
		if(sharedToItems == null){
			return null;
		}
		Set<String> uniqueIds = new TreeSet<String>();
		for(UwsShareItem si: sharedToItems){
			if(si.getShareType() == UwsShareType.User){
				uniqueIds.add(si.getShareToId());
			}
		}
		return uniqueIds;
	}
	
}
