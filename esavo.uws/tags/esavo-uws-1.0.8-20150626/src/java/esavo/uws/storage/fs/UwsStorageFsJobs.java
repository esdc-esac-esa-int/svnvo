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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import esavo.uws.UwsException;
import esavo.uws.creator.UwsCreator;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobResultMeta.ResultType;
import esavo.uws.jobs.parameters.UwsJobParameters;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobDetailsComparison;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsErrorType;
import esavo.uws.utils.UwsParameterValueType;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.xml.UwsXmlConstants;
import esavo.uws.utils.xml.UwsXmlManager;

/**
 * 
 * Jobs Metadata handler.
 * Each job has 4 files:
 * <ul>
 * <li>data.props: contains job main attributes (parameters are saved in params.xml)</li>
 * <li>params.xml: job parameters</li>
 * <li>results.xml: job results metadata (the real results are under the job data tree)</li>
 * <li>error.xml: job error metadata (the error file is under the job data tree)</li>
 * </ul>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStorageFsJobs {
	
	public static final String PARAMETER_COMMON = "COMMON";

	public static final String JOB_FILE_NAME = "data.props";
	public static final String JOB_PARAMS_FILE_NAME = "params.xml";
	public static final String JOB_RESULTS_FILE_NAME = "results.xml";
	public static final String JOB_ERROR_FILE_NAME = "error.xml";
	
	public static final String JOB_PROPERTY_JOB_ID = "job_id";
	public static final String JOB_PROPERTY_LIST_ID = "list_id";
	public static final String JOB_PROPERTY_OWNER_ID = "owner_id";
	public static final String JOB_PROPERTY_SESSION_ID = "session_id";
	public static final String JOB_PROPERTY_PHASE_ID = "phase_id";
	public static final String JOB_PROPERTY_JOB_NAME = "job_name";
	public static final String JOB_PROPERTY_QUOTE = "quote";
	public static final String JOB_PROPERTY_START_TIME = "start_time";
	public static final String JOB_PROPERTY_END_TIME = "end_time";
	public static final String JOB_PROPERTY_DESTRUCTION_TIME = "destruction_time";
	public static final String JOB_PROPERTY_EXECUTION_DURATION = "execution_duration";
	public static final String JOB_PROPERTY_RELATIVE_PATH = "relative_path";
	public static final String JOB_PROPERTY_PRIORITY = "priority";
	
	private File baseJobsMetadataDir;
	private UwsStorageFsOwners ownersStorage;
	private UwsCreator creator;
	
	public UwsStorageFsJobs(File baseJobsMetadataDir, UwsStorageFsOwners ownersStorage, UwsCreator creator) {
		this.baseJobsMetadataDir = baseJobsMetadataDir;
		this.ownersStorage = ownersStorage;
		this.creator = creator;
	}
	
	public synchronized UwsJob loadJobMeta (String jobid) throws IOException {
		File f = new File(baseJobsMetadataDir, jobid);
		File fData = new File(f, getJobDataFileName(jobid));
		if (fData.exists()) {
			Properties p = createPropertiesForJob(fData);
			if(p == null){
				return null;
			}
			return loadJobInternal(jobid, p, f);
		}
		// Not found.
		return null;
	}
	
	public synchronized boolean checkJobExists (String jobid) throws IOException {
		File f = new File(baseJobsMetadataDir, jobid);
		File fData = new File(f, getJobDataFileName(jobid));
		if (fData.exists()) {
			Properties p = createPropertiesForJob(fData);
			if(p == null){
				return false;
			}
			UwsJob job = loadJobInternal(jobid, p, f);
			return job != null;
		}
		// Not found.
		return false;
	}
	
	public List<UwsJobDetails> retrieveJobsByFilter(UwsJobsFilter filter, long offset, long limit) throws IOException {
		//get all jobs and search parameters
		List<File> jobs = new ArrayList<File>();
		findAllJobs(jobs, baseJobsMetadataDir);
		
		long l = jobs.size();
		long size = l-offset;
		if(size > limit){
			size = limit;
		}
		if(size < 0){
			size = 0;
		}
		List<UwsJobDetails> details = new ArrayList<UwsJobDetails>();
		for(long i = offset; i < size; i++){
			UwsJob job = loadJobMeta(jobs.get((int)i).getName());
			//filter
			if(isValidJob(job, filter)){
				UwsJobDetails jobDetails = new UwsJobDetails();
				jobDetails.setJobid(job.getJobId());
				jobDetails.setOwnerid(job.getOwner().getId());
				jobDetails.setPhaseid(job.getPhase().name());
				jobDetails.setStartTime(job.getStartTime().getTime());
				jobDetails.setEndTime(job.getEndTime().getTime());
				jobDetails.setRelativePath(job.getLocationId());
				jobDetails.setQuery(job.getParameters().getStringParameter("query"));
				details.add(jobDetails);
			}
		}
		return details;
	}
	
	public List<String> retrieveOldJobs(String appid, long currentTime) throws IOException {
		//get all jobs and search parameters
		List<File> jobs = new ArrayList<File>();
		findAllJobs(jobs, baseJobsMetadataDir);
		
		List<String> jobIds = new ArrayList<String>();
		for(long i = 0; i < jobs.size(); i++){
			UwsJob job = loadJobMeta(jobs.get((int)i).getName());
			//filter
			if(isValidJobByTime(job, appid, currentTime)){
				jobIds.add(job.getJobId());
			}
		}
		return jobIds;
	}
	
	private void findAllJobs(List<File> jobs, File f){
		File[] fTmp = f.listFiles();
		for(File fItem: fTmp){
			if(fItem.isDirectory()){
			jobs.add(fItem);
			}
		}
	}
	
	private boolean isValidJob(UwsJob job, UwsJobsFilter filter){
		if(filter == null){
			return true;
		}
		UwsJobDetailsComparison details = filter.getJobFilter();
		if(filter.hasFilterByJobId()){
			if(!isValidJobStringFilter(job.getJobId(), details.getJobid(), filter.isJobidComparisonLike())){
				return false;
			}
		}
		if(filter.hasFilterByOwnerId()){
			if(!isValidJobStringFilter(job.getOwner().getId(), details.getOwnerid(), filter.isOwneridComparisonLike())){
				return false;
			}
		}
		if(filter.hasFilterByPhaseid()){
			if(!isValidJobStringFilter(job.getPhase().name(), details.getPhaseid(), filter.isPhaseidComparisonLike())){
				return false;
			}
		}
		if(filter.hasFilterByQuery()){
			UwsJobParameters params = job.getParameters();
			if(params == null){
				return false;
			}
			if(!isValidJobStringFilter(params.getStringParameter("query"), details.getQuery(), filter.isQueryComparisonLike())){
				return false;
			}
		}
		if(filter.hasFilterByStartTime() || filter.hasFilterByStartTimeLimit()){
			if(!isValidJobTimeFilter(job.getStartTime().getTime(), details.getStartTime(), details.getStartTimeLimit(), filter.hasFilterByStartTime(), filter.hasFilterByStartTimeLimit())){
				return false;
			}
		}
		if(filter.hasFilterByEndTime() || filter.hasFilterByEndTimeLimit()){
			if(!isValidJobTimeFilter(job.getEndTime().getTime(), details.getEndTime(), details.getEndTimeLimit(), filter.hasFilterByEndTime(), filter.hasFilterByEndTimeLimit())){
				return false;
			}
		}
		return true;
	}
	
	private boolean isValidJobStringFilter(String value, String filter, boolean like) {
		if(value == null){
			return filter == null; 
		}
		if (like) {
			return value.equalsIgnoreCase(filter);
		} else {
			return value.equals(filter);
		}
	}

	private boolean isValidJobTimeFilter(long date, long init, long end, boolean hasInit, boolean hasEnd) {
		if (hasInit && hasEnd) {
			return date >= init && date <= end;
		} else if (hasInit) {
			return date >= init;
		} else if (hasEnd) {
			return date <= end;
		} else {
			return true;
		}
	}

	private boolean isValidJobByTime(UwsJob job, String appid, long currentTime) {
		long destructionTime = job.getDestructionTime().getTime();
		if(destructionTime <= 0){
			return false;
		}
		return destructionTime < currentTime;
	}
	
	private UwsJob loadJobInternal(String jobid, Properties p, File fJobBase) throws IOException{
		String listid = p.getProperty(JOB_PROPERTY_LIST_ID);
		String locationid = p.getProperty(JOB_PROPERTY_RELATIVE_PATH);
		String phase = p.getProperty(JOB_PROPERTY_PHASE_ID);
		long quote = UwsStorageFsUtils.getLong(p.getProperty(JOB_PROPERTY_QUOTE), "Cannot get quote for job '"+jobid+"'");
		long startTime = UwsStorageFsUtils.getLong(p.getProperty(JOB_PROPERTY_START_TIME), "Cannot get start time for job '"+jobid+"'");
		long endTime = UwsStorageFsUtils.getLong(p.getProperty(JOB_PROPERTY_END_TIME), "Cannot get end time for job '"+jobid+"'");
		long destructionTime = UwsStorageFsUtils.getLong(p.getProperty(JOB_PROPERTY_DESTRUCTION_TIME), "Cannot get destruction time for job '"+jobid+"'");
		long executionDuration = UwsStorageFsUtils.getLong(p.getProperty(JOB_PROPERTY_EXECUTION_DURATION), "Cannot get execution duration for job '"+jobid+"'");
		int priority = UwsStorageFsUtils.getInteger(p.getProperty(JOB_PROPERTY_PRIORITY), "Cannot get priority for job '"+jobid+"'");
		String ownerid = p.getProperty(JOB_PROPERTY_OWNER_ID);
		List<UwsJobResultMeta> results = loadJobResultsMeta(jobid, fJobBase);
		UwsJobParameters params = loadJobParameters(jobid, fJobBase);
		UwsJobErrorSummaryMeta error = loadJobErrorSummaryMeta(jobid, fJobBase);
		UwsJobOwner owner = ownersStorage.loadOwner(ownerid);
		UwsJob job;
		
		try{
			job = creator.createJob(jobid, owner, listid, locationid, results, priority);
		}catch(UwsException e){
			throw new IOException("Cannot create job: '" + jobid + "' due to: " + e.getMessage(), e);
		}
		
		//disable notifications
		job.setEnableUpdates(false);
		
		//set data
		try{
			job.setPhase(UwsJobPhase.valueOf(phase));
			job.setQuote(quote);
			//job.setQuote(UwsStorageFsUtils.getDateFromStorage(quote));
			job.setStartTime(UwsStorageFsUtils.getDateFromStorage(startTime));
			job.setEndTime(UwsStorageFsUtils.getDateFromStorage(endTime));
			job.setDestructionTime(UwsStorageFsUtils.getDateFromStorage(destructionTime));
			job.setExecutionDuration(executionDuration);
			job.setParameters(params);
			job.setErrorSummary(error);
		}catch(UwsException e){
			//Cannot happen. The UwsException is raised when the notification mechanism fails.
			//The notification mechaism is disabled (setEnabledNotification(false)) so, UwsException cannot appear. 
		}
		
		//enable notifications
		job.setEnableUpdates(true);
		
		return job;
	}
	
	private Properties createPropertiesForJob(File fData) throws IOException {
		Properties p = new Properties();
		InputStream reader = null;
		try{
			reader = new FileInputStream(fData);
			p.loadFromXML(reader);
		} finally {
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}
	
	private List<UwsJobResultMeta> loadJobResultsMeta(String jobid, File fJobBase) throws IOException{
		File fResults = new File(fJobBase, getJobResultsFileName(jobid));
		if(fResults.exists()){
			UwsXmlManager xmlManager = new UwsXmlManager(fResults);
			NodeList nl = xmlManager.getRootElement().getElementsByTagName(UwsXmlConstants.XML_ELEMENT_RESULT);
			int size = nl.getLength();
			Element eResult;
			List<UwsJobResultMeta> results = new ArrayList<UwsJobResultMeta>();
			String resultid;
			for(int i = 0; i < size; i++){
				eResult = (Element) nl.item(i);
				resultid = eResult.getAttribute(UwsXmlConstants.XML_ATTR_RESULT_ID);
				UwsJobResultMeta result = new UwsJobResultMeta(resultid);
				results.add(result);
				result.setMimeType(eResult.getAttribute(UwsXmlConstants.XML_ATTR_MIME_TYPE));
				result.setRows(UwsStorageFsUtils.getLong(
					eResult.getAttribute(UwsXmlConstants.XML_ATTR_ROWS),
					"Cannot parse rows for result '"+resultid+"' (job: '"+jobid+"')"));
				result.setSize(UwsStorageFsUtils.getLong(
					eResult.getAttribute(UwsXmlConstants.XML_ATTR_SIZE),
					"Cannot parse size for result '"+resultid+"' (job: '"+jobid+"')"));
				result.setType(ResultType.valueOf(eResult.getAttribute(UwsXmlConstants.XML_ATTR_TYPE)));
			}
			return results;
		}else{
			return null;
		}
	}
	
	private UwsJobParameters loadJobParameters(String jobid, File fJobBase) throws IOException{
		File fParameters = new File(fJobBase, getJobParametersFileName(jobid));
		if(fParameters.exists()){
			UwsXmlManager xmlManager = new UwsXmlManager(fParameters);
			NodeList nl = xmlManager.getRootElement().getElementsByTagName(UwsXmlConstants.XML_ELEMENT_PARAMETER);
			int size = nl.getLength();
			Element eParameter;
			UwsJobParameters parameters = new UwsJobParameters();
			String parameterid;
			Object value;
			for(int i = 0; i < size; i++){
				eParameter = (Element) nl.item(i);
				parameterid = UwsUtils.unescapeXmlAttribute(eParameter.getAttribute(UwsXmlConstants.XML_ATTR_PARAMETER_ID));
				value = getParameterValue(eParameter);
				try {
					parameters.setParameter(parameterid, value);
				} catch (UwsException e) {
					throw new IOException(e);
				}
			}
			return parameters;
		}else{
			return null;
		}
	}
	
	private Object getParameterValue(Element eParameter){
		//String parameterType = eParameter.getAttribute(XmlConstants.XML_ATTR_PARAMETER_TYPE);
		String dataType = eParameter.getAttribute(UwsXmlConstants.XML_ATTR_DATA_TYPE);
		String stringRepresentation = UwsUtils.unescapeXmlData(eParameter.getTextContent());
		Object value = UwsJobParameters.getParameterValue(dataType, stringRepresentation);
		return value;
	}
	
	
	private UwsJobErrorSummaryMeta loadJobErrorSummaryMeta(String jobid, File fJobBase) throws IOException{
		File fError = new File(fJobBase, getJobErrorFileName(jobid));
		if(fError.exists()){
			UwsXmlManager xmlManager = new UwsXmlManager(fError);
			Element root = xmlManager.getRootElement();
			String message = UwsUtils.unescapeXmlData(root.getTextContent());
			boolean hasDetails = Boolean.parseBoolean(root.getAttribute(UwsXmlConstants.XML_ATTR_HAS_DETAILS));
			UwsErrorType type = UwsErrorType.valueOf(root.getAttribute(UwsXmlConstants.XML_ATTR_TYPE));
			String detailsMimeType = root.getAttribute(UwsXmlConstants.XML_ATTR_MIME_TYPE);
			long detailsSize = Long.parseLong(root.getAttribute(UwsXmlConstants.XML_ATTR_SIZE));
			UwsJobErrorSummaryMeta error = new UwsJobErrorSummaryMeta(message, type, hasDetails, detailsMimeType, detailsSize);
			return error;
		}else{
			return null;
		}
	}
	
	
	public synchronized void addJobResultMeta(String jobid, UwsJobResultMeta res) throws IOException {
		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File fResults = new File(fJobBase, getJobResultsFileName(jobid));
		UwsXmlManager xmlManager;
		if(fResults.exists()){
			xmlManager = new UwsXmlManager(fResults);
		}else{
			xmlManager = new UwsXmlManager(UwsXmlConstants.XML_ELEMENT_RESULTS);
		}
		Element eRoot = xmlManager.getRootElement();
		Element eResult = xmlManager.createElement(UwsXmlConstants.XML_ELEMENT_RESULT);
		eRoot.appendChild(eResult);
		
		eResult.setAttribute(UwsXmlConstants.XML_ATTR_RESULT_ID, res.getId());
		eResult.setAttribute(UwsXmlConstants.XML_ATTR_TYPE, res.getType().name());
		eResult.setAttribute(UwsXmlConstants.XML_ATTR_MIME_TYPE, res.getMimeType());
		eResult.setAttribute(UwsXmlConstants.XML_ATTR_SIZE, ""+res.getSize());
		eResult.setAttribute(UwsXmlConstants.XML_ATTR_ROWS, ""+res.getRows());
		
		xmlManager.writeXmlFile(fResults);
	}
	
	public synchronized void addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws IOException {
		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File fError = new File(fJobBase, getJobErrorFileName(jobid));

		UwsXmlManager xmlManager = new UwsXmlManager(UwsXmlConstants.XML_ELEMENT_ERROR);
		Element eRoot = xmlManager.getRootElement();
		eRoot.setAttribute(UwsXmlConstants.XML_ATTR_TYPE, errorSummary.getType().name());
		eRoot.setAttribute(UwsXmlConstants.XML_ATTR_HAS_DETAILS, ""+errorSummary.hasDetails());
		eRoot.setAttribute(UwsXmlConstants.XML_ATTR_MIME_TYPE, errorSummary.getDetailsMimeType());
		eRoot.setAttribute(UwsXmlConstants.XML_ATTR_SIZE, ""+errorSummary.getDetailsSize());
		String escapedMessage = UwsUtils.escapeXmlData(errorSummary.getMessage());
		eRoot.appendChild(xmlManager.getDocument().createTextNode(escapedMessage));
		
		xmlManager.writeXmlFile(fError);
	}
	
	public synchronized void updateJobAttribute(UwsJob job) throws IOException {
		String jobid = job.getJobId();
		File f = new File(baseJobsMetadataDir, jobid);
		File fData = new File(f, getJobDataFileName(jobid));
		Properties p;
		if (fData.exists()) {
			p = createPropertiesForJob(fData);
			if(p == null){
				p = new Properties();
			}
		}else{
			if(!f.exists()){
				f.mkdirs();
			}
			p = new Properties();
		}
		p.put(JOB_PROPERTY_LIST_ID, job.getListid());
		p.put(JOB_PROPERTY_RELATIVE_PATH, job.getLocationId());
		p.put(JOB_PROPERTY_PHASE_ID, job.getPhase().name());
		p.put(JOB_PROPERTY_QUOTE, ""+job.getQuote());
		//p.put(JOB_PROPERTY_QUOTE, ""+UwsStorageFsUtils.getDateToSave(job.getQuote()));
		p.put(JOB_PROPERTY_START_TIME, ""+ UwsStorageFsUtils.getDateToSave(job.getStartTime()));
		p.put(JOB_PROPERTY_END_TIME, ""+UwsStorageFsUtils.getDateToSave(job.getEndTime()));
		p.put(JOB_PROPERTY_DESTRUCTION_TIME, ""+UwsStorageFsUtils.getDateToSave(job.getDestructionTime()));
		p.put(JOB_PROPERTY_EXECUTION_DURATION, ""+job.getExecutionDuration());
		p.put(JOB_PROPERTY_OWNER_ID, job.getOwner().getId());
		p.put(JOB_PROPERTY_PRIORITY, ""+job.getPriority());
		OutputStream os = null;
		try{
			os = new FileOutputStream(fData);
			p.storeToXML(os, "UwsStorageFileSystem");
			os.flush();
		}finally{
			os.close();
		}
	}
	
	
	public synchronized void updateAllParameters(UwsJob job) throws IOException {
		String jobid = job.getJobId();

		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File fParameters = new File(fJobBase, getJobParametersFileName(jobid));
		
		UwsXmlManager xmlManager = new UwsXmlManager(UwsXmlConstants.XML_ELEMENT_PARAMETERS);
		Element eRoot = xmlManager.getRootElement();

		UwsJobParameters parameters = job.getParameters();
		if(parameters != null){
			Set<String> params = parameters.getParameterNames();
			String escaped;
			for (String p : params) {
				Element eParam = xmlManager.createElement(UwsXmlConstants.XML_ELEMENT_PARAMETER);
				eRoot.appendChild(eParam);
	
				Object o = parameters.getParameter(p);
				UwsParameterValueType pvt = UwsJobParameters.getParameterValueType(o);
	
				eParam.setAttribute(UwsXmlConstants.XML_ATTR_DATA_TYPE, pvt.name());
				escaped = UwsUtils.escapeXmlData(UwsJobParameters.getParameterStringRepresentation(pvt, o));
				eParam.appendChild(xmlManager.getDocument().createTextNode(escaped));
				eParam.setAttribute(UwsXmlConstants.XML_ATTR_PARAMETER_ID, UwsUtils.escapeXmlAttribute(p));
				eParam.setAttribute(UwsXmlConstants.XML_ATTR_PARAMETER_TYPE, PARAMETER_COMMON);
			}
		}
		
		xmlManager.writeXmlFile(fParameters);
	}

	/**
	 * Returns true if the parameter is removed.
	 * @param jobid
	 * @param parameterid
	 * @return
	 * @throws SQLException
	 */
	public synchronized boolean removeParameter(UwsJob job, String parameterid) throws IOException{
		String jobid = job.getJobId();

		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File fParameters = new File(fJobBase, getJobParametersFileName(jobid));
		
		UwsXmlManager xmlManager = new UwsXmlManager(UwsXmlConstants.XML_ELEMENT_PARAMETERS);
		Element eRoot = xmlManager.getRootElement();
		
		NodeList nl = eRoot.getElementsByTagName(UwsXmlConstants.XML_ELEMENT_PARAMETER);
		int size = nl.getLength();
		Element eParameter;
		boolean found = false;
		String param;
		for(int i = 0; i < size; i++){
			eParameter = (Element)nl.item(i);
			param = UwsUtils.unescapeXmlAttribute(eParameter.getAttribute(UwsXmlConstants.XML_ATTR_PARAMETER_ID));
			if(parameterid.equals(param)){
				found = true;
				eRoot.removeChild(eParameter);
				break;
			}
		}
		if(found){
			xmlManager.writeXmlFile(fParameters);
		}
		return found;
	}
	
	/**
	 * This method checks whether the job already exists (useful when restoring jobs from database: the job already exists)
	 * @param Job
	 * @throws SQLException
	 */
	public synchronized boolean addNewJobMetaIfNeeded(UwsJob job) throws IOException {
		String jobid = job.getJobId();
		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File fData = new File(fJobBase, getJobDataFileName(jobid));
		if (!fData.exists()) {
			if(!fJobBase.exists()){
				fJobBase.mkdirs();
			}
			//Not necessary because UwsJobsOwnersManager creates the owner.
			//ownersStorage.addOwnerIfRequired(job.getOwner());
			ownersStorage.addJobToOwner(job.getOwner().getId(), jobid);
			updateJobAttribute(job);
			updateAllParameters(job);
			return true;
		} else {
			// already exists
			return false;
		}
	}
	
	public int getNumJobs(){
		return baseJobsMetadataDir.listFiles().length;
	}
	
	
	/**
	 * Removes job output meta (Results meta and ErrorSummary meta).<br/>
	 * Job main data (attributes) are not removed.<br/>
	 * Job parameters are not removed.<br/>
	 * This method is useful for restarting a job.
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public synchronized boolean removeJobOutputMeta (String jobid) throws IOException {
		//results meta + error summary meta
		deleteErrorSummaryMeta(jobid);
		deleteResultsMeta(jobid);
		return true;
	}
	
	/**
	 * Removes a job: metadata, parameters, results meta and error meta
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public synchronized boolean removeJobAndAssociatedMeta(String jobid) throws IOException {
		File fJobBase = new File(baseJobsMetadataDir, jobid);
		UwsStorageFsUtils.deleteDirectory(fJobBase);
		return true;
	}

	private void deleteErrorSummaryMeta(String jobid){
		deleteFile(jobid, getJobErrorFileName(jobid));
	}

	private void deleteResultsMeta(String jobid){
		deleteFile(jobid, getJobResultsFileName(jobid));
	}

	private void deleteParametersMeta(String jobid){
		deleteFile(jobid, getJobParametersFileName(jobid));
	}

	private void deleteJobDataMeta(String jobid){
		deleteFile(jobid, getJobDataFileName(jobid));
	}
	
	private void deleteFile(String jobid, String fileName){
		File fJobBase = new File(baseJobsMetadataDir, jobid);
		File f = new File(fJobBase, fileName);
		f.delete();
	}

	
	private String getJobDataFileName(String jobid){
		return jobid + '_' +JOB_FILE_NAME;
	}
	
	private String getJobParametersFileName(String jobid){
		return jobid + '_' + JOB_PARAMS_FILE_NAME;
	}
	
	private String getJobResultsFileName(String jobid){
		return jobid + '_' + JOB_RESULTS_FILE_NAME;
	}
	
	private String getJobErrorFileName(String jobid){
		return jobid + '_' + JOB_ERROR_FILE_NAME;
	}
	
}
