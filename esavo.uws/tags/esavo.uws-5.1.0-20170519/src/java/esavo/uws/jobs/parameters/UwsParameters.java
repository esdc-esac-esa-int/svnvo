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

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import esavo.uws.UwsException;
import esavo.uws.utils.UwsParameterValueType;

public class UwsParameters {
	
	public static final String PARAMETER_COMMON = "COMMON";
//	public static final String PARAMETER_ADDITIONAL = "ADDITIONAL";
//	public static final String PARAMETER_CONTROLLER = "CONTROLLER";
//	public static final String PARAMETER_EXPECTED = "EXPECTED";
	

	private SortedMap<String,Object> parameters;
	
	public UwsParameters(){
		this((SortedMap<String,Object>)null);
	}
	
	public UwsParameters(UwsParameters clone){
		this(clone.parameters);
	}
	
	public UwsParameters(SortedMap<String,Object> parameters){
		if(parameters == null){
			this.parameters = new TreeMap<String, Object>();
		} else {
			this.parameters = parameters;
		}
	}
	
	public int getNumParameters(){
		return parameters.size();
	}
	
	public void dumpParameters(StringBuilder sb){
		Object o;
		for(Entry<String, Object> e: parameters.entrySet()){
			o = e.getValue();
			sb.append(e.getKey()).append(": ").append(o == null ? "null" : o.toString());
		}
	}
	
	public Map<String, Object> getParameters(){
		return parameters;
	}
	
	public Set<String> getParameterNames(){
		return parameters.keySet();
	}
	
	public Object setParameter(String parameterName, Object value) throws UwsException {
		return parameters.put(parameterName, value);
	}
	
	public Object getParameter(String parameterName){
		return parameters.get(parameterName);
	}
	
	public boolean containsParameter(String parameterName){
		return parameters.containsKey(parameterName);
	}
	
	public Object removeParameter(String parameterName) throws UwsException {
		return parameters.remove(parameterName);
	}
	
	/**
	 * Can generate ClassCastException
	 * @param id
	 * @return
	 */
	public String getStringParameter(String parameterName){
		return (String)getParameter(parameterName);
	}

	/**
	 * Can generate ClassCastException
	 * @param id
	 * @return
	 */
	public Long getLongParameter(String parameterName){
		return (Long)getParameter(parameterName);
	}

	/**
	 * Can generate ClassCastException
	 * @param id
	 * @return
	 */
	public Integer getIntParameter(String parameterName){
		return (Integer)getParameter(parameterName);
	}

	/**
	 * Creates a java object from a database parameter string representation and the database parameter value type
	 * @param type parameter value type (from database)
	 * @param stringRepresentation parameter value (from database)
	 * @return a java object containing the parameter value.
	 */
	protected static Object getParameterValue(String type, String stringRepresentation){
		if(stringRepresentation == null){
			return null;
		}
		UwsParameterValueType pvt = UwsParameterValueType.valueOf(type);
		switch(pvt){
		case Boolean:
			return Boolean.parseBoolean(stringRepresentation);
		case Long:
			return Long.parseLong(stringRepresentation);
		case Integer:
			return Integer.parseInt(stringRepresentation);
		case Date:
			if(stringRepresentation == null || "".equals(stringRepresentation) || "0".equals(stringRepresentation)){
				return null;
			}else{
				return new Date(Long.parseLong(stringRepresentation));
			}
		default:
			return stringRepresentation;
		}
	}
	
	/**
	 * Creates a string representation of a parameter value for database ingestion.
	 * @param type parameter value type (from database)
	 * @param o parameter value.
	 * @return a string representation of a parameter value.
	 */
	protected static String getParameterStringRepresentation(UwsParameterValueType type, Object o){
		switch(type){
		case Boolean:
			return o == null ? "false":o.toString();
		case Date:
			return o == null ? "0" : ""+((Date)o).getTime();
		case Integer:
		case Long:
			return o == null ? "0" : o.toString();
		case String:
		default:
			return o == null ? null : o.toString();
		}
	}
	
	/**
	 * Returns a {@link UwsParameterValueType} based on the argument <code>o<code>
	 * @param o object to test
	 * @return a {@link UwsParameterValueType} based on the argument <code>o<code>
	 */
	protected static UwsParameterValueType getParameterValueType(Object o){
		if(o instanceof Boolean){
			return UwsParameterValueType.Boolean;
		} else if (o instanceof Long) {
			return UwsParameterValueType.Long;
		} else if (o instanceof Integer) {
			return UwsParameterValueType.Integer;
		} else if (o instanceof Date) {
			return UwsParameterValueType.Date;
		} else {
			return UwsParameterValueType.String;
		}
	}
	

}
