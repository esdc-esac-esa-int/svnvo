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
