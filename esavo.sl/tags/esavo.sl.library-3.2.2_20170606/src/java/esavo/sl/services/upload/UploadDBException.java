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
package esavo.sl.services.upload;

import java.io.IOException;

public class UploadDBException extends IOException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadDBException( String message ) {
        super( message );
    }

    public UploadDBException() {
        super();
    }

    public UploadDBException( String message, Throwable cause ) {
        super( message );
        initCause( cause );
    }

    public UploadDBException( Throwable cause ) {
        super();
        initCause( cause );
    }
}
