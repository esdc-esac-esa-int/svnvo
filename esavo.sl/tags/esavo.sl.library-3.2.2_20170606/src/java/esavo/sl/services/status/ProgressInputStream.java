package esavo.sl.services.status;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

public class ProgressInputStream extends FilterInputStream{
	
	public static final long UPDATE_TRIGGER_VALUE = 10000;
		
	private long taskId;
	private long bytesRead = 0;
	private long prevValue = 0;
	private long totalSize;
	private long updateTriggerValue;

	public ProgressInputStream(InputStream in, long totalSize, long taskId) {
		this(in, totalSize, taskId, UPDATE_TRIGGER_VALUE);
	}

	public ProgressInputStream(InputStream in, long totalSize, long taskId, long updateTriggerValue) {
		super(in);
		this.totalSize = totalSize;
		this.taskId = taskId;
		this.updateTriggerValue = updateTriggerValue;
	}

	public synchronized int read() throws IOException {
		int read = getInIfOpen().read();
		if(read < 0){
			//end reached
			updateCounter(-1);
		}else{
			updateCounter(1);
		}
		return read;
	}
		
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		int read = in.read(b, off, len);
		updateCounter(read);
		return read;
	}

	/**
	 * Updates status
	 * @param read number of read bytes or -1 if the end is found.
	 */
	private void updateCounter(int read) {
		if(read < 0){
			endReached();
			return;
		}
		bytesRead += read;

		// Update status each updateTriggerValue (by default: 10000) bytes
		if ((bytesRead - prevValue) > updateTriggerValue) {
			prevValue = bytesRead;
			UwsStatusData status = new UwsStatusData(UwsStatusData.TYPE_PARSE, "" + (100 * bytesRead / totalSize));
			if(taskId >= 0){
				try {
					UwsStatusManager.getInstance().updateStatus(taskId, status);
				} catch (IllegalArgumentException iae) {
					iae.printStackTrace();
				}
			}
		}
	}
	
	private void endReached(){
		UwsStatusData status = new UwsStatusData(UwsStatusData.TYPE_PARSE, "100");
		try{
			UwsStatusManager.getInstance().updateStatus(taskId, status);
			System.out.println("ProgressInputStream: 100%");
		}catch(IllegalArgumentException iae){
			iae.printStackTrace();
		}
	}
		
	/**
	 * Check to make sure that underlying input stream has not been nulled out
	 * due to close; if not return it;
	 * 
	 * @return
	 * @throws IOException
	 */
	private InputStream getInIfOpen() throws IOException {
		InputStream input = in;
		if (input == null) {
			throw new IOException("Stream closed");
		}
		return input;
	}

}