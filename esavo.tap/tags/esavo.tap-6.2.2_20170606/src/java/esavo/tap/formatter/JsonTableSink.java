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
package esavo.tap.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;

/**
 * Basic functionality to create Json output.
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class JsonTableSink implements TableSink{
	
	private PrintStream out;
	boolean firstRow = true;
	private long rowcount = 0; 
	
	/**
	 * Constructor
	 * @param out output
	 * @param inputIndex start point.
	 * @param pageSize num results per page (-1 means all results)
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public JsonTableSink(OutputStream out){
		this.out = new PrintStream(out);
		writeInit();
	}
	
	private void writeInit(){
		//TODO add pagination info
		//out.print("{\"index\": " + index + ", \"pageSize\": " + this.pageSize + ",");
		out.print("{");
		out.flush();
	}
	
	private void writeMeta(List<ColumnInfo> columnInfo){
		out.println("\"metadata\":");
		out.println("[");

		boolean firstTime = true;
		for(ColumnInfo vi: columnInfo){
			if(firstTime){
				firstTime = false;
			}else{
				out.println(",");
			}
			out.print("{\"name\": "+ "\"" + vi.getName() + "\", \"datatype\": \"" +	JsonTransformUtils.getDataType(vi.getContentClass())	+ "\", "
					+ "\"arraysize\": " + JsonTransformUtils.getValueForArraySize(vi.getContentClass()) + ", "
					+ "\"unit\": \"" + esavo.tap.formatter.Utils.escapeStringForJson(vi.getUnitString()) + "\", "
					+ "\"ucd\": \"" +  esavo.tap.formatter.Utils.escapeStringForJson(vi.getUCD())	+ "\", "
					+ "\"utype\": \"" + esavo.tap.formatter.Utils.escapeStringForJson(vi.getUtype()) + "\"}");
		}
		
		out.println("\n],");
		out.println("\"data\":");
		out.println("[");
	}
	
	/**
	 * 
	 * @param row
	 * @return 'true' to keep reading, 'false' to stop
	 * @throws SAXException
	 */
	private boolean writeData(Object[] row) throws SAXException {
		//valid row
		writeRow(row);
		return true;
	}

	private void writeRow(Object[] row){
		if(firstRow){
			firstRow = false;
		}else{
			out.println(",");
		}
		out.print("[");
		Object o;
		for(int i = 0; i < row.length; i++){
			o = row[i];
			if(i != 0){
				out.print(',');
			}
			if (o instanceof String) {
//				if (o == null){
//					out.print("null");
//				} else if (o instanceof String) {
//					out.print("\"" + escapeQuotes((String) o) + "\"");
//				} else if(o.getClass().isArray()) {
//					out.print("" + Utils.getStringRepresentationFromArray(o));
//				} else {
//					out.print("\"" + o + "\"");
//				}
				out.print(esavo.tap.formatter.Utils.getStringRepresentationForJson(o, true));
			} else if (o.getClass().isArray()){
				out.print(esavo.tap.formatter.Utils.getStringRepresentationFromArray(o));
			} else {
				out.print(o);
			}
		}
		out.print("]");
		rowcount++;
	}
	
	private void writeEnd(){
			out.println("\n]");
			out.println("}");
			out.flush();
	}
	
	
	//--------------------------- TableSink ------------------------------

	@Override
	public void acceptMetadata(StarTable meta) throws TableFormatException {
		List<ColumnInfo> info = new ArrayList<ColumnInfo>();
		int numCols = meta.getColumnCount();
		for(int i = 0; i < numCols; i++){
			info.add(meta.getColumnInfo(i));
		}
		writeMeta(info);
	}

	@Override
	public void acceptRow(Object[] row) throws IOException {
		if(row == null || row.length == 0){
			//skip
			return;
		}
		try {
			writeData(row);
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void endRows() throws IOException {
		writeEnd();
	}
	

}
