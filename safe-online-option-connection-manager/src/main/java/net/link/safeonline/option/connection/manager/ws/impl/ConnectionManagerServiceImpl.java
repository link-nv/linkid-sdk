package net.link.safeonline.option.connection.manager.ws.impl;

import javax.jws.WebService;

import net.link.safeonline.option.connection.manager.ws.generated.ConnectionManager;
import net.link.safeonline.option.device.OptionDevice;
import net.link.safeonline.option.device.exception.OptionDeviceException;


@WebService(portName = "ConnectionManagerPort", targetNamespace = "http://ws.manager.connection.option.safeonline.link.net/", serviceName = "ConnectionManagerService")
public class ConnectionManagerServiceImpl implements ConnectionManager {

    OptionDevice optionDevice;


    public ConnectionManagerServiceImpl(OptionDevice optionDevice) {

        this.optionDevice = optionDevice;
    }

    public String getIMEI() {

        String IMEI;

        try {
            IMEI = this.optionDevice.getIMEI();
        } catch (OptionDeviceException e) {
            return null;
        }
        return IMEI;
    }

}
