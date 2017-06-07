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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareItem;
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

	public TAPSchema addSchema(String schemaName){
		if (schemaName == null)
			return null;

		TAPSchema s = new TAPSchema(schemaName);
		addSchema(s);
		return s;
	}

	public TAPSchema addSchema(String schemaName, String description, String utype){
		if (schemaName == null)
			return null;

		TAPSchema s = new TAPSchema(schemaName, description, utype);
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
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);

		PrintWriter writer = response.getWriter();

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		switch(outputType){
		case OnlyTables:
			writeTables(writer, includeShareInfo);
			break;
		case OnlyFunctions:
			writeFunctions(writer, includeShareInfo);
			break;
		default:
			writeAll(writer, includeShareInfo);
		}
		
		writer.flush();

		return false;
	}
	
	private void writeTables(PrintWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
		writer.println("<vod:tableset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\" " +
				"xsi:type=\"vod:TableSet\" xmlns:gaiatap=\"http://esa.int/xml/GaiaTap\" " +
				"xsi:schemaLocation=\"http://www.ivoa.net/xml/VODataService/v1.1 " +
				"http://www.ivoa.net/xml/VODataService/v1.1 " +
				"http://esa.int/xml/GaiaTap "+service.getVoDataServiceSchemaExtension()+"\">");

		for(TAPSchema s : schemas.values()){
			writeTables(s, writer, includeShareInfo);
		}

		writer.println("</vod:tableset>");
		
	}

	private void writeFunctions(PrintWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
		writer.println("<vod:tableset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns:vof=\""+service.getVoFunctionsSchema()+"\" " +
				"xsi:type=\"vof:FunctionSet\" " +
				"xsi:schemaLocation=\"" +
				service.getVoFunctionsSchema()+" "+service.getVoFunctionsSchema()+"\">");

		for(TAPSchema s : schemas.values()){
			writeFunctions(s, writer);
		}

		writer.println("</vod:tableset>");
		
	}
	
	private void writeAll(PrintWriter writer, boolean shareInfo) throws IOException{
		throw new IOException("Not implemented yet");
	}

	private void writeFunctions(TAPSchema s, PrintWriter writer) throws IOException {
		final String prefix = "\t\t";
		writer.println("\t<schema>");

		writeAtt(prefix, "name", s.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(s.getDescription()), writer);
		writeAtt(prefix, "utype", s.getUtype(), writer);

		for(TAPFunction f : s.getFunctions()){
				writeFunction(f, writer);
		}

		writer.println("\t</schema>");
	}

	private void writeFunction(TAPFunction f, PrintWriter writer) throws IOException {
		final String prefix = "\t\t\t";

		writer.print("\t\t<function>"); 

		writeAtt(prefix, "name", f.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(f.getDescription()), writer);
		writeAtt(prefix, "returnType", f.getReturnType().name(), writer);
		writeAtt(prefix, "signature", f.getSignature(), writer);
		
		Iterator<TAPFunctionArgument> itCols = f.getArguments();
		while(itCols.hasNext()){
			writeArgument(itCols.next(), writer);
		}

		writer.println("\t\t</function>");
	}
	
	private void writeArgument(TAPFunctionArgument arg, PrintWriter writer) throws IOException {
		final String prefix = "\t\t\t\t";

		writer.print("\t\t\t<argument>");

		writeAtt(prefix, "name", arg.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(arg.getDescription()), writer);
		writeAtt(prefix, "type", arg.getType().name(), writer);
		Object o;
		o = arg.getDefaultValue();
		writeAtt(prefix, "defaultValue", o == null ? "null":o.toString(), writer);
		o = arg.getMaxValue();
		writeAtt(prefix, "maxValue", o == null ? "null":o.toString(), writer);
		o = arg.getMinValue();
		writeAtt(prefix, "minValue", o == null ? "null":o.toString(), writer);

		writer.println("\t\t\t</argument>");
	}


	private void writeTables(TAPSchema s, PrintWriter writer, boolean includeShareInfo) throws IOException {
		final String prefix = "\t\t";
		writer.println("\t<schema>");

		writeAtt(prefix, "name", s.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(s.getDescription()), writer);
		writeAtt(prefix, "utype", s.getUtype(), writer);

		for(TAPTable t : s.getTables()){
				writeTable(t, writer, includeShareInfo);
		}

		writer.println("\t</schema>");
	}

	private void writeTable(TAPTable t, PrintWriter writer, boolean includeShareInfo) throws IOException {
		final String prefix = "\t\t\t";

		writer.print("\t\t<table type=\""); writer.print(t.getType().equalsIgnoreCase("table")?"base_table":t.getType()); writer.print("\""); 
		if(t.getSize()!=null) {
			writer.print(" gaiatap:size=\""+t.getSize().toString()+"\"");
		}
		writer.print(" gaiatap:flags=\""+t.getFlags()+"\"");
		writer.println(">");

		writeAtt(prefix, "name", t.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(t.getDescription()), writer);
		writeAtt(prefix, "utype", t.getUtype(), writer);
		
		if(includeShareInfo){
			writeShareInfo(prefix, t.getShareInfo(), writer);
		}
		
		//if(t.getSize()!=null) writeAtt(prefix, "size", t.getSize().toString(), writer);

		Iterator<TAPColumn> itCols = t.getColumns();
		while(itCols.hasNext()){
			writeColumn(itCols.next(), writer);
		}

		Iterator<TAPForeignKey> itFK = t.getForeignKeys();
		while(itFK.hasNext()){
			writeForeignKey(itFK.next(), writer);
		}

		writer.println("\t\t</table>");
	}

	private void writeColumn(TAPColumn c, PrintWriter writer) throws IOException {
		final String prefix = "\t\t\t\t";

		writer.print("\t\t\t<column std=\""); writer.print(c.isStd());
		String flags = ""+c.getFlags();
		writer.print("\" gaiatap:flags=\"");  writer.print( flags == null ? "":flags);
		writer.println("\">");

		writeAtt(prefix, "name", c.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(c.getDescription()), writer);
		writeAtt(prefix, "unit", c.getUnit(), writer);
		writeAtt(prefix, "ucd", c.getUcd(), writer);
		writeAtt(prefix, "utype", c.getUtype(), writer);

		if (c.getDatatype() != null){
			String tapType = TAPTypes.getTapType(c.getDatatype());
			writer.print(prefix);
			writer.print("<dataType xsi:type=\"vod:TAPType\"");
//			if (votType.arraysize >= 0){
//				writer.print(" size=\"");
//				writer.print(votType.arraysize);
//				writer.print("\"");
//			}
			writer.print('>');
			writer.print(tapType);
			writer.println("</dataType>");
		}

		if (c.isIndexed())
			writeAtt(prefix, "flag", "indexed", writer);
		if (c.isPrincipal())
			writeAtt(prefix, "flag", "primary", writer);

		writer.println("\t\t\t</column>");
	}

	private void writeForeignKey(TAPForeignKey fk, PrintWriter writer) throws IOException {
		final String prefix = "\t\t\t\t";

		writer.println("\t\t\t<foreignKey>");

		writeAtt(prefix, "targetTable", fk.getTargetTable().getFullName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(fk.getDescription()), writer);
		writeAtt(prefix, "utype", fk.getUtype(), writer);

		final String prefix2 = prefix+"\t";
		for(Map.Entry<String, String> entry : fk){
			writer.print(prefix); writer.println("<fkColumn>");
			writeAtt(prefix2, "fromColumn", entry.getKey(), writer);
			writeAtt(prefix2, "targetColumn", entry.getValue(), writer);
			writer.print(prefix); writer.println("</fkColumn>");
		}

		writer.println("\t\t\t</foreignKey>");
	}

	private void writeAtt(String prefix, String attributeName, String attributeValue, PrintWriter writer) throws IOException {
		if (attributeValue != null){
			StringBuffer xml = new StringBuffer(prefix);
			xml.append('<').append(attributeName).append('>').append(attributeValue).append("</").append(attributeName).append('>');
			writer.println(xml.toString());
		}
	}
	
	private void writeShareInfo(String prefix, List<UwsShareItem> shareInfo, PrintWriter writer) throws IOException {
		if(shareInfo != null){
			String prefix2 = prefix+'\t';
			StringBuilder xml = new StringBuilder();
			xml.append(prefix).append("<shareInfo>\n");
			for(UwsShareItem i: shareInfo){
				xml.append(prefix2);
				xml.append("<sharedTo type=\"").append(i.getShareType()).append("\" mode=\"").append(i.getShareMode()).append("\">");
				xml.append("<![CDATA[").append(i.getShareToId()).append("]]></sharedTo>\n");
			}
			xml.append(prefix).append("</shareInfo>\n");
			writer.println(xml.toString());
		}
	}

}
