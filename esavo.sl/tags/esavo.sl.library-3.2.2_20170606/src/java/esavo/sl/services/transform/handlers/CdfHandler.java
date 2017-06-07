package esavo.sl.services.transform.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import uk.ac.starlink.cdf.CdfTableBuilder;

/**
 * Handler to create Json output from a CDF file.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class CdfHandler extends AbstractFileTransformHandler{
	
	/**
	 * Constructor.
	 * @param out output stream.
	 * @param resultsOffset index to the first row to dump.
	 * @param pageSize number of rows to dump.
	 * @param tmpDir directory where temporary files are saved. 
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public CdfHandler(PrintStream out, long resultsOffset, long pageSize, File tmpDir, boolean allStrings){
		super(out, resultsOffset, pageSize, tmpDir, allStrings);
	}
	
	@Override
	public void parse(InputStream is) throws IOException {
		try{
			parseFile(is, new CdfTableBuilder());
		}catch(IOException e){
			if(!isTransformEndProcessingException(e)){
				//Not an error
				throw e;
			}
		}
	}
	
	@Override
	public void dumpHeader(BufferedReader reader, PrintStream os) {
		//TODO not implemented yet.
	}
	
	@Override
	public void dumpBody(BufferedReader reader, PrintStream os, long offset) {
		//TODO not implemented yet.
	}
	

}
