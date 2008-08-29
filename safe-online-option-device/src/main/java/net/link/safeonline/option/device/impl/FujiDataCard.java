package net.link.safeonline.option.device.impl;

import net.link.safeonline.option.device.OptionDevice;
import net.link.safeonline.option.device.exception.OperationNotSupportedException;
import net.link.safeonline.option.device.exception.OptionDeviceException;
import net.link.safeonline.serial.SerialCommunication;
import net.link.safeonline.serial.exception.SerialCommunicationsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FujiDataCard implements OptionDevice {

	private final static Logger logger = LoggerFactory
			.getLogger(FujiDataCard.class);

	SerialCommunication serial;

	String manufacturer;

	String version;

	public FujiDataCard(String port) throws OptionDeviceException {
		this.serial = new SerialCommunication(port);
		try {
			this.serial.open();
			this.serial.write("ATE1 V1\r\n");
			logger.debug(this.serial.read());
		} catch (SerialCommunicationsException e) {
			this.serial.close();
			throw new OptionDeviceException("Could not open serial port", e);
		}
	}

	@Override
	public void finalize() {
		this.serial.close();
	}

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
		logger.debug("fetching IMEI");
		throw new OperationNotSupportedException("not yet implemented");
	}

}
