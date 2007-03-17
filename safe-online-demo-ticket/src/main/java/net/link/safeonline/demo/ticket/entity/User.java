/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "demo_ticket_user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Ticket> tickets;

	private String safeOnlineUserName;

	public User() {
		this.tickets = new ArrayList<Ticket>();
	}

	public User(String safeOnlineUserName) {
		this.tickets = new ArrayList<Ticket>();
		this.safeOnlineUserName = safeOnlineUserName;
	}

	@Id
	public String getSafeOnlineUserName() {
		return safeOnlineUserName;
	}

	public void setSafeOnlineUserName(String safeOnlineUserName) {
		this.safeOnlineUserName = safeOnlineUserName;
	}

	@OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

}
