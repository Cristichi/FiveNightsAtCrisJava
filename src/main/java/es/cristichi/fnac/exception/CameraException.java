package es.cristichi.fnac.exception;

/**
 * Exception thrown by Cameras to indicate that the Camera might have an issue.
 */
public class CameraException extends RuntimeException {
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
	public CameraException(String message) {
		super(message);
	}
}
