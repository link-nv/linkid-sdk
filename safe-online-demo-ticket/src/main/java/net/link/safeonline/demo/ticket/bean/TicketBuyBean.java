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
import java.util.UUID;

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
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.Period;

@Stateful
@Name("ticketBuy")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = "SafeOnlineTicketDemo/TicketBuyBean/local")
@SecurityDomain("demo-ticket")
public class TicketBuyBean implements TicketBuy {

	@Logger
	private Log log;

	@Resource
	private SessionContext sessionContext;

	@PersistenceContext(unitName = "DemoTicketEntityManager")
	private EntityManager entityManager;

	@SuppressWarnings("unused")
	@Out(required = false)
	private double ticketPrice;

	@SuppressWarnings("unused")
	@Out(required = false)
	private String visaNumber;

	@SuppressWarnings("unused")
	@Out(required = false)
	private Date startDate;

	@SuppressWarnings("unused")
	@Out(required = false)
	private Date endDate;

	public enum TicketPeriod {
		DAY("one day", Period.days(1)), WEEK("one week", Period.weeks(1)), MONTH(
				"one month", Period.months(1));

		private final String name;

		private final Period period;

		TicketPeriod(String name, Period period) {
			this.name = name;
			this.period = period;
		}

		public String getName() {
			return this.name;
		}

		public Date getEndDate(Date beginDate) {
			DateTime begin = new DateTime(beginDate);
			DateTime endDate = begin.plus(this.period);
			return endDate.toDate();
		}
	}

	private String from;

	private String to;

	private String validUntil;

	private boolean returnTicket;

	private String nrn;

	public String getFrom() {
		return this.from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return this.to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getValidUntil() {
		return this.validUntil;
	}

	public void setValidUntil(String validUntil) {
		this.validUntil = validUntil;
	}

	public boolean getReturnTicket() {
		return this.returnTicket;
	}

	public void setReturnTicket(boolean returnTicket) {
		this.returnTicket = returnTicket;
	}

	private String getUsername() {
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
		for (TicketPeriod period : TicketPeriod.values()) {
			result.add(new SelectItem(period.toString(), period.getName()));
		}
		return result;
	}

	@RolesAllowed("user")
	public String checkOut() {
		this.ticketPrice = 100;
		// TODO: retrieve VISA and NRN data from SafeOnline
		this.visaNumber = UUID.randomUUID().toString();
		this.nrn = UUID.randomUUID().toString();
		TicketPeriod valid = TicketPeriod.valueOf(this.validUntil);
		this.startDate = new Date();
		this.endDate = valid.getEndDate(this.startDate);
		return "checkout";
	}

	@RolesAllowed("user")
	@End
	// conversation begin via pages.xml
	public String confirm() {
		User user = this.entityManager.find(User.class, this.getUsername());
		if (user == null) {
			user = new User(this.getUsername(), this.nrn);
			this.entityManager.persist(user);
		}
		user.setNrn(this.nrn);
		Ticket ticket = new Ticket(user, Site.valueOf(this.from), Site
				.valueOf(this.to), this.startDate, this.endDate,
				this.returnTicket);
		user.getTickets().add(ticket);
		this.entityManager.persist(ticket);
		return "list";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
