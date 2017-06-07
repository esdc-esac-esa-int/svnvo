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
package esavo.tap.metadata;

public class TAPFunctionArgument {
	
	public enum DataType{
		BOOLEAN,
		SMALLINT,
		INTEGER,
		BIGINT,
		REAL,
		DOUBLE,
		VARBINARY,
		CHAR,
		VARCHAR,
		BINARY,
		TIMESTAMP
	}

	private String name;
	private DataType type;
	private String description;
	private TAPFunction function;
	
	private Object defaultValue;
	private Object maxValue;
	private Object minValue;
	
	public TAPFunctionArgument(String name, DataType type){
		this(name, null, type);
	}
	
	public TAPFunctionArgument(String name, String description, DataType type){
		this.name = name;
		this.description = description;
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public DataType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(DataType type) {
		this.type = type;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the function
	 */
	public TAPFunction getFunction() {
		return function;
	}

	/**
	 * @param function the function to set
	 */
	public void setFunction(TAPFunction function) {
		this.function = function;
	}

	/**
	 * @return the defaultValue
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the maxValue
	 */
	public Object getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(Object maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the minValue
	 */
	public Object getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(Object minValue) {
		this.minValue = minValue;
	}

}
