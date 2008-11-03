/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface TicketOverview extends SafeOnlineService, AbstractTicketDataClient {

    void ticketListFactory();
}
