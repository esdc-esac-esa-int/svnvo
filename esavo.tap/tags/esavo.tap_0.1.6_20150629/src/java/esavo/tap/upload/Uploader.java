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
package esavo.tap.upload;

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
import java.io.InputStream;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.votable.VOStarTable;
import uk.ac.starlink.votable.VOTableBuilder;

import com.oreilly.servlet.multipart.ExceededSizeException;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsUploadResourceLoader;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.storage.UwsQuota;
import esavo.uws.storage.UwsQuotaSingleton;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.db.DBConnection;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TAPTypes;
import esavo.tap.metadata.VotType;

public class Uploader {

	protected final TAPService service;
	//protected final DBConnection dbConn;
	//protected final int nbRowsLimit;
	protected final int nbBytesLimit;
	
	protected int nbRows = 0;

	public Uploader(final TAPService service) throws TAPException {
		if (service == null) {
			throw new NullPointerException("The given ServiceConnection is NULL !");
		}

		this.service = service;

		UwsConfiguration configuration = service.getFactory().getConfiguration();
		boolean uploadEnabled = Boolean.parseBoolean(configuration.getProperty(UwsConfiguration.CONFIG_UPLOAD_ENABLED));
		
		if (uploadEnabled) {
			nbBytesLimit = service.getConfiguration().getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
//			if (service.getUploadLimitType()[1] == LimitUnit.row) {
//				nbRowsLimit = ((service.getUploadLimit()[1] > 0) ? service.getUploadLimit()[1] : -1);
//				nbBytesLimit = service.getConfiguration().getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
//			} else {
//				nbBytesLimit = ((service.getUploadLimit()[1] > 0) ? service.getUploadLimit()[1] : -1);
//				nbRowsLimit = service.getConfiguration().getIntProperty(UwsConfiguration.CONFIG_UPLOAD_MAX_SIZE);
//			}
		} else {
//			nbRowsLimit = -1;
			nbBytesLimit = -1;
		}
	}

	public TAPSchema upload(UwsJobOwner owner, final UwsUploadResourceLoader[] loaders, DBConnection dbConn) throws TAPException, UwsException {
		long maxFileSize=0;

		TAPSchema uploadSchema = new TAPSchema("TAP_UPLOAD");
		InputStream votableIns = null;
		String tableName = null;
		nbRows = 0;
		try{
			for(UwsUploadResourceLoader loader : loaders){
				tableName = loader.getTableName();
				votableIns = loader.openStream();

//				long remainingFileQuota = 0;
//				UwsQuota quota = null;
//				try {
//					quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
//					remainingFileQuota = quota.getFileQuota()-quota.getFileCurrentSize();
//					if (nbBytesLimit > 0){
//						maxFileSize = Math.min(nbBytesLimit, remainingFileQuota);
//					}else{
//						maxFileSize = remainingFileQuota;
//					}
//				} catch (UwsException e1) {
//					throw new IOException(e1);
//				}

				
				try {
					UwsQuota quota = UwsQuotaSingleton.getInstance().createOrLoadQuota(owner);
					maxFileSize = quota.getMinFileQuotaAvailable(nbBytesLimit);
				} catch (UwsException e1) {
					votableIns.close();
					throw new IOException(e1);
				}

				votableIns = new LimitedSizeInputStream(votableIns, maxFileSize);

				// start parsing the VOTable:
				StarTableFactory factory = new StarTableFactory();
				StarTable table = factory.makeStarTable( votableIns, new VOTableBuilder() );

				// 1st STEP: Convert the VOTable metadata into DBTable:
				TAPTable tapTable = fetchTableMeta(tableName, table);
				uploadSchema.addTable(tapTable);

				// 2nd STEP: Create the corresponding table in the database:
				dbConn.createTable(tapTable);

				// 3rd STEP: Load rows into this table:
				loadTable(owner, dbConn,tapTable, table);
				votableIns.close();
			}
		}catch(ExceededSizeException ese){
			//throw new TAPException("Upload limit exceeded ! You can upload at most "+((maxFileSize > 0)?(maxFileSize+" bytes."):(nbRowsLimit+" rows.")));
			throw new TAPException("Upload limit exceeded ! You can upload at most "+ maxFileSize + " bytes.");
		}catch(IOException ioe){
			throw new TAPException("Error while reading the VOTable of \""+tableName+"\" !", ioe);
		}catch(NullPointerException npe){
			if (votableIns != null && votableIns instanceof LimitedSizeInputStream){
				//throw new TAPException("Upload limit exceeded ! You can upload at most "+((maxFileSize > 0)?(maxFileSize+" bytes."):(nbRowsLimit+" rows.")));
				throw new TAPException("Upload limit exceeded ! You can upload at most "+ maxFileSize + " bytes.");
			}else{
				throw new TAPException(npe);
			}
		}finally{
			try{
				if (votableIns != null){
					votableIns.close();
				}
			}catch(IOException ioe){;}
		}

		return uploadSchema;
	}

	/*
	private TAPTable fetchTableMeta(final String tableName, final String userId, final FieldSet fields){
		TAPTable tapTable = new TAPTable(tableName);
		tapTable.setDBName(tableName+"_"+userId);

		for(int j=0 ; j<fields.getItemCount(); j++){
			SavotField field = (SavotField)fields.getItemAt(j);
			int arraysize = TAPTypes.NO_SIZE;
			if (field.getArraySize() == null || field.getArraySize().trim().isEmpty())
				arraysize = 1;
			else if (field.getArraySize().equalsIgnoreCase("*"))
				arraysize = TAPTypes.STAR_SIZE;
			else{
				try{
					arraysize = Integer.parseInt(field.getArraySize());
				}catch(NumberFormatException nfe){
					service.getFactory().getLogger().warning("Invalid array-size in the uploaded table \""+tableName+"\" for the field \""+field.getName()+"\": \""+field.getArraySize()+"\" ! It will be considered as \"*\" !");
				}
			}
			tapTable.addColumn(field.getName(), field.getDescription(), field.getUnit(), field.getUcd(), field.getUtype(), new VotType(field.getDataType(), arraysize, field.getXtype()), false, false, false, 0);
		}

		return tapTable;
	}
	*/
	
	/**
	 * Fetches table metadata into a TAPTable object
	 * @param tableName
	 * @param votable
	 * @param ra_column Name of the ICRS RA column (to overwrite its UTYPE)
	 * @param dec_column Name of the ICRS DEC column (to overwrite its UTYPE)
	 * @return
	 */
	private TAPTable fetchTableMeta(final String tableName, final StarTable votable){
		TAPTable tapTable = new TAPTable(tableName);
		tapTable.setDBName(tableName);

		String fieldNameLowerCase;
		int flags;
		
		for(int col=0 ; col<votable.getColumnCount(); col++){
			ColumnInfo field = votable.getColumnInfo(col);
			fieldNameLowerCase = field.getName().toLowerCase();
			
			int arraysize = TAPTypes.NO_SIZE;
			if(!field.isArray())
				arraysize = 1;
			else if (field.isArray()&&field.getShape()[0]<=0)
				arraysize = TAPTypes.STAR_SIZE;
			else{
				try{
					arraysize = field.getShape()[0];
				}catch(Exception nfe){
					service.getFactory().getLogger().warning("Invalid array-size in the uploaded table \""+tableName+"\" for the field \""+field.getName()+"\": \""+arraysize+"\" ! It will be considered as \"*\" !");
				}
			}
			
			flags = 0;
			
			tapTable.addColumn(fieldNameLowerCase, 
					field.getDescription(), 
					field.getUnitString(), 
					field.getUCD(), 
					field.getUtype(), 
					new VotType((String)field.getAuxDatumValue(VOStarTable.DATATYPE_INFO, String.class), 
								arraysize, 
								(String)field.getAuxDatumValue(VOStarTable.XTYPE_INFO, String.class)), 
					false, 
					false, 
					false,
					flags);

		}
		
		return tapTable;
	}

	/*
	private int loadTable(final TAPTable tapTable, final FieldSet fields, final SavotBinary binary, DBConnection dbConn) throws TAPException, ExceededSizeException {
		// Read the raw binary data:
		DataBinaryReader reader = null;
		try{
			reader = new DataBinaryReader(binary.getStream(), fields, false);
			while(reader.next()){
				if (nbRowsLimit > 0 && nbRows >= nbRowsLimit)
					throw new ExceededSizeException();
				dbConn.insertRow(reader.getTR(), tapTable);
				nbRows++;
			}
		}catch(ExceededSizeException ese){
			throw ese;
		}catch(IOException se){
			throw new TAPException("Error while reading the binary data of the VOTable of \""+tapTable.getADQLName()+"\" !", se);
		}finally{
			try{
				if (reader != null)
					reader.close();
			}catch(IOException ioe){;}
		}

		return nbRows;
	}

	private int loadTable(final TAPTable tapTable, final FieldSet fields, final SavotTableData data, DBConnection dbConn) throws TAPException, ExceededSizeException {
		TRSet rows = data.getTRs();
		for(int i=0; i<rows.getItemCount(); i++){
			if (nbRowsLimit > 0 && nbRows >= nbRowsLimit)
				throw new ExceededSizeException();
			dbConn.insertRow((SavotTR)rows.getItemAt(i), tapTable);
			nbRows++;
		}

		return nbRows;
	}
	*/
	
	private int loadTable(UwsJobOwner owner, DBConnection dbConn, final TAPTable tapTable, StarTable votable) throws TAPException, UwsException {
		int nbRows = dbConn.loadTableData(owner, tapTable, votable);
		return nbRows;
	}

}
