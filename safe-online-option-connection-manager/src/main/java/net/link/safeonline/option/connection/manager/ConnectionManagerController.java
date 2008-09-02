package net.link.safeonline.option.connection.manager;

import javax.xml.ws.Endpoint;

import net.link.safeonline.option.connection.manager.ws.impl.ConnectionManagerServiceImpl;
import net.link.safeonline.option.device.OptionDevice;
import net.link.safeonline.option.device.impl.FujiDataCard;

public class ConnectionManagerController {

	OptionDevice optionDevice;

	public ConnectionManagerController() {
		this.optionDevice = new FujiDataCard("/dev/tty.GTM HSDPA Control");

		Endpoint.publish(ConnectionManagerConstants.URL,
				new ConnectionManagerServiceImpl(this.optionDevice));
	}

}
