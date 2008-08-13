/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;


@Local
public interface TicketBuy {

    /*
     * Field accessors.
     */

    String getFrom();

    void setFrom(String from);

    String getTo();

    void setTo(String to);

    String getValidUntil();

    void setValidUntil(String validUntil);

    boolean getReturnTicket();

    void setReturnTicket(boolean returnTicket);

    /*
     * Factories
     */

    List<SelectItem> siteListFactory();

    List<SelectItem> dateListFactory();

    /*
     * Actions
     */

    String checkOut();

    String confirm();

    /*
     * Lifecycle
     */

    void destroyCallback();
}
