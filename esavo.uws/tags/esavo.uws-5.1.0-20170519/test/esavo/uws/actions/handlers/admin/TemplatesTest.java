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
package esavo.uws.actions.handlers.admin;

import junit.framework.Assert;

import org.junit.Test;

public class TemplatesTest {
	
//	@Test
//	public void testBase(){
//		String expected;
//		String result;
//		String header;
//		String body;
//		String headerTitle;
//		
//		expected = Templates.BASE.replaceAll("\\{0\\}", "").replaceAll("\\{1\\}", "").replaceAll("\\{2\\}", "");
//		result = Templates.getHtml(null, null);
//		Assert.assertEquals("Basic body", expected, result);
//		
//		header = "<style/>";
//		body = "<p>a</p>";
//		headerTitle = "HT";
//		expected = Templates.BASE.replaceAll("\\{0\\}", headerTitle).replaceAll("\\{1\\}",header).replaceAll("\\{2\\}", body);
//		result = Templates.getHtml(headerTitle, header, body);
//		Assert.assertEquals("Basic body and header", expected, result);
//	}
	
//	@Test
//	public void testError(){
//		String expected;
//		String result;
//		String htmlBody;
//		String errorTitle;
//		String errorMsg;
//		String errorExtraMsg;
//		
//		errorTitle = "Error";
//		errorMsg = "errMsg";
//		errorExtraMsg = null;
//		htmlBody = BasicTemplates.ERROR_BODY.replaceAll("\\{0\\}", errorTitle).replaceAll("\\{1\\}", errorMsg);
//		expected = BasicTemplates.BASE.replaceAll("\\{0\\}",Templates.APPLICATION_ERROR_TITLE).replaceAll("\\{1\\}", "").replaceAll("\\{2\\}", htmlBody);
//		result = BasicTemplates.getErrorMessage(errorTitle, errorMsg, errorExtraMsg);
//		Assert.assertEquals("Basic error", expected, result);
//		
//		errorExtraMsg = "extraMsg";
//		htmlBody = BasicTemplates.ERROR_BODY.replaceAll("\\{0\\}", errorTitle).replaceAll("\\{1\\}", errorMsg+"<br/>"+errorExtraMsg);
//		expected = BasicTemplates.BASE.replaceAll("\\{0\\}",Templates.APPLICATION_ERROR_TITLE).replaceAll("\\{1\\}", "").replaceAll("\\{2\\}", htmlBody);
//		result = BasicTemplates.getErrorMessage(errorTitle, errorMsg, errorExtraMsg);
//		Assert.assertEquals("Basic error", expected, result);
//	}
	
	@Test
	public void testGetPlainText(){
		String expected = "<code></code>";
		Assert.assertEquals("Empty string", "", Templates.getPlainText(null));
		String text = "aaa";
		expected = "<code>"+text+"</code>";
		Assert.assertEquals("Case 1", expected, Templates.getPlainText(text));
		text = "a&b";
		expected = "<code>a&amp;b</code>";
		Assert.assertEquals("Case 2", expected, Templates.getPlainText(text));
	}
	
	@Test
	public void testGetSimpleJsonMsg(){
		String key = "id";
		String value = "value";
		String expected = "{ \""+key+"\": \""+value+"\" }";
		Assert.assertEquals("Simple json", expected, Templates.getSimpleJsonMsg(key, value));
	}
	
	@Test
	public void testEscapeToJson(){
		Assert.assertEquals("Null string", "", Templates.escapeToJson(null));
		String test = "This \"text\" is a \r\ntest";
		String expected = "This 'text' is a <br/>test";
		Assert.assertEquals(expected, Templates.escapeToJson(test));
	}
	
//	@Test
//	public void testJsonUser(){
//		Assert.assertEquals("Null user details", "\"\"", Templates.getJsonUserDetails(null));
//		UserDetails userDetails = new UserDetails();
//		userDetails.setId("id");
//		userDetails.setCurrentSizeDb(5L);
//		userDetails.setCurrentSizeFile(10L);
//		userDetails.setQuotaDb(100L);
//		userDetails.setQuotaFile(500L);
//		userDetails.setRoles(1);
//		String expected = Templates.JSON_USER_LIST_RECORD.replaceAll("\\{0\\}", userDetails.getId()).
//				replaceAll("\\{1\\}", ""+userDetails.getRoles()).
//				replaceAll("\\{2\\}", ""+userDetails.getQuotaDb()).
//				replaceAll("\\{3\\}", ""+userDetails.getCurrentSizeDb()).
//				replaceAll("\\{4\\}", ""+userDetails.getQuotaFile()).
//				replaceAll("\\{5\\}", ""+userDetails.getCurrentSizeFile()).
//				replaceAll("'","");
//		String result = Templates.getJsonUserDetails(userDetails);
//		Assert.assertEquals(expected, result);
//	}
	
//	@Test
//	public void testJsonJobs(){
//		Assert.assertEquals("Null job details", "\"\"", Templates.getJsonJobDetails(null));
//		JobDetails job = new JobDetails();
//		job.setJobid("jobid");
//		job.setOwnerid("ownerid");
//		job.setPhaseid("phaseid");
//		job.setQuery("query");
//		job.setStartTime(1L);
//		job.setEndTime(2L);
//		job.setRelativePath("relative");
//		String expected = Templates.JSON_JOB_LIST_RECORD.replaceAll("\\{0\\}", job.getJobid()).
//				replaceAll("\\{1\\}", job.getOwnerid()).
//				replaceAll("\\{2\\}", job.getPhaseid()).
//				replaceAll("\\{3\\}", ""+job.getStartTime()).
//				replaceAll("\\{4\\}", ""+job.getEndTime()).
//				replaceAll("\\{5\\}", job.getQuery()).
//				replaceAll("\\{6\\}", job.getRelativePath()).
//				replaceAll("'","");
//		String result = Templates.getJsonJobDetails(job);
//		Assert.assertEquals(expected, result);
//		
//		//Error summary
//		expected = Templates.JSON_JOB_ERROR_SUMMARY.replaceAll("\\{0\\}", "").
//				replaceAll("\\{1\\}", "").
//				replaceAll("\\{2\\}", "").
//				replaceAll("'","");
//		result = Templates.getJsonJobErrorSummary(null);
//		JobDetailsError error = new JobDetailsError();
//		error.setErrorSummaryMessage("msg");
//		error.setErrorSummaryType("type");
//		error.setErrorSummaryDetails("details");
//		expected = Templates.JSON_JOB_ERROR_SUMMARY.replaceAll("\\{0\\}", error.getErrorSummaryMessage()).
//				replaceAll("\\{1\\}", error.getErrorSummaryType()).
//				replaceAll("\\{2\\}", error.getErrorSummaryDetails()).
//				replaceAll("'","");
//		result = Templates.getJsonJobErrorSummary(error);
//		Assert.assertEquals(expected, result);
//		
//		//Parameters
//		Assert.assertEquals("Null error parameters", "\"\"", Templates.getJsonJobParameters(null));
//		JobDetailsParameter parameter = new JobDetailsParameter();
//		parameter.setId("paramid");
//		parameter.setDataType("dataType");
//		parameter.setParameterType("paramType");
//		parameter.setStringRepresentation("stringRep");
//		expected = Templates.JSON_JOB_PARAMETER_RECORD.replaceAll("\\{0\\}", parameter.getId()).
//				replaceAll("\\{1\\}", parameter.getParameterType()).
//				replaceAll("\\{2\\}", parameter.getDataType()).
//				replaceAll("\\{3\\}", parameter.getStringRepresentation()).
//				replaceAll("'","");
//		result = Templates.getJsonJobParameters(parameter);
//		Assert.assertEquals(expected, result);
//		
//		//Results
//		Assert.assertEquals("Null error results", "\"\"", Templates.getJsonJobResult(null));
//		JobDetailsResults r = new JobDetailsResults();
//		r.setId("resultid");
//		r.setHref("rHref");
//		r.setMimeType("rMime");
//		r.setType("rType");
//		r.setRows(100L);
//		r.setSize(500L);
//		expected = Templates.JSON_JOB_RESULTS_RECORD.replaceAll("\\{0\\}", r.getId()).
//				replaceAll("\\{1\\}", r.getType()).
//				replaceAll("\\{2\\}", r.getMimeType()).
//				replaceAll("\\{3\\}", r.getHref()).
//				replaceAll("\\{4\\}", ""+r.getRows()).
//				replaceAll("\\{5\\}", ""+r.getSize()).
//				replaceAll("'","");
//		result = Templates.getJsonJobResult(r);
//		Assert.assertEquals(expected, result);
//		
//	}

}
