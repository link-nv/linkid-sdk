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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity
@NamedQueries( { @NamedQuery(name = UserEntity.getById, query = "SELECT u FROM UserEntity u WHERE u.id = :id") })
public class UserEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String getById          = "UserEntity.getById";

    @Id
    private String             bankId;

    private String             olasId;

    private String             name;


    public UserEntity() {

    }

    public UserEntity(String bankId) {

        this();

        this.bankId = bankId;
    }

    public UserEntity(String bankId, String olasId) {

        this(bankId);

        this.olasId = olasId;
    }

    /**
     * @return The id of the user for this application.
     */
    public String getBankId() {

        return this.bankId;
    }

    /**
     * @return The OLAS mapped id of the user for this application.
     */
    public String getOlasId() {

        return this.olasId;
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

        return this.name;
    }
}
