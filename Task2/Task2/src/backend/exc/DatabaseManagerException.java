package backend.exc;

public class DatabaseManagerException extends Exception {

	public DatabaseManagerException() {
	}

	public DatabaseManagerException(String arg0) {
		super(arg0);
	}

	public DatabaseManagerException(Throwable cause) {
		super(cause);
	}

	public DatabaseManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
