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
package esavo.uws.jobs.utils;

import java.util.List;

import esavo.uws.jobs.UwsJobResultMeta;

public class UwsJobRestartArgs extends UwsJobInitArgs {
	
	private String jobid;
	private String locationid;
	private List<UwsJobResultMeta> results;

	public UwsJobRestartArgs() {

	}

	/**
	 * @return the jobid
	 */
	public String getJobid() {
		return jobid;
	}

	/**
	 * @param jobid the jobid to set
	 */
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	/**
	 * @return the locationid
	 */
	public String getLocationid() {
		return locationid;
	}

	/**
	 * @param locationid the locationid to set
	 */
	public void setLocationid(String locationid) {
		this.locationid = locationid;
	}

	/**
	 * @return the results
	 */
	public List<UwsJobResultMeta> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(List<UwsJobResultMeta> results) {
		this.results = results;
	}

}
