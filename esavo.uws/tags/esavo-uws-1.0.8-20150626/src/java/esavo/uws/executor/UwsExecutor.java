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
