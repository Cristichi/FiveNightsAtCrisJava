package es.cristichi.fnac.exception;

public class AnimatronicException extends RuntimeException {
	public AnimatronicException(String message) {
		super(message);
	}
	public AnimatronicException(String message, Throwable e) {
		super(message, e);
	}
}
