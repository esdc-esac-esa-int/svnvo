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
package esavo.sl.tap.actions;

import java.util.Properties;

import esavo.sl.dd.requests.DDFunctions;
import esavo.tap.TAPException;
import esavo.tap.service.TapAbstractServiceConnection;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;

public abstract class EsacAbstractTapServiceConnection extends TapAbstractServiceConnection implements EsacTapService {
	public static final String ESAVO_SL_SERVICE_CLASS = "esavo.sl.service.class";

	public static final String ESAVO_SL_SOFTWARE_VERSION_PROP = "esavo.sl.version";
	
	public static final String CAS_URL_BASE = "cas.server.url";

	public static final String TAP_VODATASERVICE_SCHEMA_EXTENSION = "esavo.sl.tap.actions.voDataServiceSchemaExtension";
	
	public static final String TAP_VOFUNCTIONS_SCHEMA = "esavo.sl.tap.actions.voFunctionsSchema";

	public static final String UPLOAD_DIR_PROPERTY = "esavo.sl.fileupload.FileUploadServletConfig.uploadDir";

//	public static final String PUBLIC_GROUP_ID_PROPERTY = "public.group.id";
//	public static final String PUBLIC_GROUP_OWNER_ID_PROPERTY = "public.group.owner";
	
	public static final String VOSPACE_HOST_URL = "vospace.host.url";
	public static final String VOSPACE_PROTOCOL = "vospace.protocol";
	public static final String VOSPACE_TARGET = "vospace.target";
	


	private String casServerUrlBase;
	private String voDataServiceSchemaExtension;
	private String voFunctionsSchema;
	private Properties slProperties = new Properties();
	
	public EsacAbstractTapServiceConnection(String appid) throws UwsException, TAPException{
		super(appid);
		UwsConfiguration configuration = getConfiguration();
		this.casServerUrlBase = configuration.getProperty(CAS_URL_BASE);
		this.voDataServiceSchemaExtension = configuration.getProperty(TAP_VODATASERVICE_SCHEMA_EXTENSION);
		this.voFunctionsSchema = configuration.getProperty(TAP_VOFUNCTIONS_SCHEMA);
		
		
		try{
			loadPropertiesFile(slProperties, "sl.properties");
		}catch(TAPException e){
			slProperties = new Properties();
		}

		
	}
	
	@Override
	public String getCasServerUrlBase() {
		return casServerUrlBase;
	}

	@Override
	public String getVoDataServiceSchemaExtension() {
		return voDataServiceSchemaExtension;
	}
	
	@Override
	public String getVoFunctionsSchema(){
		return voFunctionsSchema;
	}
	
	@Override
	public DDFunctions getDataDistribution(){
		return null;
	}
	
	@Override
	public String getSlVersion() {
		String slVersion = slProperties.getProperty(ESAVO_SL_SOFTWARE_VERSION_PROP);
		if(slVersion==null){
			return "n/a";
		}
		return slVersion;
	}


}
