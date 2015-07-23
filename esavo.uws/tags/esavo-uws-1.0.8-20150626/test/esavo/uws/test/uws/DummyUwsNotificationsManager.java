package esavo.uws.test.uws;

import java.util.List;
import java.util.Set;

import esavo.uws.UwsException;
import esavo.uws.notifications.UwsNotificationItem;
import esavo.uws.notifications.UwsNotificationsManager;
import esavo.uws.owner.UwsJobOwner;

public class DummyUwsNotificationsManager implements UwsNotificationsManager {

	@Override
	public UwsNotificationItem createNotification(int type, int subtype, String msg, Set<String> users) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UwsNotificationItem> getNotificationsForUser(UwsJobOwner user) throws UwsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void markNotificationAsRead(UwsJobOwner user, List<String> notificationid) throws UwsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String checkNotificationsRemovalProcedure(long deltaDestructionTime) {
		// TODO Auto-generated method stub
		return null;
	}

}
