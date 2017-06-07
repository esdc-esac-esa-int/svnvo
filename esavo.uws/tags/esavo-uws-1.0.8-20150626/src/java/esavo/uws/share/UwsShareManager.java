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
package esavo.uws.share;

import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

/**
 * Share manager functions.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsShareManager {
	
	public static final String UNSPECIFIED_RESOURCE_ID = null;
	public static final int UNSPECIFIED_RESOURCE_TYPE = -1;
	public static final UwsShareMode UNSPECIFIED_SHARE_MODE = null;
	public static final UwsShareType UNSPECIFIED_SHARE_TYPE = null;
	
	/**
	 * Returns a list of groups where the user is the owner or the user belongs to.
	 * @param userid
	 * @param includeUsers
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareGroup> getGroups(String userid, boolean includeUsers) throws UwsException;
	
	/**
	 * Returns the groups the user belongs to.
	 * @param userid
	 * @return
	 */
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid) throws UwsException;
	
	/**
	 * Returns the groups the user is the owner.
	 * @param ownerid
	 * @param includeUsers 'true' to include the users that belong to each group.
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareGroup> getGroupsByOwner(String ownerid, boolean includeUsers) throws UwsException;
	
	
	/**
	 * Adds a group
	 * @param group
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public boolean addGroup(UwsShareGroup group, UwsJobOwner owner) throws UwsException;
	

	/**
	 * Returns the shared items associated to a group. If a title pattern is provided, only the 
	 * items matching %titlePattern% are returned.
	 * @param groupid
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getGroupItems(String groupid) throws UwsException;

	/**
	 * Returns the shared items associated to a group. If a title pattern is provided, only the 
	 * items matching %titlePattern% are returned.
	 * @param groupid
	 * @param itemTitlePattern
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getGroupItems(String groupid, String itemTitlePattern) throws UwsException;
	
	/**
	 * Returns the user shared resources
	 * (resources that belong to the specified user that can be accessible by other users)
	 * @param userid
	 * @return
	 */
	public List<UwsShareItemBase> getUserSharedItems(String userid, boolean includeShareTo) throws UwsException;
	
	/**
	 * Returns the user shared resources by type
	 * (resources that belong to the specified user that can be accessible by other users)
	 * @param userid
	 * @param resourceType 'tables', 'jobs', etc.
	 * @return
	 */
	public List<UwsShareItemBase> getUserSharedItems(String userid, int resourceType, boolean includeShareTo) throws UwsException;
	
	/**
	 * Returns the data associated to the specified shared resource.
	 * @param resourceId
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public UwsShareItemBase getUserSharedItem(String resourceId, int resourceType) throws UwsException;
	
	
	/**
	 * Returns the specified user shared item. It can be shared among different users and/or groups. So it returns a list.
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareItem> getUserSharedItem(String userid, String resourceid, int resourceType) throws UwsException;
	
//	/**
//	 * Returns the shared items descriptions. 
//	 * @param userid
//	 * @return
//	 * @throws UwsException
//	 */
//	public List<UwsShareItemBase> getUserSharedItemsDescriptions(String userid) throws UwsException;
	
	/**
	 * Returns the shared items the provided user can access to
	 * (resources shared by other users that are accessible to the specified user).
	 * @param userid
	 * @return
	 */
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid) throws UwsException;
	
	/**
	 * Returns the shared items the provided user can access to
	 * (resources shared by other users that are accessible to the specified user).
	 * @param userid
	 * @param resourceType 'tables', 'jobs', etc.
	 * @param shareType 'group', 'user', etc.
	 * @param shareMode 'R', 'W'
	 * @return
	 */
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException;

	
//	/**
//	 * Returns the shared items based on the request.
//	 * @param userid identifier
//	 * @param resourceid identifier
//	 * @param resourceType 'table', 'job', etc.
//	 * @return
//	 */
//	public List<UwsShareItem> getUserSharedItemsInfo(String userid, String resourceid, String resourceType) throws UwsException;
	

	/**
	 * Returns the maximum accessibility for accessible shared items the user can access to. 
	 * See {@link #getMaxAccessibilityAccessibleSharedItems(String, String, UwsShareType, UwsShareMode)}}
	 * @param userid
	 * @param resourceType 'table', 'job', etc.
	 * @return the maximum accessibility for accessible shared items the user can access to.
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(String userid, int resourceType) throws UwsException;
	
	/**
	 * Returns the maximum accessibility for accessible shared items the user can access to.<br/>
	 * For instance, if a resource is shared in two groups the user belongs to, and in one group the access mode is 'R' (READ) and in the
	 * other group, the access is 'W' (WRITE), the resource with 'W' (WRITE) mode is selected
	 * @param userid
	 * @param resourceType 'table', 'job', etc.
	 * @param shareType 'group', 'user', etc.
	 * @param shareMode 'R', 'W'
	 * @return the maximum accessibility for accessible shared items the user can access to.
	 * @throws UwsException
	 */
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException;
	
	
	/**
	 * 
	 * @param group (groupId can be null, it means: crate group)
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String createOrUpdateGroup(UwsShareGroup group, UwsJobOwner owner) throws UwsException;
	
	/**
	 * 
	 * @param groupId
	 * @param userId
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String addUserToGroup(String groupId, String userId, UwsJobOwner owner) throws UwsException;
	
	/**
	 * 
	 * @param sharedItem
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String createOrUpdateSharedItem(UwsShareItemBase sharedItem, UwsJobOwner owner) throws UwsException;
	
	/**
	 * 
	 * @param owner
	 * @param sharedItem
	 * @return
	 * @throws UwsException
	 */
	public String addSharedItemRelation(UwsJobOwner owner, UwsShareItem sharedItem) throws UwsException;

	/**
	 * 
	 * @param groupId
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String removeGroup(String groupId, UwsJobOwner owner) throws UwsException;
	
	/**
	 * 
	 * @param groupId
	 * @param userId
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String removeGroupUser(String groupId, String userId, UwsJobOwner owner) throws UwsException;
	
	/**
	 * Deletes the specified shared item. It is removed from any relation between the item and any user and/or groups.
	 * @param resourceId
	 * @param resourceType
	 * @param owner
	 * @return
	 * @throws UwsException
	 */
	public String removeSharedItem(String resourceId, int resourceType, UwsJobOwner owner) throws UwsException;

	/**
	 * Deletes the specified shared item relation.
	 * @param owner
	 * @param shareItem
	 * @return
	 * @throws UwsException
	 */
	public String removeSharedItemRelation(UwsJobOwner owner, UwsShareItem shareItem) throws UwsException;
	
	/**
	 * Returns a list of users available
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareUser> getUsers() throws UwsException;

	/**
	 * Returns a list of users available
	 * @param pattern can be null (all users are returned)
	 * @param limit max number of results (-1: disabled)
	 * @return
	 * @throws UwsException
	 */
	public List<UwsShareUser> getUsers(String pattern, int limit) throws UwsException;
	
	
	/**
	 * Updates the users properties (i.e. the name) based on user ids from the list.
	 * @param users
	 * @throws UwsException
	 */
	public void updateUsers(List<UwsShareUser> users) throws UwsException;

	/**
	 * Returns the user details of a user
	 * @param userid
	 * @return
	 * @throws UwsException
	 */
	public UwsShareUser getSharedUser(String userid) throws UwsException;
	
	/**
	 * Checks whether the user has access to the specified item
	 * @param userid
	 * @param resourceid
	 * @param resourceType
	 * @return
	 * @throws UwsException
	 */
	public boolean hasAccess(String userid, String resourceid, int resourceType) throws UwsException;

}
