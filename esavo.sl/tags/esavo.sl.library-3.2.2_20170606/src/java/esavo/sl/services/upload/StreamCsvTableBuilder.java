package esavo.sl.services.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.formats.CsvTableBuilder;
import uk.ac.starlink.util.DataSource;

public class StreamCsvTableBuilder extends StreamTextTableBuilder {
	
	File tmpDir;
	BufferedReader inputData;
	

	public StreamCsvTableBuilder(){
	}

	public StreamCsvTableBuilder(File tmpDir){
		super(tmpDir, new CsvTableBuilder().getFormatName());
	}


	@Override
    public String getFormatName() {
        return new CsvTableBuilder().getFormatName();
    }

	@Override
	protected String createHeader() {
		String header = "";
		String join = "";
		for(ColumnInfo col: columInfos){
			header+=join+col.getName();
			join+=",";
		}
		return header;
	}

	@Override
	public StarTable makeStarTable(DataSource datsrc, boolean wantRandom,
			StoragePolicy storagePolicy) throws IOException {
		return new MetadataAwareCsvStarTable( datsrc , metadata );
	}

}
