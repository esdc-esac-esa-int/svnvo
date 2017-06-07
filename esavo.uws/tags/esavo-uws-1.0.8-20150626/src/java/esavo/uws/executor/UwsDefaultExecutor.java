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
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.jobs.parameters.UwsJobParameters;

/**
 * Simple executor. See how to write results and error in:
 * <ul>
 * <li>{@link UwsAbstractExecutor#createError(UwsJob, String, esavo.uws.utils.UwsErrorType, String, java.io.InputStream)}</li>
 * <li>{@link UwsAbstractExecutor#createError(UwsJob, String, esavo.uws.utils.UwsErrorType, String, String)}</li>
 * <li>{@link UwsAbstractExecutor#createSingleResult(UwsJob, String, java.io.InputStream, int)}</li>
 * <li>{@link UwsAbstractExecutor#createSingleResult(UwsJob, String, String, int)}</li>
 * </ul>
 * 
 * @author juan.carlos.segovia@sciops.esa.int
 */
public class UwsDefaultExecutor extends UwsAbstractExecutor implements UwsExecutor {
	
	public UwsDefaultExecutor(String appid){
		super(appid);
	}
	
	@Override
	public Object execute(UwsJob job) throws UwsException {
		//dummy executor: write a simple results file: text/plain
		StringBuilder sb = new StringBuilder("OK. Job Parameters:\n");
		int rowsCount = 1;
		UwsJobParameters parameters = job.getParameters();
		if(parameters != null){
			for(String paramName: parameters.getParameterNames()){
				sb.append(paramName).append(": ").append(parameters.getParameter(paramName)).append('\n');
				rowsCount++;
			}
		}
		UwsJobResultMeta result = createSingleResult(job, "text/plain", sb.toString(), rowsCount);
		return result;
	}

	@Override
	public void cancel(UwsJob job) throws UwsException {
		//throw new IllegalArgumentException("Cannot cancel job: " + job);
		job.cancel();
	}
	
	@Override
	public String toString(){
		return "Default executor for application '"+getAppId()+"'";
	}

}
