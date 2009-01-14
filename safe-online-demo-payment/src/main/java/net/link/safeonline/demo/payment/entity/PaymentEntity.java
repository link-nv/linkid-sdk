/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.entity;

import static net.link.safeonline.demo.payment.entity.PaymentEntity.getByOwner;

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
@NamedQueries(@NamedQuery(name = getByOwner, query = "SELECT payment FROM PaymentEntity AS payment " + "WHERE payment.owner = :owner "
        + "ORDER BY payment.paymentDate DESC"))
public class PaymentEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String getByOwner       = "PaymentEntity.getByOwner";

    @Id
    @GeneratedValue
    private long               id;

    @ManyToOne
    private PaymentUserEntity  owner;

    private Date               paymentDate;

    @Basic(optional = false)
    private String             recipient;

    private String             message;

    @Basic(optional = false)
    private String             visa;

    private double             amount;


    public PaymentEntity() {

        // empty
    }

    public PaymentEntity(PaymentUserEntity owner, Date paymentDate, String visa, double amount, String recipient, String message) {

        this.owner = owner;
        this.paymentDate = paymentDate;
        this.visa = visa;
        this.amount = amount;
        this.recipient = recipient;
        this.message = message;
    }

    public PaymentUserEntity getOwner() {

        return owner;
    }

    public void setOwner(PaymentUserEntity owner) {

        this.owner = owner;
    }

    public Date getPaymentDate() {

        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {

        this.paymentDate = paymentDate;
    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getRecipient() {

        return recipient;
    }

    public void setRecipient(String recipient) {

        this.recipient = recipient;
    }

    public String getVisa() {

        return visa;
    }

    public void setVisa(String visa) {

        this.visa = visa;
    }

    public double getAmount() {

        return amount;
    }

    public void setAmount(double amount) {

        this.amount = amount;
    }

    public static Query createQueryWhereOwner(EntityManager entityManager, PaymentUserEntity owner) {

        Query query = entityManager.createNamedQuery(getByOwner);
        query.setParameter("owner", owner);
        return query;
    }
}
