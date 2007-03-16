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

	String getUsername();

	void destroyCallback();

	boolean getReturnTicket();

	void setReturnTicket(boolean returnTicket);

	List<SelectItem> siteListFactory();

	List<SelectItem> dateListFactory();

	String checkOut();

	String confirm();

}
