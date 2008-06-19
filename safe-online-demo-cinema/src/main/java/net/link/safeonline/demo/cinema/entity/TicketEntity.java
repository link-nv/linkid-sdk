/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TicketEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long              id;

    @ManyToOne
    private UserEntity        owner;

    @ManyToOne
    private FilmEntity        film;
    private Date              showTime;


    public TicketEntity() {

    }

    public TicketEntity(UserEntity owner, FilmEntity film, Date showTime) {

        this.owner = owner;

        this.film = film;
        this.showTime = showTime;
    }

    /**
     * @return The {@link UserEntity} that this ticket is valid for.
     */
    public UserEntity getOwner() {

        return this.owner;
    }

    /**
     * @return The {@link FilmEntity} this ticket grants the owner access to.
     */
    public FilmEntity getFilm() {

        return this.film;
    }

    /**
     * @return The {@link Date} of the showing of the {@link FilmEntity} that
     *         this ticket grants the owner access to.
     */
    public Date getShowTime() {

        return this.showTime;
    }
}
