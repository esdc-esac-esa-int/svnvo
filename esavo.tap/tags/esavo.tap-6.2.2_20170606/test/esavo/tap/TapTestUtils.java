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
