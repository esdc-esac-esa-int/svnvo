package esavo.uws.jobs.parameters;

import java.util.Map;

import esavo.uws.UwsException;
import esavo.uws.jobs.UwsJob;
import esavo.uws.jobs.UwsJobPhase;
import esavo.uws.jobs.UwsJobStatusManager;
import esavo.uws.jobs.utils.UwsJobChangeType;
import esavo.uws.jobs.utils.UwsJobUpdateParameterType;
import esavo.uws.utils.UwsParameterValueType;

/**
 * These are the extra job parameters.<br/>
 * Mandatory parameters are {@link UwsJob} attributes.<br/>
 * @author juan.carlos.segovia@sciops.esa.int
 *
 */
public class UwsJobParameters extends UwsParameters {
	
	private UwsJobStatusManager jobStatus;
	
	/**
	 * Constructor
	 */
	public UwsJobParameters(){
	}
	
	/**
	 * Constructor
	 * @param parameters
	 */
	public UwsJobParameters(Map<String, Object> parameters){
		super(parameters);
	}
	
	public UwsJobParameters(UwsJobParameters clone){
		super(clone);
	}

	public void setUwsJobStatusManager(UwsJobStatusManager jobStatus){
		this.jobStatus = jobStatus;
	}
	
	public Object setParameter(String parameterName, Object value) throws UwsException {
		Object o = super.setParameter(parameterName, value);
		if(jobStatus != null){
			jobStatus.updateStatusParameter(parameterName, UwsJobUpdateParameterType.CreateOrUpdateParameter);
		}
		return o;
	}
	
	public Object removeParameter(String parameterName) throws UwsException {
		Object o = super.removeParameter(parameterName);
		if(jobStatus != null){
			jobStatus.updateStatusParameter(parameterName, UwsJobUpdateParameterType.RemovedParameter);
		}
		return o;
	}

	

	public String getParameterStringRepresentation(String parameterName){
		Object o = getParameter(parameterName);
		UwsParameterValueType type = getParameterValueType(o);
		return getParameterStringRepresentation(type, o);
	}
	
	/**
	 * Creates a java object from a database parameter string representation and the database parameter value type
	 * @param type parameter value type (from database)
	 * @param stringRepresentation parameter value (from database)
	 * @return a java object containing the parameter value.
	 */
	public static Object getParameterValue(String type, String stringRepresentation){
		if(stringRepresentation == null){
			return null;
		}
		UwsParameterValueType pvt = UwsParameterValueType.valueOf(type);
		if(pvt == UwsParameterValueType.ExecutionPhase){
			return UwsJobPhase.valueOf(stringRepresentation);
		}
		return UwsParameters.getParameterValue(type, stringRepresentation);
	}
	
	/**
	 * Creates a string representation of a parameter value for database ingestion.
	 * @param type parameter value type (from database)
	 * @param o parameter value.
	 * @return a string representation of a parameter value.
	 */
	public static String getParameterStringRepresentation(UwsParameterValueType type, Object o){
		if(type == UwsParameterValueType.ExecutionPhase){
			return ((UwsJobPhase)o).name();
		}else{
			return UwsParameters.getParameterStringRepresentation(type, o);
		}
	}
	
	/**
	 * Returns a {@link UwsParameterValueType} based on the argument <code>o<code>
	 * @param o object to test
	 * @return a {@link UwsParameterValueType} based on the argument <code>o<code>
	 */
	public static UwsParameterValueType getParameterValueType(Object o){
		if(o instanceof UwsJobPhase){
			return UwsParameterValueType.ExecutionPhase;
		}else{
			return UwsParameters.getParameterValueType(o);
		}
	}
	
	@Override
	public String toString(){
		return "Job parameters: " + getNumParameters();
	}

}
