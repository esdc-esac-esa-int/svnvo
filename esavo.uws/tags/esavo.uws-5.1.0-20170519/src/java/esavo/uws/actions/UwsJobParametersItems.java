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
package esavo.uws.actions;

import com.oreilly.servlet.MultipartRequest;

import esavo.uws.jobs.parameters.UwsJobParameters;

public class UwsJobParametersItems {
	
	private MultipartRequest multipart;
	private UwsJobParameters jobParameters;
	private UwsUploadResource[] uploadResourceLoaders;
	
	public UwsJobParametersItems(){
		
	}
	
	/**
	 * @return the multipart
	 */
	public MultipartRequest getMultipart() {
		return multipart;
	}
	/**
	 * @param multipart the multipart to set
	 */
	public void setMultipart(MultipartRequest multipart) {
		this.multipart = multipart;
	}
	/**
	 * @return the jobParameters
	 */
	public UwsJobParameters getJobParameters() {
		return jobParameters;
	}
	/**
	 * @param jobParameters the jobParameters to set
	 */
	public void setJobParameters(UwsJobParameters jobParameters) {
		this.jobParameters = jobParameters;
	}
	/**
	 * @return the uploadResourceLoaders
	 */
	public UwsUploadResource[] getUploadResourceLoaders() {
		return uploadResourceLoaders;
	}
	/**
	 * @param uploadResourceLoaders the uploadResourceLoaders to set
	 */
	public void setUploadResourceLoaders(UwsUploadResource[] uploadResourceLoaders) {
		this.uploadResourceLoaders = uploadResourceLoaders;
	}

}
