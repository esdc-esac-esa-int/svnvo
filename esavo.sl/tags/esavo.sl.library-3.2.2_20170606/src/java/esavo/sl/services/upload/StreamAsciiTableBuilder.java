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
