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


@Entity
@NamedQueries( { @NamedQuery(name = TicketEntity.findTicket, query = "SELECT t FROM TicketEntity t WHERE t.owner.nrn = :nrn") })
public class TicketEntity implements Serializable {

    private static final long    serialVersionUID = 1L;

    public static final String   findTicket       = "TicketEntity.findTicket";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                 id;

    @ManyToOne
    private UserEntity           owner;

    @ManyToOne
    private FilmEntity           film;
    private SeatOccupationEntity occupation;
    private long                 time;
    private double               price;


    public TicketEntity() {

    }

    public TicketEntity(UserEntity owner, FilmEntity film, long time, SeatOccupationEntity occupation) {

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

        return String.format("[%s]   Owner: %s, Price %s, Film: %s, Theatre: %s", new Date(getTime()), getOwner()
                .getNrn(), getPrice(), getFilm().getName(), getOccupation().getSeat().getRoom().getTheatre().getName());
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
     * @return The {@link Date} of the showing of the {@link FilmEntity} that this ticket grants the owner access to.
     */
    public long getTime() {

        return this.time;
    }

    /**
     * @return The seat occupied by this {@link TicketEntity}.
     */
    public SeatOccupationEntity getOccupation() {

        return this.occupation;
    }

    /**
     * @param price
     *            The calculated price for this {@link TicketEntity} at the time of purchase.
     */
    public void setPrice(double price) {

        this.price = price;
    }

    /**
     * @return The calculated price for this {@link TicketEntity} at the time of purchase.
     */
    public double getPrice() {

        return this.price;
    }
}
