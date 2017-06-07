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
package esavo.sl.services.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.formats.AsciiTableBuilder;
import uk.ac.starlink.util.DataSource;

public class StreamAsciiTableBuilder extends StreamTextTableBuilder {
	
	File tmpDir;
	BufferedReader inputData;
	
	public StreamAsciiTableBuilder(){
	}
	
	public StreamAsciiTableBuilder(File tmpDir){
		super(tmpDir, new AsciiTableBuilder().getFormatName());
	}
	
	@Override
    public String getFormatName() {
        return new AsciiTableBuilder().getFormatName();
    }

	@Override
	public StarTable makeStarTable(DataSource datsrc, boolean wantRandom,
			StoragePolicy storagePolicy) throws IOException {
		return new MetadataAwareAsciiStarTable( datsrc, metadata );
	}




	@Override
	protected String createHeader() {
		String header = "#";
		String join = "";
		for(ColumnInfo col: columInfos){
			header+=join+col.getName();
			join+="\t";
		}
		return header;
	}
	
}
