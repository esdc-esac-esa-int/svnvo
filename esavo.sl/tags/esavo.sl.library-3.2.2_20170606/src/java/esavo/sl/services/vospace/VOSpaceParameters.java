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
package esavo.sl.services.vospace;

public class VOSpaceParameters {

	private String location;
	private String jobid;
	private boolean overwrite;
	private String fileName;
	private String userid;
	private String urlBase;
	private String voSpaceProtocol;
	private String voSpaceTarget;
	private String userAgent;
	private String voSpaceUwsJobId;
	private long taskid;
	
	public VOSpaceParameters(){
		
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = updateLocationIfRequired(location);
	}

	/**
	 * @return the jobid
	 */
	public String getJobid() {
		return jobid;
	}

	/**
	 * @param jobid the jobid to set
	 */
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @return the filename
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}

	/**
	 * @return the urlBase
	 */
	public String getUrlBase() {
		return urlBase;
	}

	/**
	 * @param urlBase the urlBase to set
	 */
	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	/**
	 * @return the voSpaceProtocol
	 */
	public String getVoSpaceProtocol() {
		return voSpaceProtocol;
	}

	/**
	 * @param voSpaceProtocol the voSpaceProtocol to set
	 */
	public void setVoSpaceProtocol(String voSpaceProtocol) {
		this.voSpaceProtocol = voSpaceProtocol;
	}

	/**
	 * @return the voSpaceTarget
	 */
	public String getVoSpaceTarget() {
		return voSpaceTarget;
	}

	/**
	 * @param voSpaceTarget the voSpaceTarget to set
	 */
	public void setVoSpaceTarget(String voSpaceTarget) {
		this.voSpaceTarget = voSpaceTarget;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the voSpaceUwsJobId
	 */
	public String getVoSpaceUwsJobId() {
		return voSpaceUwsJobId;
	}

	/**
	 * @param voSpaceUwsJobId the voSpaceUwsJobId to set
	 */
	public void setVoSpaceUwsJobId(String voSpaceUwsJobId) {
		this.voSpaceUwsJobId = voSpaceUwsJobId;
	}
	
	public String getFileNodeURI(){
		if(fileName == null){
			return location;
		}else{
			if(location != null){
				if(location.endsWith("/")){
					return location + fileName;
				}else{
					return location + '/' + fileName;
				}
			}else{
				return fileName;
			}
		}
	}
	
	public String getContainerNodeURI(){
		if(voSpaceTarget != null){
			return voSpaceTarget + location;
//			if(voSpaceTarget.endsWith("/")){
//				return voSpaceTarget + location;
//			}else{
//				return voSpaceTarget + '/' + location;
//			}
		}else{
			return userid;
		}
	}
	
	public static String updateLocationIfRequired(String location){
		//		PRE: location add '/' at the beginning if not present
		if(location == null){
			return null;
		}
		if(location.endsWith("/")){
			return location.substring(0, location.length()-1);
		}else{
			return location;
		}
	}

	/**
	 * @return the taskid
	 */
	public long getTaskid() {
		return taskid;
	}

	/**
	 * @param taskid the taskid to set
	 */
	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}

}
