package es.cristichi.fnac.exception;

public class AssetNotFound extends RuntimeException {
	public AssetNotFound(String message, Throwable e) {
		super(message, e);
	}
}
