/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.bank.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "dBankUser")
@NamedQueries( { @NamedQuery(name = BankUserEntity.getAll, query = "SELECT u FROM BankUserEntity u"),
        @NamedQuery(name = BankUserEntity.getByBankId, query = "SELECT u FROM BankUserEntity u WHERE u.bankId = :bankId"),
        @NamedQuery(name = BankUserEntity.getByOlasId, query = "SELECT u FROM BankUserEntity u WHERE u.olasId = :olasId") })
public class BankUserEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String getAll           = "BankUserEntity.getAll";
    public static final String getByBankId      = "BankUserEntity.getByBankId";
    public static final String getByOlasId      = "BankUserEntity.getByOlasId";

    @Id
    private String             bankId;

    @Column(unique = true)
    private String             olasId;

    private String             name;


    public BankUserEntity() {

    }

    public BankUserEntity(String bankId, String name) {

        this();

        this.bankId = bankId;
        this.name = name;
    }

    public BankUserEntity(String bankId, String olasId, String name) {

        this(bankId, name);

        this.olasId = olasId;
    }

    /**
     * @return The id of the user for this application.
     */
    public String getBankId() {

        return bankId;
    }

    /**
     * @param olasId
     *            The OLAS id of the user for this application.
     */
    public void setOlasId(String olasId) {

        this.olasId = olasId;
    }

    /**
     * @return The OLAS mapped id of the user for this application.
     */
    public String getOlasId() {

        return olasId;
    }

    /**
     * @param name
     *            The OLAS username of this user.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * @return The OLAS username of this user.
     */
    public String getName() {

        return name;
    }
}
