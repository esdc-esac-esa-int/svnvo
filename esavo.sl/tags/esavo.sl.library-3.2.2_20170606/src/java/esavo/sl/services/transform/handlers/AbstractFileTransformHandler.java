package esavo.sl.services.transform.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.TableBuilder;
import uk.ac.starlink.util.DataSource;

/**
 * Base functionality to create Json from a file as input.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public abstract class AbstractFileTransformHandler extends AbstractTransformHandler{
	private File tmpDir;

	/**
	 * Constructor.
	 * @param out output
	 * @param inputIndex start point.
	 * @param pageSize num results per page (-1 means all results)
	 * @param tmpDir directory where temporary files are saved. 
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public AbstractFileTransformHandler(PrintStream out, long inputIndex, long pageSize, File tmpDir, boolean allStrings) {
		super(out, inputIndex, pageSize, allStrings);
		this.tmpDir = tmpDir;
	}
	
	@SuppressWarnings("unchecked")
	protected void parseFile(InputStream is, TableBuilder builder) throws IOException{
		File f = createTmpFile(is);
		StarTable table = builder.makeStarTable(DataSource.makeDataSource(f.getAbsolutePath()), false, StoragePolicy.getDefaultPolicy());
		@SuppressWarnings("rawtypes")
		List tableParameters = table.getParameters();
		if(tableParameters.size() < 1){
			int colNum = table.getColumnCount();
			for(int i = 0; i < colNum; i++){
				ColumnInfo ci = table.getColumnInfo(i);
				tableParameters.add(new DescribedValue(ci));
			}
		}
		acceptMetadata(table);
		RowSequence rowSeq = table.getRowSequence();
		while(rowSeq.next()){
			acceptRow(rowSeq.getRow());
		}
		endRows();
		f.delete();
	}

	/**
	 * Creates a temporary file where the input data is saved.<br/>
	 * This file will be used to create the Json output.<br/>
	 * The file is removed when the parser ends.<br/>
	 * @param is input data
	 * @return a file that contains the input data.
	 * @throws IOException
	 */
	protected File createTmpFile(InputStream is) throws IOException{
		File fTmp = new File(this.tmpDir, ""+getUniqueId());
		PrintStream ps = null;
		BufferedReader reader = null;
		try{
			ps = new PrintStream(fTmp);
			reader = new BufferedReader(new InputStreamReader(is));
			dumpHeader(reader, ps);
			dumpBody(reader, ps, getInputIndex());
			ps.flush();
			ps.close();
		}finally{
			if(ps != null){
				try{
					ps.close();
				}catch(Exception ioe){
				}
			}
			if(reader != null){
				try{
					reader.close();
				}catch(IOException ioe){
				}
			}
		}
		return fTmp;
	}
	
	/**
	 * Writes the header (metadata) to the output stream.
	 * @param reader input data.
	 * @param os output stream.
	 * @throws IOException
	 */
	public abstract void dumpHeader(BufferedReader reader, PrintStream os) throws IOException;
	
	/**
	 * Writes the body (columns) to the output stream.<br/> The implemetation must skip the number of 
	 * rows specified by 'offset' parameter and may use {@link #getPageSize()} to obtain the number of
	 * rows to dump.
	 * @param reader input data.
	 * @param os output stream.
	 * @param offset rows to skip.
	 * @throws IOException
	 */
	public abstract void dumpBody(BufferedReader reader, PrintStream os, long offset) throws IOException;
	
	private long getUniqueId(){
		long l = System.currentTimeMillis();
		long r;
		do{
			r = System.currentTimeMillis();
		}while(r == l);
		return r;
	}
	
}
