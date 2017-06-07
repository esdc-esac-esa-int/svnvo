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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.uws.owner.UwsJobOwner;
import esavo.adql.db.DBTable;
import esavo.tap.TAPService;

public class TAPMetadata implements Iterable<TAPSchema> {

	public static final String USER_SCHEMA_PREFIX = "user_";
	
	public enum OutputType{
		All,
		OnlyTables,
		OnlyFunctions
	}

	protected final Map<String, TAPSchema> schemas;
	
	private final TAPService service;

	public static String getUserSchema(UwsJobOwner owner){
		return getUserSchema(owner.getAuthUsername());
	}
	
	public static String getUserSchema(String ownerAuthUserName){
		return USER_SCHEMA_PREFIX+ownerAuthUserName;
	}
	
	public static String getQualifiedName(UwsJobOwner owner, String tableName){
		int p = tableName.lastIndexOf('.');
		if(p >= 0){
			return tableName; //already qualified
		}else{
			return getUserSchema(owner) + "." + tableName;
		}
	}
	
	public static String getSchemaFromTable(String table){
		int p = table.lastIndexOf('.');
		if(p >= 0){
			return table.substring(0,p);
		}else{
			return null;
		}
	}
	
	public static String getTableNameOnly(String table){
		int p = table.lastIndexOf('.');
		if(p >= 0){
			return table.substring(p+1);
		}else{
			return table;
		}
	}

	public TAPMetadata(TAPService service){
		schemas = new HashMap<String, TAPSchema>();
		this.service = service;
	}

	public final void addSchema(TAPSchema s){
		if (s != null && s.getName() != null)
			schemas.put(s.getName(), s);
	}

//	public TAPSchema addSchema(String schemaName){
//		if (schemaName == null)
//			return null;
//
//		TAPSchema s = new TAPSchema(schemaName);
//		addSchema(s);
//		return s;
//	}

	public TAPSchema addSchema(String schemaName, String description, String utype, String title, boolean isPublic){
		if (schemaName == null)
			return null;

		TAPSchema s = new TAPSchema(schemaName, description, utype, title, isPublic);
		addSchema(s);
		return s;
	}

	public final boolean hasSchema(String schemaName){
		if (schemaName == null)
			return false;
		else
			return schemas.containsKey(schemaName);
	}

	public final TAPSchema getSchema(String schemaName){
		if (schemaName == null)
			return null;
		else
			return schemas.get(schemaName);
	}

	public final int getNbSchemas(){
		return schemas.size();
	}

	public final boolean isEmpty(){
		return schemas.isEmpty();
	}

	public final TAPSchema removeSchema(String schemaName){
		if (schemaName == null)
			return null;
		else
			return schemas.remove(schemaName);
	}

	public final void removeAllSchemas(){
		schemas.clear();
	}

	@Override
	public final Iterator<TAPSchema> iterator() {
		return schemas.values().iterator();
	}

	public Iterator<TAPTable> getTables(){
		return new TableIterator(this);
	}
	
	public Iterator<TAPFunction> getFunctions(){
		return new FunctionIterator(this);
	}

	public boolean hasTable(String schemaName, String tableName){
		TAPSchema s = getSchema(schemaName);
		if (s != null)
			return s.hasTable(tableName);
		else
			return false;
	}

	public boolean hasTable(String tableName){
		for(TAPSchema s : this)
			if (s.hasTable(tableName))
				return true;
		return false;
	}

	//		@Override
	public TAPTable getTable(String schemaName, String tableName){
		TAPSchema s = getSchema(schemaName);
		if (s != null)
			return s.getTable(tableName);
		else
			return null;
	}

	//		@Override
	public ArrayList<DBTable> getTable(String tableName){
		ArrayList<DBTable> tables = new ArrayList<DBTable>();
		for(TAPSchema s : this)
			if (s.hasTable(tableName))
				tables.add(s.getTable(tableName));
		return tables;
	}

	public int getNbTables(){
		int nbTables = 0;
		for(TAPSchema s : this)
			nbTables += s.getNbTables();
		return nbTables;
	}

	public static class TableIterator implements Iterator<TAPTable>{
		private Iterator<TAPSchema> it;
		private Iterator<TAPTable> itTables;

		public TableIterator(TAPMetadata tapSchema){
			it = tapSchema.iterator();

			if(it.hasNext()){
				//itTables = it.next().iterator();
				itTables = it.next().getTables().iterator();
			}

			prepareNext();
		}

		protected void prepareNext(){
			while(!itTables.hasNext() && it.hasNext()){
				//itTables = it.next().iterator();
				itTables = it.next().getTables().iterator();
			}

			if (!itTables.hasNext()){
				it = null;
				itTables = null;
			}
		}

		@Override
		public boolean hasNext() {
			return itTables != null;
		}

		@Override
		public TAPTable next() {
			if (itTables == null){
				throw new NoSuchElementException("No more tables in TAP_SCHEMA !");
			}else{
				TAPTable t = itTables.next();
				prepareNext();
				return t;
			}
		}

		@Override
		public void remove() {
			if (itTables != null){
				itTables.remove();
			}else{
				throw new IllegalStateException("Impossible to remove the table because there is no more tables in TAP_SCHEMA !");
			}
		}
	}
	
	public static class FunctionIterator implements Iterator<TAPFunction>{
		private Iterator<TAPSchema> it;
		private Iterator<TAPFunction> itFunctions;

		public FunctionIterator(TAPMetadata tapSchema){
			it = tapSchema.iterator();

			if(it.hasNext()){
				//itTables = it.next().iterator();
				itFunctions = it.next().getFunctions().iterator();
			}

			prepareNext();
		}

		protected void prepareNext(){
			while(!itFunctions.hasNext() && it.hasNext()){
				//itTables = it.next().iterator();
				itFunctions = it.next().getFunctions().iterator();
			}

			if (!itFunctions.hasNext()){
				it = null;
				itFunctions = null;
			}
		}

		@Override
		public boolean hasNext() {
			return itFunctions != null;
		}

		@Override
		public TAPFunction next() {
			if (itFunctions == null){
				throw new NoSuchElementException("No more functions in TAP_SCHEMA !");
			}else{
				TAPFunction t = itFunctions.next();
				prepareNext();
				return t;
			}
		}

		@Override
		public void remove() {
			if (itFunctions != null){
				itFunctions.remove();
			}else{
				throw new IllegalStateException("Impossible to remove the function because there is no more functions in TAP_SCHEMA !");
			}
		}
	}
	
	/**
	 * Writes a reponse
	 * @param request
	 * @param response
	 * @param outputType
	 * @param shareInfo 'true' to include share info.
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, OutputType outputType, boolean includeShareInfo) throws ServletException, IOException {
		
		String format = request.getParameter("format");
		TAPMetadataWriter writer = service.getFactory().getMetadataWriter(format);
		if(writer == null){
			//use default one
			writer = new TAPMetadataWriterXml(service);
		}
		writer.writeMetadata(schemas.values(), response, outputType, includeShareInfo);
		
		return false;
	}

}
