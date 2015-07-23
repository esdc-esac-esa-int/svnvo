package esavo.uws.output;

import esavo.uws.UwsException;
import esavo.uws.storage.QuotaException;

public interface UwsOutputStreamListener {
	/**
	 * Notifies to the listener that the given number of bytes have been writen in the stream.
	 * @param size <p>Size of the written block in bytes</p>
	 */
	void notifyWrite(long bytes) throws QuotaException, UwsException;
}
