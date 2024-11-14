package es.cristichi.fnac.exception;

import java.io.IOException;

public class AssetNotFound extends IOException {
	public AssetNotFound(String message) {
		super(message);
	}
	public AssetNotFound(String message, Throwable e) {
		super(message, e);
	}
}
