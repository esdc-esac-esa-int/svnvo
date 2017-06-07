package esavoSlServer.myproject;

import java.io.File;

import esavo.sl.tap.actions.EsacTapService;
import esavo.sl.tap.actions.TapServiceFactory;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;

/**
 * Template class, in case you need a factory.<br/>
 * It is instantiated in MyProjectTapService class.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class MyProjectTapFactory extends TapServiceFactory {

	public MyProjectTapFactory(EsacTapService service, String appid, File storageDir, UwsConfiguration configuration)
			throws NullPointerException, UwsException {
		super(service, appid, storageDir, configuration);
	}

}
