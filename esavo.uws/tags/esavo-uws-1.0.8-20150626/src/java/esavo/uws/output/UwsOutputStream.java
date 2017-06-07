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
package esavo.uws.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.storage.QuotaException;

public class UwsOutputStream extends OutputStream {

	private List<UwsOutputStreamListener> listeners = new ArrayList<UwsOutputStreamListener>();
	
	private int blockSize=1024;  // By default notify each KB 
	private int partialCounter = 0;
	private boolean closed = false;
	
	private OutputStream os;
	private UwsJob job;
	
	public UwsOutputStream(UwsJob job, OutputStream os){
		this.os=os;
		this.job=job;
	}
	
	public UwsOutputStream(int blockSize, OutputStream os){
		this.blockSize=blockSize;
		this.os=os;
	}
			
	/** 
	 * Add a listener to the output stream write process
	 * @param listener
	 */
	public synchronized void addListener(UwsOutputStreamListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Writes a byte in the output stream. If the blocksize for notification is reached, listeners are
	 * notified of the written bytes.
	 * @throws IOException 
	 */
	@Override
	public void write(int b) throws IOException {
		if(closed){
			return;
		}
		if(job.isPhaseAborted()){
			return;
		}
		// TODO Auto-generated method stub
		partialCounter++;
		if(partialCounter>=blockSize){
			try{
				notifyListeners(partialCounter);
			}catch(IOException e){
				closed=true;
				os.close();
				throw e;
			}
			partialCounter=0;
		}
		os.write(b);
	}
	
	@Override
	public void flush() throws IOException{
		if(job.isPhaseAborted()){
			return;
		}
		//TODO
		notifyListeners(partialCounter);
		partialCounter=0;
		os.flush();
	}
	
	@Override
	public void close() throws IOException{
		if(closed) return;
		//TODO
		notifyListeners(partialCounter);
		partialCounter=0;
		listeners.clear();
		closed=true;
		os.close();
		
	}
	
	/**
	 * Notify to all listeners the number of bytes written in the stream
	 * @param bytesWritten
	 * @throws UwsQuotaException 
	 */
	private synchronized void notifyListeners(int bytesWritten) throws QuotaException, IOException{
		if(job.isPhaseAborted()){
			return;
		}
		for(UwsOutputStreamListener listener: listeners){
			try {
				listener.notifyWrite(bytesWritten);
			} catch (UwsException e) {
				throw new IOException(e);
			}
		}
	}
	
}
