package es.cristichi.fnac.exception;

public class CameraException extends RuntimeException {
	public CameraException(String message) {
		super(message);
	}
	public CameraException(String message, Throwable e) {
		super(message, e);
	}
}
