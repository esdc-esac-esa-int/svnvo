package esavo.uws.utils.test.database;

import java.sql.SQLException;

public interface DummyUwsDatabaseDataAccessor {
	
	public DummyUwsData getDataForQuery(String query) throws SQLException;

}
