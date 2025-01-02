package es.cristichi.fnac.exception;

/**
 * Exception thrown by Custom Night to indicate issues with {@link es.cristichi.fnac.obj.anim.AnimatronicDrawing},
 * {@link es.cristichi.fnac.cnight.CustomNightAnimatronic}, or the player's configuration of the Custom Night.
 */
public class CustomNightException extends Exception {
	/**
	 * @param message Informative human-friendly short description of the error.
	 * @param e Error that provoked this error.
	 */
	public CustomNightException(String message, Throwable e) {
		super(message, e);
	}
	
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
    public CustomNightException(String message) {
		super(message);
    }
}
