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
package esavo.uws.jobs.parameters;

import java.util.SortedMap;

import esavo.uws.utils.UwsParameterValueType;

public class UwsJobOwnerParameters extends UwsParameters {
	
	/**
	 * Constructor
	 */
	public UwsJobOwnerParameters(){
		super();
	}
	
	/**
	 * Constructor
	 * @param parameters
	 */
	public UwsJobOwnerParameters(SortedMap<String,Object> parameters){
		super(parameters);
	}
	
	/**
	 * Creates a java object from a database parameter string representation and the database parameter value type
	 * @param type parameter value type (from database)
	 * @param stringRepresentation parameter value (from database)
	 * @return a java object containing the parameter value.
	 */
	public static Object getParameterValue(String type, String stringRepresentation){
		return UwsParameters.getParameterValue(type, stringRepresentation);
	}
	
	/**
	 * Creates a string representation of a parameter value for database ingestion.
	 * @param type parameter value type (from database)
	 * @param o parameter value.
	 * @return a string representation of a parameter value.
	 */
	public static String getParameterStringRepresentation(UwsParameterValueType type, Object o){
		return UwsParameters.getParameterStringRepresentation(type, o);
	}
	
	/**
	 * Returns a {@link UwsParameterValueType} based on the argument <code>o<code>
	 * @param o object to test
	 * @return a {@link UwsParameterValueType} based on the argument <code>o<code>
	 */
	public static UwsParameterValueType getParameterValueType(Object o){
		return UwsParameters.getParameterValueType(o);
	}

	@Override
	public String toString(){
		return "Owner parameters: " + getNumParameters();
	}

}
