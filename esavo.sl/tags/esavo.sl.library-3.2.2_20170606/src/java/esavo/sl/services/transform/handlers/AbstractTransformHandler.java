package esavo.sl.services.transform.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import esavo.tap.formatter.JsonTransformUtils;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;

/**
 * Basic functionality to parse data in order to create Json output.
 * @author juan.carlos.segovia@sciops.esa.int
 */
public abstract class AbstractTransformHandler implements TransformHandler, TableSink{
	
	private PrintStream out;
	private long pageSize;
	private long counter;
	private long inputIndex;
	private long outIndex;
	private long endPoint;
	private boolean firstRow;
	private boolean endReached;
	private boolean endRequired;
	private List<ColumnInfo> columnInfo;
	private boolean allStrings;
	
	/**
	 * Constructor
	 * @param out output
	 * @param inputIndex start point.
	 * @param pageSize num results per page (-1 means all results)
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public AbstractTransformHandler(PrintStream out, long inputIndex, long pageSize, boolean allStrings){
		this.out = out;
		this.inputIndex = inputIndex;
		this.pageSize = pageSize;
		this.counter = 0;
		this.endPoint = inputIndex + pageSize;
		this.firstRow = true;
		this.endReached = false;
		this.endRequired = true;
		this.allStrings = allStrings;
		writeInit(outIndex);
	}
	
	/**
	 * Returns the start point.
	 * @return the start point.
	 */
	protected long getInputIndex(){
		return this.inputIndex;
	}
	
	/**
	 * -1 means all results
	 * @return
	 */
	protected long getPageSize(){
		return this.pageSize;
	}
	
	/**
	 * Returns a column info list.
	 * @return a column info list.
	 */
	protected List<ColumnInfo> getColumnInfo(){
		return this.columnInfo;
	}
	
	/**
	 * Parses the input.
	 */
	public abstract void parse(InputStream is) throws IOException;

	private void writeInit(long index){
		//TODO add pagination info
		//out.print("{\"index\": " + index + ", \"pageSize\": " + this.pageSize + ",");
		out.print("{");
		out.flush();
	}
	
	private void writeMeta(List<ColumnInfo> columnInfo){
		this.columnInfo = columnInfo;
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
		if(counter < inputIndex){
			counter++;
			return true;
		}
		//page found
		//endPoint < 0 => get all data
		if(endPoint >= 0 && counter >= endPoint){
			//ignore following rows
			endReached = true;
			return false;
		}
		counter++;
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
			if (allStrings || (o instanceof String)) {
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
			} else {
				out.print(o);
			}
		}
		out.print("]");
	}
	
	private void writeEnd(){
		if(endRequired){
			endRequired = false;
			out.println("\n]");
			out.println("}");
			out.flush();
		}
	}
	
	private void writeEndIfRequired(){
		if(endRequired){
			writeEnd();
		}
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
		if(endReached){
			writeEndIfRequired();
			throw new TransformEndProcessException();
		}
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
	
	/**
	 * Returns 'true' if the argument is or extends TransformEndProcessException
	 * @param t
	 * @return
	 */
	protected boolean isTransformEndProcessingException(Throwable t){
		if(t == null){
			return false;
		}
		Throwable tmp = t;
		while(true){
			if(tmp instanceof TransformEndProcessException){
				return true;
			}
			tmp = tmp.getCause();
			if(tmp == null){
				return false;
			}
		}
	}

}
