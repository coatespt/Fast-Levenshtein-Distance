/**
 * 
 */
package a140.exception;

/**
 * @author peter
 *
 */
public class InitializationException extends Exception {
	private static final long serialVersionUID = -6873666152862760057L;

	public InitializationException() {
	}

	public InitializationException(String message) {
		super(message);
	}

	public InitializationException(Throwable cause) {
		super(cause);
	}

	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
