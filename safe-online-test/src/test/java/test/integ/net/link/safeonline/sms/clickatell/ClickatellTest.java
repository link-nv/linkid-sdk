package test.integ.net.link.safeonline.sms.clickatell;

import java.net.URL;

import net.link.safeonline.sms.clickatell.ClickatellChannel;
import net.link.safeonline.sms.clickatell.impl.ClickatellSoapChannel;

import org.junit.Test;

public class ClickatellTest {

	@Test
	public void testClickatellSOAP() throws Exception {

		URL url = new URL("http://api.clickatell.com/soap/webservice.php");
		int apiId = 3150641;
		String username = "dhouthoo";
		String password = "12a70b5379253e39f55def32b9c43e4b";

		String mobileBase = "+32486920746";
		
		ClickatellChannel channel = new ClickatellSoapChannel(url, apiId,
				username, password);

		channel.send(mobileBase, "safe-online clickatell integration test");
		
	}

}
