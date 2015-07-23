package esavo.uws.test.database;

import java.sql.SQLException;

public interface DummyDatabaseDataAccessor {
	
	public DummyData getDataForQuery(String query) throws SQLException;

}
