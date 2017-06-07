package esavo.uws.event;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.UwsEventsDeletionManagerThread;

/**
 * Base class for supporting default events managers
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public abstract class UwsAbstractEventsManager implements UwsEventsManager {
	
	public static final boolean USE_DELETION_THREAD = true;
	public static final boolean DO_NOT_USE_DELETION_THREAD = false;
	
	private String appid;
	private UwsEventTypesRegistry eventsTypesRegistry;
	
	public UwsAbstractEventsManager(String appid, boolean useDeletionThread){
		this.appid = appid;
		eventsTypesRegistry = new UwsEventTypesRegistry();
		initializeDefaultRegistry();
		
		if(useDeletionThread){
			UwsEventsDeletionManagerThread deletionManagerThread = new UwsEventsDeletionManagerThread(appid);
			deletionManagerThread.setDaemon(true);
			deletionManagerThread.start();
		}
	}
	
	private void initializeDefaultRegistry(){
		UwsEventType uet;

		//Jobs
		uet = new UwsDefaultEventType(UwsEventType.JOB_CREATED_EVENT, "Job created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.JOB_UPDATED_EVENT, "Job updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.JOB_REMOVED_EVENT, "Job removed event");
		eventsTypesRegistry.register(uet);

		//Sharing
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_CREATED_EVENT, "Share items created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_UPDATED_EVENT, "Share items updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_ITEMS_REMOVED_EVENT, "Share items removed event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_CREATED_EVENT, "Share groups created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_UPDATED_EVENT, "Share groups updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_GROUPS_REMOVED_EVENT, "Share groups removed event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_CREATED_EVENT, "Share users created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_UPDATED_EVENT, "Share users updated event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.SHARE_USERS_REMOVED_EVENT, "Share users removed event");
		eventsTypesRegistry.register(uet);
		
		//Public group
		uet = new UwsDefaultEventType(UwsEventType.PUBLIC_GROUP_VIEW_TABLE, "View public group item event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.PUBLIC_GROUP_HIDE_TABLE, "Hide public group item event");
		eventsTypesRegistry.register(uet);
		
		
		//Login
		uet = new UwsDefaultEventType(UwsEventType.LOGIN_IN_EVENT, "Login in event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.LOGIN_OUT_EVENT, "Login out event");
		eventsTypesRegistry.register(uet);
		
		//Quota
		uet = new UwsDefaultEventType(UwsEventType.QUOTA_DB_UPDATED_EVENT, "Quota database event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.QUOTA_FILE_UPDATED_EVENT, "Quota file event");
		eventsTypesRegistry.register(uet);
		
		//Notifications
		uet = new UwsDefaultEventType(UwsEventType.NOTIFICATION_CREATED_EVENT, "Notification created event");
		eventsTypesRegistry.register(uet);
		uet = new UwsDefaultEventType(UwsEventType.NOTIFICATION_REMOVED_EVENT, "Notification removed event");
		eventsTypesRegistry.register(uet);
	}

	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}
	
	@Override
	public UwsEventTypesRegistry getEventsTypeRegistry() {
		return eventsTypesRegistry;
	}
	
//	@Override
//	public synchronized long getTimeForEvent(UwsJobOwner user, int eventTypeCode) throws UwsException {
//		UwsEventType eventType = getEventType(eventTypeCode);
//		if(eventType == null){
//			throw new UwsException("Event type code '"+eventTypeCode+"' not found in registry.");
//		}
//		return getTimeForEvent(user, eventType);
//	}
//	
//	@Override
//	public abstract long getTimeForEvent(UwsJobOwner user, UwsEventType eventType);
	
	@Override
	public synchronized void setEventTime(UwsJobOwner user, int eventTypeCode) throws UwsException {
		UwsEventType eventType = getEventType(eventTypeCode);
		if(eventType == null){
			throw new UwsException("Event type code '"+eventTypeCode+"' not found in registry.");
		}
		setEventTime(user, eventType);
	}
	
	@Override
	public abstract void setEventTime(UwsJobOwner user, UwsEventType eventType);

	

	public UwsEventType getEventType(int code){
		return eventsTypesRegistry.getEventType(code);
	}
	
	@Override
	public String toString(){
		return "AbstractEventsManager for application " + getAppid() + ".\n" + getEventsTypeRegistry().toString();
	}

}
