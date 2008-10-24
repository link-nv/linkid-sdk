package net.link.safeonline.option.device.impl;

import net.link.safeonline.option.device.OptionDevice;
import net.link.safeonline.option.device.exception.ConnectionException;
import net.link.safeonline.option.device.exception.OptionDeviceException;
import net.link.safeonline.serial.SerialCommunication;
import net.link.safeonline.serial.exception.SerialCommunicationsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FujiDataCard implements OptionDevice {

    private final static Logger logger = LoggerFactory.getLogger(FujiDataCard.class);

    private SerialCommunication serial;

    private String              ip;

    private String              gateway;

    private String              dns1;

    private String              dns2;


    /**
     * @return The ip of this {@link FujiDataCard}.
     */
    public String getIp() {

        return this.ip;
    }

    /**
     * @return The gateway of this {@link FujiDataCard}.
     */
    public String getGateway() {

        return this.gateway;
    }

    /**
     * @return The dns1 of this {@link FujiDataCard}.
     */
    public String getDns1() {

        return this.dns1;
    }

    /**
     * @return The dns2 of this {@link FujiDataCard}.
     */
    public String getDns2() {

        return this.dns2;
    }

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
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
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

        logger.debug("Authenticating with PIN code");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CPIN=" + pin + "\r\n");

                if (!this.serial.read().equals("AT+CPIN=" + pin)) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                String response = this.serial.read();

                if (!response.equals("OK") && !response.equals("+CME ERROR: operation not allowed")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    public void connect(String apn, String username, String password) throws OptionDeviceException {

        logger.debug("Connecting");

        cgdcont(apn);

        creg();

        cgreg();

        cgact();

        owancallUp();
        owandata();
    }

    public void disconnect() throws OptionDeviceException {

        owancallDown();
    }

    public String getIMEI() throws OptionDeviceException {

        logger.debug("fetching IMEI");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CGSN\r\n");

                if (!this.serial.read().equals("AT+CGSN")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                String output = this.serial.read();
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
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

    private void cgdcont(String apn) throws OptionDeviceException {

        logger.debug("Setting APN");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CGDCONT=1,\"IP\",\"" + apn + "\"\r\n");

                if (!this.serial.read().equals("AT+CGDCONT=1,\"IP\",\"" + apn + "\"")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                String result = this.serial.read();
                if (result.equals("ERROR"))
                    throw new ConnectionException("Probably already connected");
                if (!result.equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void creg() throws OptionDeviceException {

        logger.debug("Checking CS network registration");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CREG?\r\n");

                if (!this.serial.read().equals("AT+CREG?")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("+CREG: 0,1")) {
                    this.serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void cgreg() throws OptionDeviceException {

        logger.debug("Checking PS network registration");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CGREG?\r\n");

                if (!this.serial.read().equals("AT+CGREG?")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("+CGREG: 0,1")) {
                    this.serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void cgact() throws OptionDeviceException {

        logger.debug("Checking PS network registration");
        synchronized (this.serial) {
            try {
                this.serial.write("AT+CGACT?\r\n");

                if (!this.serial.read().equals("AT+CGACT?")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("+CGACT: 1,0")) {
                    this.serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owancallUp() throws OptionDeviceException {

        logger.debug("Connecting NDIS style");
        synchronized (this.serial) {
            try {
                this.serial.write("AT_OWANCALL=1,1\r\n");

                String input = this.serial.read();
                if (!input.equals("AT_OWANCALL=1,1")) {
                    logger.debug(input);
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owancallDown() throws OptionDeviceException {

        logger.debug("Disconnecting NDIS style");
        synchronized (this.serial) {
            try {
                this.serial.write("AT_OWANCALL=1,0\r\n");

                if (!this.serial.read().equals("AT_OWANCALL=1,0")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!this.serial.read().equals("OK")) {
                    this.serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owandata() throws OptionDeviceException {

        logger.debug("Reading network configuration");
        synchronized (this.serial) {
            try {
                this.serial.write("AT_OWANDATA=1\r\n");

                logger.debug(this.serial.read());
                logger.debug(this.serial.read());
                logger.debug(this.serial.read());

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }
}
