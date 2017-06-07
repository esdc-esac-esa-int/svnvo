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
package esavo.uws.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UwsUploadResourceLoader implements UwsUploadResource {
	
	private final String name;
	private final File file;

	public UwsUploadResourceLoader(final String name, final File file) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Resource name can not be NULL !");
		}
		this.name = name.trim();
		
		if (file == null) {
			throw new IllegalArgumentException("Resource file can not be NULL !");
		}
		this.file = file;
	}

	@Override
	public InputStream openStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public boolean deleteFile() {
		if (file != null && file.exists()) {
			return file.delete();
		} else {
			return false;
		}
	}
	
	@Override
	public String getName(){
		return name;
	}

}
