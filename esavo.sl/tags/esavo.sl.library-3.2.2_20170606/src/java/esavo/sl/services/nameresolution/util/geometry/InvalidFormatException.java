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
package esavo.sl.services.nameresolution.util.geometry;

/**
 * Thrown to indicate that a text has an invalid format.
 * 
 * @author Monica Fernandez - ESAC/ESA - Madrid, Spain
 */
public class InvalidFormatException extends Exception {
	private static final long serialVersionUID = 8615425514039452896L;

	/**
	 * Associated property.
	 */
	private String associatedProperty;
	
	/**
	 * Additional message.
	 */
	private String additionalMessage;
	
	/**
	 * Args to be used by the Exception Handler.
	 */
	private Object[] args;
	
	
	/**
	 * Default constructor.
	 * 
	 * @param prop
	 *            property associated.
	 */
	public InvalidFormatException(String prop) {
		this.associatedProperty = prop;
	}

	/**
	 * Other constructor.
	 * 
	 * @param prop
	 *            property associated.
	 * @param args
	 *            to form a clear message.
	 */
	public InvalidFormatException(String prop, Object[] args) {
		this.associatedProperty = prop;
		this.args = args;
	}
	

	/**
	 * Another constructor.
	 * 
	 * @param prop
	 *            property associated.
	 */
	public InvalidFormatException() {
		
	}

	/**
	 * @return the associatedProperty
	 */
	public String getAssociatedProperty() {
		return associatedProperty;
	}

	/**
	 * @return the additionalMessage
	 */
	public String getAdditionalMessage() {
		return additionalMessage;
	}

	/**
	 * @return the args
	 */
	public Object[] getArgs() {
		return args;
	}	
}
