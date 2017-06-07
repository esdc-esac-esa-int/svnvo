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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.event.UwsEventType;
import esavo.uws.event.UwsEventsManager;
import esavo.uws.notifications.UwsNotificationsManager;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.storage.UwsLdapHandler;
import esavo.uws.share.storage.UwsShareJdbcStorage;
import esavo.uws.share.storage.UwsShareStorage;
import esavo.uws.utils.UwsUtils;

public class UwsDefaultShareManager implements UwsShareManager {
	
	private UwsConfiguration configuration;
	private String appid;
	private UwsShareStorage storage;
	private UwsEventsManager eventsManager;
	private UwsLdapHandler ldapHandler;
	private UwsNotificationsManager notificationsManager;
	
	public UwsDefaultShareManager(String appid, UwsConfiguration configuration, UwsEventsManager eventsManager, UwsNotificationsManager notificationsManager){
		this.appid = appid;
		this.configuration = configuration;
		this.storage = new UwsShareJdbcStorage(appid, configuration);
		this.eventsManager = eventsManager;
		this.notificationsManager = notificationsManager;
		this.ldapHandler = createLdapHandler(configuration);
	}
	
	@Override
	public void updateGroupUsers(List<UwsShareGroup> groups) throws UwsException{
		if(ldapHandler != null){
			for(UwsShareGroup g: groups){
				ldapHandler.updateUsers(g.getUsers());
			}
		}
	}

	@Override
	public void updateUsers(List<UwsShareUser> users) throws UwsException{
		if(ldapHandler != null){
			ldapHandler.updateUsers(users);
		}
	}
	
	private UwsLdapHandler createLdapHandler(UwsConfiguration config) {
		String ldapServer = config.getProperty(UwsConfiguration.LDAP_SERVER);
		if(ldapServer != null && !"".equals(ldapServer)){
			try {
				return new UwsLdapHandler(config);
			} catch (UwsException e) {
				e.printStackTrace();
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Returns the application identifier.
	 * @return
	 */
	public String getAppid(){
		return this.appid;
	}
	
	@Override
	public List<UwsShareGroup> getGroups(String userid, boolean includeUsers) throws UwsException {
		List<UwsShareGroup> groupsTheUserBelongsTo = getGroupsTheUserBelongsTo(userid, includeUsers);
		List<UwsShareGroup> groupsOwneredByUser = getGroupsByOwner(userid, true);
		List<UwsShareGroup> groups = getUnifiedGroupList(userid, groupsTheUserBelongsTo, groupsOwneredByUser);
		updateGroupUsers(groups);
		return groups;
	}
	
	@Override
	public List<UwsShareGroup> getGroupUsers(List<String> groupIds) throws UwsException {
		List<UwsShareGroup> groups = storage.getGroups(groupIds, true);
		updateGroupUsers(groups);
		return groups;
	}
	
	@Override
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid, boolean includeUsers) throws UwsException{
		List<UwsShareGroup> groups = storage.getGroupsTheUserBelongsTo(userid, includeUsers);
		List<UwsShareGroup> extraGroups = UwsManager.getInstance().getFactory().getAvailableGroups(userid);
		List<UwsShareGroup> totalGroups = addExtraGroupsIfRequired(groups, extraGroups);
		return totalGroups;
	}
	
	private List<UwsShareGroup> addExtraGroupsIfRequired(List<UwsShareGroup> groups, List<UwsShareGroup> extraGroups){
		if(extraGroups == null){
			return groups;
		}else{
			if(groups == null){
				return extraGroups;
			}else{
				//Add extra groups if they are not already added
//				List<UwsShareGroup> toAdd = new ArrayList<UwsShareGroup>();
				boolean found = false;
				for(UwsShareGroup g: extraGroups){
					found = false;
					for(UwsShareGroup g2: groups){
						if(g2.getId().equals(g.getId())){
							found = true;
							break;
						}
					}
					if(!found){
						groups.add(g);
					}
				}
//				if(toAdd.size() > 0){
//					groups.addAll(toAdd);
//				}
				return groups;
			}
		}
	}
	
	@Override
	public List<UwsShareGroup> getGroupsByOwner(String ownerid, boolean includeUsers) throws UwsException {
		List<UwsShareGroup> groups = storage.getGroupsByOwner(ownerid, includeUsers);
		if(ldapHandler != null){
			for(UwsShareGroup g: groups){
				ldapHandler.updateUsers(g.getUsers());
			}
		}
		return groups;
	}
	
	@Override
	public List<UwsShareItemBase> getGroupItems(String groupid) throws UwsException{
		return getGroupItems(groupid, null);
	}
	
	@Override
	public List<UwsShareItemBase> getGroupItems(String groupid, String itemTitlePattern) throws UwsException {
		return storage.getSharedItemsBase(groupid, itemTitlePattern, false);
	}
	
	@Override
	public List<UwsShareItemBase> getUserSharedItems(String userid, boolean includeShareTo) throws UwsException {
		return getUserSharedItems(userid, UNSPECIFIED_RESOURCE_TYPE, includeShareTo);
	}
	
	@Override
	public List<UwsShareItemBase> getUserSharedItems(String userid, int resourceType, boolean includeShareTo) throws UwsException {
		return storage.getUserSharedItems(userid, resourceType, includeShareTo);
	}
	
//	@Override
//	public List<UwsShareItemBase> getUserSharedItemsDescriptions(String userid)	throws UwsException {
//		return storage.getUserSharedItemDescriptions(userid);
//	}

	@Override
	public List<UwsShareItem> getUserSharedItem(String userid, String resourceid, int resourceType) throws UwsException {
		return storage.getUserSharedItem(userid, resourceid, resourceType);
	}
	
	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid) throws UwsException {
		return getAccessibleSharedItems(userid, UNSPECIFIED_RESOURCE_TYPE, UNSPECIFIED_SHARE_TYPE, UNSPECIFIED_SHARE_MODE);
	}
	
	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException {
		List<UwsShareItemBase> items = storage.getAccessibleSharedItems(userid, resourceType, shareType, shareMode);
		List<UwsShareGroup> extraGroups = UwsManager.getInstance().getFactory().getAvailableGroups(userid);
		if(extraGroups != null && extraGroups.size() > 0){
			for(UwsShareGroup g: extraGroups){
				List<UwsShareItemBase> extraItems = getGroupItems(g.getId());
				items = addUniqueItems(items, extraItems);
			}
		}
		return items;
	}
	
	private List<UwsShareItemBase> addUniqueItems(List<UwsShareItemBase> items, List<UwsShareItemBase> extraItems){
		if(items == null){
			return extraItems;
		}
		if(extraItems != null){
			boolean found = false;
			for(UwsShareItemBase ei: extraItems){
				found = false;
				for(UwsShareItemBase i: items){
					if(i.getResourceId() == ei.getResourceId() && i.getResourceType() == ei.getResourceType()){
						found = true;
						break;
					}
				}
				if(!found){
					items.add(ei);
				}
			}
		}
		return items;
	}
	
	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(String userid, int resourceType) throws UwsException {
		return getMaxAccessibilityAccessibleSharedItems(userid, resourceType, UNSPECIFIED_SHARE_TYPE, UNSPECIFIED_SHARE_MODE);
	}
	
	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(String userid, int resourceType, UwsShareType shareType, UwsShareMode shareMode) throws UwsException {
		List<UwsShareItemBase> accessibleItems = getAccessibleSharedItems(userid, resourceType, shareType, shareMode);
		if(accessibleItems == null || accessibleItems.size() == 0){
			return accessibleItems;
		}
		for(UwsShareItemBase sib: accessibleItems){
			List<UwsShareItem> siList = getMaxAccessibilityItems(sib);
			sib.setShareToItems(siList);
		}
		return accessibleItems;
	}
	
	private List<UwsShareItem> getMaxAccessibilityItems(UwsShareItemBase sib){
		List<UwsShareItem> items = sib.getShareToItems();
		if(items == null){
				return null;
		}
		Map<String, UwsShareItem> maxAccessibilityItems = new HashMap<String, UwsShareItem>();
		String key;
		UwsShareItem maxAccessibleItem;
		for(UwsShareItem item: items){
			key = item.getShareItemFullId();
			if(maxAccessibilityItems.containsKey(key)){
				maxAccessibleItem = getMaxAccessibleItem(item, maxAccessibilityItems.get(key));
				maxAccessibilityItems.put(key, maxAccessibleItem);
			}else{
				maxAccessibilityItems.put(key, item);
			}
		}
		return new ArrayList<UwsShareItem>(maxAccessibilityItems.values());
	}
	
	@Override
	public List<UwsShareUser> getUsers() throws UwsException {
		return getUsers(null, -1);
	}

	@Override
	public List<UwsShareUser> getUsers(String pattern, int maxResults) throws UwsException {
		if(ldapHandler != null){
			//done in ldapHandler
			//if(pattern.indexOf('*') < 0){
			//	pattern = "*" + pattern + "*";
			//}
			return ldapHandler.getUsers(pattern, maxResults);
		}else{
			return storage.getUsers(pattern, maxResults);
		}
	}

	@Override
	public synchronized boolean addGroup(UwsShareGroup group, UwsJobOwner owner) throws UwsException {
		if(group.getCreator() == null || "".equals(group.getCreator())){
			throw new UwsException("Group crator cannot be null or empty.");
		}
		//No notifications
		eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_CREATED_EVENT);
		return storage.addGroup(group);
	}

	@Override
	public synchronized String createOrUpdateGroup(UwsShareGroup group, UwsJobOwner owner) throws UwsException {
		String ownerid = group.getCreator();
		String groupId = group.getId();
		if(groupId != null && !"".equals(groupId) && !"null".equals(groupId)){
			//updated requested.
			//1. Check the group exists and the user is the owner;
			boolean groupExists = checkGroupBelongsOwner(groupId, ownerid);
			if(!groupExists){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Group '"+groupId+"' not found.");
			}
			//2. Notifications (prepare data)
			Set<String> oldUsers = storage.getUsersFromGroup(groupId);
			Set<String> newUsers = getUsersAsSet(group.getUsers());
			List<UwsShareItemBase> sharedItems = storage.getSharedItemsBase(groupId, false);
			
			//3. Update
			storage.updateGroup(group);
			
			//4. Notifications + events
			createNotifications(owner, newUsers, oldUsers, sharedItems);
			eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_UPDATED_EVENT);
			return "Group '"+groupId+"' successfully updated.";
		} else {
			// group not created yet
			groupId = ownerid + "_" + UwsUtils.getUniqueIdentifier(appid);
			UwsShareGroup newGroup = new UwsShareGroup(groupId, group.getTitle(), group.getDescription(), ownerid);
			newGroup.setUsers(group.getUsers());
			//No notifications required
			storage.addGroup(newGroup);
			eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_CREATED_EVENT);
			return "Group '"+groupId+"' successfully created.";
		}
	}
	
	@Override
	public synchronized String addUserToGroup(String groupId, String userId, UwsJobOwner owner) throws UwsException {
		//1. check group belongs to owner
		boolean groupExists = checkGroupBelongsOwner(groupId, owner.getId());
		if(!groupExists){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Group '"+groupId+"' not found.");
		}
		//2. notifications (prepare data)
		List<UwsShareItemBase> items = storage.getSharedItemsBase(groupId, false);
		Set<String> usersToNotify = new TreeSet<String>();
		usersToNotify.add(userId);

		//3. add user to group
		storage.addUserToGroup(groupId, userId);
		
		//4. Notifications + events
		createNotification("Created shared item", usersToNotify, UwsEventType.SHARE_ITEMS_CREATED_EVENT, items);
		eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_UPDATED_EVENT);
		return "User '"+userId+"' added to group '"+groupId+"'";
	}

	@Override
	public synchronized String createOrUpdateSharedItem(UwsShareItemBase sharedItem, UwsJobOwner owner) throws UwsException {
		String resourceId = sharedItem.getResourceId();
		String ownerid = sharedItem.getOwnerid();
		int resourceType = sharedItem.getResourceType();
		if(resourceId != null && !"".equals(resourceId) && !"null".equals(resourceId)){
			//Updated requested
			//1.check the item belongs the user
			boolean itemExists = checkItemBelongsOwner(resourceId, resourceType, ownerid);
			if(!itemExists){
				throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Item '"+resourceId+"' (type '"+resourceType+"') not found.");
			}
			
			//2. create notification (prepare data)
			UwsShareItemBase oldItem = storage.getSharedItemBase(resourceId, resourceType);
			Set<String> oldUsers = getUsersForNotification(owner, oldItem);
			Set<String> newUsers = getUsersForNotification(owner, sharedItem);
			
			//3. update
			storage.updateSharedItem(sharedItem);

			//4. Notifications + events
			createNotifications(owner, newUsers, oldUsers, sharedItem);
			eventsManager.setEventTime(owner, UwsEventType.SHARE_ITEMS_UPDATED_EVENT);
			return "Resource '"+sharedItem.getResourceId()+"' (type '"+sharedItem.getResourceType()+"') successfully updated.";
		} else {
			// item not created yet.
			resourceId = ownerid + "_" + UwsUtils.getUniqueIdentifier(appid);
			UwsShareItemBase newSharedItem = new UwsShareItemBase(resourceId, resourceType, sharedItem.getTitle(), sharedItem.getDescription(), ownerid);
			newSharedItem.setShareToItems(sharedItem.getShareToItems());
			UwsUtils.updateSharedItemsIfRequired(newSharedItem);
			
			//2. Notifications (prepare data)
			Set<String> usersToNotify = getUsersForNotification(owner, newSharedItem);

			//3. Update
			storage.createSharedItem(newSharedItem);
			
			//4. create event + notifications
			eventsManager.setEventTime(owner, UwsEventType.SHARE_ITEMS_CREATED_EVENT);
			createNotification("Created shared item", usersToNotify, UwsEventType.SHARE_ITEMS_CREATED_EVENT, newSharedItem.getResourceType(), newSharedItem.getTitle());
			//TODO enable cross-event for shared items?
			//createEventsFor(usersToNotify, UwsEventType.SHARE_ITEMS_CREATED_EVENT);
			
			return "Resource '"+resourceId+"' (type '"+sharedItem.getResourceType()+"') successfully created.";
		}
	}

	@Override
	public synchronized String addSharedItemRelation(UwsJobOwner owner, UwsShareItem sharedItem) throws UwsException {
		//Notifications: prepare data
		Set<String> usersToNotify = getUsersForNotification(owner, sharedItem);
		//Update
		storage.addSharedItem(owner.getId(), sharedItem);
		//Notifications
		createNotification("Created shared item", usersToNotify, UwsEventType.SHARE_ITEMS_CREATED_EVENT, sharedItem.getResourceType(), sharedItem.getResourceId());
		//Events
		eventsManager.setEventTime(owner, UwsEventType.SHARE_ITEMS_UPDATED_EVENT);
		return "Resource '"+sharedItem.getResourceId()+"' (type '"+sharedItem.getResourceType()+"') shared to '"+sharedItem.getShareToId()+"'.";
	}

	@Override
	public synchronized String removeGroup(String groupId, UwsJobOwner owner) throws UwsException {
		//1. check the group belongs to the owner
		boolean groupExists = checkGroupBelongsOwner(groupId, owner.getId());
		if(!groupExists){
			return "Group '"+groupId+"' does not exists.";
		}
		//2. Notifications (prepare data)
		Set<String> usersToNotify = storage.getUsersFromGroup(groupId);
		List<UwsShareItemBase> items = storage.getSharedItemsBase(groupId, false);
		
		//3. remove the group, the users that belong to the group and shared items to that group
		storage.removeGroup(groupId);
		
		//4. Events + Notifications
		createNotification("Removed shared item", usersToNotify, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, items);
		eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_REMOVED_EVENT);
		return "Group '"+groupId+"' successfully deleted.";
	}

	@Override
	public synchronized String removeGroupUser(String groupId, String userid, UwsJobOwner owner) throws UwsException {
		//1. check the group belongs to the owner
		boolean groupExists = checkGroupBelongsOwner(groupId, owner.getId());
		if(!groupExists){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Group '"+groupId+"' not found.");
		}

		//2. Notifications (prepare data)
		List<UwsShareItemBase> items = storage.getSharedItemsBase(groupId, false);
		Set<String> usersToNotify = new TreeSet<String>();
		usersToNotify.add(userid);
		
		//3. remove the relation between the user and the group
		storage.removeGroupUser(groupId, userid);
		
		//4. Notifications + events
		createNotification("Removed shared item", usersToNotify, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, items);
		eventsManager.setEventTime(owner, UwsEventType.SHARE_GROUPS_UPDATED_EVENT);
		return "Removed user '"+userid+"' from group '"+groupId+"'";
	}

	@Override
	public synchronized String removeSharedItem(String resourceId, int resourceType, UwsJobOwner owner) throws UwsException {
		String ownerid = owner.getId();
		//1. check the item belongs to the user
		boolean resourceExists = checkItemBelongsOwner(resourceId, resourceType, ownerid);
		if(!resourceExists){
			return "Resource '"+resourceId+"' (type '"+resourceType+"') does not exist!";
		}
		
		//2. create notifications (before removing the item!): prepare data
		UwsShareItemBase sib = storage.getSharedItemBase(resourceId, resourceType);
		Set<String> usersToNotify = getUsersForNotification(owner, sib);
		
		//3. remove the item (from groups and users)
		storage.removeSharedItem(resourceId, resourceType, ownerid);
		
		//4. Notifications + events
		createNotification("Removed shared item", usersToNotify, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, sib.getResourceType(), sib.getTitle());
		eventsManager.setEventTime(owner, UwsEventType.SHARE_ITEMS_REMOVED_EVENT);
		
		//TODO enable cross-event for shared items?
		//createEventsFor(usersToNotify, UwsEventType.SHARE_ITEMS_REMOVED_EVENT);

		return "Removed resource '"+resourceId+"' (type '"+resourceType+"')";
	}
	
	@Override
	public synchronized String removeSharedItemRelation(UwsJobOwner owner, UwsShareItem shareItem) throws UwsException {
		int resourceType = shareItem.getResourceType();
		String resourceId = shareItem.getResourceId();
		String ownerid = owner.getId();
		//1. check the item belongs to the user
		boolean resourceExists = checkItemBelongsOwner(resourceId, resourceType, ownerid);
		if(!resourceExists){
			return "Resource '"+resourceId+"' (type '"+resourceType+"') does not exist!";
		}
		//2. notifications (prepare data)
		Set<String> usersToNotify = getUsersForNotification(owner, shareItem);

		//3. remove the item (from groups and users)
		String shareToId = shareItem.getShareToId();
		UwsShareType shareType = shareItem.getShareType();
		storage.removeSharedItemRelation(resourceId, resourceType, shareToId, shareType, ownerid);
		
		//4. Notifications + events
		createNotification("Created shared item", usersToNotify, UwsEventType.SHARE_ITEMS_CREATED_EVENT, shareItem.getResourceType(), shareItem.getResourceId());
		eventsManager.setEventTime(owner, UwsEventType.SHARE_ITEMS_UPDATED_EVENT);
		return null;
	}
	

	private void createNotifications(UwsJobOwner user, Set<String> newUsers, Set<String> oldUsers, UwsShareItemBase item){
//		List<UwsShareItemBase> items = new ArrayList<UwsShareItemBase>();
//		items.add(item);
//		createNotifications(newUsers, oldUsers, items);
		Set<String> usersForNewSharingNotification = getUsersForNewSharingNotification(user, newUsers, oldUsers);
		Set<String> usersForStopSharingNotification = getUsersForStopSharingNotification(user, newUsers, oldUsers);
		int resourceType = item.getResourceType();
		String title = item.getTitle();
		createNotification("Created shared item", usersForNewSharingNotification, UwsEventType.SHARE_ITEMS_CREATED_EVENT, resourceType, title);
		createNotification("Removed shared item", usersForStopSharingNotification, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, resourceType, title);

	}
	
	private void createNotifications(UwsJobOwner user, Set<String> newUsers, Set<String> oldUsers, List<UwsShareItemBase> items){
		Set<String> usersForNewSharingNotification = getUsersForNewSharingNotification(user, newUsers, oldUsers);
		Set<String> usersForStopSharingNotification = getUsersForStopSharingNotification(user, newUsers, oldUsers);
		createNotification("Created shared item", usersForNewSharingNotification, UwsEventType.SHARE_ITEMS_CREATED_EVENT, items);
		createNotification("Removed shared item", usersForStopSharingNotification, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, items);
		
//		String title;
//		int resourceType;
//		for(UwsShareItemBase item: items){
//			title = item.getTitle();
//			resourceType = item.getResourceType();
//			createNotification("Created shared item", usersForNewSharingNotification, UwsEventType.SHARE_ITEMS_CREATED_EVENT, resourceType, title);
//			createNotification("Removed shared item", usersForStopSharingNotification, UwsEventType.SHARE_ITEMS_REMOVED_EVENT, resourceType, title);
//			
//		}
	}

	private void createNotification(String msg, Set<String> usersToNotify, int typeBase, List<UwsShareItemBase> items) {
		if(items == null){
			return;
		}
		for(UwsShareItemBase item: items){
			createNotification(msg, usersToNotify, typeBase, item.getResourceType(), item.getTitle());
		}
	}
	

	/**
	 * Notifications is a sub-system to inform users about changes. The exceptions generated by it will be ignored.
	 * @param sib resource type and title must be notified. A notification contains a notification id, notification type (=typeBase)
	 * and notification sub-type (=sib resource type).
	 * @param msg
	 */
	private void createNotification(String msg, Set<String> uniqueUsers, int typeBase, int subtype, String title) {
		try{
			if(uniqueUsers == null || uniqueUsers.size() < 1){
				//nobody to notify
				return;
			}
			notificationsManager.createNotification(typeBase, subtype, msg + ": " + title, uniqueUsers);
		}catch(UwsException e){
			//ignore: it is a sub-system.
		}
	}
	
	/**
	 * This method will create event notifications for users that should receive the original event.
	 * NOTE will cause the users must handle the event.
	 * @param usersToNotify
	 * @param eventType
	 * @throws UwsException
	 */
	private void createEventsFor(Set<String> usersToNotify, int eventType) throws UwsException{
		if(usersToNotify == null){
			return;
		}
		UwsJobOwner jobOwnerTmp;
		for(String user: usersToNotify){
			jobOwnerTmp = new UwsJobOwner(user, 0);
			eventsManager.setEventTime(jobOwnerTmp, eventType);
		}
	}
	
	/**
	 * Returns all the users associated to all groups that share the specified item (sib)
	 * @param user
	 * @param sib
	 * @return
	 * @throws UwsException
	 */
	private Set<String> getUsersForNotification(UwsJobOwner user, UwsShareItemBase sib) throws UwsException{
		return getUsersForNotification(user, sib.getShareToItems());
	}
	
	/**
	 * Returns all the users associated to all groups that share the specified item (si)
	 * @param user
	 * @param sib
	 * @return
	 * @throws UwsException
	 */
	private Set<String> getUsersForNotification(UwsJobOwner user, UwsShareItem si) throws UwsException{
		List<UwsShareItem> sharedToItems = new ArrayList<UwsShareItem>();
		sharedToItems.add(si);
		return getUsersForNotification(user, sharedToItems);
	}

	/**
	 * Returns all the users associated to all groups that share the specified list (the list can contains groups or single users)
	 * @param user
	 * @param sharedToItems
	 * @return
	 * @throws UwsException
	 */
	private Set<String> getUsersForNotification(UwsJobOwner user, List<UwsShareItem> sharedToItems) throws UwsException{
		Set<String> uniqueGroups = UwsUtils.getUniqueGroupsFromSharedToItems(sharedToItems);
		Set<String> groupsOwners = storage.getGroupsOwners(uniqueGroups);
		Set<String> uniqueUsers = storage.getUniqueUsersFromGroups(uniqueGroups);
		Set<String> usersFromSharedTo = UwsUtils.getUniqueUsersFromSharedToItems(sharedToItems);
		
		if(usersFromSharedTo != null){
			for(String userid: usersFromSharedTo){
				if(!uniqueUsers.contains(userid)){
					uniqueUsers.add(userid);
				}
			}
		}
		
		if(groupsOwners != null){
			for(String userid: groupsOwners){
				if(!uniqueUsers.contains(userid)){
					uniqueUsers.add(userid);
				}
			}
		}
		
		//remove own user (not required to receive notifications)
		if(uniqueUsers != null){
			uniqueUsers.remove(user.getId());
		}
		
		return uniqueUsers;
	}

	
	/**
	 * Returns the old users that are not in new users list (so they must receive a stop sharing notification)
	 * @param newUsers
	 * @param oldUsers
	 * @return
	 */
	private Set<String> getUsersForStopSharingNotification(UwsJobOwner user, Set<String> newUsers, Set<String> oldUsers){
		if(oldUsers == null){
			//no old users: not required stop sharing notifications
			return null;
		}
		if(newUsers == null){
			//all old users must receive a stop sharing notification (new users list is empty)
			return oldUsers;
		}
		Set<String> users = new TreeSet<String>();
		for(String u: oldUsers){
			//if old user is not in new users => stop sharing
			if(!newUsers.contains(u)){
				users.add(u);
			}
		}
		//remove own users for notifications
		users.remove(user.getId());
		return users;
	}

	/**
	 * Returns the new users that are not in old users list (so they must receive a new sharing notification)
	 * @param newUsers
	 * @param oldUsers
	 * @return
	 */
	private Set<String> getUsersForNewSharingNotification(UwsJobOwner user, Set<String> newUsers, Set<String> oldUsers){
		if(newUsers == null){
			//no new users => no new sharing notifications required.
			return null;
		}
		if(oldUsers == null){
			//all new users require new sharing notification
			return newUsers;
		}
		Set<String> users = new TreeSet<String>();
		for(String u: newUsers){
			//if new user is not in old users => requires new sharing notification
			if(!oldUsers.contains(u)){
				users.add(u);
			}
		}
		//remove own users for notifications
		users.remove(user.getId());
		return users;
	}

	/**
	 * Returns 'false' if the resource does not exists. Raises an exception if the resource belongs to a different user.
	 * @param resourceId
	 * @param resourceType
	 * @param ownerid
	 * @return
	 * @throws UwsException
	 */
	private boolean checkItemBelongsOwner(String resourceId, int resourceType, String ownerid) throws UwsException {
		String realOwner = storage.getSharedItemOwner(resourceId, resourceType);
		if(realOwner == null){
			return false;
			//throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Resource '"+resourceId+"' (type '"+resourceType+"') not found.");
		}
		if(!realOwner.equals(ownerid)){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Resource '"+resourceId+"' (type '"+resourceType+"') does not belong to user '"+ownerid+"'.");
		}
		return true;
	}

	/**
	 * Returns 'false' if the groups does not exist. Raises an exception if the group belongs to a different user.
	 * @param groupId
	 * @param ownerid
	 * @return
	 * @throws UwsException
	 */
	private boolean checkGroupBelongsOwner(String groupId, String ownerid) throws UwsException{
		UwsShareGroup group = storage.getGroup(groupId, false);
		if(group == null){
			//throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Group '"+groupId+"' not found.");
			return false;
		}
		if(!group.getCreator().equals(ownerid)){
			throw new UwsException(UwsOutputResponseHandler.FORBIDDEN, "The user '"+ownerid+"' is not the creator of the group '"+groupId+"'");
		}
		return true;
	}
	
	/**
	 * This method does not check that both items contain the same id, type and owner.<br/>
	 * This method only returns the max accessibility mode: the item with 'W' is returned if one of the
	 * items has 'W' access mode. If both items have the same access mode, the returned object is unspecified.
	 * @param itemA
	 * @param itemB
	 * @return
	 */
	private UwsShareItem getMaxAccessibleItem(UwsShareItem itemA, UwsShareItem itemB){
		switch (itemA.getShareMode()) {
			case Write:
				return itemA;
			case Read:
				return itemB;
			default:
				return itemA;
		}
	}
	
	/**
	 * Returns a new list containing all the groups ownered by the user and the groups the user belongs to.
	 * @param userid
	 * @param groupsTheUserBelongsTo
	 * @param groupsOwneredByUser
	 * @return
	 */
	private List<UwsShareGroup> getUnifiedGroupList(String userid, List<UwsShareGroup> groupsTheUserBelongsTo, List<UwsShareGroup> groupsOwneredByUser) {
		List<UwsShareGroup> groups = new ArrayList<UwsShareGroup>();
		groups.addAll(groupsOwneredByUser);
		for(UwsShareGroup g: groupsTheUserBelongsTo){
			if(g.getCreator().equals(userid)){
				//the group is already added by the list 'groupsOwneredByUser'
				continue;
			} else {
				groups.add(g);
			}
		}
		return groups;
	}
	
	@Override
	public UwsShareUser getSharedUser(String userid) throws UwsException {
		if(ldapHandler != null){
			UwsShareUser user = ldapHandler.getUserDetails(userid);
			return user;
		}
		return null;
	}

	private Set<String> getUsersAsSet(List<UwsShareUser> users){
		if(users == null){
			return null;
		}
		Set<String> usersSet = new TreeSet<String>();
		for(UwsShareUser u: users){
			usersSet.add(u.getId());
		}
		return usersSet;
	}


	@Override
	public String toString(){
		return "ShareManager for application " + appid + "\nUsing connection: " + storage;
	}

	@Override
	public UwsShareItemBase getUserSharedItem(String resourceid, int resourceType) throws UwsException {
		return storage.getSharedItemBase(resourceid, resourceType);
	}

	@Override
	public boolean hasAccess(String userid, String resourceid, int resourceType) throws UwsException {
		if(userid == null){
			return false;
		}
		return storage.hasAccessToShareItem(userid, resourceid, resourceType);
	}

}
