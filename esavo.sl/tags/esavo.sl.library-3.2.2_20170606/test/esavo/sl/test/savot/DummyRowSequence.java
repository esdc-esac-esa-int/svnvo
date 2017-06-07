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
