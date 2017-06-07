package esavo.tap.publicgroup.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPService;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.publicgroup.PublicGroupHandler;
import esavo.tap.publicgroup.PublicGroupItem;
import esavo.tap.publicgroup.PublicGroupUtils;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.factory.UwsFactory;
import esavo.uws.output.UwsOutputResponseHandler;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareItemBase;

/**
 * List the visible public group tables associated to a user (share_schema.accessible_public_group_tables).<br/>
 * User is extracted from the security context.
 * <p>Example
 * <pre><tt>
 * server/PublicGroup?ACTION=list
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class ListUserTables implements PublicGroupHandler {

	public static final String ACTION = "list";

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
		List<PublicGroupItem> items;
		try {
			UwsFactory factory = service.getFactory();
			UwsConfiguration configuration = factory.getConfiguration();
			String publicGroupOwner = configuration.getProperty(TAPService.PUBLIC_GROUP_OWNER_ID_PROPERTY);

			TapJDBCPooledFunctions dbConn = (TapJDBCPooledFunctions)service.getFactory().createDBConnection(
					JDBC_PUBLIC_GROUP_CONNECTION, UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
			items = dbConn.getPublicGroupTables(user.getId());
			
			List<UwsShareItemBase> uwsItems = getUwsItems(items);
			UwsOutputResponseHandler outputHandler = factory.getOutputHandler();
			outputHandler.writeSharedItemsResponse(response, uwsItems, publicGroupOwner);
		} catch (Exception e) {
			PublicGroupUtils.writeError(response, UwsOutputResponseHandler.INTERNAL_SERVER_ERROR, 
					"Cannot obtain accessible public group tables for user '"+user.getId()+"'", e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param items
	 * @return
	 */
	private List<UwsShareItemBase> getUwsItems(List<PublicGroupItem> items){
		List<UwsShareItemBase> uwsItems = new ArrayList<UwsShareItemBase>();
		
		for(PublicGroupItem item:items){
			UwsShareItemBase uwsItem = new UwsShareItemBase();
			uwsItem.setResourceId("");
			uwsItem.setResourceType(TAPMetadataLoader.SHARED_RESOURCE_TYPE_TABLE);
			uwsItem.setTitle(item.getTableSchemaName()+"."+item.getTableName());
			uwsItem.setOwnerid(item.getOwner());
			
			uwsItems.add(uwsItem);
		}
		
		return uwsItems;
		
	}

}
