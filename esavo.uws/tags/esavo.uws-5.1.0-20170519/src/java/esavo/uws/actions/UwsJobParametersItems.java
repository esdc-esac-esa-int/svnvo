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
