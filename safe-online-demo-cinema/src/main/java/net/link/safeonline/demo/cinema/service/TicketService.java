/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.SeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.TicketEntity;
import net.link.safeonline.demo.cinema.entity.UserEntity;


/**
 * <h2>{@link TicketService}<br>
 * <sub>Service bean for {@link TicketEntity}.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jun 25, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface TicketService extends CinemaService {

    public static final String BINDING = JNDI_PREFIX + "TicketServiceBean/local";


    /**
     * @return A ticket with all information on the movie viewing that can be purchased.
     * @throws IllegalStateException
     *             When the occupation is already taken.
     */
    public TicketEntity createTicket(UserEntity user, FilmEntity film, Date time, SeatOccupationEntity occupation);

    /**
     * Mark the given ticket as reserved. The user has completed the reservation process for it.
     */
    public TicketEntity reserve(TicketEntity ticket);

    /**
     * @return The price for the given ticket with all modifiers applied.
     */
    public double calculatePrice(TicketEntity ticket);

    /**
     * @param time
     *            All valid tickets at this moment in time are returned. That means, all tickets for films that start at
     *            or before this time AND end end at or after this time.
     * 
     * @return All tickets purchased by the subject with the given national registry number that are valid for the given
     *         time.
     */
    public List<TicketEntity> getTickets(String nrn, Date time);

    /**
     * @param time
     *            All valid tickets at this moment in time are returned. That means, all tickets for films that start at
     *            or before this time AND end end at or after this time.
     * 
     * @return All tickets purchased by the subject with the given national registry number that are valid for the given
     *         time.
     */
    public List<TicketEntity> getTickets(String nrn, Date time, String theatreName);

    /**
     * @param time
     *            All valid tickets at this moment in time are returned. That means, all tickets for films that start at
     *            or before this time AND end end at or after this time.
     * 
     * @return All tickets purchased by the subject with the given national registry number that are valid for the given
     *         time.
     */
    public boolean isValid(String nrn, Date time, String theatreName, String filmName);

    /**
     * @return All tickets purchased by the given user.
     */
    public List<TicketEntity> getTickets(UserEntity user);

    /**
     * @return The name of the film this ticket is valid for.
     */
    public String getFilmName(TicketEntity ticket);

    /**
     * @return The name of the theatre in which this ticket is valid.
     */
    public String getTheatreName(TicketEntity ticket);

    /**
     * @return The name of the film room for which this ticket is valid.
     */
    public String getRoomName(TicketEntity ticket);
}
