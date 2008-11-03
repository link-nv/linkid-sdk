/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket;

import javax.ejb.Local;




@Local
public interface TicketLogon extends AbstractTicketDataClient {
    public static final String JNDI_BINDING = "SafeOnlineTicketDemo/TicketLogonBean/local";

    String login();

    String logout();

    String getUsername();
}
