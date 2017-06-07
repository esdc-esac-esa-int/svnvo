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

public class TAPValidTables {
	
	private String tapTables;
	private String tapColumns;
	private String tapFunctions;
	private String tapFunctionsArgs;
	
	public TAPValidTables(){
		
	}

	/**
	 * @return the tapTables
	 */
	public String getTapTables() {
		return tapTables;
	}

	/**
	 * @param tapTables the tapTables to set
	 */
	public void setTapTables(String tapTables) {
		this.tapTables = tapTables;
	}

	/**
	 * @return the tapColumns
	 */
	public String getTapColumns() {
		return tapColumns;
	}

	/**
	 * @param tapColumns the tapColumns to set
	 */
	public void setTapColumns(String tapColumns) {
		this.tapColumns = tapColumns;
	}

	/**
	 * @return the tapFunctions
	 */
	public String getTapFunctions() {
		return tapFunctions;
	}

	/**
	 * @param tapFunctions the tapFunctions to set
	 */
	public void setTapFunctions(String tapFunctions) {
		this.tapFunctions = tapFunctions;
	}

	/**
	 * @return the tapFunctionsArgs
	 */
	public String getTapFunctionsArgs() {
		return tapFunctionsArgs;
	}

	/**
	 * @param tapFunctionsArgs the tapFunctionsArgs to set
	 */
	public void setTapFunctionsArgs(String tapFunctionsArgs) {
		this.tapFunctionsArgs = tapFunctionsArgs;
	}

}
