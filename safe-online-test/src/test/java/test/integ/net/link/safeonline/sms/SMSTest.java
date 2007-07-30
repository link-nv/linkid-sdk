package test.integ.net.link.safeonline.sms;

import net.link.safeonline.sms.GSMModem;
import net.link.safeonline.sms.SMS;

import org.junit.Test;

public class SMSTest {

	/**
	 * You need to connect a GSM phone to your system to test this
	 * 
	 */
	@Test
	public void testSMS() {
		GSMModem modem = new GSMModem("/dev/tty.usbmodem0000103D2");
		modem.open();
		modem.sendSMS(new SMS("32477657223", "hellohello"));
		modem.close();
	}

}
