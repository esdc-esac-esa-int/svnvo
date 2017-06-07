package esavo.uws.utils.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import esavo.uws.actions.handlers.admin.AdminHandler;
import esavo.uws.actions.handlers.events.UwsEventQueryHandler;
import esavo.uws.actions.handlers.jobs.UwsJobsHandler;
import esavo.uws.actions.handlers.jobs.UwsJobsMultipleDeleteHandler;
import esavo.uws.actions.handlers.notifications.UwsNotificationsHandler;
import esavo.uws.actions.handlers.scheduler.UwsSchedulerHandler;
import esavo.uws.actions.handlers.share.UwsShareActionHandler;
import esavo.uws.actions.handlers.stats.UwsStatsHandler;
import esavo.uws.actions.handlers.tasks.UwsTasksHandler;
import esavo.uws.actions.handlers.users.UwsGetUsersHandler;
import esavo.uws.jobs.UwsJob;
import esavo.uws.utils.test.database.DummyUwsData;

public class UwsTestUtils {
	
	public static void initDirectory(File f){
		if(f == null){
			return;
		}
		removeDirectory(f);
		f.mkdirs();
	}
	
	public static void removeDirectory(File f){
		if(f == null){
			return;
		}
		if(f.isDirectory()){
			File[] fContent = f.listFiles();
			if(fContent != null){
				for(File fTmp: fContent){
					removeDirectory(fTmp);
				}
			}
		}
		f.delete();
	}
	
	public static void waitForJobFinished(UwsJob job){
		while(!job.isPhaseFinished()){
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				throw new RuntimeException("Wait interrupted", e);
			}
		}
	}

	public static List<String> findTextInXml(String xml, String xPathExpression) throws IOException{
		Document doc = getDocumentFromXml(xml);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nl;
		try {
			nl = (NodeList) xpath.evaluate(xPathExpression, doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new IOException(e);
		}
		
		if(nl == null){
			return null;
		}
		int size = nl.getLength();
		List<String> items = new ArrayList<String>();
		for(int i = 0; i < size; i++){
			String t = ((Element)nl.item(i)).getTextContent();
			items.add(t);
		}
		return items;
		
	}
	
	
	public static Document getDocumentFromXml(String xml) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		} finally{
			is.close();
		}
	}
	
	
	public static void checkList(List<String> expected, List<String> results, String msg) {
		if(expected == null){
			if(results != null){
				Assert.fail(msg + " Expected null results. Found: " + results);
			} else {
				//OK
				return;
			}
		}else{
			Assert.assertEquals(msg + " Wrong number of items.", expected.size(), results.size());
			for(String s: expected){
				if(!results.contains(s)){
					Assert.fail(msg + " '"+s+"' not found in results: " + results);
				}
			}
			//OK
		}
	}
	
	public static DummyUwsData createSingleRowDummyData(List<String> columnNames, String[] singleRowData){
		List<List<String>> rowResults = new ArrayList<List<String>>();
		appendRowData(rowResults, singleRowData);
		return createDummyData(columnNames, rowResults);
	}
	
	public static DummyUwsData createDummyData(List<String> columnNames,
			List<List<String>> results) {
		DummyUwsData d = new DummyUwsData();
		d.setColumnNames(columnNames);
		d.setData(results);
		// check dimensions
		if (results != null) {
			for (List<String> r : results) {
				if (r != null) {
					if (r.size() != columnNames.size()) {
						throw new IllegalArgumentException(
								"Wrong number of columns in query.\nColumn names: "
										+ columnNames + "\nRow data:" + r);
					}
				}
			}
		}
		return d;
	}

	public static DummyUwsData createDummyData(String[] columnNames, String[][] results) {
		DummyUwsData d = new DummyUwsData();
		d.setColumnNames(columnNames);
		d.setData(results);
		// check dimensions
		if (results != null) {
			for (String[] r : results) {
				if (r != null) {
					if (r.length != columnNames.length) {
						throw new IllegalArgumentException(
								"Wrong number of columns in query.\nColumn names: "
										+ columnNames + "\nRow data:" + r);
					}
				}
			}
		}
		return d;
	}
	
	public static DummyUwsData createInsertOrUpdateData(int numRowsAffected){
		DummyUwsData d = new DummyUwsData();
		d.setUpdateAffectedRows(numRowsAffected);
		return d;
	}

	public static void appendRowData(List<List<String>> rows, String[] data){
		List<String> row = new ArrayList<String>();
		for(String d: data){
			row.add(d);
		}
		rows.add(row);
	}

	public static boolean findErrorInHtml(String html) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(html.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try{
			while((line = br.readLine()) != null){
				if(line.indexOf("<title>SERVICE ERROR</title>") >= 0){
					return true;
				}
			}
		} finally{
			br.close();
			is.close();
		}
		return false;
	}
	
	public static boolean findInHtml(String html, String pattern) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(html.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try{
			while((line = br.readLine()) != null){
				if(line.indexOf(pattern) >= 0){
					return true;
				}
			}
		} finally{
			br.close();
			is.close();
		}
		return false;
	}
	
	public static void findInHtml(String html, String[] patterns, String msg) throws IOException {
		for(String p: patterns){
			if(!findInHtml(html, p)){
				Assert.fail(msg + " Not found in HTML: " + p);
			}
		}
	}

	public static boolean findErrorInJSon(String json) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try{
			while((line = br.readLine()) != null){
				if(line.indexOf("\"failed\":") >= 0){
					return true;
				}
				if(line.indexOf("'failed':") >= 0){
					return true;
				}
			}
		} finally{
			br.close();
			is.close();
		}
		return false;
	}
	

	public static boolean findValueInJSon(String json, String value) throws IOException {
		String realValue = value;
		if(value == null){
			realValue = "";
		}
		ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try{
			while((line = br.readLine()) != null){
				if(line.indexOf(",\"value\":\""+realValue+"\"") >= 0){
					return true;
				}
			}
		} finally{
			br.close();
			is.close();
		}
		return false;
	}
	
	
	public static List<String> getNoJobActions(){
		List<String> noJobActions = new ArrayList<String>();
		
		noJobActions.add(UwsShareActionHandler.ACTION_NAME);
		noJobActions.add(UwsEventQueryHandler.ACTION_NAME);
		noJobActions.add(UwsNotificationsHandler.ACTION_NAME);
		noJobActions.add(UwsStatsHandler.ACTION_NAME);
		noJobActions.add(UwsJobsMultipleDeleteHandler.ACTION_NAME);
		noJobActions.add(UwsJobsHandler.ACTION_NAME);
		noJobActions.add(UwsTasksHandler.ACTION_NAME);
		noJobActions.add(AdminHandler.ACTION_NAME);
		noJobActions.add(UwsGetUsersHandler.ACTION_NAME);
		noJobActions.add(UwsSchedulerHandler.ACTION_NAME);
		
		return noJobActions;
	}


}
