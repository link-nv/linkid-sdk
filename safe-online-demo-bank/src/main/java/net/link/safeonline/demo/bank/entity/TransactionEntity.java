/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.bank.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity
@NamedQueries( {
        @NamedQuery(name = TransactionEntity.getById, query = "SELECT t FROM TransactionEntity t WHERE t.id = :id"),
        @NamedQuery(name = TransactionEntity.getByCode, query = "SELECT t FROM TransactionEntity t WHERE t.source.code = :code OR t.target = :code ORDER BY t.date DESC") })
public class TransactionEntity implements Serializable, Comparable<TransactionEntity> {

    private static final long  serialVersionUID = 1L;

    public static final String getById          = "TransactionEntity.getById";
    public static final String getByCode        = "TransactionEntity.getByCode";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer            id;

    private String             description;

    @ManyToOne
    private AccountEntity      source;

    private String             target;

    private Date               date;

    private Double             amount;


    public TransactionEntity() {

    }

    public TransactionEntity(String description, AccountEntity source, String target, Date date, Double amount) {

        this();

        this.description = description;
        this.source = source;
        this.target = target;
        this.date = date;
        this.amount = amount;
    }

    /**
     * @return The identifier of this transaction.
     */
    public Integer getId() {

        return this.id;
    }

    /**
     * @return The description of this {@link TransactionEntity}.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * @return The source of this {@link TransactionEntity}.
     */
    public AccountEntity getSource() {

        return this.source;
    }

    /**
     * @return The target of this {@link TransactionEntity}.
     */
    public String getTarget() {

        return this.target;
    }

    /**
     * @return The date of this {@link TransactionEntity}.
     */
    public Date getDate() {

        return this.date;
    }

    /**
     * @return The amount of this {@link TransactionEntity}.
     */
    public Double getAmount() {

        return this.amount;
    }

    // UTILITIES ----------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public int compareTo(TransactionEntity o) {

        return this.date.compareTo(o.date);
    }
}
