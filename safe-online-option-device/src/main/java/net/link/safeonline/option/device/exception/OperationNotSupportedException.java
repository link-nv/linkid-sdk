package net.link.safeonline.option.device.exception;

public class OperationNotSupportedException extends OptionDeviceException {

    private static final long serialVersionUID = 1L;


    public OperationNotSupportedException(String message) {

        super(message);
    }

    public OperationNotSupportedException(String message, Throwable cause) {

        super(message, cause);
    }
}
