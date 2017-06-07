package esavo.sl.services.nameresolution.util.geometry;

/**
 * Thrown to indicate that a text has an invalid format.
 * 
 * @author Monica Fernandez - ESAC/ESA - Madrid, Spain
 */
public class InvalidFormatException extends Exception {
	private static final long serialVersionUID = 8615425514039452896L;

	/**
	 * Associated property.
	 */
	private String associatedProperty;
	
	/**
	 * Additional message.
	 */
	private String additionalMessage;
	
	/**
	 * Args to be used by the Exception Handler.
	 */
	private Object[] args;
	
	
	/**
	 * Default constructor.
	 * 
	 * @param prop
	 *            property associated.
	 */
	public InvalidFormatException(String prop) {
		this.associatedProperty = prop;
	}

	/**
	 * Other constructor.
	 * 
	 * @param prop
	 *            property associated.
	 * @param args
	 *            to form a clear message.
	 */
	public InvalidFormatException(String prop, Object[] args) {
		this.associatedProperty = prop;
		this.args = args;
	}
	

	/**
	 * Another constructor.
	 * 
	 * @param prop
	 *            property associated.
	 */
	public InvalidFormatException() {
		
	}

	/**
	 * @return the associatedProperty
	 */
	public String getAssociatedProperty() {
		return associatedProperty;
	}

	/**
	 * @return the additionalMessage
	 */
	public String getAdditionalMessage() {
		return additionalMessage;
	}

	/**
	 * @return the args
	 */
	public Object[] getArgs() {
		return args;
	}	
}
