/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


@Entity
@NamedQueries( { @NamedQuery(name = CinemaUserEntity.getById, query = "SELECT u FROM CinemaUserEntity u WHERE u.id = :id") })
public class CinemaUserEntity implements Serializable {

    private static final long        serialVersionUID = 1L;

    public static final String       getById          = "CinemaUserEntity.getById";

    @OneToMany(mappedBy = "owner")
    private Collection<CinemaTicketEntity> tickets;

    @Id
    private String                   id;

    private boolean                  junior;

    private String                   nrn;

    private String                   name;


    public CinemaUserEntity() {

        this.tickets = new HashSet<CinemaTicketEntity>();
    }

    public CinemaUserEntity(String id) {

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
     * @return The {@link CinemaTicketEntity}s this user has bought.
     */
    public Collection<CinemaTicketEntity> getTickets() {

        return this.tickets;
    }

    /**
     * @param isJunior
     *            <code>true</code> if this user has a junior account.
     */
    public void setJunior(boolean isJunior) {

        this.junior = isJunior;
    }

    /**
     * @return <code>true</code> if this user has a junior account.
     */
    public boolean isJunior() {

        return this.junior;
    }

    /**
     * @param nrn
     *            The user's national registry number.
     */
    public void setNrn(String nrn) {

        this.nrn = nrn;
    }

    /**
     * @return The user's national registry number.
     */
    public String getNrn() {

        return this.nrn;
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
