package esavo.uws.utils.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.logging.Logger;


/**
 * Managers the status related to a user identifier.<br/>
 * A user can have different status types.<br/>
 * This class stores a single status for each status type: ie. if a new status is set, the previous one is removed.<br/>
 * Each time a status is retrieved, the status is consumed: ie. the status is no longer available.<br/>
 * A thread verifies that an identifier is 'alive'. It means that if an identifier is not updated or accessed before 
 * an specified timeout, the identifier and associated status are removed and any further attempt to access to that identifier will
 * raise an IllegalArgumentException.<br/>
 * <p>Usage:
 * <pre><tt>
 * UserInfo userInfo = new UserInfo();
 * userInfo.setip([ip_address])
 * ...
 * StatusManager statusManager = StatusManager.getInstance();
 * long id = statusManager.createUserIdentifier(userInfo);
 * ...
 * ...
 * //Update upload status
 * long id = [task_identifier]
 * StatusUpload statusUpload = new StatusUpload("25");
 * try{
 * 		StatusManager.getInstance().updateStatus(id, statusUpload); 
 * } catch (IllegalArgumentException iae){
 * 		throw new IOException("Error updating status: " + iae.getMessage(), iae);
 * }
 * 
 * //Retrieve status
 * long id = [task_identifier]
 * StatusType type = [status_type: i.e. StatusType.UPLOAD]
 * try{
 * 		StatusData data = StatusManager.getInstance().getStatus(id, type);
 * 		System.our.println(data.getData()); 
 * } catch (IllegalArgumentException iae){
 * 		throw new IOException("Error updating status: " + iae.getMessage(), iae);
 * }
 * 
 * </tt></pre>
 * </p>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsStatusManager {
	
	//Debug
	//static final long DEBUG_CHECK_TIME = 1000 * 60 * 1; //1 minute
	//static final long DEBUG_TIMEOUT = 1000 * 60 * 1; //1 minute

	private static final Logger LOGGER = Logger.getLogger(UwsStatusManager.class.getName());
	
	private static final long CHECK_TIME = 1000 * 60 * 5; //5 minutes
	private static final long TIMEOUT = 1000 * 60 * 5; //5 minutes

	private static UwsStatusManager statusManagerInstance;
	private Map<Long, UwsUserStatusData> userStatusDataMap;
	
	private long checkTime;
	private long timeOut;
	
	//This variable is useful for testing purposes: by default, it is enabled
	private static boolean enableCheckThread = true;
	
	private UwsStatusManager(long checkTime, long timeOut) {
		this.checkTime = checkTime;
		this.timeOut = timeOut;
		userStatusDataMap = new HashMap<Long, UwsUserStatusData>();
		Timer timer = new Timer(true); // To allow the application to exit
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (enableCheckThread) {
					//by default: it is enabled
					removeInactiveEntries();
				}
			}
		}, this.checkTime, this.checkTime);
		LOGGER.info("Running check thread every: " + this.checkTime + " ms. Timeout set to: " + this.timeOut);
	}

	/**
	 * Returns a single instance of this manager.
	 * @return a single instance of this manager.
	 */
	public static UwsStatusManager getInstance() {
		return getInstance(CHECK_TIME, TIMEOUT);
	}
	
	/**
	 * Returns a single instance of this manager.
	 * @return a single instance of this manager.
	 */
	public static synchronized UwsStatusManager getInstance(long checkTime, long timeOut) {
		if (statusManagerInstance == null) {
			statusManagerInstance = new UwsStatusManager(checkTime, timeOut);
		}
		return statusManagerInstance;
	}
	
	/**
	 * Enables/disables. By default it is enabled.
	 * @param enable 'true' to enable the check mechanism.
	 */
	public static synchronized void enableCheckThread(boolean enable){
		enableCheckThread = enable;
	}
	
	/**
	 * Creates an entry associated to a user.
	 * @param userInfo information associated to a user.
	 * @return the identifier associated to the provided userInfo.
	 */
	public synchronized long createUserIdentifier(UwsUserInfo userInfo){
		long taskid = getIdentifier();
		UwsUserStatusData usd = new UwsUserStatusData(taskid, userInfo);
		userStatusDataMap.put(taskid, usd);
		return taskid;
	}
	
	/**
	 * Updates the status associated to a task identifier by status type. <br/>
	 * If the identifier is not found, an IllegalArgumentException is raised.
	 * @param id task identifier.
	 * @param data status.
	 * @throws IllegalArgumentException if the identifier is not found.
	 */
	public synchronized void updateStatus(long id, UwsStatusData data) throws IllegalArgumentException {
		UwsUserStatusData usd = getUserStatusData(id);
		usd.updateStatus(data);
	}
	
	/**
	 * Return the latest status associated to the provided task identifier by status type. <br/>
	 * If the identifier is not found, an IllegalArgumentException is raised.
	 * @param id task identifier
	 * @param type status type
	 * @return the latest status. Can be null.
	 * @throws IllegalArgumentException if the identifier is not found.
	 */
	public synchronized UwsStatusData getStatus(long id, String type) throws IllegalArgumentException {
		UwsUserStatusData usd = getUserStatusData(id);
		return usd.getStatus(type);
	}
	
	/**
	 * Returns all the status associated to the provided task identifier. <br/>
	 * If the identifier is not found, an IllegalArgumentException is raised.
	 * @param id task identifier.
	 * @return all the status associated to the provided identifier
	 * @throws IllegalArgumentException if the identifier is not found.
	 */
	public synchronized Map<String, UwsStatusData> getStatus(long id) throws IllegalArgumentException {
		UwsUserStatusData usd = getUserStatusData(id);
		return usd.getAllStatus();
	}
	
	/**
	 * Finds the specified task identifier.<br/>
	 * If the identifier is not found, an IllegalArgumentException is raised.
	 * @param id task identifier.
	 * @return The data associated to the provided identifier
	 * @throws IllegalArgumentException if the identifier is not found.
	 */
	synchronized UwsUserStatusData getUserStatusData(long id) throws IllegalArgumentException{
		UwsUserStatusData usd = userStatusDataMap.get(id);
		if(usd == null){
			//Does not exist
			throw new IllegalArgumentException("User identifier '"+id+"' not found.");
		}
		return usd;
	}
	
	/**
	 * Removes the specified task identifier
	 * @param id task identifier
	 */
	public synchronized void remove(long id){
		getUserStatusData(id); //to verify the user exists;
		userStatusDataMap.remove(id);
	}
	
	/**
	 * Test-harness: removes all entries.
	 */
	synchronized void removeAll(){
		userStatusDataMap.clear();
	}
	
	/**
	 * Returns a unique number.
	 * @return a unique number.
	 */
	private synchronized long getIdentifier(){
		long l = System.currentTimeMillis();
		long n;
		while(true){
			n = System.currentTimeMillis();
			if(n != l){
				return n;
			}
		}
	}
	
	/**
	 * Removes entries without updates.
	 */
	private synchronized void removeInactiveEntries(){
		long time = System.currentTimeMillis();
		removeInactiveEntries(time, this.timeOut);
	}
	
	/**
	 * Removes entries without updates. It checks the last time the entry was updated and performs:
	 * <pre><tt>
	 * (time - lastUpdate) > timeOut
	 * </tt></pre>
	 * If no updates have been found, the entry is removed.
	 * @param time check time (current time)
	 * @param timeOut time out (expiration)
	 */
	synchronized void removeInactiveEntries(long time, long timeOut){
		List<Long> toRemove = new ArrayList<Long>();
		try{
			LOGGER.info("Remove time: " + time + ", num task ids: " + userStatusDataMap.size());
		}catch(Exception e){
		}
		long lastUpdate;
		long key;
		for(Entry<Long, UwsUserStatusData> e: userStatusDataMap.entrySet()){
			lastUpdate = e.getValue().getLastUpdate();
			key = e.getKey();
			try{
				if((time - lastUpdate) > timeOut){
					toRemove.add(key);
					LOGGER.info("task id " + key + ", last update: " + lastUpdate + " ---> timeout");
				} else {
					LOGGER.info("task id " + key + ", last update: " + lastUpdate + ": ok");
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		for(Long id: toRemove){
			LOGGER.info("Removing task id: " + id);
			userStatusDataMap.remove(id);
		}
	}
	
	private synchronized void getInfo(StringBuilder sb){
		for(UwsUserStatusData usd: userStatusDataMap.values()){
			sb.append(usd).append("\n");
		}
	}
	
	/**
	 * Returns the current number of tasks
	 * @return the current number of tasks
	 */
	public synchronized int getNumClients(){
		return userStatusDataMap.size();
	}

	@Override
	public String toString(){
		return "Num clients: " + getNumClients() + ", check time: " + checkTime + " ms., timeout: " + timeOut + " ms.";
	}
	
	/**
	 * Dumps the current task identifier register
	 * @return the current task identifier register
	 */
	public String dump(){
		StringBuilder sb = new StringBuilder("Current clients:\n");
		getInfo(sb);
		return sb.toString();
	}

}
