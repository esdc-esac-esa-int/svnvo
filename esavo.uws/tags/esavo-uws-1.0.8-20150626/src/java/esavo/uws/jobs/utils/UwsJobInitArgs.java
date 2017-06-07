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
package esavo.uws.jobs.utils;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsJobsListManager;
import esavo.uws.actions.UwsUploadResourceLoader;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsStorage;

public class UwsJobInitArgs {

	private String appid;
	private UwsJobOwner owner;
	private String listid;
	private int priority;
	private UwsStorage storage;
	private UwsJobsListManager jobsListManager;
	private UwsEventsManager eventsManager;
	private HttpServletResponse response;
	private UwsUploadResourceLoader[] uploadResources;

	public UwsJobInitArgs() {

	}

	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}

	/**
	 * @param appid the appid to set
	 */
	public void setAppid(String appid) {
		this.appid = appid;
	}

	/**
	 * @return the owner
	 */
	public UwsJobOwner getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(UwsJobOwner owner) {
		this.owner = owner;
	}

	/**
	 * @return the listid
	 */
	public String getListid() {
		return listid;
	}

	/**
	 * @param listid the listid to set
	 */
	public void setListid(String listid) {
		this.listid = listid;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the storage
	 */
	public UwsStorage getStorage() {
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public void setStorage(UwsStorage storage) {
		this.storage = storage;
	}

	/**
	 * @return the jobsListManager
	 */
	public UwsJobsListManager getJobsListManager() {
		return jobsListManager;
	}

	/**
	 * @param jobsListManager the jobsListManager to set
	 */
	public void setJobsListManager(UwsJobsListManager jobsListManager) {
		this.jobsListManager = jobsListManager;
	}

	/**
	 * @return 'true' if a servlet response is available (used by sync jobs)
	 */
	public boolean hasHttpServletResponse(){
		return response != null;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @param reponse the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @return the uploadResources
	 */
	public UwsUploadResourceLoader[] getUploadResources() {
		return uploadResources;
	}

	/**
	 * @param uploadResources the uploadResources to set
	 */
	public void setUploadResources(UwsUploadResourceLoader[] uploadResources) {
		this.uploadResources = uploadResources;
	}

	/**
	 * @return the eventsManager
	 */
	public UwsEventsManager getEventsManager() {
		return eventsManager;
	}

	/**
	 * @param eventsManager the eventsManager to set
	 */
	public void setEventsManager(UwsEventsManager eventsManager) {
		this.eventsManager = eventsManager;
	}

}
