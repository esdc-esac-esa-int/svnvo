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
package esavo.tap.test.uws;

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
	public boolean addGroup(UwsShareGroup arg0, UwsJobOwner arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String addSharedItemRelation(UwsJobOwner arg0, UwsShareItem arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addUserToGroup(String arg0, String arg1, UwsJobOwner arg2)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createOrUpdateGroup(UwsShareGroup arg0, UwsJobOwner arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createOrUpdateSharedItem(UwsShareItemBase arg0,
			UwsJobOwner arg1) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String arg0)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getAccessibleSharedItems(String arg0,
			int arg1, UwsShareType arg2, UwsShareMode arg3) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareGroup> getGroupsByOwner(String arg0, boolean arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareGroup> getGroupsTheUserBelongsTo(String arg0)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(
			String arg0, int arg1) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getMaxAccessibilityAccessibleSharedItems(
			String arg0, int arg1, UwsShareType arg2, UwsShareMode arg3)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItem> getUserSharedItem(String arg0, String arg1,
			int arg2) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getUserSharedItems(String arg0, boolean arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getUserSharedItems(String arg0, int arg1,
			boolean arg2) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareUser> getUsers() throws UwsException {
		return getUsers(null, -1);
	}
	
	@Override
	public List<UwsShareUser> getUsers(String pattern, int maxRecords) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public String removeGroup(String arg0, UwsJobOwner arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeGroupUser(String arg0, String arg1, UwsJobOwner arg2)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeSharedItem(String arg0, int arg1, UwsJobOwner arg2)
			throws UwsException {
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
	public void updateUsers(List<UwsShareUser> users) throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UwsShareUser getSharedUser(String userid) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeSharedItemRelation(UwsJobOwner arg0, UwsShareItem arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsShareItemBase> getGroupItems(String arg0)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UwsShareItemBase getUserSharedItem(String arg0, int arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAccess(String arg0, String arg1, int arg2)
			throws UwsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<UwsShareItemBase> getGroupItems(String arg0, String arg1)
			throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

}
