package net.link.safeonline.sms.ra;

import javax.naming.NamingException;

public interface SMSConnectionFactory {

	public SMSConnection getConnection() throws NamingException;

}
