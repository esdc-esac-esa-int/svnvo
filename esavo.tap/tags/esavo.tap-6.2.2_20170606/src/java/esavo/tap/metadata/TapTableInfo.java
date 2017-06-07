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
package esavo.tap.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TapTableInfo {
	private String schemaName;
	private String tableName;
	private Map<String, Map<String, Object>> data;
	private Map<String, Class<?>> dataType;
	
	public TapTableInfo(String schemaName, String tableName){
		this.schemaName = schemaName;
		this.tableName = tableName;
		data = new HashMap<String, Map<String,Object>>();
		dataType = new HashMap<String, Class<?>>();
	}
	
	public String getSchemaName(){
		return schemaName;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public Set<String> getTableColumnNames(){
		return data.keySet();
	}
	
	public void addColumnDataType(String columnName, Class<?> dataType){
		this.dataType.put(columnName, dataType);
	}
	
	public void putColumn(String tableColumnName, String column, Object data){
		Map<String, Object> d = this.data.get(tableColumnName);
		if(d == null){
			d = new HashMap<String, Object>();
			this.data.put(tableColumnName, d);
		}
		d.put(column, data);
	}
	
	public Object getColumn(String tableColumnName, String columnName){
		Map<String, Object> d = data.get(tableColumnName);
		if(d == null){
			return null;
		}
		return d.get(columnName);
	}
	
	public Class<?> getColumnDataType(String columnName){
		return dataType.get(columnName);
	}
	
	public int getNumTableColumnNames(){
		return data.size();
	}
	
	public boolean getBoolean(String tableColumnName, String columnName){
		return (Boolean)getColumn(tableColumnName, columnName);
	}
	
	public int getInteger(String tableColumnName, String columnName){
		return (Integer)getColumn(tableColumnName, columnName);
	}
	
	public String getString(String tableColumnName, String columnName){
		return (String)getColumn(tableColumnName, columnName);
	}
	
	public boolean isString(String columnName){
		Class<?> c = getColumnDataType(columnName);
		if(c == null){
			return false;
		}else{
			return c.getName().equals(Boolean.class.getName());
		}
	}
	
	public boolean isInt(String columnName){
		Class<?> c = getColumnDataType(columnName);
		if(c == null){
			return false;
		}else{
			return c.getName().equals(Integer.class.getName());
		}
	}
	
	public static TapTableInfo createDefaultTapTableInfo(String schemaName, String tableName){
		TapTableInfo t = new TapTableInfo(schemaName, tableName);
		t.addColumnDataType("description", String.class);
		t.addColumnDataType("ucd",         String.class);
		t.addColumnDataType("utype",       String.class);
		t.addColumnDataType("datatype",    String.class);
		t.addColumnDataType("unit",        String.class);
		t.addColumnDataType("size",        Integer.class);
		t.addColumnDataType("principal",   Integer.class);
		t.addColumnDataType("std",         Integer.class);
		t.addColumnDataType("indexed",     Integer.class);
		t.addColumnDataType("flags",       Integer.class);
		return t;
	}
	

}
