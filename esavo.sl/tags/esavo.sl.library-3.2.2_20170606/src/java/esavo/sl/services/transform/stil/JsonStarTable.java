package esavo.sl.services.transform.stil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import esavo.tap.formatter.JsonTransformUtils;
import uk.ac.starlink.table.AbstractStarTable;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;
import uk.ac.starlink.util.DataSource;

/**
 * Creates a STIL table from Json data
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class JsonStarTable extends AbstractStarTable {
	
	private InputStream inputStream;
	private List<ColumnInfo> columnInfo;
	private TableSink tableSink;
	private Parser parser;
	private List<Metadata> metadata;

	/**
	 * Constructor.
	 * @param datsrc input data.
	 * @throws IOException
	 */
	public JsonStarTable(DataSource datsrc) throws IOException{
		start(datsrc.getInputStream(), null);
	}
	
	/**
	 * Constructor.
	 * @param is input stream.
	 * @param sink listener. The listener will be notified when metadata is found, any row is loaded and the end of the data is reached.
	 * @throws IOException
	 */
	public JsonStarTable(InputStream is, TableSink sink) throws IOException{
		start(is, sink);
	}
	
	private void start(InputStream is, TableSink sink) throws IOException{
		inputStream = is;
		tableSink = sink;
		parser = new Parser(is);
		columnInfo = new ArrayList<ColumnInfo>();
		readHeader();
	}

	@Override
	public ColumnInfo getColumnInfo(int icol) {
		return columnInfo.get(icol);
	}

	@Override
	public int getColumnCount() {
		return columnInfo.size();
	}

	@Override
	public long getRowCount() {
		// TODO this object is created for random access, so the final number of rows cannot be calculated in advance.
		return 0;
	}

	@Override
	public RowSequence getRowSequence() throws IOException {
		return new RowSequence() {
			@Override
			public boolean next() throws IOException {
				return parser.hasMoreData();
			}
			
			@Override
			public Object[] getRow() throws IOException {
				return readRow();
			}
			
			@Override
			public Object getCell(int icol) throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public void close() throws IOException {
			}
		};
	}

	@SuppressWarnings("unchecked")
	private void readHeader() throws IOException{
		metadata = readMeta(inputStream, parser);
		parser.skipTo('[');
		for(Metadata meta: metadata){
			ColumnInfo cinfo = new ColumnInfo(meta.getName());
			cinfo.setContentClass(JsonTransformUtils.getClassFromDatatype(meta.getDataType()));
			cinfo.setUCD(meta.getUcd());
			cinfo.setUnitString(meta.getUnit());
			cinfo.setUtype(meta.getUtype());
			columnInfo.add(cinfo);
			getParameters().add(new DescribedValue(cinfo));
		}
		notifyMeta();
	}
	
	
	private List<Metadata> readMeta(InputStream is, Parser parser) throws IOException{
		String token = parser.readToken(':', new int[]{'{','['});
		if(!"metadata".equalsIgnoreCase(token)){
			//error
			throw new IOException("Invalid file: 'metadata' not found");
		}
		if(!parser.skipTo('[')){
			//error end of file
			throw new IOException("Invalid file: Unexpected end of file while reading metadata.");
		}
		int[] defaultItemsToSkip = {' ', '\t'};
		parser.setItemsToSkip(defaultItemsToSkip);
		int[] skipItems = new int[]{'{','}'};
		List<String> items = parser.readTokens('}', ']', skipItems);
		parser.setItemsToSkip(Parser.DEFAULT_SKIP_ITEMS);
		if(items == null || items.size() < 1){
			//end of row or end of file
			throw new IOException("Invalid file: metadata not found.");
		}
		return Metadata.parseMetadata(items);
	}

	
	private Object[] readRow() throws IOException{
		Object[] results = readRow(inputStream, parser);
		if(results == null){
			notifyEnd();
		}
		notifyRow(results);
		if(results != null){
			if(!parser.skipTo(',')){
				notifyEnd();
			}
		}
		return results;
	}
	
	private Object[] readRow(InputStream is, Parser parser) throws IOException{
		int[] skipItems = new int[]{'[',']',','};
		List<String> items = parser.readTokens(',', ']', skipItems);
		if(items == null){
			return null;
		}
		Object[] row = new Object[items.size()];
		for(int i = 0; i < items.size(); i++){
			row[i]=JsonTransformUtils.getSuitableData(items.get(i), metadata.get(i).getDataType());
		}
		return row;
	}

	
	private void notifyMeta(){
		if(tableSink != null){
			try {
				tableSink.acceptMetadata(this);
			} catch (TableFormatException e) {
			}
		}
	}
	
	private void notifyRow(Object[] row){
		if(tableSink != null){
			try {
				tableSink.acceptRow(row);
			} catch (IOException e) {
			}
		}
	}
	
	private void notifyEnd(){
		if(tableSink != null){
			try {
				tableSink.endRows();
			} catch (IOException e) {
			}
		}
	}
	
}
