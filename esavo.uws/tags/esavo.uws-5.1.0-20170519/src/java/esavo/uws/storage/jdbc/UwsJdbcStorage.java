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
package esavo.uws.storage.jdbc;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
//import java.util.logging.Logger;












import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.creator.UwsCreator;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.utils.UwsJobAttribute;
import esavo.uws.jobs.utils.UwsJobDetails;
import esavo.uws.jobs.utils.UwsJobsFilter;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.utils.UwsJobsOwnersFilter;
import esavo.uws.storage.UwsAbstractStorage;
import esavo.uws.storage.UwsOwnerSessionFilter;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;
import esavo.uws.utils.jdbc.UwsDatabaseProperties;
import esavo.uws.utils.jdbc.UwsJdbcManager;
import esavo.uws.utils.jdbc.UwsJdbcSingleton;

/**
 * Jdbc Pool storage
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class UwsJdbcStorage extends UwsAbstractStorage implements UwsStorage {
	
	//private static final Logger LOG = Logger.getLogger(UwsJdbcStorage.class.getName());
	
	private UwsCreator creator;
	private UwsJdbcSingleton poolManager;
	private UwsDatabaseProperties databaseProperties;
	
	public UwsJdbcStorage(String appid, File storageDir, UwsCreator creator, UwsConfiguration configuration) {
		super(storageDir, appid);
		this.creator = creator;
		poolManager = UwsJdbcManager.getInstance(UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
		databaseProperties = poolManager.getDatabaseProperties();
	}

	private UwsJdbcPooledConnection createConnection() throws UwsException{
		try {
			return new UwsJdbcPooledConnection(poolManager.getConnection(), creator, databaseProperties.getTimeOutMillis());
		} catch (SQLException e) {
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unable to create connection to database", e);
		}
	}
	
	private void closeConnection(UwsJdbcPooledConnection conn) {
		if (conn == null) {
			return;
		} else {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public UwsJob getJobMeta(String jobid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getJobMeta(jobid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean checkJobExists(String jobid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.checkJobExists(jobid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public UwsJobOwner getOwner(String ownerid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getOwner(ownerid, true);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public UwsJobOwner getOwnerIfAvailable(String ownerid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getOwner(ownerid, false);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}


	
	@Override
	public void addOwner(UwsJobOwner owner) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.addOwner(owner);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void updateOwner(UwsJobOwner owner) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.updateOwner(owner);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.updateOwnerParameter(owner, parameterName);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public void updateOwnerRoles(UwsJobOwner owner) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.updateOwnerRoles(owner);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public List<UwsJobOwner> retrieveOwners(UwsJobsOwnersFilter filter, long offset, long limit) throws UwsException{
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.retrieveOwners(filter, offset, limit);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean addNewJobMetaIfNeeded(UwsJob job) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.addNewJobMetaIfNeeded(job);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsJob> searchByParameter(String parameterName, String value) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.searchByParameter(parameterName, value);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void updateAllParameters(UwsJob job) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.updateAllParameters(job);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean removeParameter(UwsJob job, String parameterid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.removeParameter(job.getJobId(), parameterid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean updateParameter(UwsJob job, String parameterid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.updateParameter(job, parameterid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean createParameter(UwsJob job, String parameterid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.createParameter(job, parameterid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean createOrUpdateParameter(UwsJob job, String parameterid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.createOrUpdateParameter(job, parameterid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean updateJobAttribute(UwsJob job, UwsJobAttribute attributeid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.updateJobAttribute(job, attributeid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean addJobResultMeta(String jobid, UwsJobResultMeta res) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.addJobResultMeta(jobid, res);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean addErrorSummaryMeta(String jobid, UwsJobErrorSummaryMeta errorSummary) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.addErrorSummaryMeta(jobid, errorSummary);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public int getNumJobs() throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getNumJobs();
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public int getNumOwners() throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getNumOwners();
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsJob> getJobsMetaByOwner(String ownerid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getJobsMetaByOwner(ownerid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsJob> getJobsByFilter(String listName, UwsJobsFilter filter, String appid, Integer limit, Integer offset, String order, boolean onlyMeta) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getJobsByFilter(listName, filter, appid, limit, offset, order, onlyMeta);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public Integer getJobsNumberByFilter(String listName, UwsJobsFilter filter, String appid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getJobsNumberByFilter(listName, filter, appid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public Integer getNumberOfJobsByList(String listName,
			List<UwsOwnerSessionFilter> ownersFilter, String appid)
			throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getNumberOfJobsByList(listName, ownersFilter, appid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}


	@Override
	public List<UwsJob> getPendingJobs(String appid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getPendingJobs(appid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public boolean removeJobOutputData(UwsJob job) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		boolean ret = false;
		try {
			ret = conn.removeJobOutputMeta(job.getJobId());
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
		if(ret){
			return super.removeJobOutputData(job);
		}
		return false;
	}

	@Override
	public boolean removeJobMetaDataAndOutputData(UwsJob job) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		boolean updateJobMeta = job.getStatusManager().getEnableUpdates();
		if(updateJobMeta){
			try {
				boolean ret = conn.removeJobAndAssociatedMeta(job.getJobId());
				if (!ret){
					return false;
				}
			} catch (SQLException sqle) {
				throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
			} finally {
				closeConnection(conn);
			}
		}
		return super.removeJobOutputData(job);
	}

	@Override
	public String toString(){
		return "JDBC storage:" +
				"\n" + super.toString() +
				"\nPool(s):\n" + UwsJdbcManager.dump();
	}

	@Override
	public List<UwsJobDetails> getJobsToDestroy(String appid, long currentTime) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getJobsToDestroy(appid, currentTime);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsJobDetails> getOlderJobs(String appid, UwsJobPhase phaseId, long time) throws UwsException {
		
		if(phaseId==null){
			throw new UwsException("Phase ID should not be null.");
		}
		
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getOlderJobs(appid, phaseId.name(), time);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public long calculateDbSize(String ownerid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getDbSize(ownerid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public long calculateFileSize(String ownerid) throws UwsException {
		
		String ownerDirName = UwsUtils.getOwnerSubDir(ownerid);
		if(ownerDirName==null || ownerDirName.trim().length()==0) return 0;
		File ownerDir = new File(getStorageDir(), ownerDirName); 
		
		return filesystemSize(ownerDir);
	}
	
	/**
	 * Obtains the total size of a directory/file in bytes.
	 * @param dir
	 * @return
	 */
	private static long filesystemSize(File dir){
		long size=0;
		
		if(!dir.isDirectory()) return dir.length();
		for(File file: dir.listFiles()){
			size+=filesystemSize(file);
		}
		return size;
	}

	@Override
	public void createNotification(UwsNotificationItem uwsNotificationItem) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.createNotification(uwsNotificationItem);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsNotificationItem> getNotificationsForUser(String userid)	throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.getNotificationsForUser(userid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void deleteNotificationRelation(String userid, List<String> notificationid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.deleteNotificationRelation(userid, notificationid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public int deleteNotifications(long currentTime, long deltaDestructionTime)	throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.deleteNotifications(currentTime, deltaDestructionTime);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public int deleteSystemWarningNotifications(List<String> notificationids)	throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.deleteSystemWarningNotifications(notificationids);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}


	@Override
	public boolean changeJobName(String jobid, String jobName) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.changeJobName(jobid, jobName);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}
	
	@Override
	public int updateParameterToJson() throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			return conn.updateParametersToJson();
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void updateParameterToJson(String jobid) throws UwsException {
		UwsJdbcPooledConnection conn = createConnection();
		try {
			conn.updateParametersToJson(jobid);
		} catch (SQLException sqle) {
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		} finally {
			closeConnection(conn);
		}
	}


}
