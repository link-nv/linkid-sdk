/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.service;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface TicketService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/TicketServiceBean/local";

    public static final String LOCAL_BINDING = "SafeOnlineTicketDemo/TicketServiceBean/local";


    boolean hasValidPass(String nrn, String from, String to);
}
