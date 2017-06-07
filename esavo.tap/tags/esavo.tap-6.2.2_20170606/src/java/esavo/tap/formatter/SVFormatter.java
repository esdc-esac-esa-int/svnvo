package esavo.tap.formatter;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.starlink.table.formats.CsvTableWriter;
import esavo.adql.db.DBColumn;
import esavo.tap.TAPException;
import esavo.tap.TAPExecutionReport;
import esavo.tap.TAPService;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.storage.UwsStorage;

public class SVFormatter extends SVFormat implements OutputFormat {

	public SVFormatter(final TAPService service, char colSeparator, boolean delimitStrings) {
		super(service, colSeparator, delimitStrings);
	}

	public SVFormatter(final TAPService service, char colSeparator) {
		super(service, colSeparator);
	}

	public SVFormatter(final TAPService service, String colSeparator, boolean delimitStrings) {
		super(service, colSeparator, delimitStrings);
	}

	public SVFormatter(final TAPService service, String colSeparator) {
		super(service, colSeparator);
	}

	@Override
	protected DBColumn[] writeMetadata(UwsJob job, ResultSet queryResult, PrintWriter writer, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException {
		DBColumn[] selectedColumns = execReport.resultingColumns;
		int nbColumns = (selectedColumns == null) ? -1 : selectedColumns.length;
		if (nbColumns > 0){
			for(int i=0; i<nbColumns-1; i++){
				if(job.isPhaseAborted()){
					return selectedColumns;
				}
				writer.print(selectedColumns[i].getADQLName());
				writer.print(separator);
			}
			writer.print(selectedColumns[nbColumns-1].getADQLName());
			writer.println();
			writer.flush();
		}
		return selectedColumns;
	}

	@Override
	protected int writeData(UwsJob job, ResultSet queryResult, DBColumn[] selectedColumns, PrintWriter writer, TAPExecutionReport execReport) throws IOException, TAPException, InterruptedException {
		int nbRows = 0;
		try{
			int nbColumns = queryResult.getMetaData().getColumnCount();
			while(queryResult.next()){
				if(job.isPhaseAborted()){
					return nbRows;
				}
				if (execReport.parameters.getMaxRec() > 0 && nbRows >= execReport.parameters.getMaxRec()) // that's to say: OVERFLOW !
					break;

				Object value;
				for(int i=1; i<=nbColumns; i++){
					value = queryResult.getObject(i);
					writeFieldValue(value, selectedColumns[i-1], writer);
					if (i != nbColumns)
						writer.print(separator);
//					if (thread.isInterrupted())
//						throw new InterruptedException();
				}
				writer.println();
				nbRows++;

//				if (thread.isInterrupted())
//					throw new InterruptedException();
			}
			writer.flush();
		}catch(SQLException se){
			throw new TAPException("Job "+execReport.jobID+" - Impossible to get the "+(nbRows+1)+"-th rows from the given ResultSet !", se);
		}

		return nbRows;
	}

	@Override
	public String getContentEncoding() { return null; }

	@Override
	public void translateFromCommonFormat(UwsJob job, UwsJobResultMeta result, OutputStream out)
			throws TAPException {
		try {
			UwsStorage storage = service.getFactory().getUwsManager().getFactory().getStorageManager();
			GzipVOTableStreamer streamer = new GzipVOTableStreamer( job, result.getId(), storage );
		    // new FitsTableWriter().writeStarTable( table, file );
		    new  CsvTableWriter().writeStarTable( streamer.createTable(), out );
		} catch (IOException e) {
			throw new TAPException(e);
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				throw new TAPException("Unable to close output stream.",e);
			}
		}
	}
}
