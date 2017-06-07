package esavo.tap.service;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.translator.ADQLTranslator;
import esavo.adql.translator.Q3cPgSphereTranslator;
import esavo.tap.ADQLExecutor;
import esavo.tap.AbstractTAPFactory;
import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.db.DBConnection;
import esavo.tap.db.TapJDBCPooledConnection;
import esavo.tap.formatter.OutputFormat;
import esavo.tap.log.DefaultTAPLog;
import esavo.tap.log.TAPLog;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsDefaultScheduler;
import esavo.uws.share.UwsShareGroup;
import esavo.uws.utils.jdbc.UwsDatabaseProperties;
import esavo.uws.utils.jdbc.UwsJdbcManager;
import esavo.uws.utils.jdbc.UwsJdbcSingleton;

public class TapDefaultServiceFactory extends AbstractTAPFactory {
	
	private DefaultTAPLog tapLog = new DefaultTAPLog(); 


	protected TapDefaultServiceFactory(TAPService service, String appid, File storageDir, UwsConfiguration configuration) throws NullPointerException, UwsException {
		super(service, appid, storageDir, configuration);
		
		executor = new ADQLExecutor(service, appid, tapLog);
		scheduler = new UwsDefaultScheduler(appid);
		
		uwsManager = UwsManager.getManager(this);
	}

	@Override
	public ADQLTranslator createADQLTranslator() throws TAPException {
		return new Q3cPgSphereTranslator();
	}

	@Override
	public DBConnection createDBConnection(String jobID, String poolid) throws TAPException {
		UwsJdbcSingleton poolManager = UwsJdbcManager.getInstance(poolid);
		UwsDatabaseProperties databaseProperties = poolManager.getDatabaseProperties();
		try {
			return new TapJDBCPooledConnection(service, poolManager.getConnection(), databaseProperties.getTimeOutMillis());
		} catch (SQLException e) {
			throw new TAPException(e);
		}
	}

	@Override
	public ADQLQueryFactory createQueryFactory(UwsJobOwner owner) throws TAPException{
		return null;
	}

	@Override
	public TAPLog getLogger() {
		return tapLog;
	}
	
}
