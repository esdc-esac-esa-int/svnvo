package esavo.tap.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TapSql {
	
	private static final String SQL_CREATE_FILE = "tap_create.sql";
	private static final String SQL_INSERT_FILE = "tap_insert.sql";
	private static final String SQL_DROP_FILE = "tap_drop.sql";
	
	private static final String TAP_SCHEMA = "tap_schema";
	
	private static final String TAP_SCHEMA_PATTERN = " " + TAP_SCHEMA + "\\.";
	private static final String TAP_SCHEMA_PATTERN2 = " " + TAP_SCHEMA + ";";
	private static final String TAP_SCHEMA_VALUE = "'" + TAP_SCHEMA + "'";
	
	
	public static String getCreateTapSchemaSql(String tapSchemaName) throws IOException{
		StringBuilder sb = new StringBuilder();
		createSchema(tapSchemaName, sb);
		insertIntoSchema(tapSchemaName, sb);
		return sb.toString();
	}
	
	private static String createSchema(String tapSchemaName, StringBuilder sb) throws IOException {
		BufferedReader br = getSqlFile(SQL_CREATE_FILE);
		try{
			String line;
			String tmp;
			while((line = br.readLine()) != null){
				if(line.trim().isEmpty()){
					continue;
				}
				if(line.startsWith("--")){
					continue;
				}
				tmp = line.replaceAll(TAP_SCHEMA_PATTERN, " " + tapSchemaName + ".");
				tmp = tmp.replaceAll(TAP_SCHEMA_PATTERN2, " " + tapSchemaName + ";");
				sb.append(tmp).append('\n');
			}
			return sb.toString();
		}finally{
			br.close();
		}
	}
	
	private static void insertIntoSchema(String tapSchemaName, StringBuilder sb) throws IOException {
		BufferedReader br = getSqlFile(SQL_INSERT_FILE);
		try{
			String line;
			String tmp;
			while((line = br.readLine()) != null){
				if(line.trim().isEmpty()){
					continue;
				}
				if(line.startsWith("--")){
					continue;
				}
				tmp = line.replaceAll(TAP_SCHEMA_PATTERN, " " + tapSchemaName + ".");
				tmp = tmp.replaceAll(TAP_SCHEMA_PATTERN2, " " + tapSchemaName + ";");
				tmp = tmp.replaceAll(TAP_SCHEMA_VALUE, "'" + tapSchemaName + "'");
				sb.append(tmp).append('\n');
			}
			
		}finally{
			br.close();
		}
	}

	public static String getDeleteTapSchemaSql(String tapSchemaName) throws IOException {
		BufferedReader br = getSqlFile(SQL_DROP_FILE);
		try{
			String line;
			String tmp;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				if(line.trim().isEmpty()){
					continue;
				}
				if(line.startsWith("--")){
					continue;
				}
				tmp = line.replaceAll(TAP_SCHEMA_PATTERN, " " + tapSchemaName + ".");
				tmp = tmp.replaceAll(TAP_SCHEMA_PATTERN2, " " + tapSchemaName + ";");
				sb.append(tmp).append('\n');
			}
			return sb.toString();
		}finally{
			br.close();
		}
	}
	
	private static BufferedReader getSqlFile(String file) throws IOException {
		InputStream is = TapSql.class.getResourceAsStream(file);
		if(is == null){
			throw new IOException("Cannot find file '"+file+"'");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br;
	}

}
