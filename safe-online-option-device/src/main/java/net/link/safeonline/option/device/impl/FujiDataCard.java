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

        return ip;
    }

    /**
     * @return The gateway of this {@link FujiDataCard}.
     */
    public String getGateway() {

        return gateway;
    }

    /**
     * @return The dns1 of this {@link FujiDataCard}.
     */
    public String getDns1() {

        return dns1;
    }

    /**
     * @return The dns2 of this {@link FujiDataCard}.
     */
    public String getDns2() {

        return dns2;
    }

    public FujiDataCard(String port) throws OptionDeviceException {

        serial = new SerialCommunication(port);
        initialize();
    }

    public void initialize()
            throws OptionDeviceException {

        synchronized (serial) {
            try {
                serial.open();
                serial.write("ATE1 V1\r\n");
                if (!serial.read().equals("ATE1 V1")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
            } catch (SerialCommunicationsException e) {
                serial.close();
                throw new OptionDeviceException("Could not open serial port", e);
            }
        }
    }

    @Override
    public void finalize() {

        synchronized (serial) {
            serial.close();
        }
    }

    public void authenticate(String pin)
            throws OptionDeviceException {

        logger.debug("Authenticating with PIN code");
        synchronized (serial) {
            try {
                serial.write("AT+CPIN=" + pin + "\r\n");

                if (!serial.read().equals("AT+CPIN=" + pin)) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                String response = serial.read();

                if (!response.equals("OK") && !response.equals("+CME ERROR: operation not allowed")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    public void connect(String apn, String username, String password)
            throws OptionDeviceException {

        logger.debug("Connecting");

        cgdcont(apn);

        creg();

        cgreg();

        cgact();

        owancallUp();
        owandata();
    }

    public void disconnect()
            throws OptionDeviceException {

        owancallDown();
    }

    public String getIMEI()
            throws OptionDeviceException {

        logger.debug("fetching IMEI");
        synchronized (serial) {
            try {
                serial.write("AT+CGSN\r\n");

                if (!serial.read().equals("AT+CGSN")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                String output = serial.read();
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
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

    private void cgdcont(String apn)
            throws OptionDeviceException {

        logger.debug("Setting APN");
        synchronized (serial) {
            try {
                serial.write("AT+CGDCONT=1,\"IP\",\"" + apn + "\"\r\n");

                if (!serial.read().equals("AT+CGDCONT=1,\"IP\",\"" + apn + "\"")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                String result = serial.read();
                if (result.equals("ERROR"))
                    throw new ConnectionException("Probably already connected");
                if (!result.equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void creg()
            throws OptionDeviceException {

        logger.debug("Checking CS network registration");
        synchronized (serial) {
            try {
                serial.write("AT+CREG?\r\n");

                if (!serial.read().equals("AT+CREG?")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("+CREG: 0,1")) {
                    serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void cgreg()
            throws OptionDeviceException {

        logger.debug("Checking PS network registration");
        synchronized (serial) {
            try {
                serial.write("AT+CGREG?\r\n");

                if (!serial.read().equals("AT+CGREG?")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("+CGREG: 0,1")) {
                    serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void cgact()
            throws OptionDeviceException {

        logger.debug("Checking PS network registration");
        synchronized (serial) {
            try {
                serial.write("AT+CGACT?\r\n");

                if (!serial.read().equals("AT+CGACT?")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("+CGACT: 1,0")) {
                    serial.close();
                    throw new ConnectionException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owancallUp()
            throws OptionDeviceException {

        logger.debug("Connecting NDIS style");
        synchronized (serial) {
            try {
                serial.write("AT_OWANCALL=1,1\r\n");

                String input = serial.read();
                if (!input.equals("AT_OWANCALL=1,1")) {
                    logger.debug(input);
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owancallDown()
            throws OptionDeviceException {

        logger.debug("Disconnecting NDIS style");
        synchronized (serial) {
            try {
                serial.write("AT_OWANCALL=1,0\r\n");

                if (!serial.read().equals("AT_OWANCALL=1,0")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }
                if (!serial.read().equals("OK")) {
                    serial.close();
                    throw new OptionDeviceException("Unexpected behaviour from datacard");
                }

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }

    private void owandata()
            throws OptionDeviceException {

        logger.debug("Reading network configuration");
        synchronized (serial) {
            try {
                serial.write("AT_OWANDATA=1\r\n");

                logger.debug(serial.read());
                logger.debug(serial.read());
                logger.debug(serial.read());

            } catch (SerialCommunicationsException e) {
                throw new OptionDeviceException("Serial communication error", e);
            }
        }
    }
}
