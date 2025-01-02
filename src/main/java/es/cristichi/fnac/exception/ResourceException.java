package es.cristichi.fnac.exception;

import java.io.IOException;

/**
 * Exception thrown to indicate that any resources have issues (like not found).
 */
public class ResourceException extends IOException {
	/**
	 * @param message Informative human-friendly short description of the error.
	 * @param e Error that provoked this error.
	 */
	public ResourceException(String message, Throwable e) {
		super(message, e);
	}
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
	public ResourceException(String message) {
		super(message);
	}
}
