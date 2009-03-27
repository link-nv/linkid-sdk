/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.bank.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "dBankAccount")
@NamedQueries( { @NamedQuery(name = BankAccountEntity.getByCode, query = "SELECT a FROM BankAccountEntity a WHERE a.code = :code"),
        @NamedQuery(name = BankAccountEntity.getByUser, query = "SELECT a FROM BankAccountEntity a WHERE a.user = :user") })
public class BankAccountEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String getByCode        = "BankAccountEntity.getByCode";
    public static final String getByUser        = "BankAccountEntity.getByUser";

    @Id
    private String             code;

    @ManyToOne
    private BankUserEntity     user;

    private String             name;

    private Double             amount;


    public BankAccountEntity() {

        amount = 0d;
    }

    public BankAccountEntity(BankUserEntity user, String name, String code) {

        this();

        this.user = user;
        this.name = name;
        this.code = code;
    }

    /**
     * @return The owner of this {@link BankAccountEntity}.
     */
    public BankUserEntity getUser() {

        return user;
    }

    /**
     * @param name
     *            The friendly account name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * @return The friendly account name.
     */
    public String getName() {

        return name;
    }

    /**
     * @return The bank account's national code.
     */
    public String getCode() {

        return code;
    }

    /**
     * @param amount
     *            The amount of currency in this account.
     */
    public void setAmount(double amount) {

        this.amount = amount;
    }

    /**
     * @return The amount of currency in this account.
     */
    public Double getAmount() {

        return amount;
    }
}
