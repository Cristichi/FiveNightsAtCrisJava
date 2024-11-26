package es.cristichi.fnac.exception;

import java.io.IOException;

public class ResourceNotFound extends IOException {
	public ResourceNotFound(String message) {
		super(message);
	}
	public ResourceNotFound(String message, Throwable e) {
		super(message, e);
	}
}
