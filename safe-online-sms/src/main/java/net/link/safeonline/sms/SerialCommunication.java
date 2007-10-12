/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import net.link.safeonline.sms.exception.SerialCommunicationsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class SerialCommunication {

	private static final Log LOG = LogFactory.getLog(SerialCommunication.class);

	private String serialPortName = null;

	private SerialPort serialPort;

	private OutputStream outputStream;

	private BufferedReader br;

	public SerialCommunication(String serialPortName) {
		this.serialPortName = serialPortName;
	}

	@SuppressWarnings("unchecked")
	public void open() throws SerialCommunicationsException {
		boolean portFound = false;

		Enumeration portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {

			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();

			if (portId.getName().equals(this.serialPortName)) {
				LOG.debug("Found port " + this.serialPortName);

				portFound = true;

				try {
					this.serialPort = (SerialPort) portId
							.open("GSMModem", 2000);
				} catch (PortInUseException e) {
					LOG.debug("Port in use.");
					throw new SerialCommunicationsException();
				}

				try {
					this.outputStream = this.serialPort.getOutputStream();
					this.br = new BufferedReader(new InputStreamReader(
							this.serialPort.getInputStream()));
				} catch (IOException e) {
					LOG.debug("Error opening input or output stream");
					throw new SerialCommunicationsException();
				}

				try {
					this.serialPort.setSerialPortParams(9600,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (UnsupportedCommOperationException e) {
					LOG.debug("Could not set port parameters");
					throw new SerialCommunicationsException();
				}

				try {
					this.serialPort.notifyOnOutputEmpty(true);
				} catch (Exception e) {
					LOG.debug("Error setting event notification");
					throw new SerialCommunicationsException();
				}

			}
		}

		if (!portFound) {
			LOG.debug("port " + this.serialPortName + " not found.");
			throw new SerialCommunicationsException();
		}
	}

	public void close() {
		this.serialPort.close();
	}

	public void write(String message) throws SerialCommunicationsException {
		try {
			this.write(message.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			LOG.debug("Unsupported Encoding");
			throw new SerialCommunicationsException();
		}

	}

	public void write(byte[] message) throws SerialCommunicationsException {
		LOG.trace(">> " + new String(message));
		try {
			this.outputStream.write(message);
		} catch (Exception e) {
			LOG.debug("Exception while writing to serial port");
			throw new SerialCommunicationsException();
		}
	}

	public String read() throws SerialCommunicationsException {
		String result = null;
		try {
			result = this.br.readLine();
			LOG.trace("<< " + result);
		} catch (Exception e) {
			LOG.debug("Exception while reading output");
			throw new SerialCommunicationsException();
		}
		return result;
	}

	public String getSerialPortName() {
		return this.serialPortName;
	}

	public void setSerialPortName(String serialPortName) {
		this.serialPortName = serialPortName;
	}
}
