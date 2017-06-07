package esavo.tap.metadata;

import java.sql.Timestamp;

public class TAPFunctionsUtils {
	
	public static Object getValueFromDB(TAPFunctionArgument.DataType type, String dbValue){
		switch(type){
		case BOOLEAN:
			return Boolean.parseBoolean(dbValue);
		case SMALLINT:
			return Short.parseShort(dbValue);
		case VARBINARY:
			return Byte.parseByte(dbValue);
		case INTEGER:
			return Integer.parseInt(dbValue);
		case BIGINT:
			return Long.parseLong(dbValue);
		case REAL:
			return Float.parseFloat(dbValue);
		case DOUBLE:
			return Double.parseDouble(dbValue);
		case TIMESTAMP:
			return Timestamp.valueOf(dbValue);
		default:
			return dbValue;
		}
	}

}
