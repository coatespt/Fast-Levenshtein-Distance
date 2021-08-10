/**
 * 
 */
package a140.exception;

public class BadPropertyException extends Exception {
	private static final long serialVersionUID = 2477077639796366244L;

	public BadPropertyException() {
	}

	public BadPropertyException(String message) {
		super(message);
	}

	public BadPropertyException(Throwable cause) {
		super(cause);
	}

	public BadPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

}
