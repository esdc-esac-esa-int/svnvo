package esavo.uws.jobs.utils;

import javax.servlet.http.HttpServletResponse;

import esavo.uws.UwsJobsListManager;
import esavo.uws.actions.UwsUploadResource;
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
	private UwsUploadResource[] uploadResources;

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
	public UwsUploadResource[] getUploadResources() {
		return uploadResources;
	}

	/**
	 * @param uploadResources the uploadResources to set
	 */
	public void setUploadResources(UwsUploadResource[] uploadResources) {
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
