/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.entity;

import static net.link.safeonline.demo.ticket.entity.User.QUERY_WHERE_NRN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;


@Entity
@Table(name = "demo_ticket_user")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_NRN, query = "SELECT user FROM User AS user " + "WHERE user.nrn = :nrn") })
public class User implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String QUERY_WHERE_NRN  = "user.where.nrn";

    private List<Ticket>       tickets;

    private String             safeOnlineUserName;

    private String             nrn;


    public User() {

        this.tickets = new ArrayList<Ticket>();
    }

    public User(String safeOnlineUserName, String nrn) {

        this.tickets = new ArrayList<Ticket>();
        this.safeOnlineUserName = safeOnlineUserName;
        this.nrn = nrn;
    }

    public User(String safeOnlineUserName) {

        this(safeOnlineUserName, null);
    }

    @Id
    public String getSafeOnlineUserName() {

        return this.safeOnlineUserName;
    }

    public void setSafeOnlineUserName(String safeOnlineUserName) {

        this.safeOnlineUserName = safeOnlineUserName;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    public List<Ticket> getTickets() {

        return this.tickets;
    }

    public void setTickets(List<Ticket> tickets) {

        this.tickets = tickets;
    }

    @Column(unique = true)
    public String getNrn() {

        return this.nrn;
    }

    public void setNrn(String nrn) {

        this.nrn = nrn;
    }

    public static Query createQueryWhereNrn(EntityManager entityManager, String nrn) {

        Query query = entityManager.createNamedQuery(QUERY_WHERE_NRN);
        query.setParameter("nrn", nrn);
        return query;
    }
}
