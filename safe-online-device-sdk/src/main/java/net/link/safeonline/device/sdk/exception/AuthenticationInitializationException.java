package net.link.safeonline.device.sdk.exception;

public class AuthenticationInitializationException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;

	public AuthenticationInitializationException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

}
