/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms.ra;

import javax.naming.NamingException;

public interface SMSConnectionFactory {

	public SMSConnection getConnection() throws NamingException;

}
