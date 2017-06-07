package esavo.tap.publicgroup.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.tap.publicgroup.PublicGroupHandler;
import esavo.tap.publicgroup.PublicGroupUtils;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.factory.UwsFactory;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareItemBase;
import esavo.uws.share.UwsShareManager;

/**
 * List all public group tables (UWS share schema share_schema.share_groups)
 * <p>Example
 * <pre><tt>
 * server/PublicGroup?ACTION=list_all
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class ListAllTables implements PublicGroupHandler {

	public static final String ACTION = "list_all";

	@Override
	public String getAction(){
		return ACTION;
	}

	@Override
	public boolean canHandle(String action) {
		return ACTION.equalsIgnoreCase(action);
	}

	@Override
	public void handle(UwsJobOwner user, HttpServletResponse response, TAPService service, Map<String,String> parameters) throws IOException {
		//No synch required. It returns the current status.
		try {
			UwsFactory factory = service.getFactory();
			UwsConfiguration configuration = factory.getConfiguration();
			UwsShareManager shareManager = factory.getShareManager();
			String publicGroupId = configuration.getProperty(TAPService.PUBLIC_GROUP_ID_PROPERTY);
			String publicGroupOwner = configuration.getProperty(TAPService.PUBLIC_GROUP_OWNER_ID_PROPERTY);
			
			String itemTitlePattern = parameters.get(PublicGroupHandler.PARAM_TITLE_PATTERN);
			
			List<UwsShareItemBase> sharedItems = shareManager.getGroupItems(publicGroupId,itemTitlePattern);
			UwsOutputResponseHandler outputHandler = factory.getOutputHandler();
			outputHandler.writeSharedItemsResponse(response, sharedItems, publicGroupOwner);
		} catch (UwsException e) {
			PublicGroupUtils.writeError(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, 
					"Cannot obtain all public group tables", e.getMessage());
		}
	}

}
