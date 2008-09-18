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


@Entity
@NamedQueries( {
        @NamedQuery(name = AccountEntity.getByCode, query = "SELECT a FROM AccountEntity a WHERE a.code = :code"),
        @NamedQuery(name = AccountEntity.getByUser, query = "SELECT a FROM AccountEntity a WHERE a.user = :user") })
public class AccountEntity implements Serializable {

    private static final long       serialVersionUID = 1L;

    public static final String      getByCode        = "AccountEntity.getByCode";

    public static final String      getByUser        = "AccountEntity.getByUser";

    @Id
    private String                  code;

    @ManyToOne
    private UserEntity              user;

    private String                  name;

    private Double                  amount;


    public AccountEntity() {

    }

    public AccountEntity(UserEntity user, String name, String code) {

        this();

        this.user = user;
        this.name = name;
        this.code = code;
    }

    /**
     * @return The owner of this {@link AccountEntity}.
     */
    public UserEntity getUser() {

        return this.user;
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

        return this.name;
    }

    /**
     * @return The bank account's national code.
     */
    public String getCode() {

        return this.code;
    }

    /**
     * @return The amount of currency in this account.
     */
    public Double setAmount() {

        return this.amount;
    }

    /**
     * @return The amount of currency in this account.
     */
    public Double getAmount() {

        return this.amount;
    }
}
