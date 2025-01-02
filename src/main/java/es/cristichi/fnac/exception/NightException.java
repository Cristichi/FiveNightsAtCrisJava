package es.cristichi.fnac.exception;

/**
 * Exception thrown by {@link es.cristichi.fnac.gui.NightJC} to indicate that something is not properly set.
 */
public class NightException extends Exception {
	/**
	 * @param message Informative human-friendly short description of the error.
	 * @param e Error that provoked this error.
	 */
	public NightException(String message, Exception e) {
		super(message, e);
	}
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
	public NightException(String message) {
		super(message);
	}
}
