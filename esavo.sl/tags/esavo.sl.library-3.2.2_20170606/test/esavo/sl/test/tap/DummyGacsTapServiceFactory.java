package esavo.sl.test.tap;

import java.io.File;

import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.parser.QueryChecker;
import esavo.adql.translator.ADQLTranslator;
import esavo.tap.TAPException;
import esavo.tap.TAPFactory;
import esavo.tap.db.DBConnection;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.TAPLog;
import esavo.tap.metadata.TAPMetadataWriter;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.upload.Uploader;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.utils.test.uws.DummyUwsFactory;

public class DummyGacsTapServiceFactory extends DummyUwsFactory implements TAPFactory {
	
	protected DummyGacsTapDatabaseConnection databaseConnection;
	
	public DummyGacsTapServiceFactory(String appid, File storageDir, UwsConfiguration configuration, StorageType storageType) throws UwsException {
		super(appid, storageDir, configuration, storageType);
		databaseConnection = new DummyGacsTapDatabaseConnection(getDatabaseConnection());
	}
	
	public DummyGacsTapDatabaseConnection getDummyTapDatabaseConnection(){
		return databaseConnection;
	}
	
	@Override
	public ADQLTranslator createADQLTranslator() throws TAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DBConnection createDBConnection(String arg0, String poolid) throws TAPException {
		return databaseConnection;
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

	@Override
	public void setOutputFormats(OutputFormat[] formats) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TAPMetadataWriter getMetadataWriter(String format) {
		// TODO Auto-generated method stub
		return null;
	}

}
