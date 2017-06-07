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
package esavo.tap;

import java.util.Collection;
import java.util.List;

import esavo.tap.resource.TAP;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;

public interface TAPService {
	
	public static final String VERSION_PROP = "tap.version";
	
	public static final String CONF_PROP_TAP_WELCOME_MESSAGE = "tap.welcome.message";
	public static final String CONF_PROP_TAP_PROVIDER_NAME = "tap.provider.name";
	public static final String CONF_PROP_TAP_PROVIDER_DESC = "tap.provider.desc";
	
	public static final String CONF_PROP_TAP_USER_UPLOAD_TABLESPACE = "tap.userupload.tablespace";
	
	public static final String PUBLIC_GROUP_ID_PROPERTY = "tap.public.group.id";
	public static final String PUBLIC_GROUP_OWNER_ID_PROPERTY = "tap.public.group.owner";

	
	//POOLS
	public static final String TAP_POOL_JOBS_SYNC_PROPERTY_PREFIX = "tap.jdbc.jobs.sync";
	public static final String TAP_POOL_JOBS_ASYNC_PROPERTY_PREFIX = "tap.jdbc.jobs.async";
	public static final String TAP_JDBC_STORAGE_JOBS_SYNC_POOL_ID = "tap_jobs_sync_pool_storage";
	public static final String TAP_JDBC_STORAGE_JOBS_ASYNC_POOL_ID = "tap_jobs_async_pool_storage";
	public static final int TAP_JDBC_DEFAULT_JOBS_SYNC_CONNECTIONS = 100;
	public static final int TAP_JDBC_DEFAULT_JOBS_ASYNC_CONNECTIONS = 100;
	
	//CONSTANTS
	public static final String TAP_ADQL_FUNCTION_XMATCH = "crossmatch_positional";
	public static final String TAP_ADQL_FUNCTION_STDDEV = "stddev";


	public TAPFactory getFactory();
	
	public UwsConfiguration getConfiguration();
	
	public TAPSchemaInfo getTapSchemaInfo(UwsJobOwner user) throws TAPException;
	
	/**
	 * Returns the URL of the xsd file with the xml schema to allow adding "size" attribute 
	 * to TAP metadata xml as a foreign namespace attribute.. 
	 * @return
	 */
	public String getVoDataServiceSchemaExtension();
	
	public String getVoFunctionsSchema();
	
	//public Iterator<OutputFormat> getOutputFormats();

	public int[] getRetentionPeriod();

	public long[] getExecutionDuration(String listId, UwsJobOwner owner) throws UwsException;

	public int[] getOutputLimit();

	public LimitUnit[] getOutputLimitType();

	public int[] getUploadLimit();

	public LimitUnit[] getUploadLimitType();
	
	public Collection<String> getCoordinateSystems();
	
	public List<String> getUwsJobsToIgnoreParameters();
	
	/**
	 * returns the project Tap name.
	 * @return
	 */
	public String getProjectTapName();
	
	/**
	 * Returns the available schemas for the specified user.
	 * @param owner user
	 * @return a list of schema names that are available for the user.
	 */
	public List<String> getAvailableSchemas(UwsJobOwner owner);

	/**
	 * Can return 'null' which means use default tablespace
	 * @return
	 */
	public String getUserUploadTableSpace();
	
	public TAP getTap();
	
	public String getProperty(String name);

	public String getTapVersion();

}
