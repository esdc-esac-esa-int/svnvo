package esavo.sl.services.upload;

import org.apache.commons.fileupload.ProgressListener;

import esavo.uws.utils.status.UwsStatusData;
import esavo.uws.utils.status.UwsStatusManager;

public class UploadProgressListener implements ProgressListener {

	private long taskId = -1;
	
	private long numBlock = 0;

	private long theBytesRead = 0;
	private long theContentLength = -1;
	private int whichItem = 0;
	private int percentDone = 0;
	private boolean contentLengthKnown = false;
	
	private int fakePercent=0;
	
	// Block size in bytes
	private static final long BLOCK_SIZE = 100000;

	public void update(long bytesRead, long contentLength, int items) {
		if (contentLength > -1) {
			contentLengthKnown = true;
		}
		theBytesRead = bytesRead;
		theContentLength = contentLength;
		whichItem = items;

		long nowNumBlock = bytesRead / BLOCK_SIZE;
		// Only run this code once every 100K
		if (nowNumBlock > numBlock) {
			numBlock = nowNumBlock;
			
			if (contentLengthKnown) {
				percentDone = (int) Math.round(100.00 * bytesRead / contentLength);
			}else{
				// Show something is going on
				if(fakePercent>=90){
					fakePercent=0;
				}else{
					fakePercent+=5;
				}
				percentDone=fakePercent;
			}
			
			
			//StatusUpload statusUpload = new StatusUpload(""+percentDone);
			UwsStatusData statusUpload = new UwsStatusData(UwsStatusData.TYPE_UPLOAD, ""+percentDone);
			if(taskId >= 0){
				try{
					UwsStatusManager.getInstance().updateStatus(taskId, statusUpload); 
				} catch (IllegalArgumentException iae){
					iae.printStackTrace();
					//throw new IOException("Error updating status: " + iae.getMessage(), iae);
				}
			}

			//System.out.println(getMessage());
		}
	}

	public String getMessage() {
		if (theContentLength == -1) {
			return "" + theBytesRead + " of Unknown-Total bytes have been read.";
		} else {
			return "" + theBytesRead + " of " + theContentLength + " bytes have been read (" + percentDone + "% done).";
		}

	}
	
	public long getNumBlock() {
		return numBlock;
	}

	public void setNum100Ks(long numBlock) {
		this.numBlock = numBlock;
	}

	public long getTheBytesRead() {
		return theBytesRead;
	}

	public void setTheBytesRead(long theBytesRead) {
		this.theBytesRead = theBytesRead;
	}

	public long getTheContentLength() {
		return theContentLength;
	}

	public void setTheContentLength(long theContentLength) {
		this.theContentLength = theContentLength;
	}

	public int getWhichItem() {
		return whichItem;
	}

	public void setWhichItem(int whichItem) {
		this.whichItem = whichItem;
	}

	public int getPercentDone() {
		return percentDone;
	}

	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}

	public boolean isContentLengthKnown() {
		return contentLengthKnown;
	}

	public void setContentLengthKnown(boolean contentLengthKnown) {
		this.contentLengthKnown = contentLengthKnown;
	}
	
	public void setTaskId(long taskId){
		this.taskId=taskId;
	}

}