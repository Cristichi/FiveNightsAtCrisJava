package es.cristichi.fnac.exception;

/**
 * Error thrown by {@link es.cristichi.fnac.anim.AnimatronicDrawing} related to the behaviour during Nights
 * or loading.
 */
public class AnimatronicException extends Exception {
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
	public AnimatronicException(String message) {
		super(message);
	}
}
