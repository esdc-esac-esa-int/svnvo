package esavo.sl.services.transform.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import esavo.sl.services.transform.stil.JsonTableBuilder;

/**
 * Handler to create Json output from a Json input stream.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class JsonHandler extends AbstractTransformHandler{
	
	/**
	 * Constructor
	 * @param out output stream.
	 * @param resultsOffset index to the first row to dump.
	 * @param pageSize number of rows to dump.
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public JsonHandler(PrintStream out, long resultsOffset, long pageSize, boolean allStrings){
		super(out, resultsOffset, pageSize, allStrings);
	}

	@Override
	public void parse(InputStream is) throws IOException {
		try{
			new JsonTableBuilder().streamStarTable(is, this, "0");
		}catch(IOException e){
			if(!isTransformEndProcessingException(e)){
				//Not an error
				throw e;
			}
		}
	}
	
//	public static void main(String args[]) throws Exception{
//		//File f = new File("/home/jsegovia/gaia/tap_response_json_2.txt");
//		//File f = new File("/home/jsegovia/gaia/tap_response_json_1.txt");
//		//File f = new File("/home/jsegovia/gaia/g10_mw_error.js");
//		//File f = new File("/home/jsegovia/gaia/tap_json_222.js");
//		File f = new File("/home/jsegovia/gaia/tap_test_pag.js");
//		FileInputStream fis = new FileInputStream(f);
//		JsonHandler handler = new JsonHandler(System.out, 0, -1);
//		handler.parse(fis);
//		fis.close();
//		System.out.flush();
//	}
	
}
