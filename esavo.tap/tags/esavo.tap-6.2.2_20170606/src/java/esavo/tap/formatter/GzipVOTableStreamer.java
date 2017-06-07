package esavo.tap.formatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.UwsStorage;
import uk.ac.starlink.table.OnceRowPipe;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableBuilder;
import uk.ac.starlink.table.WrapperStarTable;
import uk.ac.starlink.votable.VOTableBuilder;

public class GzipVOTableStreamer {

    private final TableBuilder reader = new VOTableBuilder();
    private final UwsJob job;
    private final UwsStorage storage;
    private final String resultId;

    public GzipVOTableStreamer( UwsJob job, String resultId, UwsStorage storage ) {
    	this.job = job;
        this.resultId = resultId;
        this.storage = storage;
    }


    /**
     * Returns a table that streams the data from the input VOTable
     * every time it's required, rather than (maybe) caching it in memory.
     * Some TableBuilders effectively do that anyway, but not all.
     *
     * @param   reader   table input handler
     * @param   datsrc   factory for the input stream
     */
     public StarTable createTable() throws IOException {
        final OnceRowPipe pipe0 = createPipe();
        StarTable meta = pipe0.waitForStarTable();
        return new WrapperStarTable( meta ) {
            boolean usedPipe0 = false;
            public RowSequence getRowSequence() throws IOException {
                if ( ! usedPipe0 ) {
                	//System.out.println("FIRST PASS");
                    usedPipe0 = true;
                    return pipe0;
                }
                else {
                	//System.out.println("SECOND PASS");
                    return createPipe();
                }
            }
        };
    }

    private OnceRowPipe createPipe() throws IOException {
        final OnceRowPipe pipe = new OnceRowPipe();
        new Thread() {
            public void run() {
                try {
                	InputStream in = new GZIPInputStream(storage.getJobResultDataInputSource(job, resultId));
                    reader.streamStarTable( in, pipe, null );
                }
                catch ( IOException e ) {
                    pipe.setError( e );
                } catch (UwsException e) {
					pipe.setError(new IOException(e));
				}
            }
        }.start();
        return pipe;
    }

}