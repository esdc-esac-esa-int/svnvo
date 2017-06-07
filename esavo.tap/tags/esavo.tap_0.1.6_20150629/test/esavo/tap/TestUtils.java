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
package esavo.tap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import esavo.tap.test.database.DummyData;
import junit.framework.Assert;

public class TestUtils {
	
	public static List<String> getSchemasFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return findTextInXml(xml, "/tableset/schema/name");
	}
	
	public static List<String> getTablesFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return findTextInXml(xml, "/tableset/schema/table/name");
	}
	
	public static List<String> getAllColumnsFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return findTextInXml(xml, "/tableset/schema/table/column/name");
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
	
	public static DummyData createSingleRowDummyData(List<String> columnNames, String[] singleRowData){
		List<List<String>> rowResults = new ArrayList<List<String>>();
		appendRowData(rowResults, singleRowData);
		return createDummyData(columnNames, rowResults);
	}
	
	public static DummyData createDummyData(List<String> columnNames,
			List<List<String>> results) {
		DummyData d = new DummyData();
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

	public static DummyData createDummyData(String[] columnNames, String[][] results) {
		DummyData d = new DummyData();
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
	
	public static DummyData createInsertOrUpdateData(int numRowsAffected){
		DummyData d = new DummyData();
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

}
