package esavo.sl.services.upload;

import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.TableBuilder;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;
import uk.ac.starlink.table.formats.RowEvaluator;
import uk.ac.starlink.util.DataSource;

public abstract class StreamTextTableBuilder implements TableBuilder {
	
	public static final int TMP_FILE_SIZE = 50000;
	
	private File tmpDir;
	private String format;
	private BufferedReader inputData;
	protected List<ColumnInfo> columInfos;
	protected RowEvaluator.Metadata metadata=null;

	public StreamTextTableBuilder(){
	
	};
	
	public StreamTextTableBuilder(File tmpDir, String format){
		this.tmpDir=tmpDir;
		this.format=format;
	};
	
	@Override
	public void streamStarTable( InputStream in, TableSink sink, String pos )
            throws TableFormatException {
        
		// Read first 1000 rows
		inputData = new BufferedReader(new InputStreamReader(in));
		
		boolean endReached = false;
		File tmpFile = null;
		try {

			boolean metadataAdded = false;
			while(!endReached){
				
				tmpFile = File.createTempFile("Parse", ".txt", tmpDir);
				PrintStream ps = new PrintStream(tmpFile);
				if(columInfos!=null){
					ps.println(createHeader());
				}

				// Write n rows to the tmp file 
				int buff = 0;
				while(buff < TMP_FILE_SIZE){
					String line=inputData.readLine();
					if(line==null){
						endReached=true;
						break;
					}
					// First line of the file MUST be the header
					ps.println(line);
					buff++;
				}
				ps.flush();
				ps.close();
				
				// Create StarTable from the buffer
				StarTable starTable;
				try {
					if(format.equalsIgnoreCase(new StreamCsvTableBuilder().getFormatName())){
						starTable = new MetadataAwareCsvStarTable(DataSource.makeDataSource(tmpFile.toString()),metadata);
						if(metadata==null){
							metadata=((MetadataAwareCsvStarTable)starTable).obtainMetadata();
						}
					}else if(format.equalsIgnoreCase(new StreamAsciiTableBuilder().getFormatName())){
						starTable = new MetadataAwareAsciiStarTable(DataSource.makeDataSource(tmpFile.toString()),metadata);
						if(metadata==null){
							metadata=((MetadataAwareAsciiStarTable)starTable).obtainMetadata();
						}
					}else{
						starTable = new StarTableFactory().makeStarTable(tmpFile.toString());
					}
				} catch (IOException e) {
					throw new TableFormatException(e);
				}
				
				
				// Add metadata if it has not been added yet
				if(!metadataAdded){
					columInfos = new ArrayList<ColumnInfo>();
					for(int i=0; i<starTable.getColumnCount(); i++){
						columInfos.add(starTable.getColumnInfo(i));
					}
					sink.acceptMetadata(starTable);
					metadataAdded=true;
				}
				
				// Send rows to the stream handler
				RowSequence rowSeq = starTable.getRowSequence();
				
				while(rowSeq.next()){
					sink.acceptRow(rowSeq.getRow());
				}
				
				if(tmpFile!=null && tmpFile.exists()){
					tmpFile.delete();
				}

			}
			
			sink.endRows();
			
		} catch (IOException e) {
			throw new TableFormatException(e);
		} finally{
			if(tmpFile!=null && tmpFile.exists()){
				tmpFile.delete();
			}
		}
    }

	@Override
	public String getFormatName() {
		return format;
	}
	
	@Override
	public boolean canImport(DataFlavor flavor) {
		return false;
	}

	
	protected abstract String createHeader();
}
