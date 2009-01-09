/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms;

import net.link.safeonline.serial.SerialCommunication;
import net.link.safeonline.serial.exception.SerialCommunicationsException;
import net.link.safeonline.sms.exception.SMSException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GSMModem implements SMSC {

    private static final Logger LOG = LoggerFactory.getLogger(GSMModem.class);

    SerialCommunication         serialComm;


    public GSMModem(String serialPortName) {

        serialComm = new SerialCommunication(serialPortName);
    }

    public void open()
            throws SMSException {

        try {
            serialComm.open();
        } catch (SerialCommunicationsException e) {
            throw new SMSException();
        }
    }

    public void close() {

        serialComm.close();
    }

    public void sendSMS(SMS sms)
            throws SMSException {

        try {
            serialComm.write("ATE1 V1\r\n");
            serialComm.read();
            serialComm.read();
            serialComm.read();
            serialComm.write("AT\r\n");
            serialComm.read();
            serialComm.read();
            serialComm.read();
            byte[] msg = sms.getEncoded();
            serialComm.write("AT+CMGS=" + (msg.length - 1) + "\r\n");
            serialComm.read();
            serialComm.read();
            serialComm.write(new String(Hex.encodeHex(msg)));
            byte[] ctrlz = { 0x1A };
            serialComm.write(ctrlz);
            serialComm.read();
            serialComm.read();
        } catch (Exception e) {
            LOG.debug("Exception while writing SMS to output stream");
            LOG.debug(e.getMessage());
            LOG.debug(e.getStackTrace().toString());
            throw new SMSException();
        }
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            LOG.debug("Exception while sleeping after writing SMS to output stream");
            LOG.debug(e.getMessage());
            LOG.debug(e.getStackTrace().toString());
        }
    }

    public String getSerialPortName() {

        return serialComm.getSerialPortName();
    }

}
