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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.share.UwsShareItem;
import esavo.uws.utils.UwsUtils;
import esavo.tap.TAPService;
import esavo.tap.metadata.TAPMetadata.OutputType;

public class TAPMetadataWriterXml implements TAPMetadataWriter {
	
	private TAPService service;
	private String format = "xml";
	
	public TAPMetadataWriterXml(TAPService service){
		this.service=service;
	}
	
	public boolean writeMetadata(Collection<TAPSchema> schemas, HttpServletResponse response, OutputType outputType, boolean includeShareInfo ) throws ServletException, IOException{
		response.setContentType(UwsOutputResponseHandler.MIME_TYPE_XML);

		PrintWriter writer = response.getWriter();

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		switch(outputType){
		case OnlyTables:
			writeTables(schemas, writer, includeShareInfo);
			break;
		case OnlyFunctions:
			writeFunctions(schemas, writer, includeShareInfo);
			break;
		default:
			writeAll(writer, includeShareInfo);
		}
		
		writer.flush();

		return false;
		
	}
	
	
	private void writeTables(Collection<TAPSchema> schemas, PrintWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
		writer.println("<vod:tableset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\" " +
				"xsi:type=\"vod:TableSet\" xmlns:esatapplus=\"http://esa.int/xml/EsaTapPlus\" " +
				"xsi:schemaLocation=\"http://www.ivoa.net/xml/VODataService/v1.1 " +
				"http://www.ivoa.net/xml/VODataService/v1.1 " +
				"http://esa.int/xml/EsaTapPlus "+service.getVoDataServiceSchemaExtension()+"\">");

		for(TAPSchema s : schemas){
			writeTables(s, writer, includeShareInfo);
		}

		writer.println("</vod:tableset>");
		
	}

	private void writeFunctions(Collection<TAPSchema> schemas, PrintWriter writer, boolean includeShareInfo) throws IOException{
		//TODO shareInfo
		// TODO Change the xsi:schemaLocation attribute with a CDS URL !
		//writer.println("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOSITables/v1.0 http://vo.ari.uni-heidelberg.de/docs/schemata/VODataService-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
		writer.println("<vod:functionset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns:vod=\""+service.getVoFunctionsSchema()+"\" " +
				"xsi:type=\"vod:FunctionSet\" " +
				"xsi:schemaLocation=\"" +
				service.getVoFunctionsSchema()+" "+service.getVoFunctionsSchema()+"\">");

		for(TAPSchema s : schemas){
			writeFunctions(s, writer);
		}

		writer.println("</vod:functionset>");
		
	}
	
	private static void writeAll(PrintWriter writer, boolean shareInfo) throws IOException{
		throw new IOException("Not implemented yet");
	}

	private static void writeFunctions(TAPSchema s, PrintWriter writer) throws IOException {
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

	private static void writeFunction(TAPFunction f, PrintWriter writer) throws IOException {
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
	
	private static void writeArgument(TAPFunctionArgument arg, PrintWriter writer) throws IOException {
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


	private static void writeTables(TAPSchema s, PrintWriter writer, boolean includeShareInfo) throws IOException {
		final String prefix = "\t\t";

		String publicSchema = " esatapplus:public=\""+s.isPublic()+"\"";
		String schemaTitle = "";
		if(s.getTitle()!=null && !s.getTitle().trim().isEmpty()){
			schemaTitle = " esatapplus:title=\""+UwsUtils.escapeXmlAttribute(s.getTitle())+"\"";
		}
		
		writer.println("\t<schema "+publicSchema + schemaTitle+">");

		writeAtt(prefix, "name", s.getName(), writer);
		writeAtt(prefix, "description", StringEscapeUtils.escapeXml(s.getDescription()), writer);
		writeAtt(prefix, "utype", s.getUtype(), writer);
		//writeAtt(prefix, "title", s.getTitle(), writer);

		for(TAPTable t : s.getTables()){
				writeTable(t, writer, includeShareInfo);
		}

		writer.println("\t</schema>");
	}

	private static void writeTable(TAPTable t, PrintWriter writer, boolean includeShareInfo) throws IOException {
		final String prefix = "\t\t\t";

		writer.print("\t\t<table type=\""); writer.print(t.getType().equalsIgnoreCase("table")?"base_table":t.getType()); writer.print("\""); 
		if(t.getSize()!=null) {
			writer.print(" esatapplus:size=\""+t.getSize().toString()+"\"");
		}
		writer.print(" esatapplus:flags=\""+t.getFlags()+"\"");
		
		String hierarcy = t.getHierarchy();
		if(hierarcy!=null && !hierarcy.trim().isEmpty()) {
			writer.print(" esatapplus:hierarchy=\""+hierarcy.trim()+"\"");
		}
		
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

	private static void writeColumn(TAPColumn c, PrintWriter writer) throws IOException {
		final String prefix = "\t\t\t\t";

		writer.print("\t\t\t<column std=\""); writer.print(c.isStd());
		String flags = ""+c.getFlags();
		writer.print("\" esatapplus:flags=\"");  writer.print( flags == null ? "":flags);
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

	private static void writeForeignKey(TAPForeignKey fk, PrintWriter writer) throws IOException {
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

	private static void writeAtt(String prefix, String attributeName, String attributeValue, PrintWriter writer) throws IOException {
		if (attributeValue != null){
			StringBuffer xml = new StringBuffer(prefix);
			xml.append('<').append(attributeName).append('>').append(attributeValue).append("</").append(attributeName).append('>');
			writer.println(xml.toString());
		}
	}
	
	private static void writeShareInfo(String prefix, List<UwsShareItem> shareInfo, PrintWriter writer) throws IOException {
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

	public String getFormat() {
		return format;
	}

}
