package esavo.uws.factory;

import java.io.File;

import esavo.uws.UwsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.executor.UwsDefaultExecutor;
import esavo.uws.scheduler.UwsDefaultScheduler;

/**
 * Default factory. It defines the following handlers (See {@link UwsDefaultFactory} to obtain a list of other handlers defined):
 * <ul>
 * <li>executor</li>
 * <li>scheduler</li>
 * <li>uwsManager<li>
 * </ul>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsSimpleExecutorFactory extends UwsDefaultFactory {

	/**
	 * Constructor
	 * @param appid
	 * @param storageDir
	 * @param configuration
	 */
	public UwsSimpleExecutorFactory(String appid, File storageDir, UwsConfiguration configuration) {
		super(appid, storageDir, configuration);
		executor = new UwsDefaultExecutor(appid);
		scheduler = new UwsDefaultScheduler(appid, executor);
		uwsManager = UwsManager.getManager(this);
	}

}
