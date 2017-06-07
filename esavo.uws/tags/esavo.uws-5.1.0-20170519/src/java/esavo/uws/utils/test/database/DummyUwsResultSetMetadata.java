package esavo.uws.utils.test.database;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DummyUwsResultSetMetadata implements ResultSetMetaData {
	
	private DummyUwsData data;
	
	public DummyUwsResultSetMetadata(DummyUwsData data){
		this.data = data;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return data.getColumnNames().size();
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int isNullable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		return data.getColumnNames().get(column-1);
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return data.getColumnNames().get(column-1);
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		return data.getSchemaName();
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return data.getTableName();
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		return data.getCatalogName();
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		return data.getDatabaseTypes().get(column-1);
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return "" + getColumnType(column);
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return true;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return data.getJavaTypes().get(column-1);
	}

}
