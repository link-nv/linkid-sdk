package net.link.safeonline.sms;

import net.link.safeonline.sms.exception.SMSException;

public interface SMSC {

	void sendSMS(SMS sms) throws SMSException;

}
