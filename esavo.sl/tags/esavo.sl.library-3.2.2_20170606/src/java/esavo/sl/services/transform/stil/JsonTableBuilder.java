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
package esavo.sl.services.transform.stil;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStream;

import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.TableBuilder;
import uk.ac.starlink.table.TableSink;
import uk.ac.starlink.util.DataSource;

/**
 * Creates a Json table.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class JsonTableBuilder implements TableBuilder{
	
	@Override
	public StarTable makeStarTable(DataSource datsrc, boolean wantRandom, StoragePolicy storagePolicy) throws IOException {
		return new JsonStarTable(datsrc);
	}

	@Override
	public void streamStarTable(InputStream istrm, TableSink sink, String pos) throws IOException {
		JsonStarTable table = new JsonStarTable(istrm, sink);
		RowSequence rs = table.getRowSequence();
		while(rs.next()){
			rs.getRow();
		}
	}

	@Override
	public boolean canImport(DataFlavor flavor) {
		return false;
	}

	@Override
	public String getFormatName() {
		return "JSON";
	}

}
