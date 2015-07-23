package esavo.uws.share.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
//import java.util.logging.Logger;




import java.util.TreeSet;

import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;
import esavo.uws.share.UwsShareUser;

/**
 * Database handler.
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class UwsShareJdbcPooledConnection {
	
	//private static final Logger LOG = Logger.getLogger(UwsShareJdbcPooledConnection.class.getName());
	
	private static final String GROUP_INSERT = "INSERT INTO share_schema.groups "+
			"(group_id, title, description, creator) "+
			"VALUES (?,?,?,?)";
	
	private static final String GROUP_UPDATE = "UPDATE share_schema.groups SET title=?, description=? WHERE group_id=? AND creator=?";
	
	private static final String USER_GROUP_INSERT = "INSERT INTO share_schema.user_groups (group_id, user_id) VALUES (?,?)";
	
	private static final String SHARED_ITEM_CREATE = "INSERT INTO share_schema.share (resource_id, resource_type, owner_id, title, description) " +
			"VALUES (?, ?, ?, ?, ?)";
	
	private static final String SHARED_ITEM_UPDATE = "UPDATE share_schema.share SET title=?, description=? WHERE resource_id=? AND resource_type=? AND owner_id=?";
	
	private static final String ADD_SHARED_ITEM_GROUP = "INSERT INTO share_schema.share_groups (resource_id, resource_type, owner_id, group_id, share_mode) " +
			"VALUES (?, ?, ?, ?, ?)";

	private static final String ADD_SHARED_ITEM_USER = "INSERT INTO share_schema.share_groups (resource_id, resource_type, owner_id, user_id, share_mode) " +
			"VALUES (?, ?, ?, ?, ?)";
	
	private Connection dbConn = null;
	//private Statement stmt = null;	
	//private UwsShareJdbcStorageSingleton jdbcSingleton;
	
	public UwsShareJdbcPooledConnection(UwsShareJdbcStorageSingleton jdbcSingleton) throws SQLException {
		//this.jdbcSingleton = jdbcSingleton;
		this.dbConn = jdbcSingleton.getConnection();
		dbConn.createStatement().execute("SET statement_timeout TO 1800000");
	}
	
	public void close() throws SQLException {
		try {
			dbConn.close();
		} catch (SQLException e) {
			throw new SQLException("Impossible to close DB connection, because: " + e.getMessage(), e);
		}
	}
	
	public UwsShareGroup getGroup(String groupid) throws SQLException {
		String query = "SELECT g.group_id, g.title, g.description, g.creator FROM " +
				"share_schema.groups g WHERE g.group_id = '"+groupid+"'";
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			if (result.next()) {
				UwsShareGroup group = new UwsShareGroup();
				group.setId(result.getString("group_id"));
				group.setTitle(result.getString("title"));
				group.setDescription(result.getString("description"));
				group.setCreator(result.getString("creator"));
				return group;
			}
			return null;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain group due to: " + e.getMessage(), e);
		}
	}
	
	public Set<String> getGroupsOwners(Set<String> groupsIds) throws SQLException {
		if(groupsIds == null || groupsIds.size() < 1){
			return new HashSet<String>();
		}
		StringBuilder sb = new StringBuilder("SELECT g.creator FROM ").
				append("share_schema.groups g WHERE g.group_id IN (");
		boolean firstTime = true;
		for(String g: groupsIds){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(", ");
			}
			sb.append('\'').append(g).append('\'');
		}
		sb.append(")");
		String query = sb.toString();
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			Set<String> groupsOwners = new HashSet<String>();
			if (result.next()) {
				groupsOwners.add(result.getString("creator"));
			}
			return groupsOwners;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain group due to: " + e.getMessage(), e);
		}
	}
	
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid) throws SQLException {
		String query = "SELECT g.group_id, g.title, g.description, g.creator FROM " +
				"share_schema.groups g, share_schema.user_groups z WHERE " +
				"g.group_id = z.group_id AND z.user_id = '"+userid+"'";
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			List<UwsShareGroup> groups = new ArrayList<UwsShareGroup>();
			while (result.next()) {
				UwsShareGroup group = new UwsShareGroup();
				group.setId(result.getString("group_id"));
				group.setTitle(result.getString("title"));
				group.setDescription(result.getString("description"));
				group.setCreator(result.getString("creator"));
				groups.add(group);
			}
			return groups;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain groups list due to: " + e.getMessage(), e);
		}
	}
	
	public List<UwsShareGroup> getGroupsByOwner(String ownerid,	boolean includeUsers) throws SQLException {
		String query = "SELECT g.group_id, g.title, g.description, g.creator FROM " +
				"share_schema.groups g WHERE g.creator = '"+ownerid+"'";
		ResultSet result;
		try {
			result = dbConn.createStatement().executeQuery(query);
			List<UwsShareGroup> groups = new ArrayList<UwsShareGroup>();
			while (result.next()) {
				UwsShareGroup group = new UwsShareGroup();
				group.setId(result.getString("group_id"));
				group.setTitle(result.getString("title"));
				group.setDescription(result.getString("description"));
				group.setCreator(result.getString("creator"));
				groups.add(group);
			}
			String baseQuery = "SELECT user_id FROM share_schema.user_groups WHERE group_id = ";
			for(UwsShareGroup g: groups){
				query = baseQuery + "'"+g.getId()+"'";
				result = dbConn.createStatement().executeQuery(query);
				List<UwsShareUser> users = new ArrayList<UwsShareUser>();
				g.setUsers(users);
				while(result.next()){
					UwsShareUser su = new UwsShareUser(result.getString("user_id"));
					users.add(su);
				}
			}
			return groups;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain groups list due to: " + e.getMessage(), e);
		}
	}
	
	public boolean addGroup(UwsShareGroup group) throws SQLException {
		String groupId = group.getId();
		try{
			dbConn.setAutoCommit(false);
			PreparedStatement statement = dbConn.prepareStatement(GROUP_INSERT);
			statement.setString(1, groupId);
			statement.setString(2, group.getTitle());
			statement.setString(3, group.getDescription());
			statement.setString(4, group.getCreator());
			
			statement.executeUpdate();
			
			List<UwsShareUser> users = group.getUsers();
			addUsersToGroup(groupId, users, false);
			
			dbConn.commit();
			return true;
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot add group '"+group.getId()+"' due to: " + e.getMessage(), e);
		}
	}
	
	public boolean updateGroup(UwsShareGroup group) throws SQLException {
		String groupId = group.getId();
		try{
			dbConn.setAutoCommit(false);
			PreparedStatement statement = dbConn.prepareStatement(GROUP_UPDATE);
			statement.setString(1, group.getTitle());
			statement.setString(2, group.getDescription());
			statement.setString(3, group.getId());
			statement.setString(4, group.getCreator());
			
			statement.executeUpdate();
			
			List<UwsShareUser> users = group.getUsers();
			addUsersToGroup(groupId, users, true);
			
			dbConn.commit();
			return true;
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot update group '"+groupId+"' due to: " + e.getMessage(), e);
		}
	}
	
	/**
	 * This is part of a transaction. DO NOT CALL WITHOUT A dbConn.setAutoCommit(false)/commit/rollback.
	 * @param groupId
	 * @param users
	 * @param clearPrevUsers
	 * @throws SQLException
	 */
	private void addUsersToGroup(String groupId, List<UwsShareUser> users, boolean clearPrevUsers) throws SQLException{
		if(clearPrevUsers){
			Statement st = dbConn.createStatement();
			st.execute("DELETE FROM share_schema.user_groups WHERE group_id = '"+groupId+"'");
		}
		if(users == null){
			return;
		}
		PreparedStatement statement = dbConn.prepareStatement(USER_GROUP_INSERT);
		for(UwsShareUser user: users){
			statement.setString(1, groupId);
			statement.setString(2, user.getId());
			statement.executeUpdate();
		}
	}
	
	public boolean deleteGroup(String groupId) throws SQLException {
		List<String> queries = new ArrayList<String>();
		queries.add("DELETE FROM share_schema.share_groups WHERE group_id = '"+groupId+"'");
		queries.add("DELETE FROM share_schema.user_groups WHERE group_id = '"+groupId+"'");
		queries.add("DELETE FROM share_schema.groups WHERE group_id = '"+groupId+"'");
		try{
			//int affected = dbConn.createStatement().executeUpdate(query);
			//return affected;
			dbConn.setAutoCommit(false);
			Statement statement = dbConn.createStatement();
			for(String query: queries){
				statement.executeUpdate(query);
			}
			dbConn.commit();
			return true;
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot delete group '"+groupId+"' due to: " + e.getMessage(), e);
		}
	}
	
	public boolean addUserToGroup(String groupId, String userid) throws SQLException {
		PreparedStatement statement = dbConn.prepareStatement(USER_GROUP_INSERT);
		statement.setString(1, groupId);
		statement.setString(2, userid);
		try{
			return statement.executeUpdate() == 1;
		}catch(SQLException e){
			throw new SQLException("Cannot add user '"+userid+"' to group '"+groupId+"' due to: " + e.getMessage(), e);
		}
	}
	
	public boolean deleteGroupUser(String groupId, String userid) throws SQLException {
		String query = "DELETE FROM share_schema.user_groups WHERE group_id = '"+groupId+"' AND user_id = '"+userid+"'";
		Statement statement = dbConn.createStatement();
		try{
			int result = statement.executeUpdate(query);
			return result == 1;
		}catch(SQLException e){
			throw new SQLException("Cannot delete user '"+userid+"' from group '"+groupId+"' due to: " + e.getMessage(), e);
		}
	}
	
	
//	public UwsShareItemBase getUserSharedItemDescription(String ownerid, String resourceId, int resourceType) throws SQLException {
//		String query = "SELECT title, description FROM share_schema.share WHERE " +
//				"resource_id = '"+resourceId+"' AND resourceType = "+resourceType+" AND owner_id = '"+ownerid+"'";
//		Statement statement;
//		try {
//			statement = dbConn.createStatement();
//			ResultSet result = statement.executeQuery(query);
//			if (result.next()) {
//				UwsShareItemBase sharedItem = new UwsShareItemBase();
//				sharedItem.setResourceId(resourceId);
//				sharedItem.setResourceType(resourceType);
//				sharedItem.setOwnerid(ownerid);
//				sharedItem.setTitle(result.getString("title"));
//				sharedItem.setDescription(result.getString("description"));
//				return sharedItem;
//			} else {
//				return null;
//			}
//		} catch (SQLException e) {
//			throw new SQLException("Cannot obtain resource '"+resourceId+"' (type: "+resourceType+") due to: " + e.getMessage(), e);
//		}
//	}
	
//	public List<UwsShareItemBase> getUserSharedItemDescriptions(String ownerid) throws SQLException {
//		String query = "SELECT resource_id, resource_type, title, description FROM share_schema.share WHERE owner_id = '"+ownerid+"'";
//		Statement statement;
//		try {
//			statement = dbConn.createStatement();
//			ResultSet result = statement.executeQuery(query);
//			List<UwsShareItemBase> sharedItems = new ArrayList<UwsShareItemBase>();
//			while (result.next()) {
//				UwsShareItemBase sharedItem = new UwsShareItemBase();
//				sharedItem.setResourceId(result.getString("resource_id"));
//				sharedItem.setResourceType(result.getInt("resource_type"));
//				sharedItem.setOwnerid(ownerid);
//				sharedItem.setTitle(result.getString("title"));
//				sharedItem.setDescription(result.getString("description"));
//				sharedItems.add(sharedItem);
//			}
//			return sharedItems;
//		} catch (SQLException e) {
//			throw new SQLException("Cannot obtain resources descriptions due to: " + e.getMessage(), e);
//		}
//	}
	
	public String getSharedItemOwner(String resourceId, int resourceType) throws SQLException {
		String query = "SELECT owner_id FROM share_schema.share WHERE " +
				"resource_id = '"+resourceId+"' AND resource_type = " + resourceType;
		Statement statement;
		try {
			statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) {
				String ownerid = result.getString("owner_id");
				return ownerid;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain owner id for resource '"+resourceId+"' (type: "+resourceType+") due to: " + e.getMessage(), e);
		}
	}
	
	public List<UwsShareItemBase> getUserSharedItems(String ownerid, int resourceType, boolean includeShareTo) throws SQLException {
		String query = "SELECT resource_id, resource_type, title, description FROM share_schema.share WHERE owner_id = '"+ownerid+"'";
		Statement statement;
		try {
			statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			List<UwsShareItemBase> sharedItems = new ArrayList<UwsShareItemBase>();
			while (result.next()) {
				UwsShareItemBase sharedItem = new UwsShareItemBase();
				sharedItem.setResourceId(result.getString("resource_id"));
				sharedItem.setResourceType(result.getInt("resource_type"));
				sharedItem.setOwnerid(ownerid);
				sharedItem.setTitle(result.getString("title"));
				sharedItem.setDescription(result.getString("description"));
				sharedItems.add(sharedItem);
			}
			if(includeShareTo){
				Map<UwsShareType, String> queries;
				List<UwsShareItem> shareToItems;
				for(UwsShareItemBase sib: sharedItems){
					queries = getQueriesForUserSharedItems(ownerid, sib.getResourceId(), sib.getResourceType(), UwsShareType.All, UwsShareMode.All);
					shareToItems = getUserSharedItems(ownerid, queries);
					sib.setShareToItems(shareToItems);
				}
			}
			return sharedItems;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain resources descriptions due to: " + e.getMessage(), e);
		}
	}
	
	public List<UwsShareItem> getUserSharedItem(String userid, String resourceId, int resourceType) throws SQLException {
		Map<UwsShareType, String> queries = getQueriesForUserSharedItems(userid, resourceId, resourceType, UwsShareType.All, UwsShareMode.All);
		return getUserSharedItems(userid, queries);
	}
	
	private List<UwsShareItem> getUserSharedItems(String userid, Map<UwsShareType, String> queries) throws SQLException {
		ResultSet result;
		try {
			List<UwsShareItem> sharedItems = new ArrayList<UwsShareItem>();
			String queryItem;
			UwsShareType shareItem;
			for(Entry<UwsShareType, String> e: queries.entrySet()){
				shareItem = e.getKey();
				queryItem = e.getValue();
				result = dbConn.createStatement().executeQuery(queryItem);
				while (result.next()) {
					UwsShareItem sharedItem = new UwsShareItem();
					
					sharedItem.setResourceId(result.getString(1));
					sharedItem.setResourceType(result.getInt(2));
					sharedItem.setOwnerId(userid);
					sharedItem.setShareToId(result.getString(3));
					sharedItem.setShareType(shareItem);
					sharedItem.setShareMode(UwsShareMode.values()[result.getInt(4)]);
					
					sharedItems.add(sharedItem);
				}
			}
			return sharedItems;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain shared items list due to: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Main search by owner_id (resource owner)
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @param shareType
	 * @param shareMode
	 * @return
	 */
	private Map<UwsShareType, String> getQueriesForUserSharedItems(String userid, String resourceid, int resourceType, UwsShareType shareType, UwsShareMode shareMode){
		Map<UwsShareType, String> queries = new HashMap<UwsShareType, String>();
		
		/*
		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.User){
			queries.put(UwsShareType.User, "SELECT s.resource_id, s.resource_type, s.user_id, s.share_mode " +
				"FROM share_schema.share_users s WHERE s.owner_id = '"+userid+"' " + 
				getClauseForSharedItems(resourceid, resourceType, shareMode));
		};
		*/

		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.Group){
			queries.put(UwsShareType.Group, "SELECT s.resource_id, s.resource_type, s.group_id, s.share_mode " +
				"FROM share_schema.share_groups s WHERE owner_id = '"+userid+"' " + 
				getClauseForSharedItems(resourceid, resourceType, shareMode));
		}

		return queries;
	}
	
	
	public UwsShareItemBase getSharedItemBase(String resourceid, int resourceType) throws SQLException {
		String query = "SELECT resource_id, resource_type, title, description, owner_id FROM share_schema.share "
				+ "WHERE resource_id = '"+resourceid+"' AND resource_type = " + resourceType;
		try {
			Statement statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (!result.next()) {
				return null;
			}
			
			String ownerid = result.getString("owner_id"); 
			UwsShareItemBase sib = new UwsShareItemBase();
			sib.setResourceId(result.getString("resource_id"));
			sib.setResourceType(result.getInt("resource_type"));
			sib.setOwnerid(ownerid);
			sib.setTitle(result.getString("title"));
			sib.setDescription(result.getString("description"));

			Map<UwsShareType, String> queries = getQueriesForUserSharedItems(
					ownerid, sib.getResourceId(), sib.getResourceType(), UwsShareType.All, UwsShareMode.All);
			List<UwsShareItem> shareToItems = getUserSharedItems(ownerid, queries);
			sib.setShareToItems(shareToItems);

			return sib;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain resources descriptions due to: " + e.getMessage(), e);
		}

	}
	
	
	public List<UwsShareItemBase> getSharedItemsBase(String groupid, String titlePattern, boolean includeSharedToItems) throws SQLException {
		String query = "SELECT s.resource_id, s.resource_type, s.title, s.description, s.owner_id FROM "
				+ "share_schema.share AS s, share_schema.share_groups AS g WHERE "
				+ "s.resource_id = g.resource_id AND s.resource_type = g.resource_type AND g.group_id = '"+groupid+"'";
		
		if(titlePattern!=null && !titlePattern.trim().isEmpty()){
			query += " AND s.title ILIKE '%"+titlePattern+"%'";
		}
		try {
			Statement statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			List<UwsShareItemBase> items = new ArrayList<UwsShareItemBase>();
			while(result.next()){
				String ownerid = result.getString("owner_id"); 
				UwsShareItemBase sib = new UwsShareItemBase();
				sib.setResourceId(result.getString("resource_id"));
				sib.setResourceType(result.getInt("resource_type"));
				sib.setOwnerid(ownerid);
				sib.setTitle(result.getString("title"));
				sib.setDescription(result.getString("description"));
	
				if(includeSharedToItems){
					Map<UwsShareType, String> queries = getQueriesForUserSharedItems(
						ownerid, sib.getResourceId(), sib.getResourceType(), UwsShareType.All, UwsShareMode.All);
					List<UwsShareItem> shareToItems = getUserSharedItems(ownerid, queries);
					sib.setShareToItems(shareToItems);
				}else{
					sib.setShareToItems(null);
				}
				items.add(sib);
			}

			return items;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain resources due to: " + e.getMessage(), e);
		}
	}

	
	/**
	 * Returns the items the user can access to.
	 * @param userid
	 * @param resourceType
	 * @param shareType
	 * @param shareMode
	 * @return
	 * @throws SQLException
	 */
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws SQLException {
		Map<UwsShareType, String> queries = getQueriesForSharedItems(userid, resourceType, shareType, shareMode);
		ResultSet result;
		try{
			List<UwsShareItemBase> sharedItems = new ArrayList<UwsShareItemBase>();
			//List<UwsShareItem> sharedItems = new ArrayList<UwsShareItem>();
			String queryItem;
			UwsShareType shareItem;
			String title;
			String itemResourceId;
			int itemResourceType;
			String itemOwnerId;
			String description;
			for(Entry<UwsShareType, String> e: queries.entrySet()){
				shareItem = e.getKey();
				queryItem = e.getValue();
				result = dbConn.createStatement().executeQuery(queryItem);
				while(result.next()){
					UwsShareItem sharedItem = new UwsShareItem();
					itemResourceId = result.getString(1);
					itemResourceType = result.getInt(2);
					itemOwnerId = result.getString(3);
					sharedItem.setResourceId(itemResourceId);
					sharedItem.setResourceType(itemResourceType);
					sharedItem.setOwnerId(itemOwnerId);
					sharedItem.setShareToId(result.getString(4));
					sharedItem.setShareType(shareItem);
					sharedItem.setShareMode(UwsShareMode.values()[result.getInt(5)]);
					title = result.getString(6);
					description = result.getString(7);
					
					UwsShareItemBase sib = findSharedItemBase(sharedItems, sharedItem);
					if(sib == null){
						sib = new UwsShareItemBase(itemResourceId, itemResourceType, title, description, itemOwnerId);
						sharedItems.add(sib);
					}
					List<UwsShareItem> items = sib.getShareToItems();
					if(items == null){
						items = new ArrayList<UwsShareItem>();
						sib.setShareToItems(items);
					}
					items.add(sharedItem);
					//sharedItems.add(sharedItem);
				}
			}
			return sharedItems;
		}catch(SQLException e){
			throw new SQLException("Cannot obtain shared items list for user '"+userid+"' due to: " + e.getMessage(), e);
		}
	}
	
	private UwsShareItemBase findSharedItemBase(List<UwsShareItemBase> sharedItems, UwsShareItem sharedItem){
		if(sharedItems == null || sharedItems.size() < 1){
			return null;
		}
		for(UwsShareItemBase sib: sharedItems){
			if(sharedItem.getResourceId().equals(sib.getResourceId()) && sharedItem.getResourceType() == sib.getResourceType() && sharedItem.getOwnerid().equals(sib.getOwnerid())){
				return sib;
			}
		}
		return null;
	}
	
	/**
	 * Main search by user_id (user that is authorized to access to the resource)
	 * @param userid
	 * @param resourceType
	 * @param shareType
	 * @param shareMode
	 * @return
	 */
	private Map<UwsShareType, String> getQueriesForSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode){
		Map<UwsShareType, String> queries = new HashMap<UwsShareType, String>();
		
		/*
		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.User){
			queries.put(UwsShareType.User, "SELECT s.resource_id, s.resource_type, s.owner_id, s.user_id, s.share_mode, x.title, x.description " +
				"FROM share_schema.share_users s, share_schema.share x WHERE " +
				"(s.user_id = '"+userid+"' AND s.resource_id = x.resource_id AND s.resource_type = x.resource_type AND s.owner_id = x.owner_id) " + 
				getClauseForSharedItems(resourceType, shareMode));
		};
		*/

		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.Group){
			queries.put(UwsShareType.Group, "SELECT s.resource_id, s.resource_type, s.owner_id, s.group_id, s.share_mode, x.title, x.description " +
				"FROM share_schema.share_groups s, share_schema.user_groups g, share_schema.share x WHERE " +
				"(s.group_id = g.group_id AND g.user_id = '"+userid+"' AND s.owner_id <> '"+userid+"') AND " +
				"(s.resource_id = x.resource_id AND s.resource_type = x.resource_type AND s.owner_id = x.owner_id) " + 
				getClauseForSharedItems(resourceType, shareMode));
		}

		return queries;
	}

	private String getClauseForSharedItems(int resourceType, UwsShareMode shareMode){
		return getClauseForSharedItems(UwsShareManager.UNSPECIFIED_RESOURCE_ID, resourceType, shareMode);
	}

	private String getClauseForSharedItems(String resourceid, int resourceType, UwsShareMode shareMode){
		String query = "";
		
		if(resourceid != UwsShareManager.UNSPECIFIED_RESOURCE_ID){
			query += " AND (s.resource_id = '"+resourceid+"')";
		}
		if(resourceType != UwsShareManager.UNSPECIFIED_RESOURCE_TYPE){
			query += " AND (s.resource_type = " + resourceType + ")";
		}
		if(shareMode != null && shareMode != UwsShareMode.All){
			query += " AND (s.share_mode = " + shareMode.ordinal() + ")";
		}
		
		return query;
	}
	

	/**
	 * Main search by user_id (user that is authorized to access to the resource)
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @param shareType
	 * @param shareMode
	 * @return
	 */
	private Map<UwsShareType, String> getQueriesForSharedItems(String userid, String resourceid, int resourceType, UwsShareType shareType, UwsShareMode shareMode){
		Map<UwsShareType, String> queries = new HashMap<UwsShareType, String>();
		
		/*
		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.User){
			queries.put(UwsShareType.User, "SELECT s.resource_id, s.resource_type, s.owner_id, s.user_id, s.share_mode, x.title, x.description " +
				"FROM share_schema.share_users s, share_schema.share x WHERE " +
				"(s.user_id = '"+userid+"' AND s.resource_id = x.resource_id AND s.resource_type = x.resource_type AND s.owner_id = x.owner_id) " + 
				getClauseForSharedItems(resourceid, resourceType, shareMode));
		};
		*/

		if(shareType == null || shareType == UwsShareType.All || shareType == UwsShareType.Group){
			queries.put(UwsShareType.Group, "SELECT s.resource_id, s.resource_type, s.owner_id, s.group_id, s.share_mode, x.title, x.description " +
				"FROM share_schema.share_groups s, share_schema.user_groups g, share_schema.share x WHERE " +
				"(s.group_id = g.group_id AND g.user_id = '"+userid+"' AND s.owner_id <> '"+userid+"') AND " +
				"(s.resource_id = x.resource_id AND s.resource_type = x.resource_type AND s.owner_id = x.owner_id) " + 
				getClauseForSharedItems(resourceid, resourceType, shareMode));
		}

		return queries;
	}

//	/**
//	 * Returns the items the user is sharing.
//	 * @param userid
//	 * @param resourceid
//	 * @param resourceType
//	 * @return
//	 * @throws SQLException
//	 */
//	public List<UwsShareItem> getUserSharedItemsInfo(String userid, String resourceid, int resourceType) throws SQLException {
//		String query = "SELECT share_to_id, share_type, share_mode FROM share_schema.share WHERE " +
//				"owner_id = '"+userid+"' AND resource_id = '"+resourceid+"' AND resource_type ILIKE '"+resourceType+"'";
//		
//		ResultSet result;
//		try{
//			result = dbConn.createStatement().executeQuery(query);
//			List<UwsShareItem> sharedItems = new ArrayList<UwsShareItem>();
//			while (result.next()) {
//				UwsShareItem sharedItem = new UwsShareItem();
//				
//				sharedItem.setResourceId(resourceid);
//				sharedItem.setResourceType(resourceType);
//				sharedItem.setOwnerId(userid);
//				sharedItem.setShareToId(result.getString("share_to_id"));
//				sharedItem.setShareType(UwsShareType.valueOf(result.getString("share_type")));
//				sharedItem.setShareMode(UwsShareMode.valueOf(result.getString("share_mode")));
//				
//				sharedItems.add(sharedItem);
//			}
//			return sharedItems;
//		}catch(SQLException e){
//			throw new SQLException("Cannot obtain shared items list for resource '"+resourceid+"' (type: '"+resourceType+"') for user user '"+userid+"'", e);
//		}
//	}
	
	
	public void removeSharedItem(String resourceid, int resourceType, String ownerid) throws SQLException {
		List<String> queries = new ArrayList<String>();
		queries.add("DELETE FROM share_schema.share_groups WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"'");
		//queries.add("DELETE FROM share_schema.share_users WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"'");
		queries.add("DELETE FROM share_schema.share WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"'");
		try{
			dbConn.setAutoCommit(false);
			Statement statement = dbConn.createStatement();
			for(String query: queries){
				statement.executeUpdate(query);
			}
			dbConn.commit();
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot delete resource '"+resourceid+"' (type '"+resourceType+"') due to: " + e.getMessage(), e);
		}
	}
	
	public void removeSharedItemRelation(String resourceid, int resourceType, String shareToId, UwsShareType shareType, String ownerid) throws SQLException {
		List<String> queries = new ArrayList<String>();
		switch(shareType){
		case Group:
			queries.add("DELETE FROM share_schema.share_groups WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"' AND group_id = '"+shareToId+"'");
			break;
		case User:
			//queries.add("DELETE FROM share_schema.share_users WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"' AND user_id = '"+shareToId+"'");
			break;
		case All:
			queries.add("DELETE FROM share_schema.share_groups WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"' AND group_id = '"+shareToId+"'");
			//queries.add("DELETE FROM share_schema.share_users WHERE resource_id = '"+resourceid+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"' AND user_id = '"+shareToId+"'");
			break;
		}
		try{
			dbConn.setAutoCommit(false);
			Statement statement = dbConn.createStatement();
			for(String query: queries){
				statement.executeUpdate(query);
			}
			dbConn.commit();
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot delete resource '"+resourceid+"' (type '"+resourceType+"') due to: " + e.getMessage(), e);
		}
	}
	
	public List<UwsShareUser> getUsers(String pattern, int maxResults) throws SQLException {
		String query = "SELECT owner_id FROM uws2_schema.owners";
		if(pattern != null && !"".equals(pattern)){
			query += " WHERE owner_id ILIKE '"+getSuitablePattern(pattern)+"'";
		}
		query += " ORDER BY owner_id";
		if(maxResults >= 0){
			query += " LIMIT " + maxResults;
		}
		Statement statement;
		try {
			statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			List<UwsShareUser> users = new ArrayList<UwsShareUser>();
			String id;
			while (result.next()) {
				id =  result.getString("owner_id");
				UwsShareUser usu = new UwsShareUser(id, id);
				users.add(usu);
			}
			return users;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain users list due to: " + e.getMessage(), e);
		}
	}
	
	public boolean createSharedItem(UwsShareItemBase sharedItem) throws SQLException {
		try{
			dbConn.setAutoCommit(false);
			PreparedStatement statement = dbConn.prepareStatement(SHARED_ITEM_CREATE);
			statement.setString(1, sharedItem.getResourceId());
			statement.setInt(2, sharedItem.getResourceType());
			statement.setString(3, sharedItem.getOwnerid());
			statement.setString(4, sharedItem.getTitle());
			statement.setString(5, sharedItem.getDescription());
			
			statement.executeUpdate();
			
			List<UwsShareItem> sharedItems = sharedItem.getShareToItems();
			addSharedItemRelations(sharedItem, sharedItems, false);
			
			dbConn.commit();
			return true;
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot create shared item '"+sharedItem.getResourceId()+"' (type '"+sharedItem.getResourceType()+"') due to: " + e.getMessage(), e);
		}
	}
	
	public boolean updateSharedItem(UwsShareItemBase sharedItem) throws SQLException {
		try{
			dbConn.setAutoCommit(false);
			PreparedStatement statement = dbConn.prepareStatement(SHARED_ITEM_UPDATE);
			statement.setString(1, sharedItem.getTitle());
			statement.setString(2, sharedItem.getDescription());
			statement.setString(3, sharedItem.getResourceId());
			statement.setInt(4, sharedItem.getResourceType());
			statement.setString(5, sharedItem.getOwnerid());
			
			statement.executeUpdate();
			
			List<UwsShareItem> sharedItems = sharedItem.getShareToItems();
			addSharedItemRelations(sharedItem, sharedItems, true);
			
			dbConn.commit();
			return true;
		}catch(SQLException e){
			dbConn.rollback();
			throw new SQLException("Cannot update item '"+sharedItem.getResourceId()+"' (type '"+sharedItem.getResourceType()+"') due to: " + e.getMessage(), e);
		}
	}

	/**
	 * This is part of a transaction. DO NOT CALL WITHOUT A dbConn.setAutoCommit(false)/commit/rollback.
	 * @param sharedItem
	 * @param sharedItems
	 * @param clearPrevRelations
	 * @throws SQLException
	 */
	private void addSharedItemRelations(UwsShareItemBase sharedItem, List<UwsShareItem> sharedItems, boolean clearPrevRelations) throws SQLException{
		String resourceId = sharedItem.getResourceId();
		int resourceType = sharedItem.getResourceType();
		String ownerid = sharedItem.getOwnerid();

		if(clearPrevRelations){
			Statement st = dbConn.createStatement();
			st.execute("DELETE FROM share_schema.share_groups WHERE resource_id = '"+resourceId+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"'");
			//st.execute("DELETE FROM share_schema.share_users WHERE resource_id = '"+resourceId+"' AND resource_type = "+resourceType+" AND owner_id = '"+ownerid+"'");

		}
		
		if(sharedItems == null){
			return;
		}
		
		for(UwsShareItem si: sharedItems){
			addSharedItem(ownerid, si);
		}
	}

	public boolean addSharedItem(String ownerid, UwsShareItem sharedItem) throws SQLException {
		PreparedStatement statement;
		switch(sharedItem.getShareType()){
		case Group:
			statement = dbConn.prepareStatement(ADD_SHARED_ITEM_GROUP);
			break;
		case User:
			statement = dbConn.prepareStatement(ADD_SHARED_ITEM_USER);
			break;
		default:
			throw new SQLException("Invalid share type: " + sharedItem.getShareType() + ", when adding shared item: " + sharedItem.getResourceId() + " (type: "+sharedItem.getResourceType()+")");
		}
		statement.setString(1, sharedItem.getResourceId());
		statement.setInt(2, sharedItem.getResourceType());
		statement.setString(3, sharedItem.getOwnerid());
		statement.setString(4, sharedItem.getShareToId());
		statement.setInt(5, sharedItem.getShareMode().ordinal());
		try{
			return statement.executeUpdate() == 1;
		}catch(SQLException e){
			throw new SQLException("Cannot add shared item '"+sharedItem.getResourceId()+"' (type '"+sharedItem.getResourceType()+"') due to: " + e.getMessage(), e);
		}
	}
	
	private static String getSuitablePattern(String pattern) {
		if (pattern == null) {
			return "%";
		} else {
			if (pattern.indexOf('%') >= 0) {
				return pattern;
			} else {
				return '%' + pattern + '%';
			}
		}
	}
	
	public Set<String> getUniqueGroupsUsers(Set<String> groupIds) throws SQLException {
		if(groupIds == null){
			return null;
		}
		if(groupIds.size() < 1){
			return null;
		}
		StringBuilder sb = new StringBuilder("SELECT DISTINCT(user_id) FROM share_schema.user_groups WHERE group_id IN (");
		boolean firstTime = true;
		for(String groupId: groupIds){
			if(firstTime){
				firstTime = false;
			}else{
				sb.append(',');
			}
			sb.append('\'').append(groupId).append('\'');
		}
		sb.append(')');
		try {
			Statement statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(sb.toString());
			Set<String> users = new TreeSet<String>();
			String id;
			while (result.next()) {
				id =  result.getString(1);
				users.add(id);
			}
			return users;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain users list due to: " + e.getMessage(), e);
		}
	}

	public Set<String> getGroupUsers(String groupid) throws SQLException {
		String query = "SELECT user_id FROM share_schema.user_groups WHERE group_id = '"+groupid+"'";
		try {
			Statement statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery(query);
			Set<String> users = new TreeSet<String>();
			String id;
			while (result.next()) {
				id =  result.getString(1);
				users.add(id);
			}
			return users;
		} catch (SQLException e) {
			throw new SQLException("Cannot obtain users list due to: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Check whether the user can access to the specified item
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @return
	 * @throws SQLException
	 */
	public boolean hasAccessToShareItem(String userid, String resourceid, int resourceType) throws SQLException {
		Map<UwsShareType, String> queries = getQueriesForSharedItems(userid, resourceid, resourceType, UwsShareType.All, UwsShareMode.All);
		ResultSet result;
		try{
			String queryItem;
			for(Entry<UwsShareType, String> e: queries.entrySet()){
				queryItem = e.getValue();
				result = dbConn.createStatement().executeQuery(queryItem);
				if(result.next()){
					return true;
				}
			}
			return false;
		}catch(SQLException e){
			throw new SQLException("Cannot obtain shared items list for user '"+userid+"' due to: " + e.getMessage(), e);
		}
	}
}
