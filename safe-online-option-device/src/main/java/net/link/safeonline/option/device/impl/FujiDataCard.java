package net.link.safeonline.option.device.impl;

import net.link.safeonline.option.device.OptionDevice;
import net.link.safeonline.option.device.exception.OperationNotSupportedException;
import net.link.safeonline.option.device.exception.OptionDeviceException;

public class FujiDataCard implements OptionDevice {

	public void authenticate(String pin) throws OptionDeviceException {
		throw new OperationNotSupportedException("not yet implemented");
	}

	public void connect(String apn, String username, String password)
			throws OptionDeviceException {
		throw new OperationNotSupportedException("not yet implemented");
	}

	public void disconnect() throws OptionDeviceException {
		throw new OperationNotSupportedException("not yet implemented");
	}

	public String getIMEI() throws OptionDeviceException {
		throw new OperationNotSupportedException("not yet implemented");
	}

}
