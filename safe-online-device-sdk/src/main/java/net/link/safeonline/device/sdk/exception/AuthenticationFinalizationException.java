package net.link.safeonline.device.sdk.exception;

public class AuthenticationFinalizationException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;

	public AuthenticationFinalizationException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}
