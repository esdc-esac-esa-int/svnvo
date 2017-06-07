package esavo.tap.formatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import esavo.adql.db.DBColumn;
import esavo.adql.db.DBTable;
import esavo.adql.db.UserFunctionDBColumn;
import esavo.tap.TAPService;
import esavo.tap.metadata.TAPColumn;
import esavo.tap.metadata.TAPFunction;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPMetadataLoaderArgs;
import esavo.tap.parameters.TAPParameters;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobErrorSummaryMeta;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.QuotaException;
import esavo.uws.utils.UwsUtils;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.WrapperRowSequence;
import uk.ac.starlink.table.jdbc.SequentialResultSetStarTable;
import uk.ac.starlink.table.jdbc.StarResultSet;
import uk.ac.starlink.table.jdbc.TypeMapper;
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

    private static final TypeMapper TAP_TYPE_HANDLER = new TapWriterValueHandler();

    private final DataFormat dfmt_;
    private final VOTableVersion version_;
    private final long maxrec_;
    private final UwsJob job;
    private TAPService service;

    /**
     * Constructor.
     *
     * @param  dfmt  selects VOTable serialization format
     *               (TABLEDATA, BINARY, BINARY2, FITS)
     * @param  version  selects VOTable version
     * @param  maxrec   maximum record count before overflow
     */
    public TapWriter(UwsJob job, DataFormat dfmt, VOTableVersion version, long maxrec, TAPService service ) {
        dfmt_ = dfmt;
        version_ = version;
        maxrec_ = maxrec;
        this.job = job;
        this.service = service;
    }

    /**
     * Writes a result set to an output stream as a VOTable.
     *
     * @param   rset  result set
     * @param   ostrm  destination stream
     */
    public long writeVOTable( DBColumn[] columnTAPMetadata, ResultSet rset, OutputStream ostrm ) throws QuotaException, IOException, SQLException {

        /* Turns the result set into a table. */
        LimitedResultSetStarTable table = new LimitedResultSetStarTable(job, rset, maxrec_ );

        // Add column metadata
        updateTableColumnInfo(columnTAPMetadata, table, rset);
		if(job.isPhaseAborted()){
			return table.getNbRowsRead();
		}

		//Write
		writeVOTable(table, ostrm);
        
        return table.getNbRowsRead();
    }
    
    private void writeVOTable(LimitedResultSetStarTable table, OutputStream ostrm) throws IOException{

        /* Prepares the object that will do the serialization work. */
        VOSerializer voser = VOSerializer.makeSerializer( dfmt_, version_, table );
        BufferedWriter out = new BufferedWriter( new OutputStreamWriter( ostrm ) );
        try{
	        /* Write header. */
	        out.write( "<VOTABLE"
	                 + VOSerializer.formatAttribute( "version", version_.getVersionNumber() )
	                 + VOSerializer.formatAttribute( "xmlns", version_.getXmlNamespace() )
	                 + ">" );
	        out.newLine();
	        out.write( "<RESOURCE type=\"results\">" );
	        out.newLine();
	        out.write( "<INFO name=\"QUERY_STATUS\" value=\"OK\"/>" );
	        out.newLine();
	        
	        TAPParameters tapParams = new TAPParameters(job.getParameters());
	        String adqlQuery = tapParams.getQuery();
	        writeInfo(out, "QUERY", adqlQuery);
	        
	        String caption = tapParams.getCaption();
	        writeInfo(out, "CAPTION", caption);
	        
	        writeInfo(out, "PAGE", tapParams.getPage());
	        writeInfo(out, "PAGE_SIZE", tapParams.getPageSize());
	        writeInfo(out, "JOBID", job.getJobId());
	        writeInfo(out, "JOBNAME", job.getName());
	        
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
	        		out.write( "<INFO name=\"QUERY_STATUS\" value=\"OVERFLOW\"/>" );
	        		out.newLine();
	        	}
	        	
	        	if ( table.jobWasAborted() ){
					UwsJobErrorSummaryMeta errorSummary = job.getErrorSummary();
					String error;
					if(errorSummary != null){
						error = errorSummary.getMessage();
		        		if(error==null){
		        			error="Job was aborted.";
		        		}
	        		}else{
	        			error="Job was aborted.";
	        		}

	        		out.write( "<INFO name=\"QUERY_STATUS\" value=\"ERROR\">"+UwsUtils.escapeXmlData(error)+"</INFO>" );
	        		out.newLine();
	        	}
	        }else{
	        	//metadata only
	        	voser.writePreDataXML(out);
	        	voser.writePostDataXML(out);
	            out.write( "<INFO name=\"QUERY_STATUS\" value=\"OVERFLOW\"/>" );
	            out.newLine();
	        }
	        
			/* Write footer. */
			out.write("</RESOURCE>");
			out.newLine();
			out.write("</VOTABLE>");
			out.newLine();
		}finally {
			out.flush();
		}
    }
    
    private void updateTableColumnInfo(DBColumn[] columnTAPMetadata, LimitedResultSetStarTable table, ResultSet rset) throws SQLException, IOException{
    	if(columnTAPMetadata == null){
    		return;
    	}

    	String dataType;
		ColumnInfo columnInfo;
		String xtype;
		
		ResultSetMetaData rsm = rset.getMetaData();
		DBColumn field;
		int columnCount = rsm.getColumnCount();
		for(int index = 1; index <= columnCount; index++){
			if(job.isPhaseAborted()){
				//return table.getNbRowsRead();
				return;
			}
			columnInfo = table.getColumnInfo(index-1);
			if(columnInfo == null){
				throw new IOException("Cannot find column info for '"+rsm.getColumnName(index)+"'");
			}
			field = getField(rsm, index, columnTAPMetadata);
			if(field == null){
				if(columnInfo.getContentClass() == Object.class){
					columnInfo.setContentClass(String.class);
				} else {
					TAPFunction tapFunction = getFunctionInfo(columnInfo, columnTAPMetadata);
					if(tapFunction != null){
						setArrayColumnInfo(columnInfo, tapFunction);
					}
				}
				continue;
			}
			if(field instanceof TAPColumn){
				TAPColumn tapCol = (TAPColumn)field;
				columnInfo.setDescription(tapCol.getDescription());
				columnInfo.setUCD(tapCol.getUcd());
				columnInfo.setUtype(tapCol.getUtype());
				columnInfo.setUnitString(tapCol.getUnit());
				dataType = tapCol.getDatatype();
				if(dataType.equals("SMALLINT")){
					columnInfo.setContentClass(Short.class);
				} else if (dataType.equals("TIMESTAMP")){
					xtype = tapCol.getVotType().xtype;
					columnInfo.setAuxDatum(new DescribedValue(VOStarTable.XTYPE_INFO, xtype));
				} else if (dataType.equals("VARCHAR") && columnInfo.getContentClass() == Object.class){
					columnInfo.setContentClass(String.class);
				} else if (dataType.equals("VARBINARY") && columnInfo.getContentClass() == java.sql.Array.class){
					setArrayColumnInfo(columnInfo, tapCol);
				}
			} else {
				// In case column info has not a known class, set as String
				if(columnInfo.getContentClass()==Object.class){
					columnInfo.setContentClass(String.class);
				}
				columnInfo.setDescription(field.getADQLName());
			}
		}
    }
    
	private TAPFunction getFunctionInfo(ColumnInfo columnInfo, DBColumn[] columnTAPMetadata) throws SQLException {
		//FIXME always public ?
		String functionName = columnInfo.getName();
		String fullQualifiedFunctionName = "public." + functionName;
		try{
			return getFunctionInfo(fullQualifiedFunctionName);
		}catch(Exception e){
			//May be, functionName is an alias, try to find it in DBColumns
		}
		functionName = findFunctionAlias(functionName, columnTAPMetadata);
		if(functionName == null){
			//No info about this function: return null
			return null;
		}
		fullQualifiedFunctionName = "public." + functionName.toLowerCase();
		try{
			return getFunctionInfo(fullQualifiedFunctionName);
		}catch(Exception e){
			//No info about this function: return null
			return null;
		}
	}
	
	private String findFunctionAlias(String functionAlias, DBColumn[] columnTAPMetadata){
		if(columnTAPMetadata == null){
			return null;
		}
		String name;
		for(DBColumn dbColumn: columnTAPMetadata){
			if(dbColumn instanceof UserFunctionDBColumn){
				name = dbColumn.getDBName();
				if(name.equals(functionAlias)){
					return ((UserFunctionDBColumn)dbColumn).getFunctionName();
				}
			}
		}
		return null;
	}
	
	private TAPFunction getFunctionInfo(String fullQualifiedFunctionName) throws SQLException {
		TAPMetadataLoaderArgs args = new TAPMetadataLoaderArgs();
		args.setFullQualifiedFunctionName(fullQualifiedFunctionName);
		UwsJobOwner owner = job.getOwner();
		try {
			return TAPMetadataLoader.getTAPSingleFunction(service, owner, args);
		} catch (Exception e) {
			throw new SQLException("Cannot find function '"+fullQualifiedFunctionName+"'", e);
		}
	}
	
	private void setArrayColumnInfo(ColumnInfo columnInfo, TAPFunction tapFunction){
		Class contentClass = getArrayClass(tapFunction);
		int[] dims = getArrayDims(tapFunction);
		setArrayColumnInfo(columnInfo, contentClass, dims);
	}
    
    private void setArrayColumnInfo(ColumnInfo columnInfo, TAPColumn tapCol){
		Class contentClass = getArrayClass(tapCol);
		int[] dims = getArrayDims(tapCol);
		setArrayColumnInfo(columnInfo, contentClass, dims);
    }
    
    private void setArrayColumnInfo(ColumnInfo columnInfo, Class contentClass, int[] dims){
    	if(contentClass != null){
    		columnInfo.setContentClass(contentClass);
    	}
		if(dims != null){
			columnInfo.setShape(dims);
//			if(contentClass == String[].class){
//				int size = 1;
//				for(int i: dims){
//					size *= i;
//				}
//				//columnInfo.setElementSize(size);
//				columnInfo.setElementSize(1);
//			}
		}
		if(contentClass == String[].class){
			if(dims.length == 1){
				int[] nDims = new int[1];
				if(dims[0] == -1){
					nDims[0] = 1;
					columnInfo.setElementSize(1);
				}else{
					nDims[0] = dims[0];
					columnInfo.setElementSize(dims[0]);
				}
				columnInfo.setShape(nDims);
			}else{
				int[] nDims = new int[dims.length-1];
				columnInfo.setElementSize(dims[0]);
				for(int i = 1; i < dims.length; i++){
					nDims[i-1]=dims[i];
				}
				columnInfo.setShape(nDims);
			}
		}
    }

    private Class getArrayClass(TAPColumn tapColumn){
    	String arrayType = tapColumn.getArrayType();
    	return getArrayClass(arrayType);
    }
    
    private Class getArrayClass(String arrayType){
		if("short".equals(arrayType)){
			return short[].class;
		}
		if("int".equals(arrayType)){
			return int[].class;
		}
		if("long".equals(arrayType)){
			return long[].class;
		}
		if("float".equals(arrayType)){
			return float[].class;
		}
		if("double".equals(arrayType)){
			return double[].class;
		}
		if("char".equals(arrayType)){
			return String[].class;
		}
    	return null;
    }
    
    private Class getArrayClass(TAPFunction tapFunction){
    	String arrayDimType = tapFunction.getArrayDimType();
    	if(arrayDimType == null || arrayDimType.isEmpty()){
    		return null;
    	}
    	String arrayType = arrayDimType.toLowerCase();
    	return getArrayClass(arrayType);
//		if("smallint".equals(arrayType)){
//			return short[].class;
//		}
//		if("integer".equals(arrayType)){
//			return int[].class;
//		}
//		if("bigint".equals(arrayType)){
//			return long[].class;
//		}
//		if("real".equals(arrayType)){
//			return float[].class;
//		}
//		if("double".equals(arrayType)){
//			return double[].class;
//		}
//    	return null;
    }
    
    
    private int[] getArrayDims(TAPColumn tapColumn){
    	String arrayDims = tapColumn.getArrayDims();
    	return getArrayDims(arrayDims);
    }
    
    private int[] getArrayDims(TAPFunction tapFunction){
    	String arrayDims = tapFunction.getArrayDims();
    	return getArrayDims(arrayDims);
    }
    
    private int[] getArrayDims(String arrayDims){
    	if(arrayDims == null || arrayDims.isEmpty()){
    		return null;
    	}
    	String[] dims = arrayDims.split("x");
    	if(dims == null || dims.length < 1){
    		//variable length
    		return null;
    	}
    	int[] d = new int[dims.length];
    	//format: 2x64x*
    	for(int i = 0; i < dims.length; i++){
    		if(dims[i].equals("*")){
    			d[i] = -1;
    		}else{
    			try{
    				d[i] = Integer.parseInt(dims[i]);
    			}catch(NumberFormatException nfe){
    				d[i] = -1;
    			}
    		}
    	}
    	return d;
    }

    
    private DBColumn getField(ResultSetMetaData rsm, int index, DBColumn[] columnTAPMetadata) throws SQLException {
    	String tableName = rsm.getTableName(index);
    	String columnName = rsm.getColumnName(index);
    	//System.out.println("Request: " + tableName + " .  " + columnName);
    	DBTable sourceTable;
    	String tableNameTmp;
    	String columnNameTmp;
    	for(DBColumn column: columnTAPMetadata){
    		sourceTable = column.getTable();
        	if(sourceTable != null){
        		//System.out.println("table src: adql table name: " + sourceTable.getADQLName() + " - db table name: " + sourceTable.getDBName() + " / column: " + column.getADQLName() + ", " + column.getDBName());
        		tableNameTmp = sourceTable.getDBName();
        		columnNameTmp = column.getADQLName();
        	}else{
        		//System.out.println("table src: None - None / column: " + column.getADQLName() + ", " + column.getDBName());
        		tableNameTmp = null;
        		columnNameTmp = column.getADQLName();
        	}
        	if(tableName.equalsIgnoreCase(tableNameTmp) && columnName.equalsIgnoreCase(columnNameTmp)){
        		return column;
        	}
    	}
    	return null;
    }
    
    private void writeInfo(BufferedWriter out, String infoId, String value) throws IOException{
    	if(value != null){
    		out.write("<INFO name=\""+infoId+"\">"+UwsUtils.escapeXmlData(value)+"</INFO>");
    		out.newLine();
    	} else {
    		out.write("<INFO name=\""+infoId+"\"></INFO>");
    	}
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
        private boolean jobAborted_;
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
        	//super(new StarResultSet(rset, TypeMappers.DALI));
        	super(new StarResultSet(rset, TAP_TYPE_HANDLER));
            maxrec_ = maxrec;
            this.job = job;
            
          
        }

        /**
         * Indicates whether the last row sequence dispensed by
         * this table's getRowSequence method was truncated at maxrec rows.
         *
         * @return   true if the last row sequence overflowed
         */
        public boolean lastSequenceOverflowed() {
            return overflow_;
        }

        /**
         * Indicates whether the corresponding job was aborted.
         *
         * @return   true if the corresponding Job was aborted.
         */
        public boolean jobWasAborted() {
            return jobAborted_;
        }
        
        /**
         * Returns the number of rows read.
         * @return
         */
        public int getNbRowsRead() {
            return nbRowsRead;
        }

        @Override
        public RowSequence getRowSequence() {
            overflow_ = false;
            try{
	            return new WrapperRowSequence( super.getRowSequence() ) {
	                long irow = -1;
	                @Override
	                public boolean next() throws IOException {
	                	if(job.isPhaseAborted()){
	                    	jobAborted_ = true;
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
            }catch(Exception e){
            	e.printStackTrace();
            	return null;
            }
        }
    }

}