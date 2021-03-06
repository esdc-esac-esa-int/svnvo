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
package esavo.sl.dd.requests;

public class DDException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	
	public DDException(int code, String msg){
		super(msg);
		this.code = code;
	}
	
	public DDException(int code, String msg, Exception cause){
		super(msg, cause);
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

}
