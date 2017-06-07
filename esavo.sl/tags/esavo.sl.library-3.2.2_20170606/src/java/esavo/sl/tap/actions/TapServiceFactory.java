package esavo.sl.tap.actions;

import java.io.File;

import esavo.sl.tap.adql.EsacADQLQueryFactory;
import esavo.adql.parser.ADQLQueryFactory;
import esavo.adql.translator.ADQLTranslator;
import esavo.adql.translator.Q3cPgSphereTranslator;
import esavo.tap.AbstractTAPFactory;
import esavo.tap.TAPException;
import esavo.tap.log.DefaultTAPLog;
import esavo.tap.log.TAPLog;
import esavo.uws.UwsException;
import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.scheduler.UwsDefaultScheduler;

public class TapServiceFactory extends AbstractTAPFactory {
	
	private DefaultTAPLog tapLog = new DefaultTAPLog();


	public TapServiceFactory(EsacTapService service, String appid, File storageDir, UwsConfiguration configuration) throws NullPointerException, UwsException {
		super(service, appid, storageDir, configuration);
		
		securityManager = new TapSecurityManager(appid);
		executor = new EsdcADQLExecutor(service, appid, tapLog);
		scheduler = new UwsDefaultScheduler(appid);
		
		uwsManager = UwsManager.getManager(this);
	}

	@Override
	public ADQLTranslator createADQLTranslator() throws TAPException {
		return new Q3cPgSphereTranslator();
	}

	@Override
	public ADQLQueryFactory createQueryFactory(UwsJobOwner owner) throws TAPException{
		return new EsacADQLQueryFactory(owner);
	}

	@Override
	public TAPLog getLogger() {
		return tapLog;
	}
	
}
