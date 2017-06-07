package esavo.tap.formatter;

import java.sql.Time;
import java.util.Date;

/**
 * Json transformer utilities.
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class JsonTransformUtils {

	/**
	 * Returns the suitable java class from the specified type.
	 * @param type query data type
	 * @return the suitable java class from the specified type.
	 */
	public static Class<?> getClassFromDatatype(String type){
		if("byte".equals(type)){
			return Byte.class;
		}else if("short".equals(type)){
			return Short.class;
		}else if("int".equals(type)){
			return Integer.class;
		}else if("long".equals(type)){
			return Long.class;
		}else if("float".equals(type)){
			return Float.class;
		}else if("double".equals(type)){
			return Double.class;
		}else if("time".equals(type)){
			return Time.class;
		}else if("date".equals(type)){
			return Date.class;
		}else{
			return String.class;
		}
	}
	
	/**
	 * Returns the query data type based on the java class type.
	 * @param type java class type.
	 * @return the query data type based on the java class type.
	 */
	public static String getDataType(Class<?> type){
		String name = type.getName();
		if(Byte.class.getName().equals(name)){
			return "byte";
		}else if(Short.class.getName().equals(name)){
			return "short";
		}else if(Integer.class.getName().equals(name)){
			return "int";
		}else if(Long.class.getName().equals(name)){
			return "long";
		}else if(Float.class.getName().equals(name)){
			return "float";
		}else if(Double.class.getName().equals(name)){
			return "double";
		}else if(Time.class.getName().equals(name)){
			return "time";
		}else if(Date.class.getName().equals(name)){
			return "date";
		}else if(name.charAt(0) == '['){
			return getDimensionType(name);
		}else{
			return "char";
		}
	}
	
	/**
	 * Returns '*' if the type is String, '1' in any other case. 
	 * @param type java class type
	 * @return '*' if the type is String, '1' in any other case. 
	 */
	public static String getValueForArraySize(Class<?> type){
		String name = type.getName();
		if(type.getName().equals(String.class.getName())){
			return "\"*\"";
		}else if(name.charAt(0) == '['){
			//return "\"" + getNumDimensions(name) + "\"";
			return "\"*\"";
		}else{
			return "\"1\"";
		}
	}
	
	/**
	 * Returns the number of dimensions of type. Based on the number of '[' chars found.<br/>
	 * I.e. '[' dimension = 1, '[[' dimension = 2, etc.
	 * @param type
	 * @return the number of dimensions of type. type must be something like '[S', etc.
	 */
	public static int getNumDimensions(String type){
		if(type == null){
			return 0;
		}
		if(type.charAt(0) != '['){
			return 0;
		}
		int size = type.length();
		char c;
		int counter = 0;
		for(int i = 0; i < size; i++){
			c = type.charAt(i);
			if(c != '['){
				break;
			}
			counter++;
		}
		return counter;
	}
	
	
	/**
	 * <pre><tt>
	 * boolean               Z
	 * byte                  B
	 * char                  C
	 * class or interface    Lclassname;
	 * double                D
	 * float                 F
	 * int                   I
	 * long                  J
	 * short                 S
	 * <tt></pre> 
	 * @param type
	 * @return
	 */
	public static String getDimensionType(String type){
		if(type == null){
			return null;
		}
		if(type.charAt(0) != '['){
			return null;
		}
		char c = type.charAt(1);
		switch(c){
		case 'Z':
			return "boolean";
		case 'B':
			return "byte";
		case 'L':
			return "char";
		case 'D':
			return "double";
		case 'F':
			return "float";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'S':
			return "short";
		default:
			return "char";
		}
	}

	/**
	 * Converts the string into the suitable object based on the type argument.
	 * @param value query data.
	 * @param type query data type.
	 * @return a suitable java object based on the provided type.
	 */
	public static Object getSuitableData(String value, String type){
		if(value == null || "null".equals(value)){
			return null;
		}
		if("byte".equals(type)){
			return Byte.parseByte(value);
		}else if("short".equals(type)){
			return Short.parseShort(value);
		}else if("int".equals(type)){
			return Integer.parseInt(value);
		}else if("long".equals(type)){
			return Long.parseLong(value);
		}else if("float".equals(type)){
			return Float.parseFloat(value);
		}else if("double".equals(type)){
			return Double.parseDouble(value);
		}else if("time".equals(type)){
			//TODO
			return value;
		}else if("date".equals(type)){
			//TODO
			return value;
		}else{
			return value;
		}
	}


}
