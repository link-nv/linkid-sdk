/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.demo.ticket.TicketOverview;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Stateful
@Name("ticketOverview")
@LocalBinding(jndiBinding = "SafeOnlineTicketDemo/TicketOverviewBean/local")
@SecurityDomain("demo-ticket")
public class TicketOverviewBean implements TicketOverview {

	@Logger
	private Log log;

	@Resource
	private SessionContext sessionContext;

	@RolesAllowed("user")
	public String getUsername() {
		Principal principal = this.sessionContext.getCallerPrincipal();
		String name = principal.getName();
		log.debug("username #0", name);
		return name;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
