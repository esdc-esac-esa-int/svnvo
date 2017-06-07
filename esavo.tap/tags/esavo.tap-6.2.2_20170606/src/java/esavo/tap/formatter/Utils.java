package esavo.tap.formatter;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Utils {
	
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * @param s
	 * @return
	 */
	public static String escapeStringForJson(String s){
		if(s==null)
			return null;
        StringBuffer sb = new StringBuffer();
        escapeStringForJson(s, sb);
        return sb.toString();
    }

    /**
     * @param s - Must not be null.
     * @param sb
     */
    static void escapeStringForJson(String s, StringBuffer sb) {
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
                //Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
					String ss=Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0;k<4-ss.length();k++){
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}//for
	}
	
	public static String getStringRepresentationForJson(Object o, boolean includeQuotes) {
		if (o == null) {
			return "null";
		} else if (o instanceof String) {
			if(includeQuotes){
				return "\"" + escapeStringForJson(((String) o)) + "\"";
			}else{
				return escapeStringForJson(((String) o));
			}
//			if(includeQuotes){
//				return "\"" + escapeQuotes((String) o) + "\"";
//			}else{
//				return escapeQuotes((String) o);
//			}
		} else if (o.getClass().isArray()) {
			return "" + getStringRepresentationFromArray(o);
		} else {
			if(includeQuotes){
				return "\"" + o + "\"";
			}else{
				return o.toString();
			}
		}
	}
	
//	private static String escapeQuotes(String value) {
//		if (value == null) {
//			return null;
//		} else {
//			return value.replaceAll("\"", "\\\\\"");
//		}
//	}
	
	public static String getStringRepresentationFromArray(Object o){
		Object[] oo = (Object[]) objectify(o);
		//deepToString handles 'null' values
		return Arrays.deepToString(oo);
	}
	
	public static Object objectify(Object data){
		if(data == null){
			return null;
		}
		if(!data.getClass().isArray()){
			return data;
		}
		if(data instanceof Object[]){
			return (Object[])data;
		}
		//array of primitive types
		int len = Array.getLength(data);
		Object[] output = new Object[len];
		Object d;
		for(int i = 0; i < len; i++){
			d = Array.get(data,i);
			output[i] = objectify(d);
		}
		return output;
	}


	/**
	 * Escape all quotes of the input string
	 * @param input
	 * @return
	 */
//	public static String escapeQuotes(String input){
//		if(input==null){
//			return null;
//		}
//		
//		String output = input.replaceAll("\"", "\\\"");
//		
//		return output;
//		
//	}
}
