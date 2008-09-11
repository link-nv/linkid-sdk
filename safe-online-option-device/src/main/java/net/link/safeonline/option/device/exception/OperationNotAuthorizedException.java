package net.link.safeonline.option.device.exception;

public class OperationNotAuthorizedException extends OptionDeviceException {

	private static final long serialVersionUID = 1L;

	public OperationNotAuthorizedException(String message) {
		super(message);
	}

	public OperationNotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

}
