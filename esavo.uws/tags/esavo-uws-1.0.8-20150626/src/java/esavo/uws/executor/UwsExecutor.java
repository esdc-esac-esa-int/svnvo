package esavo.uws.executor;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;

/**
 * To write results:
 * <pre><tt>
 * UwsManager manager = UwsManager.getInstance("appid");
 * UwsStorage storage = uwsManager.getFactory().getStorageManager();
 * 
 * //Create results:
 * UwsJobResultMeta result = new UwsJobResultMeta("resultid");
 * 
 * OutputStream output = storage.getJobResultsDataOutputStream(job, "resultid");
 * //write data (result data)
 * output.write...
 * 
 * //close stream
 * output.close();
 * 
 * //Update result. E.g. size, number of rows...
 * result.setSize(size);
 * result.setRows(numRows);
 * result.setMimeType("application/x-votable+xml");
 * //result.setMimeType("application/xml");
 * //result.setMimeType("text/plain");
 * //result.setMimeType("text/html");
 * //result.setMimeType("application/json");
 * 
 * </tt></pre>
 * 
 * <p>To write error:
 * <pre><tt>
 * UwsManager manager = UwsManager.getInstance("appid");
 * UwsStorage storage = uwsManager.getFactory().getStorageManager();
 * 
 * //Create results:
 * UwsJobErrorSummaryMeta errorSummary = new UwsJobErrorSummaryMeta("ErrorExplanation", UwsErrorType.FATAL);
 * 
 * OutputStream output = storage.getJobErrorDetailsDataOutputStream(job);
 * //write data (error details)
 * output.write...
 * 
 * //close stream
 * output.close();
 * 
 * //Update result. E.g. size, number of rows...
 * errorSummary.setSize(size);
 * errorSummary.setMimeType("application/x-votable+xml");
 * //errorSummary.setMimeType("application/xml");
 * //errorSummary.setMimeType("text/plain");
 * //errorSummary.setMimeType("text/html");
 * //errorSummary.setMimeType("application/json");
 * </tt></pre>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public interface UwsExecutor {
	
	/**
	 * Executes a job.
	 * @param job
	 * @throws InterruptedException
	 * @throws UwsException
	 */
	public Object execute(UwsJob job) throws InterruptedException, UwsException;
	
	/**
	 * Cancels a job.
	 * @param job
	 */
	public void cancel(UwsJob job) throws UwsException;
	
}
