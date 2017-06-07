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
package esavo.sl.services.transform.handlers;

import java.io.IOException;

/**
 * This class is used to specify an end of processing.<br/>
 * When the user specifies a page size (i.e. a number of rows to dump) it is necessary to tell the parser to stop processing the data.
 * Nevertheless, parsers do not have a method to request to stop processing.
 * This exception is raised by the handlers so the high level functionality can determine whether a real exception has been raised or
 * a stop processing is requested. 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TransformEndProcessException extends IOException{

	private static final long serialVersionUID = 1L;

}
