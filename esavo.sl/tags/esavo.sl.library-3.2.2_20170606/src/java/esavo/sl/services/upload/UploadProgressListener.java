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
