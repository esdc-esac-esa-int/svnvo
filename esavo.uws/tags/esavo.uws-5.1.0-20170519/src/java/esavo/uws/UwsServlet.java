package esavo.uws;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import esavo.uws.actions.UwsActionsManager;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.config.UwsConfigurationManager;
import esavo.uws.factory.UwsAbstractFactory;
import esavo.uws.factory.UwsDefaultFactory;
import esavo.uws.factory.UwsFactory;
import esavo.uws.factory.UwsSimpleExecutorFactory;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.owner.UwsJobsOwnersManager;
import esavo.uws.security.UwsSecurity;
import esavo.uws.utils.UwsUtils;

/**
 * Servlet entry point.<br/>
 * It is necessary to initialize some properties. See {@link UwsConfiguration} class.<br/>
 * Mainly: {@link UwsConfiguration#CONFIG_APP_ID} and {@link UwsConfiguration#CONFIG_PROPERTY_STORAGE}.
 * 
 * <p>If you have more than one servlet, you should obtain your factory by means of a singleton pattern. I.e.
 * <pre><tt>
 * class MyFactoryCreator{
 * private UwsFactory myFactory = null;
 * public static synchronized UwsFactory getFactory("appid", storageDir, configuration){
 *   if(myFactory == null){
 *      myFactory = new [factory_class_that_implements_UwsFactory]("appid", storageDir, configuration);
 *      //you can extend from {@link UwsDefaultFactory} or {@link UwsAbstractFactory} (see {@link UwsSimpleExecutorFactory})
 *   }
 *   return myFactory;
 * }
 * </tt></pre>
 * <p>Then:
 * <pre><tt>
 * UwsConfiguration configuration = ConfigurationManager.getConfiguration("appid");
 * UwsUtils.updateConfiguration(configuration, servletConfig); //to update configuration variables
 * UwsFactory factory = MyFactoryCreator.getFactory("appid", storageDir, configuration);
 * UwsManager manager = UwsManager.getManager(factory);
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private String appid;
	//private UwsManager manager;
	private UwsFactory factory;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = getServletContext();

		appid = UwsUtils.getAppIdFromContext(context, config);
		if(appid == null){
			throw new IllegalArgumentException("Application identifier must be defined. Use configuration variable: '"+UwsConfiguration.CONFIG_APP_ID+"'");
		}
		UwsConfiguration configuration = UwsConfigurationManager.getConfiguration(appid);
		UwsUtils.updateConfiguration(configuration, context);
		UwsUtils.updateConfiguration(configuration, config);

		String storage = configuration.getProperty(UwsConfiguration.CONFIG_PROPERTY_STORAGE);
		if(storage == null){
			throw new IllegalArgumentException("Application storage directory must be defined. Use configuration variable: '"+UwsConfiguration.CONFIG_PROPERTY_STORAGE+"'");
		}
		File storageDir = new File(storage);
		

		//There is only one servlet in this context, it is not necessary to obtain the factory using singleton's.
		//If you have more than one servlet, you must obtain your factory by means of the singleton pattern.
		//The factory defines a UwsManager
		try{
			factory = new UwsSimpleExecutorFactory(appid, storageDir, configuration);
		}catch(UwsException uws){
			throw new ServletException(uws);
		}
		
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//UwsSecurity security = manager.getFactory().getSecurityManager();
		UwsSecurity security = factory.getSecurityManager();
		UwsJobOwner user;
		try {
			user = security.getUser(request);
		} catch (UwsException e1) {
			throw new ServletException("Cannot obtain current user");
		}
		if(user == null){
			//user = UwsJobOwner.ANONYMOUS_OWNER;
			UwsJobsOwnersManager jobsOwnersManager = factory.getJobsOwnersManager(); 
			try {
				user = jobsOwnersManager.loadOrCreateOwner(UwsUtils.ANONYMOUS_USER);
			} catch (UwsException e) {
				throw new ServletException("Cannot obtain user: " + e.getMessage(), e);
			}
		}
		UwsActionsManager actionsManager = UwsActionsManager.getInstance();
		UwsManager manager = factory.getUwsManager(); //you may also use: UwsManager.getInstance();
		actionsManager.executeRequest(manager, user, request, response, null);
	}

}
