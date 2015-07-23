package esavo.tap.test.database;

import java.sql.SQLException;

public interface DummyDatabaseDataAccessor {
	
	public DummyData getDataForQuery(String query) throws SQLException;

}
