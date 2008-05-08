/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.entity;

import static net.link.safeonline.demo.payment.entity.PaymentEntity.QUERY_WHERE_OWNER;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

@Entity
@Table(name = "demo_payment")
@NamedQueries(@NamedQuery(name = QUERY_WHERE_OWNER, query = "SELECT payment FROM PaymentEntity AS payment "
		+ "WHERE payment.owner = :owner " + "ORDER BY payment.paymentDate DESC"))
public class PaymentEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_OWNER = "ticket.where.owner";

	private long id;

	private UserEntity owner;

	private Date paymentDate;

	private String recipient;

	private String message;

	private String visa;

	private double amount;

	public PaymentEntity() {
		// empty
	}

	public PaymentEntity(UserEntity owner, Date paymentDate, String visa,
			double amount, String recipient, String message) {
		this.owner = owner;
		this.paymentDate = paymentDate;
		this.visa = visa;
		this.amount = amount;
		this.recipient = recipient;
		this.message = message;
	}

	@ManyToOne
	public UserEntity getOwner() {
		return this.owner;
	}

	public void setOwner(UserEntity owner) {
		this.owner = owner;
	}

	public Date getPaymentDate() {
		return this.paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	@Id
	@GeneratedValue
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Basic(optional = false)
	public String getRecipient() {
		return this.recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@Basic(optional = false)
	public String getVisa() {
		return this.visa;
	}

	public void setVisa(String visa) {
		this.visa = visa;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public static Query createQueryWhereOwner(EntityManager entityManager,
			UserEntity owner) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_OWNER);
		query.setParameter("owner", owner);
		return query;
	}
}
