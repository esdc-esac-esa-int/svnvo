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
package esavo.uws.utils.test.uws;

import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.share.UwsShareItem;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;
import esavo.uws.share.UwsShareMode;
import esavo.uws.share.UwsShareType;
import esavo.uws.share.UwsShareUser;

public class DummyUwsShareManager implements UwsShareManager {


	@Override
	public List<UwsShareGroup> getGroupsByOwner(String ownerid,
			boolean includeUsers) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addGroup(UwsShareGroup group, UwsJobOwner owner)
			throws UwsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<UwsShareItemBase> getUserSharedItems(String userid,
			boolean includeShareTo) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getUserSharedItems(String userid,
			int resourceType, boolean includeShareTo) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItem> getUserSharedItem(String userid,
			String resourceid, int resourceType) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String userid,
			int resourceType, UwsShareType shareType, UwsShareMode shareMode)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(
			String userid, int resourceType) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(
			String userid, int resourceType, UwsShareType shareType,
			UwsShareMode shareMode) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createOrUpdateGroup(UwsShareGroup group, UwsJobOwner owner)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addUserToGroup(String groupId, String userId,
			UwsJobOwner owner) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createOrUpdateSharedItem(UwsShareItemBase sharedItem,
			UwsJobOwner owner) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addSharedItemRelation(UwsJobOwner owner,
			UwsShareItem sharedItem) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeGroup(String groupId, UwsJobOwner owner)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeGroupUser(String groupId, String userId,
			UwsJobOwner owner) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeSharedItem(String resourceId, int resourceType,
			UwsJobOwner owner) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeSharedItemRelation(UwsJobOwner owner, UwsShareItem shareItem)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareUser> getUsers() throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareGroup> getGroups(String userid, boolean includeUsers)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareUser> getUsers(String pattern, int limit) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateUsers(List<UwsShareUser> users) throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UwsShareUser getSharedUser(String userid) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getGroupItems(String groupid)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<UwsShareItemBase> getGroupItems(String groupid,
			String itemTitlePattern) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UwsShareItemBase getUserSharedItem(String resourceId,
			int resourceType) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAccess(String userid, String resourceid, int resourceType)
			throws UwsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<UwsShareGroup> getGroupUsers(List<String> groupid)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String userid,
			boolean includeUsers) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateGroupUsers(List<UwsShareGroup> groups)
			throws UwsException {
		// TODO Auto-generated method stub
		
	}

}
