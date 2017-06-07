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
package esavo.tap.test.tap;

import java.io.File;

import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.QueryChecker;
import esavo.adql.translator.ADQLTranslator;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.db.DBConnection;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.TAPLog;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.upload.Uploader;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;
import esavo.tap.test.database.DummyDatabaseConnection;
import esavo.tap.test.uws.DummyUwsFactory;
import esavo.tap.test.uws.DummyUwsFactory.StorageType;

public class DummyTapServiceFactory extends DummyUwsFactory implements TAPFactory {
	
	public DummyTapServiceFactory(String appid, File storageDir, UwsConfiguration configuration, StorageType storageType) {
		super(appid, storageDir, configuration, storageType);
		 
	}
	
	@Override
	public ADQLTranslator createADQLTranslator() throws TAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DBConnection createDBConnection(String arg0) throws TAPException {
		DummyDatabaseConnection dbConnection = getDatabaseConnection();
		return new DummyTapDatabaseConnection(dbConnection);
	}

	@Override
	public QueryChecker createQueryChecker(TAPSchema arg0, UwsJobOwner arg1, boolean includeAccessibleSharedItems) throws TAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ADQLQueryFactory createQueryFactory(UwsJobOwner arg0) throws TAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uploader createUploader() throws TAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TAPLog getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputFormat getOutputFormat(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputFormat[] getOutputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

}
