package net.link.safeonline.sms;

import net.link.safeonline.sms.exception.SMSException;
import net.link.safeonline.sms.exception.SerialCommunicationsException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GSMModem implements SMSC {

	Log LOG = LogFactory.getLog(GSMModem.class);

	SerialCommunication serialComm;

	public GSMModem(String serialPortName) {
		this.serialComm = new SerialCommunication(serialPortName);
	}

	public void open() throws SMSException {
		try {
			this.serialComm.open();
		} catch (SerialCommunicationsException e) {
			throw new SMSException();
		}
	}

	public void close() {
		this.serialComm.close();
	}

	public void sendSMS(SMS sms) throws SMSException {
		try {
			this.serialComm.write("ATE1 V1\r\n");
			this.serialComm.read();
			this.serialComm.read();
			this.serialComm.read();
			this.serialComm.write("AT\r\n");
			this.serialComm.read();
			this.serialComm.read();
			this.serialComm.read();
			byte[] msg = sms.getEncoded();
			this.serialComm.write("AT+CMGS=" + (msg.length - 1) + "\r\n");
			this.serialComm.read();
			this.serialComm.read();
			this.serialComm.write(new String(Hex.encodeHex(msg)));
			byte[] ctrlz = { 0x1A };
			this.serialComm.write(ctrlz);
			this.serialComm.read();
			this.serialComm.read();
		} catch (Exception e) {
			LOG.debug("Exception while writing SMS to output stream");
			LOG.debug(e.getMessage());
			LOG.debug(e.getStackTrace());
			throw new SMSException();
		}
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			LOG
					.debug("Exception while sleeping after writing SMS to output stream");
			LOG.debug(e.getMessage());
			LOG.debug(e.getStackTrace());
		}
	}

	public String getSerialPortName() {
		return this.serialComm.getSerialPortName();
	}

}
