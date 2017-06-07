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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.share.UwsShareItem;
import esavo.tap.formatter.Utils;
import esavo.tap.metadata.TAPMetadata.OutputType;

public class TAPMetadataWriterJson implements TAPMetadataWriter {
	
	private String format = "json";
	
	public TAPMetadataWriterJson(){
	}
	
	public boolean writeMetadata(Collection<TAPSchema> schemas, HttpServletResponse response, OutputType outputType, boolean includeShareInfo ) throws ServletException, IOException{
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_JSON);

		PrintWriter pw = response.getWriter();
		JSONWriter writer = new JSONWriter(pw);

		switch(outputType){
		case OnlyTables:
			writeSchemas(pw, schemas, writer, includeShareInfo);
			break;
		case OnlyFunctions:
			writeFunctions(schemas, writer, includeShareInfo);
			break;
		default:
			writeAll(writer, includeShareInfo);
		}
		
		pw.flush();

		return false;
		
	}
	
	
	private void writeSchemas(PrintWriter pw, Collection<TAPSchema> schemas, JSONWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");

		try {
			writer.array();
			for(TAPSchema s : schemas){
				writeSchema(s, writer, includeShareInfo);
				pw.flush();
			}

			writer.endArray();
		} catch (JSONException e) {
			throw new IOException(e);
		}
		
		
	}

	private void writeFunctions(Collection<TAPSchema> schemas, JSONWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");

		try{
			writer.array();
			for(TAPSchema s : schemas){
				writeFunctions(s, writer);
			}
			writer.endArray();
		
		} catch (JSONException e) {
			throw new IOException(e);
		}
		
	}
	
	private static void writeAll(JSONWriter writer, boolean shareInfo) throws IOException{
		throw new IOException("Not implemented yet");
	}

	private static void writeFunctions(TAPSchema s, JSONWriter writer) throws JSONException {

		// New schema
		writer.object();
		writer.key("name").value(s.getName());
		writer.key("description").value(Utils.escapeStringForJson(s.getDescription()));
		writer.key("title").value(Utils.escapeStringForJson(s.getTitle()));
		writer.key("ispublic").value(s.isPublic());
		writer.key("utype").value(Utils.escapeStringForJson(s.getUtype()));
		
		writer.key("functions");
		writer.array();
		for(TAPFunction f : s.getFunctions()){
				writeFunction(f, writer);
		}
		writer.endArray();
		writer.endObject();
	}

	private static void writeFunction(TAPFunction f, JSONWriter writer) throws JSONException {
		

		writer.object();
		writer.key("name").value(f.getName());
		writer.key("description").value(Utils.escapeStringForJson(f.getDescription()));
		writer.key("returnType").value(f.getReturnType().name());
		writer.key("signature").value(f.getSignature());

		writer.key("arguments");
		writer.array();
		Iterator<TAPFunctionArgument> itCols = f.getArguments();
		while(itCols.hasNext()){
			writeArgument(itCols.next(), writer);
		}
		writer.endArray();
		writer.endObject();
	}
	
	private static void writeArgument(TAPFunctionArgument arg, JSONWriter writer) throws JSONException {
		writer.object();
		
		writer.key("name").value(arg.getName());
		writer.key("description").value(Utils.escapeStringForJson(arg.getDescription()));
		writer.key("type").value(arg.getType().name());

		Object o;
		o = arg.getDefaultValue();
		writer.key("defaultValue").value(o == null ? null:o.toString());
		o = arg.getMaxValue();
		writer.key("maxValue").value(o == null ? null:o.toString());
		o = arg.getMinValue();
		writer.key("minValue").value(o == null ? null:o.toString());

		writer.endObject();
	}


	private static void writeSchema(TAPSchema s, JSONWriter writer, boolean includeShareInfo) throws JSONException {

		// New schema
		writer.object();
		writer.key("name").value(s.getName());
		writer.key("description").value(Utils.escapeStringForJson(s.getDescription()));
		writer.key("title").value(Utils.escapeStringForJson(s.getTitle()));
		writer.key("ispublic").value(s.isPublic());
		writer.key("utype").value(Utils.escapeStringForJson(s.getUtype()));

		writer.key("tables");
		writer.array();
		for(TAPTable t : s.getTables()){
				writeTable(t, writer, includeShareInfo);
		}
		writer.endArray();
		
		writer.endObject();
			
	}

	private static void writeTable(TAPTable t, JSONWriter writer, boolean includeShareInfo) throws JSONException {

		// New table
		writer.object();
		writer.key("name").value(t.getName());
		writer.key("type").value(Utils.escapeStringForJson(t.getType().equalsIgnoreCase("table")?"base_table":t.getType()));
		writer.key("description").value(Utils.escapeStringForJson(t.getDescription()));
		writer.key("utype").value(Utils.escapeStringForJson(t.getUtype()));
		
		if(t.getSize()!=null) {
			writer.key("size").value(t.getSize().toString());
		}else{
			writer.key("size").value(null);
		}
		
		writer.key("flags").value(t.getFlags());
		
		String hierarcy = t.getHierarchy();
		if(hierarcy!=null && !hierarcy.trim().isEmpty()) {
			writer.key("hierarchy").value(Utils.escapeStringForJson(hierarcy.trim()));
		}else{
			writer.key("hierarchy").value(null);
		}

		
		if(includeShareInfo){
			writeShareInfo(t.getShareInfo(), writer);
		}
		
		writer.key("columns");
		writer.array();
		Iterator<TAPColumn> itCols = t.getColumns();
		while(itCols.hasNext()){
			writeColumn(itCols.next(), writer);
		}
		writer.endArray();

		writer.key("foreignkeys");
		writer.array();
		Iterator<TAPForeignKey> itFK = t.getForeignKeys();
		while(itFK.hasNext()){
			writeForeignKey(itFK.next(), writer);
		}
		writer.endArray();

		writer.endObject();
	}

	private static void writeColumn(TAPColumn c, JSONWriter writer) throws JSONException {

		// New column
		writer.object();
		
		writer.key("name").value(c.getName());
		writer.key("std").value(c.isStd());
		String flags = ""+c.getFlags();
		writer.key("binflags").value(flags == null ? "":flags);

		writer.key("description").value(Utils.escapeStringForJson(c.getDescription()));
		writer.key("unit").value(c.getUnit());
		writer.key("ucd").value(c.getUcd());
		writer.key("utype").value(c.getUtype());


		if (c.getDatatype() != null){
			String tapType = TAPTypes.getTapType(c.getDatatype());
			writer.key("datatype").value(tapType);
		}else{
			writer.key("datatype").value(null);
		}

		// Flags array
		writer.key("flags");
		writer.array();
		
		if (c.isIndexed())
			writer.value("indexed");
		if (c.isPrincipal())
			writer.value("principal");
		writer.endArray();
		
		writer.endObject();
	}

	private static void writeForeignKey(TAPForeignKey fk, JSONWriter writer) throws JSONException {

		writer.object();

		writer.key("targetTable").value(fk.getTargetTable().getFullName());
		writer.key("description").value(Utils.escapeStringForJson(fk.getDescription()));
		writer.key("utype").value(fk.getUtype());
		
		writer.key("fkColums");
		writer.array();
		for(Map.Entry<String, String> entry : fk){
			writer.object();
			writer.key("fromColumn").value(entry.getKey());
			writer.key("targetColumn").value(entry.getValue());
			writer.endObject();
		}
		writer.endArray();

		writer.endObject();
	}
	
	private static void writeShareInfo(List<UwsShareItem> shareInfo, JSONWriter writer) throws JSONException {
		if(shareInfo != null){
			writer.key("share");
			writer.array();
			for(UwsShareItem i: shareInfo){
				writer.object();
				
				writer.key("type").value(i.getShareType());
				writer.key("mode").value(i.getShareMode());
				writer.key("sharedto").value(Utils.escapeStringForJson(i.getShareToId()));
				writer.endObject();
			}
			writer.endArray();
		}
	}

	public String getFormat() {
		return format;
	}

}
