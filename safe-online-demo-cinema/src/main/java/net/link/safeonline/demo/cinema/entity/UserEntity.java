/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class UserEntity {

    @OneToMany(mappedBy = "owner")
    private Set<TicketEntity> tickets;

    @Id
    private String      id;


    public UserEntity() {

        this.tickets = new HashSet<TicketEntity>();
    }

    public UserEntity(String id) {

        this();
        this.id = id;
    }

    /**
     * @return The OLAS mapped id of the user for this application.
     */
    public String getId() {

        return this.id;
    }

    /**
     * @return The {@link TicketEntity}s this user has bought.
     */
    public Set<TicketEntity> getTickets() {

        return this.tickets;
    }
}
