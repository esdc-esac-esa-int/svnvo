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
import java.util.Iterator;
import java.util.List;

import esavo.tap.formatter.OutputFormat;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;

public interface TAPService {
	
	public static final String VERSION = "1.0.9_20150326";
	
	public static final String CONF_PROP_TAP_WELCOME_MESSAGE = "tap.welcome.message";
	public static final String CONF_PROP_TAP_PROVIDER_NAME = "tap.provider.name";
	public static final String CONF_PROP_TAP_PROVIDER_DESC = "tap.provider.desc";
	
	public TAPFactory getFactory();
	
	public UwsConfiguration getConfiguration();
	
	public TAPSchemaInfo getTapSchemaInfo();
	
	/**
	 * Returns the URL of the xsd file with the xml schema to allow adding "size" attribute 
	 * to TAP metadata xml as a foreign namespace attribute.. 
	 * @return
	 */
	public String getVoDataServiceSchemaExtension();
	
	public String getVoFunctionsSchema();
	
	public Iterator<OutputFormat> getOutputFormats();

	public int[] getRetentionPeriod();

	public int[] getExecutionDuration();

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


}
