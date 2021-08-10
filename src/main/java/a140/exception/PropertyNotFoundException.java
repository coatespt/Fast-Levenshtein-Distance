package a140.exception;
public class PropertyNotFoundException extends Exception {
	private static final long serialVersionUID = 2477077639796366244L;

	public PropertyNotFoundException() {
	}

	public PropertyNotFoundException(String message) {
		super(message);
	}

	public PropertyNotFoundException(Throwable cause) {
		super(cause);
	}

	public PropertyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
