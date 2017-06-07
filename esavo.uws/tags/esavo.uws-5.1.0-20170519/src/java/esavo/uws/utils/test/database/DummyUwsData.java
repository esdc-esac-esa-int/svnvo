package esavo.uws.utils.test.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Container to support database data and metadata.
 * 
 * <p>Usually you need to set 'data' and 'columnNames' for SELECTs queries (unless you use ResultSetMetadata to perform more analysis)
 * <pre><tt>
 *   DummyData d = new DummyData();
 *   List<List<String>> rows = new ArrayList<List<String>>();
 *   List<String>row = new ArrayList<String>();
 *   row.append("col1_data");
 *   row.append("col2_data");
 *   rows.append(row);
 *   d.setData(rows);
 *   List<String> columnNames = new ArrayList<String>(Arrays.asList(new String[]{"col1_name","col2_name"}));
 *   d.setColumnNames(columnNames);
 * </tt></pre>
 * </p>
 * 
 * <p>You need to set 'number of affected rows' for non SELECTs queries (unless you use ResultSetMetadata to perform more analysis)
 * <pre><tt>
 *   DummyData d = new DummyData();
 *   d.setUpdateAffectedRows(1);
 * </tt></pre>
 * </p>
 * 
 * <p>Usually you must associate the data to the query using a dummy database connection:
 * <pre><tt>
 *   DummyUWSDatabaseConnection connection = new DummyUWSDatabaseConnection();
 *   DummyData d = new DummyData();
 *   d.setData(...);
 *   d.setColumnNames(...);
 *   String query = "SELECT ...";
 *   connection.setDataForQuery(query,d);
 * </tt></pre>
 * 
 * </p>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class DummyUwsData {
	
	private String catalogName;
	private String tableName;
	private String schemaName;
	private List<Integer> databaseTypes;
	private List<String> javaTypes;
	private List<String> columnNames;
	private List<String> columnNamesWithoutSchemas;
	private List<List<String>> data;
	private int updateAffectedRows;
	
	public DummyUwsData(){
		
	}


	/**
	 * @return the catalogName
	 */
	public String getCatalogName() {
		return catalogName;
	}


	/**
	 * @param catalogName the catalogName to set
	 */
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}


	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}


	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}


	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}


	/**
	 * @return the databaseTypes
	 */
	public List<Integer> getDatabaseTypes() {
		return databaseTypes;
	}


	/**
	 * @param databaseTypes the databaseTypes to set
	 */
	public void setDatabaseTypes(List<Integer> databaseTypes) {
		this.databaseTypes = databaseTypes;
	}


	/**
	 * @return the javaTypes
	 */
	public List<String> getJavaTypes() {
		return javaTypes;
	}


	/**
	 * @param javaTypes the javaTypes to set
	 */
	public void setJavaTypes(List<String> javaTypes) {
		this.javaTypes = javaTypes;
	}


	/**
	 * @return the columnNames
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}


	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
		this.columnNamesWithoutSchemas = createColumnNamesWithoutSchemas(columnNames);
	}

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(String[] columnNames) {
		if(columnNames == null){
			setColumnNames((List<String>)null);
		}else{
			List<String> l = new ArrayList<String>(Arrays.asList(columnNames));
			setColumnNames(l);
		}
	}

	/**
	 * @return the data
	 */
	public List<List<String>> getData() {
		return data;
	}


	/**
	 * @param data the data to set
	 */
	public void setData(List<List<String>> data) {
		this.data = data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(String[][] data) {
		if(data == null){
			this.data = null;
			return;
		}else{
			this.data = new ArrayList<List<String>>();
			for(String[] d: data){
				List<String> l = new ArrayList<String>(Arrays.asList(d));
				this.data.add(l);
			}
		}
	}

	/**
	 * @return the updateAffectedRows
	 */
	public int getUpdateAffectedRows() {
		return updateAffectedRows;
	}


	/**
	 * @param updateAffectedRows the updateAffectedRows to set
	 */
	public void setUpdateAffectedRows(int updateAffectedRows) {
		this.updateAffectedRows = updateAffectedRows;
	}
	
	
	private List<String> createColumnNamesWithoutSchemas(List<String> columnNames){
		List<String> c = new ArrayList<String>();
		int pos;
		for(String cItem: columnNames){
			pos = cItem.indexOf('.');
			if(pos >= 0){
				c.add(cItem.substring(pos+1));
			} else {
				c.add(cItem);
			}
		}
		return c;
	}

	/**
	 * Returns the column index associated to the provided columnName.
	 * If columnName contains a schema name, columnName object is used.
	 * If columnName does not contain a schema name, columnNameWithoutSchemaNames is used.
	 * @param columnName
	 * @return
	 */
	public int findColumnIndex(String columnName){
		if(columnName.indexOf('.') >= 0){
			return columnNames.indexOf(columnName);
		}else{
			return columnNamesWithoutSchemas.indexOf(columnName);
		}
	}
	

}
