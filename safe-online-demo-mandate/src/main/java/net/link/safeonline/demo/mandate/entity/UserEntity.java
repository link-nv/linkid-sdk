/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "demo_mandate_user")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            name;

    private boolean           admin;


    public UserEntity() {

        this(null);
    }

    public UserEntity(String name) {

        this.name = name;
    }

    @Id
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean isAdmin() {

        return admin;
    }

    public void setAdmin(boolean admin) {

        this.admin = admin;
    }
}
