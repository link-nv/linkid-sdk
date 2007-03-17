/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.ticket.TicketOverview;
import net.link.safeonline.demo.ticket.entity.Ticket;
import net.link.safeonline.demo.ticket.entity.User;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
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

	@PersistenceContext(unitName = "DemoTicketEntityManager")
	private EntityManager entityManager;

	@DataModel("ticketList")
	@SuppressWarnings("unused")
	private List<Ticket> ticketList;

	@Factory("ticketList")
	@RolesAllowed("user")
	@SuppressWarnings("unchecked")
	public void ticketListFactory() {
		User user = this.entityManager.find(User.class, this.getUsername());
		if (user == null) {
			user = new User(this.getUsername());
			this.entityManager.persist(user);
		}
		this.ticketList = user.getTickets();
		log.debug("Ticket List: " + this.ticketList.size());
	}

	private String getUsername() {
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
