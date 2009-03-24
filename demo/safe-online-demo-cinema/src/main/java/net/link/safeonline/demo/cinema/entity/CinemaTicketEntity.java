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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "dCinemaTicket")
@NamedQueries( { @NamedQuery(name = CinemaTicketEntity.getById, query = "SELECT t FROM CinemaTicketEntity t WHERE t.id = :id"),
        @NamedQuery(name = CinemaTicketEntity.getByUser, query = "SELECT t FROM CinemaTicketEntity t WHERE t.owner = :user"),
        @NamedQuery(name = CinemaTicketEntity.getByNrn, query = "SELECT t FROM CinemaTicketEntity t WHERE t.owner.nrn = :nrn") })
public class CinemaTicketEntity implements Serializable {

    private static final long          serialVersionUID = 1L;

    public static final String         getById          = "CinemaTicketEntity.getById";
    public static final String         getByUser        = "CinemaTicketEntity.getByUser";
    public static final String         getByNrn         = "CinemaTicketEntity.getByNrn";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                       id;

    @ManyToOne
    private CinemaUserEntity           owner;

    @ManyToOne
    private CinemaFilmEntity           film;

    @OneToOne
    private CinemaSeatOccupationEntity occupation;
    private long                       time;
    private double                     price;


    public CinemaTicketEntity() {

    }

    public CinemaTicketEntity(CinemaUserEntity owner, CinemaFilmEntity film, long time, CinemaSeatOccupationEntity occupation) {

        this.owner = owner;

        this.film = film;
        this.occupation = occupation;
        this.time = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("[%s]   Owner: %s, Price %s, Film: %s, Theatre: %s", new Date(getTime()), getOwner().getNrn(), getPrice(),
                getFilm().getName(), getOccupation().getSeat().getRoom().getTheatre().getName());
    }

    /**
     * @return The id of this {@link CinemaTicketEntity}.
     */
    public long getId() {

        return id;
    }

    /**
     * @return The {@link CinemaUserEntity} that this ticket is valid for.
     */
    public CinemaUserEntity getOwner() {

        return owner;
    }

    /**
     * @return The {@link CinemaFilmEntity} this ticket grants the owner access to.
     */
    public CinemaFilmEntity getFilm() {

        return film;
    }

    /**
     * @return The {@link Date} of the showing of the {@link CinemaFilmEntity} that this ticket grants the owner access to.
     */
    public long getTime() {

        return time;
    }

    /**
     * @return The seat occupied by this {@link CinemaTicketEntity}.
     */
    public CinemaSeatOccupationEntity getOccupation() {

        return occupation;
    }

    /**
     * @param price
     *            The calculated price for this {@link CinemaTicketEntity} at the time of purchase.
     */
    public void setPrice(double price) {

        this.price = price;
    }

    /**
     * @return The calculated price for this {@link CinemaTicketEntity} at the time of purchase.
     */
    public double getPrice() {

        return price;
    }
}
