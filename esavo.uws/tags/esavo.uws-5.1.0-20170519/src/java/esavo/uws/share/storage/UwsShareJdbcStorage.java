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
package esavo.uws.share.storage;

import java.sql.SQLException;
import java.util.List;




import java.util.Set;

import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;
import esavo.uws.share.UwsShareUser;
import esavo.uws.utils.jdbc.UwsDatabaseProperties;
import esavo.uws.utils.jdbc.UwsJdbcManager;
import esavo.uws.utils.jdbc.UwsJdbcSingleton;

/**
 * Jdbc Pool storage
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class UwsShareJdbcStorage implements UwsShareStorage {
	
	//private static final Logger LOG = Logger.getLogger(UwsShareJdbcStorage.class.getName());

	//private UwsShareJdbcStorageSingleton poolManager;
	private String appid;
	private UwsJdbcSingleton poolManager;
	private UwsDatabaseProperties databaseProperties;

	
	public UwsShareJdbcStorage(String appid, UwsConfiguration configuration) {
		this.appid = appid;
		//databaseProperties = new UwsDatabaseProperties(configuration);
		////poolManager = UwsJdbcManager.getInstance(appid, appid, databaseProperties);
		//UwsJdbcManager.createInstance(appid, poolId, databaseProperties)
		poolManager = UwsJdbcManager.getInstance(UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
		databaseProperties = poolManager.getDatabaseProperties();
	}
	
	public String getAppId(){
		return appid;
	}

	private UwsShareJdbcPooledConnection createConnection() throws UwsException{
		try {
			return new UwsShareJdbcPooledConnection(poolManager.getConnection(), databaseProperties.getTimeOutMillis());
		} catch (SQLException e) {
			throw new UwsException(UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, "Unable to create connection to database", e);
		}
	}
	
	private void closeConnection(UwsShareJdbcPooledConnection conn) {
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
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid, boolean includeUsers) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroupsTheUserBelongsTo(userid, includeUsers);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public boolean addGroup(UwsShareGroup group) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.addGroup(group);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public boolean updateGroup(UwsShareGroup group) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.updateGroup(group);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
//	@Override
//	public UwsShareGroup getGroup(String groupId) throws UwsException {
//		UwsShareJdbcPooledConnection conn = createConnection();
//		try{
//			return conn.getGroup(groupId);
//		}catch(SQLException sqle){
//			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
//		}finally{
//			closeConnection(conn);
//		}
//	}
	
	@Override
	public UwsShareGroup getGroup(String groupId, boolean includeUsers) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroup(groupId, includeUsers);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public List<UwsShareGroup> getGroups(List<String> groupIds, boolean includeUsers) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroups(groupIds, includeUsers);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public Set<String> getGroupsOwners(Set<String> groupsIds) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroupsOwners(groupsIds);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}


	
	@Override
	public List<UwsShareGroup> getGroupsByOwner(String ownerid,	boolean includeUsers) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroupsByOwner(ownerid, includeUsers);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}


	
	@Override
	public boolean removeGroup(String groupId) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.deleteGroup(groupId);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public List<UwsShareItemBase> getUserSharedItems(String userid, int resourceType, boolean includeShareTo) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getUserSharedItems(userid, resourceType, includeShareTo);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public List<UwsShareItem> getUserSharedItem(String ownerid, String resourceId, int resourceType) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getUserSharedItem(ownerid, resourceId, resourceType);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
//	@Override
//	public List<UwsShareItemBase> getUserSharedItemDescriptions(String ownerid) throws UwsException {
//		UwsShareJdbcPooledConnection conn = createConnection();
//		try{
//			return conn.getUserSharedItemDescriptions(ownerid);
//		}catch(SQLException sqle){
//			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
//		}finally{
//			closeConnection(conn);
//		}
//	}
	
//	@Override
//	public UwsShareItemBase getUserSharedItemDescription(String ownerid, String resourceId, int resourceType) throws UwsException {
//		UwsShareJdbcPooledConnection conn = createConnection();
//		try{
//			return conn.getUserSharedItemDescription(ownerid, resourceId, resourceType);
//		}catch(SQLException sqle){
//			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
//		}finally{
//			closeConnection(conn);
//		}
//	}
	
	@Override
	public UwsShareItemBase getSharedItemBase(String resourceid, int resourceType) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getSharedItemBase(resourceid, resourceType);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public List<UwsShareItemBase> getSharedItemsBase(String groupid, boolean includeSharedToItems) throws UwsException{
		return getSharedItemsBase(groupid, null, includeSharedToItems);
	}

	@Override
	public List<UwsShareItemBase> getSharedItemsBase(String groupid, String titlePattern, boolean includeSharedToItems) throws UwsException{
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getSharedItemsBase(groupid, titlePattern, includeSharedToItems);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public String getSharedItemOwner(String resourceId, int resourceType) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getSharedItemOwner(resourceId, resourceType);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}


	
	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException{
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getAccessibleSharedItems(userid, resourceType, shareType, shareMode);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public List<UwsShareUser> getUsers(String pattern, int maxResults) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getUsers(pattern, maxResults);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}



//	@Override
//	public List<UwsShareItem> getUserSharedItemsInfo(String userid, String resourceid, String resourceType) throws UwsException {
//		UwsShareJdbcPooledConnection conn = createConnection();
//		try{
//			return conn.getUserSharedItemsInfo(userid, resourceid, resourceType);
//		}catch(SQLException sqle){
//			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
//		}finally{
//			closeConnection(conn);
//		}
//	}
	
	@Override
	public void removeSharedItem(String resourceid, int resourceType, String ownerid) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			conn.removeSharedItem(resourceid, resourceType, ownerid);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public void removeSharedItemRelation(String resourceid, int resourceType, String shareToId, UwsShareType shareType, String ownerid)
			throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			conn.removeSharedItemRelation(resourceid, resourceType, shareToId, shareType, ownerid);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}



	@Override
	public boolean addUserToGroup(String groupId, String userToAdd) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.addUserToGroup(groupId, userToAdd);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public boolean removeGroupUser(String groupId, String userid) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.deleteGroupUser(groupId, userid);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public void createSharedItem(UwsShareItemBase sharedItem) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			conn.createSharedItem(sharedItem);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public void updateSharedItem(UwsShareItemBase sharedItem) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			conn.updateSharedItem(sharedItem);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	@Override
	public void addSharedItem(String ownerid, UwsShareItem sharedItem) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			conn.addSharedItem(ownerid, sharedItem);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}
	
	
	@Override
	public String toString(){
		return "JDBC storage Pool: " + poolManager.toString();
	}

	@Override
	public Set<String> getUniqueUsersFromGroups(Set<String> groupIds) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getUniqueGroupsUsers(groupIds);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public Set<String> getUsersFromGroup(String groupid) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.getGroupUsers(groupid);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}

	@Override
	public boolean hasAccessToShareItem(String userid, String resourceid, int resourceType) throws UwsException {
		UwsShareJdbcPooledConnection conn = createConnection();
		try{
			return conn.hasAccessToShareItem(userid, resourceid, resourceType);
		}catch(SQLException sqle){
			throw new UwsException("SQL exception: " + sqle.getMessage(), sqle);
		}finally{
			closeConnection(conn);
		}
	}




}
