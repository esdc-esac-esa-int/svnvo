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
package esavo.tap.parameters;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */


import esavo.tap.TAPException;
import esavo.uws.UwsException;
import esavo.uws.jobs.parameters.UwsJobParameters;

/**
 * This class describes all defined parameters of a TAP request.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2012
 */
public class TAPParameters extends UwsJobParameters {
	
	public static final String DEFAULT_OUTPUT_FORMAT = "votable";
	
	public static final String PARAM_REQUEST = "request";
	public static final String REQUEST_DO_QUERY = "doQuery";
	public static final String REQUEST_GET_CAPABILITIES = "getCapabilities";

	public static final String PARAM_LANGUAGE = "lang";
	public static final String LANG_ADQL = "ADQL";
	public static final String LANG_PQL = "PQL";

	public static final String PARAM_VERSION = "version";
	public static final String VERSION_1_0 = "1.0";

	public static final String PARAM_FORMAT = "format";
	public static final String FORMAT_VOTABLE = "votable";

	public static final String PARAM_MAX_REC = "maxrec";
	public static final int UNLIMITED_MAX_REC = -1;

	public static final String PARAM_QUERY = "query";
	public static final String PARAM_UPLOAD = "upload";

	public static final String PARAM_PROGRESSION = "progression";
	
	public static final String PARAM_JOB_NAME = "jobname";
	public static final String PARAM_JOB_DESCRIPTION = "jobdescription";
	public static final String PARAM_JOB_TYPE = "jobtype";
	public static final String PARAM_SESSION = "session";
	
	//public static final String PARAM_INCLUDE_ACCESSIBLE_SHARED_ITEMS = "includeaccessibleshareditems";


	/**
	 * All the TAP parameters.
	 */
	protected static final String[] TAP_PARAMETERS = new String[]{PARAM_REQUEST, PARAM_LANGUAGE, PARAM_VERSION, PARAM_FORMAT, PARAM_QUERY, PARAM_MAX_REC, PARAM_UPLOAD};

	
	public TAPParameters(UwsJobParameters parameters){
		super(parameters);
	}



	protected final String getStringParam(final String paramName){
		Object o = getParameter(paramName);
		if(o == null){
			return null;
		}else{
			return o.toString();
		}
	}
	
	protected final boolean getBooleanParam(final String paramName){
		Object o = getParameter(paramName);
		if(o == null){
			return false;
		}else{
			return Boolean.parseBoolean(o.toString());
		}
	}

	public final String getRequest(){
		return getStringParam(PARAM_REQUEST);
	}

	public final String getLang(){
		return getStringParam(PARAM_LANGUAGE);
	}

	public final String getVersion(){
		return getStringParam(PARAM_VERSION);
	}

	public final String getFormat(){
		return getStringParam(PARAM_FORMAT);
	}

	public final String getQuery(){
		return getStringParam(PARAM_QUERY);
	}

	public final String getUpload(){
		return getStringParam(PARAM_UPLOAD);
	}

	public final String getName(){
		return getStringParam(PARAM_JOB_NAME);
	}

	public final String getDescription(){
		return getStringParam(PARAM_JOB_DESCRIPTION);
	}

	public final String getType(){
		return getStringParam(PARAM_JOB_TYPE);
	}

	public final String getSession(){
		return getStringParam(PARAM_SESSION);
	}
	
//	public final boolean getIncludeAccessibleSharedItems(){
//		return getBooleanParam(PARAM_INCLUDE_ACCESSIBLE_SHARED_ITEMS);
//	}

	/**
	 * Returns -1 if the parameter {@link #PARAM_MAX_REC} is not defined.
	 * @return
	 * @throws TAPException 
	 */
	public final Integer getMaxRec() throws TAPException{
		Object value = getParameter(PARAM_MAX_REC);
		if (value != null){
			if (value instanceof Integer)
				return (Integer)value;
			else if (value instanceof String){
				try{
					Integer maxRec = Integer.parseInt((String)value);
					//synchronized(params){
						try {
							setParameter(PARAM_MAX_REC, maxRec);
						} catch (UwsException e) {
							throw new TAPException(e);
						}
					//}
					return maxRec;
				}catch(NumberFormatException nfe){ ; }
			}
		}
		//return (Integer)null;
		return -1;
	}

}
