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
