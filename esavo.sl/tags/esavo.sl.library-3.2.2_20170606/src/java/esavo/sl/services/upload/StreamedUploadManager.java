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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import uk.ac.starlink.table.formats.AsciiTableBuilder;
import uk.ac.starlink.table.formats.CsvTableBuilder;
import uk.ac.starlink.votable.VOTableBuilder;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPSchema;
import esavo.uws.owner.UwsJobOwner;

public class StreamedUploadManager {

	public static String upload(InputStream ins, 
			TAPService service, 
			TapJDBCPooledFunctions dbConn,
			UwsJobOwner owner,
			TAPSchema schema, 
			String tableName, 
			String tableDesc,
			String format, 
			String raCol, 
			String deCol,
			String tableSpace) throws IOException, TAPException{
		
	
		StreamedUploadHandler streamedUploadHandler = new StreamedUploadHandler(service, 
				dbConn,
				owner,
				schema, 
				tableName, 
				tableDesc, 
				raCol, 
				deCol,
				tableSpace);

		if(format!=null && format.trim().equalsIgnoreCase( new CsvTableBuilder().getFormatName())){
			File uploadDir = service.getFactory().getStorageManager().getUploadDir(owner);
			new StreamCsvTableBuilder(uploadDir).streamStarTable( ins, streamedUploadHandler, "0");
		}else if(format!=null && format.trim().equalsIgnoreCase( new AsciiTableBuilder().getFormatName())){
			File uploadDir = service.getFactory().getStorageManager().getUploadDir(owner);
			new StreamAsciiTableBuilder(uploadDir).streamStarTable( ins, streamedUploadHandler, "0");
		}else{
			// VOTABLE by default
			new VOTableBuilder().streamStarTable( ins, streamedUploadHandler, "0");
		}
		
		return streamedUploadHandler.getMessage();
	}
}
