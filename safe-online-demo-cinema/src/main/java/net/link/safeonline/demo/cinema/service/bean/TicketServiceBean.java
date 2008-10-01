/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.service.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import net.link.safeonline.demo.cinema.CinemaConstants;
import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.SeatService;
import net.link.safeonline.demo.cinema.service.TicketService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link TicketServiceBean}<br>
 * <sub>Service bean for {@link TicketService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 25, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = TicketService.BINDING)
public class TicketServiceBean extends AbstractCinemaServiceBean implements TicketService {

    @EJB
    private transient SeatService seatService;


    /**
     * {@inheritDoc}
     */
    public CinemaTicketEntity createTicket(CinemaUserEntity user, CinemaFilmEntity film, Date time,
            CinemaSeatOccupationEntity occupation) {

        // Occupy our seat.
        CinemaSeatOccupationEntity ticketOccupation = this.seatService.validate(occupation);

        // Create a ticket for them.
        CinemaTicketEntity ticket = new CinemaTicketEntity(user, film, time.getTime(), ticketOccupation);
        ticket.setPrice(calculatePrice(ticket));

        return ticket;
    }

    /**
     * {@inheritDoc}
     */
    public CinemaTicketEntity reserve(CinemaTicketEntity ticket) {

        ticket.getOccupation().reserve();
        this.em.persist(ticket);

        return ticket;
    }

    /**
     * {@inheritDoc}
     */
    public double calculatePrice(CinemaTicketEntity ticket) {

        double modifier = 1;
        int basePrice = ticket.getFilm().getPrice();

        // Discount for Junior users.
        if (ticket.getOwner().isJunior()) {
            modifier = 1 - CinemaConstants.JUNIOR_DISCOUNT;
        }

        return Math.round(basePrice * modifier * 10) / 10;
    }

    @SuppressWarnings("unchecked")
    public List<CinemaTicketEntity> getTickets(String nrn, Date time) {

        LOG.debug("looking up ticket for {nrn: " + nrn + "} at " + time);
        try {
            // NOTE: time parameter is not checked because this is a demo.
            return this.em.createNamedQuery(CinemaTicketEntity.getByNrn).setParameter("nrn", nrn).getResultList();
        }

        catch (NoResultException e) {
            return new ArrayList<CinemaTicketEntity>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<CinemaTicketEntity> getTickets(String nrn, Date time, String theatreName) {

        List<CinemaTicketEntity> tickets = getTickets(nrn, time);
        for (Iterator<CinemaTicketEntity> it = tickets.iterator(); it.hasNext();)
            if (!it.next().getOccupation().getSeat().getRoom().getTheatre().getName().equalsIgnoreCase(theatreName)) {
                it.remove();
            }

        // Feed the log.
        LOG.debug("----");
        LOG.debug("Found " + tickets.size() + " tickets:");
        for (CinemaTicketEntity ticket : tickets) {
            LOG.debug(ticket);
        }

        return tickets;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid(String nrn, Date time, String theatreName, String filmName) {

        for (CinemaTicketEntity ticket : getTickets(nrn, time, theatreName))
            if (ticket.getFilm().getName().equalsIgnoreCase(filmName))
                return true;

        LOG.debug("None for film " + filmName);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<CinemaTicketEntity> getTickets(CinemaUserEntity user) {

        if (user != null) {
            CinemaUserEntity attachedUser = attach(user);
            if (attachedUser != null)
                return new LinkedList<CinemaTicketEntity>(attachedUser.getTickets());
        }

        return new ArrayList<CinemaTicketEntity>();
    }

    /**
     * {@inheritDoc}
     */
    public String getFilmName(CinemaTicketEntity ticket) {

        return ticket.getFilm().getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getRoomName(CinemaTicketEntity ticket) {

        return ticket.getOccupation().getSeat().getRoom().getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getTheatreName(CinemaTicketEntity ticket) {

        return ticket.getOccupation().getSeat().getRoom().getTheatre().getName();
    }
}
