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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import esavo.tap.metadata.TAPFunctionArgument.DataType;

public class TAPFunction {
	private final String adqlName;

	private String dbName = null;

	private TAPSchema schema = null;

	protected final Map<String, TAPFunctionArgument> arguments;
	
	private String description;
	private DataType returnType;

	public TAPFunction(String functionName) {
		if (functionName == null || functionName.trim().length() == 0) {
			throw new NullPointerException("Missing function name !");
		}
		int indPrefix = functionName.lastIndexOf('.');
		adqlName = (indPrefix >= 0) ? functionName.substring(indPrefix + 1).trim()
				: functionName.trim();
		dbName = adqlName;
		arguments = new LinkedHashMap<String, TAPFunctionArgument>();
	}

	public TAPFunction(String tableName, String description, DataType returnType) {
		this(tableName);
		this.description = description;
		this.returnType = returnType;
	}

	public final String getFullName() {
		if (schema != null) {
			return schema.getName() + "." + adqlName;
		} else {
			return adqlName;
		}
	}

	/**
	 * @return The name.
	 */
	public final String getName() {
		return getADQLName();
	}

	public final String getADQLName() {
		return adqlName;
	}

	public final String getDBName() {
		return dbName;
	}

	public final void setDBName(String name) {
		name = (name != null) ? name.trim() : name;
		dbName = (name == null || name.length() == 0) ? adqlName : name;
	}

	public String getADQLCatalogName() {
		return null;
	}

	public String getDBCatalogName() {
		return null;
	}

	public final String getADQLSchemaName() {
		return schema.getADQLName();
	}

	public final String getDBSchemaName() {
		return schema.getDBName();
	}

	/**
	 * @return The schema.
	 */
	public final TAPSchema getSchema() {
		return schema;
	}

	/**
	 * @param schema
	 *            The schema to set.
	 */
	final void setSchema(TAPSchema schema) {
		this.schema = schema;
	}

	/**
	 * @return The type.
	 */
	public final DataType getReturnType() {
		return returnType;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public final void setReturnType(DataType returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return The description.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	public final void addArgument(TAPFunctionArgument newArgument) {
		if (newArgument != null && newArgument.getName() != null) {
			arguments.put(newArgument.getName(), newArgument);
			newArgument.setFunction(this);
		}
	}

	public final TAPFunctionArgument addArgument(String argumentName, TAPFunctionArgument.DataType type) {
		if (argumentName == null) {
			return null;
		}

		TAPFunctionArgument arg = new TAPFunctionArgument(argumentName, type);
		addArgument(arg);
		return arg;
	}

	public TAPFunctionArgument addArgument(String argumentName, String description, TAPFunctionArgument.DataType type) {
		if (argumentName == null) {
			return null;
		}

		TAPFunctionArgument arg = new TAPFunctionArgument(argumentName, description, type);
		addArgument(arg);
		return arg;
	}

	public TAPFunctionArgument addArgument(String argumentName, String description, TAPFunctionArgument.DataType type,
			Object defaultValue, Object maxValue, Object minValue) {
		if (argumentName == null) {
			return null;
		}

		TAPFunctionArgument arg = new TAPFunctionArgument(argumentName, description, type);
		arg.setDefaultValue(defaultValue);
		arg.setMaxValue(maxValue);
		arg.setMinValue(minValue);
		addArgument(arg);
		return arg;
	}

	public final boolean hasArgument(String name) {
		if (name == null) {
			return false;
		} else {
			String argName = name.toLowerCase();
			return arguments.containsKey(argName);
		}
	}

	public Iterator<TAPFunctionArgument> getArguments() {
		return arguments.values().iterator();
	}

	public final TAPFunctionArgument getArgument(String name) {
		if (name == null) {
			return null;
		} else {
			String argName = name.toLowerCase();
			return arguments.get(argName);
		}
	}

	public final int getNbArguments() {
		return arguments.size();
	}

	public final boolean isEmpty() {
		return arguments.isEmpty();
	}

	public final TAPFunctionArgument removeArgument(String name) {
		if (name == null) {
			return null;
		}

		String argName = name.toLowerCase();

		TAPFunctionArgument removedArg = arguments.remove(argName);
		return removedArg;
	}

	public final void removeAllArguments() {
		arguments.clear();
	}
	
	public String getSignature(){
		//TODO
		return "";
	}

	@Override
	public String toString() {
		return ((schema != null) ? (schema.getName() + ".") : "") + adqlName;
	}

}
