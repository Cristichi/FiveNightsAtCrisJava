package es.cristichi.fnac.exception;

/**
 * Exception thrown by {@link es.cristichi.fnac.gui.MenuJC} to indicate that something is not properly set.
 */
public class MenuItemNotFound extends RuntimeException {
	/**
	 * @param message Informative human-friendly short description of the error.
	 */
	public MenuItemNotFound(String message) {
		super(message);
	}
}
