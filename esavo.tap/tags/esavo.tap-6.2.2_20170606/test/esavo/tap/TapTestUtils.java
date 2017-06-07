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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import esavo.uws.utils.test.UwsTestUtils;

public class TapTestUtils {
	
	public static final String DATA_DIR = "/data/";
	
	public static String readDataFromResource(String resource) throws IOException {
		BufferedReader br = getReaderFromResource(resource);
		try{
			String line;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				sb.append(line).append('\n');
			}
			return sb.toString();
		}finally{
			br.close();
		}
	}
	
	public static InputStream getResource(String resource){
		return TapTestUtils.class.getClass().getResourceAsStream(resource);
	}
	
	public static BufferedReader getReaderFromResource(String resource){
		InputStream is = getResource(resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br;
	}

	public static List<String> getSchemasFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return UwsTestUtils.findTextInXml(xml, "/tableset/schema/name");
	}
	
	public static List<String> getTablesFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return UwsTestUtils.findTextInXml(xml, "/tableset/schema/table/name");
	}
	
	public static List<String> getAllColumnsFromTapXmlResponse(String xml) throws IOException{
		if(xml == null){
			return null;
		}
		return UwsTestUtils.findTextInXml(xml, "/tableset/schema/table/column/name");
	}
	
}
