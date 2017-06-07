package esavo.sl.test.savot;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;

public class DummyStarTable implements StarTable {
	
	private int rowCount;
	private RowSequence rowSequence;
	
	public void setRowCount(int rowCount){
		this.rowCount = rowCount;
	}
	
	public void setRowSequence(RowSequence sequence){
		this.rowSequence = sequence;
	}
	
	@Override
	public void setURL(URL arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setParameter(DescribedValue arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isRandom() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public URL getURL() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public RowSequence getRowSequence() throws IOException {
		return rowSequence;
	}
	
	@Override
	public long getRowCount() {
		return rowCount;
	}
	
	@Override
	public Object[] getRow(long arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List getParameters() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DescribedValue getParameterByName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ColumnInfo getColumnInfo(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public List getColumnAuxDataInfos() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object getCell(long arg0, int arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
