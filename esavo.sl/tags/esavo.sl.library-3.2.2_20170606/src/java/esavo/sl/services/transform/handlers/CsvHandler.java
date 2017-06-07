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
package esavo.sl.services.transform.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import uk.ac.starlink.table.formats.CsvTableBuilder;

/**
 * Handler to create Json output from a CSV file.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class CsvHandler extends AbstractFileTransformHandler{
	
	/**
	 * Constructor.
	 * @param out output stream.
	 * @param resultsOffset index to the first row to dump.
	 * @param pageSize number of rows to dump.
	 * @param tmpDir directory where temporary files are saved. 
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public CsvHandler(PrintStream out, long resultsOffset, long pageSize, File tmpDir, boolean allStrings){
		super(out, resultsOffset, pageSize, tmpDir, allStrings);
	}
	
	@Override
	public void parse(InputStream is) throws IOException {
		try{
			parseFile(is, new CsvTableBuilder());
		}catch(IOException e){
			if(!isTransformEndProcessingException(e)){
				//Not an error
				throw e;
			}
		}
	}
	
	@Override
	public void dumpHeader(BufferedReader reader, PrintStream os) throws IOException {
		// TODO header requires parser?
	}

	@Override
	public void dumpBody(BufferedReader reader, PrintStream os, long offset) throws IOException {
		//skip lines (CRLF)
		for(long i = 0; i < offset; i++){
			reader.readLine();
		}
		//write pageSize
		long pageSize = getPageSize();
		String line;
		if (pageSize > 0) {
			for (long i = 0; i < pageSize; i++) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				os.println(line);
			}
		} else {
			while((line = reader.readLine()) != null){
				os.println(line);
			}
		}
	}
	
}
