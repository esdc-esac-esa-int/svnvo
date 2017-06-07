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
package esavo.tap.formatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import esavo.adql.db.DBColumn;
import esavo.tap.metadata.TAPColumn;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.QuotaException;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.WrapperRowSequence;
import uk.ac.starlink.table.jdbc.SequentialResultSetStarTable;
import uk.ac.starlink.table.jdbc.StarResultSet;
import uk.ac.starlink.table.jdbc.TypeMappers;
import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOSerializer;
import uk.ac.starlink.votable.VOStarTable;
import uk.ac.starlink.votable.VOTableVersion;

/**
 * Writes SQL ResultSets to VOTable using
 * It's all streamed, so no appreciable amount of memory should be required,
 * and a maximum record count can be imposed.
 */
public class TapWriter {

    private final DataFormat dfmt_;
    private final VOTableVersion version_;
    private final long maxrec_;
    private final UwsJob job;

    /**
     * Constructor.
     *
     * @param  dfmt  selects VOTable serialization format
     *               (TABLEDATA, BINARY, BINARY2, FITS)
     * @param  version  selects VOTable version
     * @param  maxrec   maximum record count before overflow
     */
    public TapWriter(UwsJob job, DataFormat dfmt, VOTableVersion version, long maxrec ) {
        dfmt_ = dfmt;
        version_ = version;
        maxrec_ = maxrec;
        this.job = job;
    }

    /**
     * Writes a result set to an output stream as a VOTable.
     *
     * @param   rset  result set
     * @param   ostrm  destination stream
     */
    public long writeVOTable( DBColumn[] columnTAPMetadata, ResultSet rset, OutputStream ostrm )
            throws QuotaException, IOException, SQLException {

    	//long nbRows = 0;
        /* Turns the result set into a table. */
        LimitedResultSetStarTable table = new LimitedResultSetStarTable(job, rset, maxrec_ );
        
        //// Add column metadata
		if (columnTAPMetadata != null){
			int indField=0;
			String dataType;
			ColumnInfo columnInfo;
			String xtype;
			for(DBColumn field : columnTAPMetadata){
				if(job.isPhaseAborted()){
					return table.getNbRowsRead();
				}
				columnInfo = table.getColumnInfo(indField);
				if(field instanceof TAPColumn){
					TAPColumn tapCol = (TAPColumn)field;
					columnInfo.setDescription(tapCol.getADQLName());
					columnInfo.setUCD(tapCol.getUcd());
					columnInfo.setUtype(tapCol.getUtype());
					columnInfo.setUnitString(tapCol.getUnit());
					dataType = tapCol.getDatatype();
					if(dataType.equals("SMALLINT")){
						columnInfo.setContentClass(Short.class);
					} else if (dataType.equals("TIMESTAMP")){
						xtype = tapCol.getVotType().xtype;
						columnInfo.setAuxDatum(new DescribedValue(VOStarTable.XTYPE_INFO, xtype));
					}
				} else {
					columnInfo.setDescription(field.getADQLName());
				}
				
				indField++;
			}
		}

        /* Prepares the object that will do the serialization work. */
        VOSerializer voser =
            VOSerializer.makeSerializer( dfmt_, version_, table );
        BufferedWriter out =
            new BufferedWriter( new OutputStreamWriter( ostrm ) );
        try{
	
	        /* Write header. */
	        out.write( "<VOTABLE"
	                 + VOSerializer.formatAttribute( "version",
	                                          version_.getVersionNumber() )
	                 + VOSerializer.formatAttribute( "xmlns", version_.getXmlNamespace() )
	                 + ">" );
	        out.newLine();
	        out.write( "<RESOURCE type=\"results\">" );
	        out.newLine();
	        out.write( "<INFO name='QUERY_STATUS' value='OK'/>" );
	        out.newLine();
	
	    	//Rec TAP 1.0 27-Mar-2010 2.3.7, 2.7.4 & 2.9.1
	        if(maxrec_ != 0){
	        	//normal case
	        	/* Write table element. */
	        	try{
	        		voser.writeInlineTableElement(out);
	        	}catch(IOException e){
	        		if(e instanceof QuotaException){
	        			throw new QuotaException(e);
	        		}
	        	}
	        	/* Check for overflow and write INFO if required. */
	        	if ( table.lastSequenceOverflowed() ) {
	        		out.write( "<INFO name='QUERY_STATUS' value='OVERFLOW'/>" );
	        		out.newLine();
	        	}
	        }else{
	        	//metadata only
	        	voser.writePreDataXML(out);
	        	voser.writePostDataXML(out);
	            out.write( "<INFO name='QUERY_STATUS' value='OVERFLOW'/>" );
	            out.newLine();
	        }
	        
			/* Write footer. */
			out.write("</RESOURCE>");
			out.newLine();
			out.write("</VOTABLE>");
			out.newLine();
			out.flush();
		} finally {
			out.close();
		}

        return table.getNbRowsRead();
    }

    /**
     * StarTable implementation which is based on a ResultSet, and which
     * is limited to a fixed number of rows when its row iterator is used.
     * Note this implementation is OK for one-pass table output handlers
     * like VOTable, but won't work for ones which require two passes
     * such as FITS (which needs row count up front).
     */
    private static class LimitedResultSetStarTable
            extends SequentialResultSetStarTable {

        private final long maxrec_;
        private boolean overflow_;
        private int nbRowsRead=0;
        private UwsJob job;

        /**
         * Constructor.
         *
         * @param   rset  result set supplying the data
         * @param   maxrec   maximum number of rows that will be iterated over
         * @param   rowCounter supplied counter to record the number of rows read 
         */
        LimitedResultSetStarTable(UwsJob job, ResultSet rset, long maxrec)
                throws SQLException {
            //super( rset );
        	super(new StarResultSet(rset, TypeMappers.DALI));
            maxrec_ = maxrec;
            this.job = job;
        }

        /**
         * Indicates whether the last row sequence dispensed by
         * this table's getRowSequence method was truncated at maxrec rows.
         *
         * @return   true iff the last row sequence overflowed
         */
        public boolean lastSequenceOverflowed() {
            return overflow_;
        }

        
        /**
         * Returns the number of rows read.
         * @return
         */
        public int getNbRowsRead() {
            return nbRowsRead;
        }

        @Override
        public RowSequence getRowSequence() throws IOException {
            overflow_ = false;
            return new WrapperRowSequence( super.getRowSequence() ) {
                long irow = -1;
                @Override
                public boolean next() throws IOException {
                	if(job.isPhaseAborted()){
                		return false;
                	}
                    irow++;
                    if ( irow < maxrec_ || maxrec_ == -1) {
                        boolean next = super.next();
                        if(next) nbRowsRead++;
                        return next;
                    }
                    if ( irow == maxrec_ ) {
                    	overflow_ = true;
                        //overflow_ = super.next();
                    }
                    return false;
                }
            };
        }
    }

}
