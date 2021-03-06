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

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import esavo.adql.db.DBColumn;
import esavo.adql.db.DBTable;
import esavo.uws.share.UwsShareItem;

public class TAPTable implements DBTable {
	
	public static final boolean PUBLIC_TABLE = true;
	public static final boolean PRIVATE_TABLE = false;

	private final String adqlName;

	private String dbName = null;

	private TAPSchema schema = null;

	private String type = "table";

	private String description = null;

	private String utype = null;
	
	private Long size = null;
	
	private String hierarchy = null;

	protected final Map<String, TAPColumn> columns;

	protected final ArrayList<TAPForeignKey> foreignKeys;

	protected Object otherData = null;
	
	private int flags = 0;
	
	private List<UwsShareItem> shareInfo;
	
	private boolean isPublic;


	public TAPTable(String tableName, boolean isPublic){
		if (tableName == null || tableName.trim().length() == 0){
			throw new NullPointerException("Missing table name !");
		}
		int indPrefix = tableName.lastIndexOf('.');
		adqlName = (indPrefix >= 0)?tableName.substring(indPrefix+1).trim():tableName.trim();
		dbName = adqlName;
		columns = new LinkedHashMap<String, TAPColumn>();
		foreignKeys = new ArrayList<TAPForeignKey>();
		this.isPublic = isPublic;
	}

	public TAPTable(String tableName, String tableType, boolean isPublic){
		this(tableName, isPublic);
		type = tableType;
	}

	public TAPTable(String tableName, String tableType, String description, String utype, boolean isPublic){
		this(tableName, tableType, isPublic);
		this.description = description;
		this.utype = utype;
	}

	public final String getFullName() {
		if (schema != null) {
			return schema.getName() + "." + adqlName;
		} else {
			return adqlName;
		}
	}

	/**
	 * @return The name.
	 */
	public final String getName() {
		return getADQLName();
	}

	@Override
	public final String getADQLName() {
		return adqlName;
	}

	@Override
	public final String getDBName() {
		return dbName;
	}

	public final void setDBName(String name){
		name = (name != null)?name.trim():name;
		dbName = (name == null || name.length() == 0)?adqlName:name;
	}

	@Override
	public String getADQLCatalogName() {
		return null;
	}

	@Override
	public String getDBCatalogName() {
		return null;
	}

	@Override
	public final String getADQLSchemaName() {
		return schema.getADQLName();
	}

	@Override
	public final String getDBSchemaName() {
		return schema.getDBName();
	}

	/**
	 * @return The schema.
	 */
	public final TAPSchema getSchema() {
		return schema;
	}

	/**
	 * @param schema The schema to set.
	 */
	 final void setSchema(TAPSchema schema) {
		this.schema = schema;
	}

	/**
	 * @return The type.
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public final void setType(String type) {
		this.type = type;
	}

	/**
	 * @return The description.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	
	
	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return The utype.
	 */
	public final String getUtype() {
		return utype;
	}

	/**
	 * @param utype The utype to set.
	 */
	public final void setUtype(String utype) {
		this.utype = utype;
	}

	
	
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Object getOtherData(){
		return otherData ;
	}

	public void setOtherData(Object data){
		otherData = data;
	}
	
	public int getFlags(){
		return flags;
	}
	
	public void setFlags(int flags){
		this.flags = flags;
	}

	public final void addColumn(TAPColumn newColumn){
		if (newColumn != null && newColumn.getName() != null){
			columns.put(newColumn.getName(), newColumn);
			newColumn.setTable(this);
		}
	}

	public final TAPColumn addColumn(String columnName){
		if (columnName == null){
			return null;
		}

		TAPColumn c = new TAPColumn(columnName);
		addColumn(c);
		return c;
	}

	public TAPColumn addColumn(String columnName, String description, String unit, String ucd, String utype){
		if (columnName == null){
			return null;
		}

		TAPColumn c = new TAPColumn(columnName, description, unit, ucd, utype);
		addColumn(c);
		return c;
	}

	public TAPColumn addColumn(String columnName, String description, String unit, String ucd, String utype, String datatype, int size, boolean principal, boolean indexed, boolean std, int flags){
		if (columnName == null){
			return null;
		}

		TAPColumn c = new TAPColumn(columnName, description, unit, ucd, utype);
		c.setDatatype(datatype, size);
		c.setPrincipal(principal);
		c.setIndexed(indexed);
		c.setStd(std);
		c.setFlags(flags);
		addColumn(c);
		return c;
	}

	public TAPColumn addColumn(String columnName, String description, String unit, String ucd, String utype, VotType votType, boolean principal, boolean indexed, boolean std, int flags, int pos){
		if (columnName == null){
			return null;
		}

		TAPColumn c = new TAPColumn(columnName, description, unit, ucd, utype);
		c.setVotType(votType);
		c.setPrincipal(principal);
		c.setIndexed(indexed);
		c.setStd(std);
		c.setFlags(flags);
		c.setPos(pos);
		//System.out.println(c + ", votType: " + c.getVotType());
		addColumn(c);
		return c;
	}

	public final boolean hasColumn(String name){
		if (name == null){
			return false;
		} else {
			String columnName = name.toLowerCase();
			return columns.containsKey(columnName);
		}
	}

	public List<TAPColumn> getColumnsList(){
		return new ArrayList<TAPColumn>(columns.values());
	}
	
	public Iterator<TAPColumn> getColumns(){
		return columns.values().iterator();
	}

	@Override
	public DBColumn getColumn(String name, boolean byAdqlName) {
		if (byAdqlName)
			return getColumn(name);
		else {
			if (name != null && name.length() > 0) {
				String colName = name.toLowerCase();
				Collection<TAPColumn> collColumns = columns.values();
				for (TAPColumn column : collColumns) {
					if (column.getDBName().equalsIgnoreCase(colName)) {
						return column;
					}
				}
			}
			return null;
		}
	}

	public final TAPColumn getColumn(String name){
		if (name == null){
			return null;
		} else {
			String columnName = name.toLowerCase();
			return columns.get(columnName);
		}
	}

	public boolean hasColumn(String colName, boolean byAdqlName) {
		return (getColumn(colName, byAdqlName) != null);
	}

	public final int getNbColumns(){
		return columns.size();
	}

	public final boolean isEmpty(){
		return columns.isEmpty();
	}

	public final TAPColumn removeColumn(String name){
		if (name == null){
			return null;
		}
		
		String columnName = name.toLowerCase();

		TAPColumn removedColumn = columns.remove(columnName);
		if (removedColumn != null) {
			deleteColumnRelations(removedColumn);
		}
		return removedColumn;
	}

	protected final void deleteColumnRelations(TAPColumn col){
		// Remove the relation between the column and this table:
		col.setTable(null);

		// Remove the relations between the column and other tables/columns:
		Iterator<TAPForeignKey> it = col.getTargets();
		while(it.hasNext()) {
			removeForeignKey(it.next());
		}

		it = col.getSources();
		while(it.hasNext()){
			TAPForeignKey key = it.next();
			key.getFromTable().removeForeignKey(key);
		}
	}

	public final void removeAllColumns(){
		Iterator<Map.Entry<String, TAPColumn>> it = columns.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, TAPColumn> entry = it.next();
			it.remove();
			deleteColumnRelations(entry.getValue());
		}
	}

	public final void addForeignKey(TAPForeignKey key) throws Exception {
		if (key == null)
			return;

		String keyId = key.getKeyId();
		final String errorMsgPrefix = "Impossible to add the foreign key \""+keyId+"\" because ";

		if (key.getFromTable() == null) {
			throw new Exception(errorMsgPrefix+"no source table is specified !");
		}

		if (!this.equals(key.getFromTable())) {
			throw new Exception(errorMsgPrefix+"the source table is not \""+getName()+"\"");
		}

		if (key.getTargetTable() == null) {
			throw new Exception(errorMsgPrefix+"no target table is specified !");
		}

		if (key.isEmpty()) {
			throw new Exception(errorMsgPrefix+"it defines no relation !");
		}

		if (foreignKeys.add(key)){
			try{
				TAPTable targetTable = key.getTargetTable();
				for(Map.Entry<String, String> relation : key){
					if (!hasColumn(relation.getKey())) {
						throw new Exception(errorMsgPrefix+"the source column \""+relation.getKey()+"\" doesn't exist in \""+getName()+"\" !");
					} else if (!targetTable.hasColumn(relation.getValue())) {
						throw new Exception(errorMsgPrefix+"the target column \""+relation.getValue()+"\" doesn't exist in \""+targetTable.getName()+"\" !");
					} else {
						getColumn(relation.getKey()).addTarget(key);
						targetTable.getColumn(relation.getValue()).addSource(key);
					}
				}
			}catch(Exception ex){
				foreignKeys.remove(key);
				throw ex;
			}
		}
	}

	public TAPForeignKey addForeignKey(String keyId, TAPTable targetTable, Map<String, String> columns) throws Exception {
		TAPForeignKey key = new TAPForeignKey(keyId, this, targetTable, columns);
		addForeignKey(key);
		return key;
	}

	public TAPForeignKey addForeignKey(String keyId, TAPTable targetTable, Map<String, String> columns, String description, String utype) throws Exception {
		TAPForeignKey key = new TAPForeignKey(keyId, this, targetTable, columns, description, utype);
		addForeignKey(key);
		return key;
	}

	public final Iterator<TAPForeignKey> getForeignKeys(){
		return foreignKeys.iterator();
	}

	public final int getNbForeignKeys(){
		return foreignKeys.size();
	}

	public final boolean removeForeignKey(TAPForeignKey keyToRemove){
		if (foreignKeys.remove(keyToRemove)){
			deleteRelations(keyToRemove);
			return true;
		}else{
			return false;
		}
	}

	public final void removeAllForeignKeys(){
		Iterator<TAPForeignKey> it = foreignKeys.iterator();
		while(it.hasNext()){
			deleteRelations(it.next());
			it.remove();
		}
	}

	protected final void deleteRelations(TAPForeignKey key){
		for(Map.Entry<String, String> relation : key){
			TAPColumn col = key.getFromTable().getColumn(relation.getKey());
			if (col != null) {
				col.removeTarget(key);
			}

			col = key.getTargetTable().getColumn(relation.getValue());
			if (col != null) {
				col.removeSource(key);
			}
		}
	}

	@Override
	public Iterator<DBColumn> iterator() {
		return new Iterator<DBColumn>() {
			private final Iterator<TAPColumn> it = getColumns();
			@Override
			public boolean hasNext() { return it.hasNext(); }
			@Override
			public DBColumn next() { return it.next(); }
			@Override
			public void remove() { it.remove(); }
		};
	}
	
	public DBTable copy(final String dbName, final String adqlName) {
		TAPTable copy = new TAPTable((adqlName==null)?this.adqlName:adqlName, isPublic);
		copy.setDBName((dbName==null)?this.dbName:dbName);
		copy.setSchema(schema);
		Collection<TAPColumn> collColumns = columns.values();
		for(TAPColumn col : collColumns)
			copy.addColumn((TAPColumn)col.copy());
		copy.setDescription(description);
		copy.setHierarchy(hierarchy);
		copy.setOtherData(otherData);
		copy.setType(type);
		copy.setUtype(utype);
		copy.isPublic = isPublic;
		return copy;
	}

	/**
	 * @return the shareInfo
	 */
	public List<UwsShareItem> getShareInfo() {
		return shareInfo;
	}

	/**
	 * @param shareInfo the shareInfo to set
	 */
	public void setShareInfo(List<UwsShareItem> shareInfo) {
		this.shareInfo = shareInfo;
	}


	@Override
	public String toString(){
		return ((schema != null)?(schema.getName()+"."):"")+adqlName;
	}

	public static void main(String[] args) throws Exception {
		//TAPSchema schema1 = new TAPSchema("monSchema1");
		//TAPSchema schema2 = new TAPSchema("monSchema2");
		TAPSchema schema1 = new TAPSchema("monSchema1", "desc1", "utype1", "title1", true);
		TAPSchema schema2 = new TAPSchema("monSchema2", "desc2", "utype2", "title2", true);

		TAPTable tRef = schema1.addTable("ToRef", TAPTable.PUBLIC_TABLE);
		tRef.addColumn("monMachin");

		TAPTable t = schema2.addTable("Test", TAPTable.PUBLIC_TABLE);
		t.addColumn("machin");
		t.addColumn("truc");
		HashMap<String, String> mapCols = new HashMap<String, String>();
		mapCols.put("machin", "monMachin");
		TAPForeignKey key = new TAPForeignKey("KeyID", t, tRef, mapCols);
		t.addForeignKey(key);
		mapCols = new HashMap<String, String>();
		mapCols.put("truc", "monMachin");
		key = new TAPForeignKey("2ndKey", t, tRef, mapCols);
		t.addForeignKey(key);

		printSchema(schema1);
		printSchema(schema2);

		System.out.println();

		schema2.removeTable("Test");
		printSchema(schema1);
		printSchema(schema2);
	}

	public static void printSchema(TAPSchema schema){
		System.out.println("*** SCHEMA \""+schema.getName()+"\" ***");
		System.out.println("Tables:");
		for(TAPTable t : schema.getTables()){
			printTable(t);
		}
	}

	public static void printTable(TAPTable t){
		System.out.println("TABLE: "+t+"\nNb Columns: "+t.getNbColumns()+"\nNb Relations: "+t.getNbForeignKeys());
		Iterator<TAPColumn> it = t.getColumns();
		while(it.hasNext()){
			TAPColumn col = it.next();
			System.out.print("\t- "+col+"( ");
			Iterator<TAPForeignKey> keys = col.getTargets();
			while(keys.hasNext())
				for(Map.Entry<String, String> relation : keys.next())
					System.out.print(">"+relation.getKey()+"/"+relation.getValue()+" ");
			keys = col.getSources();
			while(keys.hasNext())
				for(Map.Entry<String, String> relation : keys.next())
					System.out.print("<"+relation.getKey()+"/"+relation.getValue()+" ");
			System.out.println(")");
		}
	}

	/**
	 * @return the isPublic
	 */
	public boolean isPublic() {
		return isPublic;
	}


}
