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

	public FujiDataCard(String port) throws OptionDeviceException {
		this.serial = new SerialCommunication(port);
		initialize();
	}

	public void initialize() throws OptionDeviceException {
		synchronized (this.serial) {
			try {
				this.serial.open();
				this.serial.write("ATE1 V1\r\n");
				if (!this.serial.read().equals("ATE1 V1")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
				if (!this.serial.read().equals("")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
				if (!this.serial.read().equals("OK")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
			} catch (SerialCommunicationsException e) {
				this.serial.close();
				throw new OptionDeviceException("Could not open serial port", e);
			}
		}
	}

	@Override
	public void finalize() {
		synchronized (this.serial) {
			this.serial.close();
		}
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
		synchronized (this.serial) {
			try {
				this.serial.write("AT+CGSN\r\n");

				if (!this.serial.read().equals("AT+CGSN")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}

				if (!this.serial.read().equals("")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
				String output = this.serial.read();
				if (!this.serial.read().equals("")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
				if (!this.serial.read().equals("OK")) {
					this.serial.close();
					throw new OptionDeviceException(
							"Unexpected behaviour from datacard");
				}
				int comma = output.indexOf(',');
				String IMEI = output.substring(0, comma);
				logger.debug("found IMEI: " + IMEI);
				return IMEI;

			} catch (SerialCommunicationsException e) {
				throw new OptionDeviceException("Serial communication error", e);
			}
		}
	}
}
