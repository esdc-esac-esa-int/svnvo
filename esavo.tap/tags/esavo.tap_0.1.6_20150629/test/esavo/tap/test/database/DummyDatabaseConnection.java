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
package esavo.tap.test.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Dummy database connection.<br/>
 * In order to generate exceptions, use:<br/>
 * <ul>
 * <li>For queries: {@link #enableGenerateExceptionRequested(String)} and {@link #enableGenerateExceptionRequested(String, int)}</li>
 * <li>For non queries methods (e.g. startTransac), use {@link #setGenerateNonQueryException(boolean)}</li>
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class DummyDatabaseConnection implements Connection, DummyDatabaseDataAccessor {
	
	private Map<String, DummyData> queriesAndResults;
	private Map<String, Class<?>> typeMap;
	private Map<String, Integer> requestedExceptions;
	private List<String> executedQueries;
	private String catalog;
	private String schemaName;
	private int holdability;
	private Savepoint savePoint;
	//private boolean generateNonQueryException;
	private int generateNonQueryExceptionCounter;
	private boolean autocommit;
	private boolean commitCalled;
	private boolean closed;
	
	public DummyDatabaseConnection() {
		queriesAndResults = new HashMap<String, DummyData>();
		requestedExceptions = new HashMap<String, Integer>();
		executedQueries = new ArrayList<String>();
		//generateNonQueryException = false;
		generateNonQueryExceptionCounter = -1;
		autocommit = true;
		commitCalled = false;
		closed = false;
	}
	
	public void setDataForQuery(String query, DummyData data){
		queriesAndResults.put(query, data);
	}
	
	public void removeDataForQuery(String query){
		queriesAndResults.remove(query);
	}
	
	@Override
	public DummyData getDataForQuery(String query) throws SQLException {
		if(isGenerateExceptionRequested(query)){
			throw new SQLException("Exception requested for query: " + query);
		}
		DummyData d = queriesAndResults.get(query);
		if(d == null){
			//throw new RuntimeException("The following query is not found in dummy database: " + query + "\nYou must add the query and its results to this dummy connection");
			System.out.println("Test query not found: " + query);
			return null;
		}
		executedQueries.add(query);
		return d;
		//return queriesAndResults.get(query);
	}
	
	private boolean isGenerateExceptionRequested(String query){
		Integer i = requestedExceptions.remove(query);
		if(i == null){
			return false;
		}
		int counter = i - 1;
		if(counter <= 0){
			return true;
		}
		//Not yet
		requestedExceptions.put(query, counter);
		return false;
	}

	/**
	 * Returns 'true' when the counter is 0.
	 * If counter >= 0, counter is decremented.
	 * If counter < 0, returns 'false'
	 * @return
	 */
	private boolean isGenerateNonQueryExceptionRequested(){
		if(generateNonQueryExceptionCounter < 0){
			return false;
		}
		generateNonQueryExceptionCounter--;
		if (generateNonQueryExceptionCounter == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Enables exception mechanism to raise an exception the next time the specified query is requested.<br/>
	 * If you want to enable exceptions when executing non query methods (e.g. startTransac), use {@link #setGenerateNonQueryException(boolean)}
	 * @param query query to check.
	 */
	public void enableGenerateExceptionRequested(String query){
		enableGenerateExceptionRequested(query, 1);
	}
	
	/**
	 * Enables exception mechanism to raise an exception when the query is requested 'timesRequested' times.<br/>
	 * If you want to enable exceptions when executing non query methods (e.g. startTransac), use {@link #setGenerateNonQueryException(boolean)}
	 * @param query query to check
	 * @param timesRequested the amount of requests to be accepted before the exception is raised (i.e. if 'timesRequested' is '2',
	 * after the second call to the request, an exception will be raised). If 'timesRequested' is lower than 1, the exception
	 * mechanism is disabled for this query.
	 */
	public void enableGenerateExceptionRequested(String query, int timesRequested){
		if (timesRequested < 1) {
			requestedExceptions.remove(query);
		} else {
			requestedExceptions.put(query, timesRequested);
		}
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return false;
	}

	@Override
	public Statement createStatement() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyStatement(this, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		this.autocommit = autoCommit;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return this.autocommit;
	}

	@Override
	public void commit() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		commitCalled = true;
	}

	@Override
	public void rollback() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
	}

	@Override
	public void close() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		closed = true;
	}

	@Override
	public boolean isClosed() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return closed;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return false;
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		this.catalog = catalog;
	}

	@Override
	public String getCatalog() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return catalog;
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyStatement(this, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return typeMap;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		this.typeMap = map;
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		this.holdability = holdability;
	}

	@Override
	public int getHoldability() throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return holdability;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyStatement(this, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return new DummyPreparedStatement(sql, this, this);
	}

	@Override
	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return false;
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return null;
	}

	public void setSchema(String schema) throws SQLException {
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		this.schemaName = schema;
	}

	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return schemaName;
	}

	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		
	}

	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		if(isGenerateNonQueryExceptionRequested()){
			throw new SQLException("Exception requested");
		}
		return 0;
	}
	
	/**
	 * This method enables exception when executing non query mehtods (e.g. startTransac).<br/>
	 * In order to generate sql query exceptions, use {@link #enableGenerateExceptionRequested(String)} or
	 * {@link #enableGenerateExceptionRequested(String, int)}
	 * @param generate
	 */
	public void setGenerateNonQueryException(){
		setGenerateNonQueryException(1);
	}
	
	/**
	 * If counter > 1, it is enabled (until the counter is 0, then it is disabled). So, if counter is 3, 3 exceptions are raised.
	 * If counter = 0, it is disabled.
	 * If counter < 0, it is always enabled.
	 * @param counter
	 */
	public void setGenerateNonQueryException(int timesRequested){
		this.generateNonQueryExceptionCounter = timesRequested;
	}
	
	public void setCommitCalled(boolean commitCalled){
		this.commitCalled = commitCalled;
	}
	
	public boolean getCommitCalled(){
		return commitCalled;
	}
	
	public void setClosed(boolean closed){
		this.closed = closed;
	}
	
	public void clearExecutedQueries(){
		this.executedQueries.clear();
	}
	
	public void clearExecutedQuery(String query){
		this.executedQueries.remove(query);
	}
	
	public boolean isExecutedQuery(String query){
		return this.executedQueries.contains(query);
	}

	/**
	 * @return the savePoint
	 */
	public Savepoint getSavePoint() {
		return savePoint;
	}
	
}
