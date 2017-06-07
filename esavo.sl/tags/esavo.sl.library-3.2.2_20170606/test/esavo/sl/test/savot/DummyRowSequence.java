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
package esavo.sl.test.savot;

import java.io.IOException;

import uk.ac.starlink.table.RowSequence;

public class DummyRowSequence implements RowSequence {
	private int index;
	private int size;
	private Object[][] data;
	
	public DummyRowSequence(){
		index = 0;
		size = 0;
	}
	
	public DummyRowSequence(Object[][] data){
		this();
		setData(data);
	}
	
	public void setData(Object[][] data){
		index = 0;
		this.data = data;
		size = 0;
		if(data != null){
			size = data.length;
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getCell(int arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getRow() throws IOException {
		Object[] o = data[index];
		index++;
		return o;
	}

	@Override
	public boolean next() throws IOException {
		return index < size;
	}

}
