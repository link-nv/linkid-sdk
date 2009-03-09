/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.bank.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "dBankTransaction")
@NamedQueries( {
        @NamedQuery(name = BankTransactionEntity.getById, query = "SELECT t FROM BankTransactionEntity t WHERE t.id = :id"),
        @NamedQuery(name = BankTransactionEntity.getByCode, query = "SELECT t FROM BankTransactionEntity t WHERE t.source.code = :code OR t.target = :code ORDER BY t.date DESC") })
public class BankTransactionEntity implements Serializable, Comparable<BankTransactionEntity> {

    private static final long  serialVersionUID = 1L;

    public static final String getById          = "BankTransactionEntity.getById";
    public static final String getByCode        = "BankTransactionEntity.getByCode";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer            id;

    private String             description;

    @ManyToOne
    private BankAccountEntity  source;

    private String             target;

    @Column(name = "processDate")
    private Date               date;

    private Double             amount;


    public BankTransactionEntity() {

    }

    public BankTransactionEntity(String description, BankAccountEntity source, String target, Date date, Double amount) {

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

        return id;
    }

    /**
     * @return The description of this {@link BankTransactionEntity}.
     */
    public String getDescription() {

        return description;
    }

    /**
     * @return The source of this {@link BankTransactionEntity}.
     */
    public BankAccountEntity getSource() {

        return source;
    }

    /**
     * @return The target of this {@link BankTransactionEntity}.
     */
    public String getTarget() {

        return target;
    }

    /**
     * @return The date of this {@link BankTransactionEntity}.
     */
    public Date getDate() {

        return date;
    }

    /**
     * @return The amount of this {@link BankTransactionEntity}.
     */
    public Double getAmount() {

        return amount;
    }

    // UTILITIES ----------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public int compareTo(BankTransactionEntity o) {

        return date.compareTo(o.date);
    }
}
