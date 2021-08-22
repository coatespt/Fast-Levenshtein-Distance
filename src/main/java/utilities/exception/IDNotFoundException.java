package utilities.exception;


/**
 * Thrown when an ID is sought that does not appear in the filter set. 
 * @author pcoates
 */
public class IDNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public IDNotFoundException() {
		super();
	}
	
	public IDNotFoundException(String s) {
		super(s);
	}
}
