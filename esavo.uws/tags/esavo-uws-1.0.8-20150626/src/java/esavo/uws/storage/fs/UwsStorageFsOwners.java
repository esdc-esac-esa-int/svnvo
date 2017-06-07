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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import esavo.uws.UwsException;
import esavo.uws.jobs.parameters.UwsJobOwnerParameters;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.utils.UwsParameterValueType;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.xml.UwsXmlConstants;
import esavo.uws.utils.xml.UwsXmlManager;

/**
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStorageFsOwners {
	
	public static final String OWNER_FILE_NAME = "data.props";
	public static final String OWNER_PARAMETERS_FILE_NAME = "parameters";
	public static final String OWNER_JOBS_FILE_NAME = ".jobs";
	
	public static final String OWNER_PROPERTY_OWNER_ID = "owner_id";
	public static final String OWNER_PROPERTY_SESSION = "session";
	public static final String OWNER_PROPERTY_PSEUDO = "pseudo";
	public static final String OWNER_PROPERTY_AUTH_USER_NAME = "auth";
	public static final String OWNER_PROPERTY_ROLES = "roles";
	
	private File baseOwnersDir;
	
	public UwsStorageFsOwners(File baseOwnersDir){
		this.baseOwnersDir = baseOwnersDir;
	}
	
	public synchronized UwsJobOwner loadOwner(String ownerid) throws IOException {
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerDataFileName(ownerid));
		if (fData.exists()) {
			Properties p = createPropertiesForOwner(fData);
			if(p == null){
				return null;
			}
			int roles = UwsStorageFsUtils.getInteger(p.getProperty(OWNER_PROPERTY_ROLES), "Cannot parse roles for owner '"+ownerid+"'");
			UwsJobOwner owner = new UwsJobOwner(p.getProperty(OWNER_PROPERTY_OWNER_ID), roles);
			String tmp;
			tmp = UwsStorageFsUtils.getStringFromProperty(p.getProperty(OWNER_PROPERTY_AUTH_USER_NAME));
			owner.setAuthUsername(tmp);
			tmp = UwsStorageFsUtils.getStringFromProperty(p.getProperty(OWNER_PROPERTY_PSEUDO));
			owner.setPseudo(tmp);
			UwsJobOwnerParameters parameters = loadOwnerParameters(ownerid, fData);
			owner.setParameters(parameters);
			return owner;
		}
		// Not found.
		return null;
	}
	
	public List<UwsJobOwner> retrieveOwners(final UwsJobsOwnersFilter filter, long offset, long limit) throws IOException {
		File[] ownerFiles;
		if(filter != null && filter.hasIdFilter()){
			ownerFiles = baseOwnersDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().contains(filter.getIdFilter());
				}
			});
		}else{
			ownerFiles = baseOwnersDir.listFiles();
		}
		long l = ownerFiles.length;
		long size = l - offset;
		if (size > limit){
			size = limit;
		}
		if(size < 0){
			size = 0;
		}
		List<UwsJobOwner> owners = new ArrayList<UwsJobOwner>();
		for(long i = offset; i < size; i++){
			owners.add(loadOwner(ownerFiles[(int)i].getName()));
		}
		return owners;
	}
	
	private Properties createPropertiesForOwner(File fData) throws IOException {
		Properties p = new Properties();
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(fData);
			p.loadFromXML(fis);
		} finally {
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}
	
	private UwsJobOwnerParameters loadOwnerParameters(String ownerid, File fJobBase) throws IOException{
		File fParameters = new File(fJobBase, getOwnerParametersFileName(ownerid));
		if(fParameters.exists()){
			UwsXmlManager xmlManager = new UwsXmlManager(fParameters);
			NodeList nl = xmlManager.getRootElement().getElementsByTagName(UwsXmlConstants.XML_ELEMENT_PARAMETER);
			int size = nl.getLength();
			Element eParameter;
			UwsJobOwnerParameters parameters = new UwsJobOwnerParameters();
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
		String textNode = eParameter.getTextContent();
		//String stringRepresentation = XmlManager.getCDataFromNode(eParameter);
		String stringRepresentation = UwsUtils.unescapeXmlData(textNode);
		Object value = UwsJobOwnerParameters.getParameterValue(dataType, stringRepresentation);
		return value;
	}
	
	public synchronized List<String> getJobs(String ownerid) throws IOException{
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerJobsFileName(ownerid));
		if (fData.exists()) {
			BufferedReader br = null;
			List<String> jobs = new ArrayList<String>();
			String line;
			try{
				br = new BufferedReader(new FileReader(fData));
				while((line = br.readLine()) != null){
					jobs.add(line.trim());
				}
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return jobs;
		}
		//not found
		return null;
	}
	
	public synchronized void addJobToOwner(String ownerid, String jobid) throws IOException {
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerJobsFileName(ownerid));
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(fData, true));
			bw.append(jobid+'\n');
		}finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void addOwner(UwsJobOwner owner) throws IOException {
		String ownerid = owner.getId();
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerDataFileName(ownerid));
		if(fData.exists()){
			throw new IOException("Onwer '"+owner.getId()+"' already exists at: " + fData.getAbsolutePath());
		}
		if (!f.exists()) {
			f.mkdirs();
		}
		Properties p = new Properties();
		p.put(OWNER_PROPERTY_OWNER_ID, owner.getId());
		p.put(OWNER_PROPERTY_AUTH_USER_NAME, UwsStorageFsUtils.getStringForProperty(owner.getAuthUsername()));
		p.put(OWNER_PROPERTY_PSEUDO, UwsStorageFsUtils.getStringForProperty(owner.getPseudo()));
		p.put(OWNER_PROPERTY_ROLES, "" + owner.getRoles());
		FileOutputStream fos = new FileOutputStream(fData);
		try {
			p.storeToXML(fos, "Owner file");
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		addOwnerParameters(owner);
	}

	
	public synchronized void updateOwner(UwsJobOwner owner) throws IOException {
		String ownerid = owner.getId();
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerDataFileName(ownerid));
		if(!fData.exists()){
			throw new IOException("Onwer '"+owner.getId()+"' not found at: " + fData.getAbsolutePath());
		}
		if (!f.exists()) {
			f.mkdirs();
		}
		Properties p = new Properties();
		p.put(OWNER_PROPERTY_OWNER_ID, owner.getId());
		p.put(OWNER_PROPERTY_AUTH_USER_NAME, UwsStorageFsUtils.getStringForProperty(owner.getAuthUsername()));
		p.put(OWNER_PROPERTY_PSEUDO, UwsStorageFsUtils.getStringForProperty(owner.getPseudo()));
		p.put(OWNER_PROPERTY_ROLES, "" + owner.getRoles());
		FileOutputStream fos = new FileOutputStream(fData);
		try {
			p.storeToXML(fos, "Owner file");
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		//Recreate parameters
		addOwnerParameters(owner);
	}
	
	public synchronized void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws IOException {
		String ownerid = owner.getId();
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerDataFileName(ownerid));
		if(!fData.exists()){
			throw new IOException("Onwer '"+owner.getId()+"' not found at: " + fData.getAbsolutePath());
		}
		//dump all parameters
		addOwnerParameters(owner);
	}
	
	public synchronized void updateOwnerRoles(UwsJobOwner owner) throws IOException {
		String ownerid = owner.getId();
		File f = new File(baseOwnersDir, ownerid);
		File fData = new File(f, getOwnerDataFileName(ownerid));
		if(!fData.exists()){
			throw new IOException("Onwer '"+owner.getId()+"' not found at: " + fData.getAbsolutePath());
		}
		Properties p = new Properties();
		p.put(OWNER_PROPERTY_OWNER_ID, owner.getId());
		p.put(OWNER_PROPERTY_AUTH_USER_NAME, UwsStorageFsUtils.getStringForProperty(owner.getAuthUsername()));
		p.put(OWNER_PROPERTY_PSEUDO, UwsStorageFsUtils.getStringForProperty(owner.getPseudo()));
		p.put(OWNER_PROPERTY_ROLES, "" + owner.getRoles());
		FileOutputStream fos = new FileOutputStream(fData);
		try {
			p.storeToXML(fos, "Owner file");
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	
//	public synchronized void addOwnerIfRequired(UwsJobOwner owner) throws IOException{
//		String ownerid = owner.getId();
//		File f = new File(baseOwnersDir, ownerid);
//		File fData = new File(f, getOwnerDataFileName(ownerid));
//		if(!fData.exists()){
//			if(!f.exists()){
//				f.mkdirs();
//			}
//			Properties p = new Properties();
//			p.put(OWNER_PROPERTY_OWNER_ID, owner.getId());
//			p.put(OWNER_PROPERTY_AUTH_USER_NAME, UwsStorageFsUtils.getStringForProperty(owner.getAuthUsername()));
//			p.put(OWNER_PROPERTY_PSEUDO, UwsStorageFsUtils.getStringForProperty(owner.getPseudo()));
//			p.put(OWNER_PROPERTY_ROLES, ""+owner.getRoles());
//			FileOutputStream fos = new FileOutputStream(fData);
//			try{
//				p.storeToXML(fos, "Owner file");
//			}finally{
//				if(fos != null){
//					fos.close();
//				}
//			}
//			addOwnerParameters(owner);
//		}
//	}
	
	private void addOwnerParameters(UwsJobOwner owner) throws IOException {
		String ownerid = owner.getId();

		File fOwnerBase = new File(baseOwnersDir, ownerid);
		File fParameters = new File(fOwnerBase, getOwnerParametersFileName(ownerid));
		
		UwsXmlManager xmlManager = new UwsXmlManager(UwsXmlConstants.XML_ELEMENT_PARAMETERS);
		Element eRoot = xmlManager.getRootElement();

		UwsJobOwnerParameters parameters = owner.getParameters();
		if(parameters != null){
			Set<String> params = parameters.getParameterNames();
			String escaped;
			for (String p : params) {
				Element eParam = xmlManager.createElement(UwsXmlConstants.XML_ELEMENT_PARAMETER);
				eRoot.appendChild(eParam);

				Object o = parameters.getParameter(p);
				UwsParameterValueType pvt = UwsJobOwnerParameters.getParameterValueType(o);

				escaped = UwsUtils.escapeXmlAttribute(pvt.name());
				eParam.setAttribute(UwsXmlConstants.XML_ATTR_DATA_TYPE, escaped);
				escaped = UwsUtils.escapeXmlData(UwsJobOwnerParameters.getParameterStringRepresentation(pvt, o));
				eParam.appendChild(xmlManager.getDocument().createTextNode(escaped));
				eParam.setAttribute(UwsXmlConstants.XML_ATTR_PARAMETER_ID, p);
			}
		}
		
		xmlManager.writeXmlFile(fParameters);
	}


	public int getNumOwners(){
		return baseOwnersDir.listFiles().length;
	}
		
	private String getOwnerDataFileName(String ownerid){
		return ownerid + '_' +OWNER_FILE_NAME;
	}
	
	private String getOwnerJobsFileName(String ownerid){
		return ownerid + OWNER_JOBS_FILE_NAME;
	}
	
	private String getOwnerParametersFileName(String ownerid){
		return ownerid + '_' + OWNER_PARAMETERS_FILE_NAME;
	}
	

}
