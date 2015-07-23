package esavo.tap.resource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPMetadataLoaderArgs;
import esavo.tap.metadata.TAPMetadata.OutputType;
import esavo.uws.UwsException;
import esavo.uws.owner.UwsJobOwner;

public class Functions implements TAPResource {

	public static final String RESOURCE_NAME = "functions";
	public static final String[] RESOURCE_ITEMS = {RESOURCE_NAME};
	
	private TAPService service;
	
	@Override
	public void init(TAPService service) {
		this.service = service;
	}

	@Override
	public String getName() {
		return RESOURCE_NAME;
	}
	
	@Override
	public String[] getResourceItems(){
		return RESOURCE_ITEMS;
	}

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response, UwsJobOwner user) throws ServletException, IOException, TAPException, UwsException {
		boolean shareInfoFlag = false;
		if(request.getParameterMap().containsKey(SHARE_INFO_PARAM)){
			shareInfoFlag = Boolean.parseBoolean(request.getParameter(SHARE_INFO_PARAM));
		}
		TAPMetadata tapMetadata;
		try {
			TAPMetadataLoaderArgs args = new TAPMetadataLoaderArgs();
			args.setIncludeShareInfo(shareInfoFlag);
			args.setIncludeAccessibleSharedItems(TAPMetadataLoader.DO_NOT_INCLUDE_ACCESSIBLE_SHARED_ITEMS);
			tapMetadata = TAPMetadataLoader.getTAPMetadata(service, user, args);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return tapMetadata.executeResource(request, response, OutputType.OnlyFunctions, shareInfoFlag);
	}
	
	@Override
	public boolean canHandle(String action) {
		return RESOURCE_NAME.equals(action);
	}
}
