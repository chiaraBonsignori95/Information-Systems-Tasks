package backend.exc;

public class UsernameAlreadyPresentException extends Exception {

	public UsernameAlreadyPresentException() {
		super();
	}

	public UsernameAlreadyPresentException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public UsernameAlreadyPresentException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UsernameAlreadyPresentException(String arg0) {
		super(arg0);
	}

	public UsernameAlreadyPresentException(Throwable arg0) {
		super(arg0);
	}

}
