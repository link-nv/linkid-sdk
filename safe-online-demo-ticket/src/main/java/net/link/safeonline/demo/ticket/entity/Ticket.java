/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "demo_ticket")
public class Ticket implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Site {
		GENT("Gent"), BRUSSEL("Brussel"), ANTWERPEN("Antwerpen"), HERZELE(
				"Herzele"), MELLE("Melle"), GENTBRUGGE("Gentbrugge");

		private final String name;

		Site(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	};

	private long id;

	private User owner;

	private Site start;

	private Site destination;

	private Date validFrom;

	private Date validTo;

	private boolean biDirectional;

	public Ticket() {
		// empty
	}

	public Ticket(User owner, Site start, Site destination, Date validFrom,
			Date validTo, boolean biDirectional) {
		this.owner = owner;
		this.start = start;
		this.destination = destination;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.biDirectional = biDirectional;
	}

	@Enumerated(EnumType.STRING)
	public Site getDestination() {
		return destination;
	}

	public void setDestination(Site destination) {
		this.destination = destination;
	}

	@ManyToOne
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Enumerated(EnumType.STRING)
	public Site getStart() {
		return start;
	}

	public void setStart(Site start) {
		this.start = start;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public boolean isBiDirectional() {
		return biDirectional;
	}

	public void setBiDirectional(boolean biDirectional) {
		this.biDirectional = biDirectional;
	}

	@Id
	@GeneratedValue
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
