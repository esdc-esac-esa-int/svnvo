package esavo.uws.actions.handlers;

import java.util.ArrayList;
import java.util.List;

import esavo.uws.UwsException;
import esavo.uws.actions.UwsActionRequest;
import esavo.uws.output.UwsOutputResponseHandler;

public class UwsHandlersUtils {
	
	/**
	 * Returns the specified parameter or raises an exception if the parameter does not exist.
	 * @param actionRequest
	 * @param parameterName
	 * @return
	 * @throws UwsException
	 */
	public static String getExistingParam(UwsActionRequest actionRequest, String parameterName) throws UwsException {
		if(!actionRequest.hasHttpParameter(parameterName)){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Not found required parameter '"+parameterName+"'");
		}
		String value = actionRequest.getHttpParameter(parameterName);
		if(value == null || "".equals(value)){
			throw new UwsException(UwsOutputResponseHandler.BAD_REQUEST, "Required parameter '"+parameterName+"' cannot be null.");
		}
		return value;
	}
	
	
	public static List<String> getList(String param){
		if(param == null){
			return null;
		}
		if("".equals(param.trim())){
			return null;
		}
		List<String> l = new ArrayList<String>();
		String[] items = param.split(",");
		if(items == null){
			return l;
		}
		for(String s: items){
			l.add(s.trim());
		}
		return l;
	}


}
