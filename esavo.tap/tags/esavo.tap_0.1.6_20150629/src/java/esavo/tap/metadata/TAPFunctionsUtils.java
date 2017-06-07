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

public class TAPFunctionsUtils {
	
	
	public static Object getValueFromDB(TAPFunctionArgument.DataType type, String dbValue){
		switch(type){
		case Boolean:
			return Boolean.parseBoolean(dbValue);
		case Short:
			return Short.parseShort(dbValue);
		case Byte:
			return Byte.parseByte(dbValue);
		case Integer:
			return Integer.parseInt(dbValue);
		case Long:
			return Long.parseLong(dbValue);
		case Float:
			return Float.parseFloat(dbValue);
		case Double:
			return Double.parseDouble(dbValue);
		default:
			return dbValue;
		}
	}

}
