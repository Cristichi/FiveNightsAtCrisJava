package es.cristichi.fnac.exception;

public class NightException extends Exception {
	public NightException(String message) {
		super(message);
	}
	public NightException(String message, Exception e) {
		super(message, e);
	}
}
