package esavo.tap.metadata;

public class TAPFunctionsUtils {
	
	
	public static Object getValueFromDB(TAPFunctionArgument.DataType type, String dbValue){
		switch(type){
		case Boolean:
			return Boolean.parseBoolean(dbValue);
		case Short:
			return Short.parseShort(dbValue);
		case Byte:
			return Byte.parseByte(dbValue);
		case Integer:
			return Integer.parseInt(dbValue);
		case Long:
			return Long.parseLong(dbValue);
		case Float:
			return Float.parseFloat(dbValue);
		case Double:
			return Double.parseDouble(dbValue);
		default:
			return dbValue;
		}
	}

}
