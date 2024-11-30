package es.cristichi.fnac.exception;

import java.io.IOException;

public class ResourceException extends IOException {
	public ResourceException(String message) {
		super(message);
	}
	public ResourceException(String message, Throwable e) {
		super(message, e);
	}
}
