package net.link.safeonline.option.device;

import net.link.safeonline.option.device.exception.OptionDeviceException;

public interface OptionDevice {

	public String getIMEI() throws OptionDeviceException;

	public void authenticate(String pin) throws OptionDeviceException;

	public void connect(String apn, String username, String password)
			throws OptionDeviceException;

	public void disconnect() throws OptionDeviceException;

}
