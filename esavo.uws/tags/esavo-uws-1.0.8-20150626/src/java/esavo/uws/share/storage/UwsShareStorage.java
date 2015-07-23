package esavo.uws.share.storage;

import java.util.List;
import java.util.Set;

import esavo.uws.UwsException;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;
import esavo.uws.share.UwsShareUser;

/**
 * ShareManager storage functions.
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsShareStorage {

	/**
	 * Returns the groups the provided user belongs to.
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid) throws UwsException;
	
	/**
	 * Adds the specified group.
	 * @param group
	 * @return
	 * @throws UwsException
	 */
	public boolean addGroup(UwsShareGroup group) throws UwsException;
	
	/**
	 * Updates a group
	 * @param group
	 * @return
	 * @throws UwsException
	 */
	public boolean updateGroup(UwsShareGroup group) throws UwsException;
	
	/**
	 * Deletes a group
	 * @param groupId
	 * @return
	 * @throws UwsException
	 */
	public boolean removeGroup(String groupId) throws UwsException;
	
	/**
	 * 
	 * @param groupId
	 * @return
	 * @throws UwsException
	 */
	public UwsShareGroup getGroup(String groupId) throws UwsException;
	
	/**
	 * 
	 * @param groupsIds
	 * @return
	 * @throws UwsException
	 */
	public Set<String> getGroupsOwners(Set<String> groupsIds) throws UwsException;
	
	/**
	 * 
	 * @param groupId
	 * @param userToAdd
	 * @return
	 * @throws UwsException
	 */
	public boolean addUserToGroup(String groupId, String userToAdd) throws UwsException;
	
	/**
	 * 
	 * @param groupId
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public boolean removeGroupUser(String groupId, String userid) throws UwsException;
	
	/**
	 * Returns the user shared resources by type
	 * (resources that belong to the specified user that can be accessible by other users)
	 * @param userid
	 * @param resourceType
	 * @param inlcudeShareTo
	 * @return
	 */
	public List<UwsShareItemBase> getUserSharedItems(String userid, int resourceType, boolean includeShareTo) throws UwsException;
	
	/**
	 * Returns the specified user shared resource. It can be shared among different users and/or groups.
	 * (resources that belong to the specified user that can be accessible by other users)
	 * @param userid
	 * @param resourceId
	 * @param resourceType
	 * @return
	 */
	public List<UwsShareItem> getUserSharedItem(String userid, String resourceId, int resourceType) throws UwsException;
	
	/**
	 * Returns the owner of the specified resource.
	 * @param resourceId
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public String getSharedItemOwner(String resourceId, int resourceType) throws UwsException;
	
	/**
	 * Returns the shared items the provided user can access to
	 * (resources shared by other users that are accessible to the specified user).
	 * @param userid
	 * @param resourceType
	 * @param shareType
	 * @param shareMode
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException;
	
	/**
	 * Returns the groups that belong to the specified user.
	 * @param ownerid
	 * @param includeUsers 'true' to include the users that belong to each group.
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareGroup> getGroupsByOwner(String ownerid, boolean includeUsers) throws UwsException;

	/**
	 * Removes a shared item
	 * @param resourceid
	 * @param resourceType
	 * @param ownerid
	 * @throws UwsException
	 */
	public void removeSharedItem(String resourceid, int resourceType, String ownerid) throws UwsException;
	
	/**
	 * Removes a shared item relation.
	 * @param resourceid
	 * @param resourceType
	 * @param shareToId
	 * @param shareType
	 * @param ownerid
	 * @throws UwsException
	 */
	public void removeSharedItemRelation(String resourceid, int resourceType, String shareToId, UwsShareType shareType, String ownerid) throws UwsException;
	
	/**
	 * 
	 * @param sharedItem
	 * @throws UwsException
	 */
	public void createSharedItem(UwsShareItemBase sharedItem) throws UwsException;
	
	/**
	 * 
	 * @param sharedItem
	 * @throws UwsException
	 */
	public void updateSharedItem(UwsShareItemBase sharedItem) throws UwsException;

	/**
	 * 
	 * @param ownerid
	 * @param sharedItem
	 * @throws UwsException
	 */
	public void addSharedItem(String ownerid, UwsShareItem sharedItem) throws UwsException;
	
	
	/**
	 * Returns a shared item including the share to items
	 * @param resourceid
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public UwsShareItemBase getSharedItemBase(String resourceid, int resourceType) throws UwsException;

	/**
	 * Returns all the items shared to the specified group. 
	 * @param groupid
	 * @param includeSharedToItems
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getSharedItemsBase(String groupid, boolean includeSharedToItems) throws UwsException;

	/**
	 * Returns all the items shared to the specified group. If a title pattern is provided, only the 
	 * items matching %titlePattern% are returned.
	 * @param groupid
	 * @param titlePattern
	 * @param includeSharedToItems
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getSharedItemsBase(String groupid, String titlePattern, boolean includeSharedToItems) throws UwsException;
	
	/**
	 * 
	 * @param pattern can be null (all users are returned)
	 * @param maxResults -1 disabled.
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareUser> getUsers(String pattern, int maxResults) throws UwsException;
	
	
	/**
	 * Returns a unique list of users from a shared to items list
	 * @param sharedItems
	 * @return
	 * @throws UwsException
	 */
	public Set<String> getUniqueUsersFromGroups(Set<String> groupIds) throws UwsException;
	
	/**
	 * Returns the users that belong to the specified group
	 * @param groupid
	 * @return
	 * @throws UwsException
	 */
	public Set<String> getUsersFromGroup(String groupid) throws UwsException;

	/**
	 * Returns whether the user has access to the specified resource. 
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public boolean hasAccessToShareItem(String userid, String resourceid, int resourceType) throws UwsException;

}
