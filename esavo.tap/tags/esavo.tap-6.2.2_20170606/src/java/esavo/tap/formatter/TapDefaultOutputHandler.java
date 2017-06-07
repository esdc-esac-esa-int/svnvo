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
package esavo.tap.formatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import esavo.tap.TAPException;
import esavo.tap.TAPService;
import esavo.tap.parameters.TAPParameters;
import esavo.uws.UwsException;
import esavo.uws.actions.UwsTemplates;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobResultMeta;
import esavo.uws.output.UwsDefaultOutputHandler;
import esavo.uws.output.UwsOutputUtils;

/**
 * This Output handler generates XML output.<br/>
 * The exception is a job result. It is dumped as it is found.<br/>
 * If you want to write the data in a different way, extend this class and overwrite the required methods.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class TapDefaultOutputHandler extends UwsDefaultOutputHandler {

	TAPService service;
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TapDefaultOutputHandler.class.getName());

	public TapDefaultOutputHandler(final TAPService service, String appid){
		super(appid);
		this.service = service;
	}
	
	
	@Override
	public void writeJobResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException {
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job parameters list", e);
		}
		
		pw.println(UwsTemplates.XML_INIT);
		pw.println(UwsTemplates.XML_JOB_INIT_WITH_NAMESPACES);
		
		
		// Overwrite results mime type with the format requested by user at job creation. 
		List<UwsJobResultMeta> resultsOrig = job.getResults();

		TAPParameters tapParams = new TAPParameters(job.getParameters());
		OutputFormat jobFormat=null;
		try {
			jobFormat = getFormatter(tapParams);
		} catch (TAPException e) {
			throw new UwsException(e);
		}
		
		List<UwsJobResultMeta> resultsCopy = null;
		if(resultsOrig!=null){
			resultsCopy = new ArrayList<UwsJobResultMeta>();
			for(UwsJobResultMeta result:resultsOrig){
				UwsJobResultMeta resultCopy = result.clone();
				if(jobFormat!=null){
						resultCopy.setMimeType(jobFormat.getMimeType());
						resultCopy.setContentEncoding(jobFormat.getContentEncoding());
						resultCopy.setFileExtension(jobFormat.getFileExtension());
				}
				resultsCopy.add(resultCopy);
			}
		}
		
		writeJobResponseInternal(pw, baseUrl, job, resultsCopy);
		
		//end
		pw.println(UwsTemplates.XML_JOB_END);
		pw.flush();
	}

	@Override
	public void writeJobResultListResponse(HttpServletResponse response, String baseUrl, UwsJob job) throws UwsException {
		
		// Overwrite results mime type with the format requested by user at job creation. 
		
		TAPParameters tapParams = new TAPParameters(job.getParameters());
		OutputFormat jobFormat=null;
		try {
			jobFormat = getFormatter(tapParams);
		} catch (TAPException e) {
			throw new UwsException(e);
		}
		
		response.setStatus(OK);
		response.setContentType(MIME_TYPE_XML);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new UwsException("Cannot obtain output handler to write job results list", e);
		}
		pw.println(UwsTemplates.XML_INIT);
		String jobid = job.getJobId();
		String listid = job.getListid();

		
		List<UwsJobResultMeta> resultsOrig = job.getResults();

		List<UwsJobResultMeta> resultsCopy = new ArrayList<UwsJobResultMeta>();
		
		for(UwsJobResultMeta result:resultsOrig){
			UwsJobResultMeta resultCopy = result.clone();
			if(jobFormat!=null){
				resultCopy.setMimeType(jobFormat.getMimeType());
				resultCopy.setContentEncoding(jobFormat.getContentEncoding());
				resultCopy.setFileExtension(jobFormat.getFileExtension());
			}
			resultsCopy.add(resultCopy);
		}
		
		writeJobResultsListResponseInternal(pw, baseUrl, listid, jobid, resultsCopy, true);
	}
	
	
	@Override
	public void writeJobResultDataResponse(HttpServletResponse response, UwsJob job, UwsJobResultMeta result, String outputFormat, InputStream source) throws UwsException {
		if(DirectFormat.DIRECT_FORMAT.equalsIgnoreCase(outputFormat)){
			//DIRECT FORMAT: dump results
			super.writeJobResultDataResponse(response, job, result, outputFormat, source);
		}

		TAPParameters tapParams = new TAPParameters(job.getParameters());
		
		OutputFormat jobFormat=null;
		OutputFormat resultFormat=null;
		OutputFormat requestedFormatter = null;
		try {
			jobFormat = getFormatter(tapParams);
			resultFormat = getFormatter(result.getMimeType());
			if(outputFormat!=null && !outputFormat.trim().isEmpty()){
				requestedFormatter = getFormatter(outputFormat);
			}else{
				requestedFormatter = jobFormat;
			}
		} catch (TAPException e) {
			throw new UwsException(e);
		}
		
		// IF REQUESTED FORMAT IS NULL OR EQUALS THE RESULT FORMAT, RETURN RESULTS DIRECTLY
		if(requestedFormatter.getShortMimeType().equals(resultFormat.getShortMimeType()) ){
			super.writeJobResultDataResponse(response, job, result, outputFormat, source);
			return;
		}
		
		// IF THE RESULTS FORMAT IS NOT THE COMMON FORMAT, SHOW ERROR
		if(!resultFormat.getShortMimeType().equals(TAPParameters.DEFAULT_OUTPUT_FORMAT)){
			throw new UwsException("Conversion of formats not allowed for jobs originally created in ");
		}
		
		// TRANSLATE TO REQUESTED FORMAT
		dumpTranslatedData(response, job, result, requestedFormatter);
	}
	
	private void dumpTranslatedData(HttpServletResponse response, UwsJob job, UwsJobResultMeta result, OutputFormat requestedFormatter) throws UwsException{
		String mimeType = requestedFormatter.getMimeType();
		String contentEncoding = requestedFormatter.getContentEncoding();
		String fileExtension = requestedFormatter.getFileExtension();
		String suitableFileName = UwsOutputUtils.getSuitableContentDispositionFileName(job, result);
		long size=-1;
		
		UwsOutputUtils.writeDataResponseHeader(response, suitableFileName, mimeType, fileExtension, contentEncoding, size);
		
		OutputStream output = null;
		try{
			output = response.getOutputStream();
		}catch(IOException e){
			throw new UwsException("Cannot obtain output handler to write a job result", e);
		}
		try {
			requestedFormatter.translateFromCommonFormat(job, result, output);
		} catch (TAPException e) {
			throw new UwsException("Cannot write result for job", e);
		} finally {
			try {
				output.flush();
			} catch (IOException e) {
				throw new UwsException(
						"Cannot flush output handler when writing a job result",
						e);
			}
		}
	}
	

	protected OutputFormat getFormatter(String format) throws TAPException {
		OutputFormat formatter = service.getFactory().getOutputFormat(format);
		if (formatter == null) {
			throw new TAPException("Impossible to format the result: no formatter has been found for the given MIME type \""+format+"\" (short form) !");
		}

		return formatter;
	}

	protected OutputFormat getFormatter(TAPParameters tapParams) throws TAPException {
		// Search for the corresponding formatter:
		String format = tapParams.getFormat();
		OutputFormat formatter = service.getFactory().getOutputFormat((format == null)?TAPParameters.DEFAULT_OUTPUT_FORMAT:format);
		if (format != null && formatter == null){
			formatter = service.getFactory().getOutputFormat(TAPParameters.DEFAULT_OUTPUT_FORMAT);
		}

		// Format the result:
		if (formatter == null) {
			throw new TAPException("Impossible to format the query result: no formatter has been found for the given MIME type \""+format+"\" and for the default MIME type \""+TAPParameters.DEFAULT_OUTPUT_FORMAT+"\" (short form) !");
		}

		return formatter;
	}
	
	protected OutputFormat getCommonFormatter() throws TAPException {
		// Search for the corresponding formatter:
		OutputFormat formatter = service.getFactory().getOutputFormat(TAPParameters.COMMON_OUTPUT_FORMAT);

		// Format the result:
		if (formatter == null) {
			throw new TAPException("Impossible to format the query result: no formatter has been found for the MIME type \""+TAPParameters.COMMON_OUTPUT_FORMAT+"\" (short form) !");
		}

		return formatter;
	}



}
