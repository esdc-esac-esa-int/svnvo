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
package esavo.uws.utils;

import java.io.IOException;
import java.io.OutputStream;

public class UwsOutputStreamWrapper extends OutputStream {

    private OutputStream out;
    private volatile boolean isActive = true;

    public UwsOutputStreamWrapper(OutputStream out) {
        this.out = out;
    }

    @Override
    public void close() throws IOException {
        if (isActive) {
            isActive = false; // deactivate
            try {
                out.close();
            } finally {
                out = null;
            }
        }
    }

    @Override
    public void flush() throws IOException {
      if(isActive) {
        out.flush();
      }
      // otherwise do nothing (prevent polluting the stream)
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (isActive)
            out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (isActive)
            out.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        if (isActive)
            out.write(b);
    }   

}
