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
package esavo.sl.dd.requests;


import java.util.Map;

import org.apache.log4j.Logger;

import esavo.sl.dd.util.DDUtils;
import esavo.uws.owner.UwsJobOwner;


public class DDRetrievalRequest {
    
	public final static int PUBLIC			= 0;
	public final static int PROPRIETARY 	= 1;
	public final static int UNAUTHORIZED	= 2;
	
	
    static Logger logger = Logger.getLogger(DDRetrievalRequest.class);
    
    private int uniqueId;
    private Map<String,String[]> properties;
    private UwsJobOwner user;
    
	private DDRetrievalAccess retrievalAccess = DDRetrievalAccess.DIRECT;
	private DDCompressionType compression = null;
	private String origin;
	private String email;
	private String ip;
	private String dnsName;
	private boolean calculateSizeFlag = false;
	private String basePrefix = null;
    
    public DDRetrievalRequest(Map<String,String[]> properties, UwsJobOwner user){
    	this.uniqueId = DDUtils.getAproxUniqueId();
    	this.properties = properties;
    	this.user = user;
    	setRetrievalAccess();
    }
    
    private void setRetrievalAccess(){
    	String retrievalAccess = getProperty(DDProperties.PARAM_ID_RETRIEVAL_ACCESS);
    	if(retrievalAccess == null){
    		retrievalAccess = DDRetrievalAccess.DIRECT.name();
    		properties.put(DDProperties.PARAM_ID_RETRIEVAL_ACCESS, new String[]{retrievalAccess});
    		
    	}
    	try{
    		DDRetrievalAccess ra = DDRetrievalAccess.valueOf(retrievalAccess);
    		setRetrievalAccess(ra);
    	}catch(IllegalArgumentException iae){
    		setRetrievalAccess(DDRetrievalAccess.DIRECT);
    		logger.info("Invalid retrieval access type: '"+retrievalAccess+"', using " + DDRetrievalAccess.DIRECT.name() + " instead. For request: " + this.toString());
    	}
    }
    
    public String getProperty(String id){
    	String[] values = getPropertyMultiple(id);
    	if(values == null || values.length < 1){
    		return null;
    	}
    	return values[0];
    }
    
    public boolean getPropertyBoolean(String id){
    	String value = getProperty(id);
    	if(value == null){
    		return false;
    	}else{
    		return Boolean.parseBoolean(value);
    	}
    }
    
    public String[] getPropertyMultiple(String id){
    	return properties.get(id);
    }
    
    public String[] getPropertyMultiple(String id, boolean force){
    	String[] values = getPropertyMultiple(id);
		if(values == null){
			return null;
		}
    	if(!force){
    		return values;
    	}else{
    		if(values.length > 1){
    			return values;
    		}else{
    			if(values[0] == null){
    				return new String[]{};
    			}
    			String[] items = values[0].split(",");
    			return items;
    		}
    	}
    }
    
    public boolean containsProperty(String id){
    	return properties.containsKey(id);
    }
    
    public boolean isPropertyMultiple(String id){
    	String[] v = getPropertyMultiple(id);
    	if(v == null){
    		return false;
    	}
    	if(v.length < 2){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    public int getReqId(){
    	return uniqueId;
    }
    
    public String getUsername(){
    	return user.getId();
    }
    
    public String getReqIdComplete(){
    	basePrefix = getBasePrefix();
    	if(basePrefix == null){
    		return getUsername() + getReqId();
    	}else{
    		return basePrefix + getUsername() + getReqId();
    	}
    }

	/**
	 * @return the retrieval Access
	 */
	public DDRetrievalAccess getRetrievalAccess() {
		return retrievalAccess;
	}

	/**
	 * @param retrievalAccess the retrieval access to set
	 */
	public void setRetrievalAccess(DDRetrievalAccess retrievalAccess) {
		this.retrievalAccess = retrievalAccess;
	}

	/**
	 * @return the compression
	 */
	public DDCompressionType getCompression() {
		return compression;
	}

	/**
	 * @param compression the compression to set
	 */
	public void setCompression(DDCompressionType compression) {
		this.compression = compression;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the dnsName
	 */
	public String getDnsName() {
		return dnsName;
	}

	/**
	 * @param dnsName the dnsName to set
	 */
	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	/**
	 * @return the calculateSizeFlag
	 */
	public boolean isCalculateSizeFlag() {
		return calculateSizeFlag;
	}

	/**
	 * @param calculateSizeFlag the calculateSizeFlag to set
	 */
	public void setCalculateSizeFlag(boolean calculateSizeFlag) {
		this.calculateSizeFlag = calculateSizeFlag;
	}
	
	public String getRetrievalType(){
		return getProperty(DDProperties.PARAM_ID_RETRIEVAL_TYPE);
	}
	
	public boolean isDirectRetrieval(){
		return retrievalAccess == DDRetrievalAccess.DIRECT;
	}
	
	/**
	 * @return the user
	 */
	public UwsJobOwner getUser() {
		return user;
	}
	
	/**
	 * @return the basePrefix
	 */
	public String getBasePrefix() {
		return basePrefix;
	}

	/**
	 * @param basePrefix the basePrefix to set
	 */
	public void setBasePrefix(String basePrefix) {
		this.basePrefix = basePrefix;
	}

	@Override
	public String toString(){
		return "Reqid: " + getReqIdComplete() + ", retrieval access: " + retrievalAccess + 
				", retrieval type: " + getRetrievalType() + ", compression: " + compression;
	}

}
