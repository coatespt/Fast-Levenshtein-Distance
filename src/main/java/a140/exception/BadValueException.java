package a140.exception;

public class BadValueException extends Exception {
	private static final long serialVersionUID = -8421125768466847790L;

	public BadValueException() {
		super();
	}

	public BadValueException(String message) {
		super(message);
	}

	public BadValueException(Throwable cause) {
		super(cause);
	}

	public BadValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
