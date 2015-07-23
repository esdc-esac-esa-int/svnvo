package esavo.uws.test.database;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DummyPreparedStatement extends DummyStatement implements PreparedStatement {
	
	private String query;
	private List<String> values;
	private List<Integer> positions;
	
	public DummyPreparedStatement(String query, DummyDatabaseDataAccessor dataAccessor, Connection connection){
		super(dataAccessor, connection);
		this.query = query;
		this.values = new ArrayList<String>();
		this.positions = new ArrayList<Integer>();
		locateValues();
	}
	
	private void locateValues(){
		int p = 0;
		int length = query.length();
		while((p = query.indexOf('?', p)) != -1){
			values.add("");
			positions.add(p);
			p++;
			if(p >= length){
				break;
			}
		}
	}
	
	private String getParsedQuery(){
		int beginIndex = 0;
		int endIndex;
		StringBuilder sb = new StringBuilder();
		int p;
		for(int index = 0; index < positions.size(); index++){
			p = positions.get(index);
			endIndex = p;
			sb.append(query.substring(beginIndex, endIndex));
			beginIndex = p+1;
			sb.append(values.get(index));
		}
		if(beginIndex < query.length()){
			sb.append(query.substring(beginIndex));
		}
		return sb.toString();
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		return super.executeQuery(getParsedQuery());
	}

	@Override
	public int executeUpdate() throws SQLException {
		String parsedQuery = getParsedQuery();
		return super.executeUpdate(parsedQuery);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		values.set(parameterIndex-1, "NULL");
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		values.set(parameterIndex-1, ""+x);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		values.set(parameterIndex-1, "'"+x+"'");
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearParameters() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		if (x == null) {
			setObject(parameterIndex, null);
		} else {
			switch (targetSqlType) {
			case java.sql.Types.BOOLEAN:
				setBoolean(parameterIndex, Boolean.parseBoolean(x.toString()));
				break;
			case java.sql.Types.SMALLINT:
				setShort(parameterIndex, Short.parseShort(x.toString()));
				break;
			case java.sql.Types.INTEGER:
				setInt(parameterIndex, Integer.parseInt(x.toString()));
				break;
			case java.sql.Types.BIGINT:
				setLong(parameterIndex, Long.parseLong(x.toString()));
				break;
			case java.sql.Types.FLOAT:
				setFloat(parameterIndex, Float.parseFloat(x.toString()));
				break;
			case java.sql.Types.DOUBLE:
				setDouble(parameterIndex, Double.parseDouble(x.toString()));
				break;
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			default:
				setObject(parameterIndex, x);
			}
		}
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		setString(parameterIndex, x == null ? "":x.toString());
	}

	@Override
	public boolean execute() throws SQLException {
		String parsedQuery = getParsedQuery();
		return super.execute(parsedQuery);
	}

	@Override
	public void addBatch() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		setObject(parameterIndex, null);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNString(int parameterIndex, String value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		setObject(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
