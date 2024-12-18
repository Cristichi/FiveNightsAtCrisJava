package es.cristichi.fnac.exception;

/**
 * Error thrown by an Animatronic. Animatronics should be prepared to not throw errors in normal circumstances.
 */
public class AnimatronicException extends RuntimeException {
	public AnimatronicException(String message) {
		super(message);
	}
}
