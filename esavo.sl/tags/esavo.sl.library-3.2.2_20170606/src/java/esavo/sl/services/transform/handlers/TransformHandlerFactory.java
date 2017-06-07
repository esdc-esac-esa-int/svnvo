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

import java.io.File;
import java.io.PrintStream;

/**
 * Creates the right handler (json transformer) based on the source data type.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TransformHandlerFactory {
	
	/**
	 * Source data type.
	 *
	 */
	public enum TransformHandlerType{
		VOTABLE,
		JSON,
		CSV,
		CDF
	}

	/**
	 * Creates the suitable handler (Json transformer). A class that reads source data (different formats) and creates Json data. 
	 * @param type source data type.
	 * @param out output stream.
	 * @param resultsOffset initial row index (rows to skip).
	 * @param pageSize number of rows to parse.
	 * @param tmpDir directory where temporary files are saved.
	 * @param allStrings 'true' if all results must be handled as strings. 
	 * @return a handler (a Json transformer)
	 */
	public static TransformHandler createHandler(TransformHandlerType type, PrintStream out, long resultsOffset,
			long pageSize, File tmpDir, boolean allStrings){
		switch(type){
		case VOTABLE:
			return new VoTableHandler(out, resultsOffset, pageSize, allStrings);
		case JSON:
			return new JsonHandler(out, resultsOffset, pageSize, allStrings);
		case CSV:
			return new CsvHandler(out, resultsOffset, pageSize, tmpDir, allStrings);
		case CDF:
			return new CdfHandler(out, resultsOffset, pageSize, tmpDir, allStrings);
		}
		return null;
	}

}
