/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.ticket.TicketBuy;
import net.link.safeonline.demo.ticket.entity.Ticket;
import net.link.safeonline.demo.ticket.entity.User;
import net.link.safeonline.demo.ticket.entity.Ticket.Site;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;

@Stateful
@Name("ticketBuy")
@LocalBinding(jndiBinding = "SafeOnlineTicketDemo/TicketBuyBean/local")
@SecurityDomain("demo-ticket")
public class TicketBuyBean implements TicketBuy {

	@Logger
	private Log log;

	@Resource
	private SessionContext sessionContext;

	@PersistenceContext(unitName = "DemoTicketEntityManager")
	private EntityManager entityManager;

	public enum Period {
		DAY("one day", 24), WEEK("one week", 168), MONTH("one month", 5208);

		private final String name;

		private final long duration;

		Period(String name, long duration) {
			this.name = name;
			this.duration = duration;
		}

		public String getName() {
			return this.name;
		}

		public long getDuration() {
			return this.duration;
		}

	}

	@In(required = false)
	@Out(required = false)
	private String from;

	@In(required = false)
	@Out(required = false)
	private String to;

	@In(required = false)
	@Out(required = false)
	private String validUntil;

	// @In(required = false)
	// @Out(required = false)
	private boolean returnTicket;

	@Out(value = "ticketList", required = false)
	@SuppressWarnings("unused")
	private List<Ticket> ticketList;

	public boolean getReturnTicket() {
		return this.returnTicket;
	}

	public void setReturnTicket(boolean returnTicket) {
		this.returnTicket = returnTicket;
	}

	@RolesAllowed("user")
	public String getUsername() {
		Principal principal = this.sessionContext.getCallerPrincipal();
		String name = principal.getName();
		log.debug("username #0", name);
		return name;
	}

	@Factory("siteList")
	public List<SelectItem> siteListFactory() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (Ticket.Site site : Ticket.Site.values()) {
			result.add(new SelectItem(site.toString(), site.getName()));
		}
		return result;
	}

	@Factory("dateList")
	public List<SelectItem> dateListFactory() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (Period period : Period.values()) {
			result.add(new SelectItem(period.toString(), period.getName()));
		}
		return result;
	}

	@RolesAllowed("user")
	public String checkOut() {
		User user = this.entityManager.find(User.class, this.getUsername());
		if (user == null) {
			user = new User(this.getUsername());
			this.entityManager.persist(user);
		}
		Period valid = Period.valueOf(this.validUntil);
		long duration = valid.getDuration() * 3600;
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = new Date(System.currentTimeMillis() + duration);
		Ticket ticket = new Ticket(user, Site.valueOf(from), Site.valueOf(to),
				startDate, endDate, this.returnTicket);
		user.getTickets().add(ticket);
		this.entityManager.persist(ticket);
		this.ticketList = user.getTickets();
		return "list";
	}

	@RolesAllowed("user")
	public String confirm() {
		return "list";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
