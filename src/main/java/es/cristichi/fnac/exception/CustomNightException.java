package es.cristichi.fnac.exception;

public class CustomNightException extends Exception {
	public CustomNightException(String message, Throwable e) {
		super(message, e);
	}

    public CustomNightException(String message) {
		super(message);
    }
}
