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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import uk.ac.starlink.votable.VOTableBuilder;

/**
 * Handler to create Json output from a VOTable input stream.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class VoTableHandler extends AbstractTransformHandler{
	
	/**
	 * Constructor.
	 * @param out output stream.
	 * @param resultsOffset index to the first row to dump.
	 * @param pageSize number of rows to dump.
	 * @param allStrings 'true' if all results must be handled as strings.
	 */
	public VoTableHandler(PrintStream out, long resultsOffset, long pageSize, boolean allStrings){
		super(out, resultsOffset, pageSize, allStrings);
	}
	
	@Override
	public void parse(InputStream in) throws IOException{
		try{
			new VOTableBuilder().streamStarTable(in, this, "0");
		}catch(IOException e){
			if(!isTransformEndProcessingException(e)){
				//Not an error
				throw e;
			}
		}
	}
	
//	public static void main(String[] args) throws Exception{
//		File f = new File("/home/jsegovia/gaia/vo_table_response_1.xml");
//		FileInputStream fis = new FileInputStream(f);
//		VoTableHandler handler = new VoTableHandler(System.out, 0, -1);
//		handler.parse(fis);
//		fis.close();
//	}

}
