/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.webapp;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.RoomEntity;
import net.link.safeonline.demo.cinema.entity.SeatEntity;
import net.link.safeonline.demo.cinema.entity.SeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.TheatreEntity;
import net.link.safeonline.demo.cinema.entity.TicketEntity;
import net.link.safeonline.demo.cinema.entity.UserEntity;
import net.link.safeonline.demo.cinema.service.TicketService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Session;

/**
 * <h2>{@link CinemaSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jun 10, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class CinemaSession extends Session {

    private static final long        serialVersionUID = 1L;
    private static final Log         LOG              = LogFactory
                                                              .getLog(CinemaSession.class);

    private transient TicketService  ticketService;

    private UserEntity               user;
    private FilmEntity               film;
    private TheatreEntity            theatre;
    private Date                     time;
    private RoomEntity               room;
    private SeatOccupationEntity     occupation;
    private TicketEntity             ticket;


    public CinemaSession(Request request) {

        super(request);

        try {
            InitialContext context = new InitialContext();

            this.ticketService = (TicketService) context
                    .lookup(TicketService.BINDING);
        }

        catch (NamingException e) {
            LOG.error("EJB Injection On Session Failed.", e);
        }
    }

    public void setUser(UserEntity user) {

        this.user = user;
    }

    public UserEntity getUser() {

        return this.user;
    }

    /**
     * @param film
     *            The film the user selected for viewing.
     */
    public void setFilm(FilmEntity film) {

        this.film = film;
    }

    /**
     * @return The film the user selected for viewing.
     */
    public FilmEntity getFilm() {

        return this.film;
    }

    /**
     * @param theatre
     *            The theatre the user will view his film in.
     */
    public void setTheatre(TheatreEntity theatre) {

        this.theatre = theatre;
    }

    /**
     * @return The theatre the user will view his film in.
     */
    public TheatreEntity getTheatre() {

        return this.theatre;
    }

    /**
     * @param time
     *            The time of showing for the selected film.
     */
    public void setTime(Date time) {

        this.time = time;
    }

    /**
     * @param room
     *            The room of the theatre the user will view the film in.
     */
    public void setRoom(RoomEntity room) {

        this.room = room;
    }

    /**
     * @return The time of showing for the selected film.
     */
    public Date getTime() {

        return this.time;
    }

    /**
     * @return The room of the theatre the user will view the film in.
     */
    public RoomEntity getRoom() {

        return this.room;
    }

    /**
     * @return The seat occupied by the user.
     */
    public SeatOccupationEntity getOccupation() {

        return this.occupation;
    }

    /**
     * @return The complete ticket for the selections made by the user or
     *         <code>null</code> if not all required selections have been made
     *         yet.
     */
    public TicketEntity getTicket() {

        if (this.ticket == null) {
            if (getUser() != null && getFilm() != null && getTime() != null
                    && getOccupation() != null) {
                try {
                    this.ticket = this.ticketService.createTicket(getUser(),
                            getFilm(), getTime(), getOccupation());
                }

                catch (IllegalStateException e) {
                    LOG.error("Removing seat selection.", e);
                    this.occupation = null;
                }
            }
        }

        return this.ticket;
    }

    /**
     * Change the seat occupation to the given seat, or unoccupy that seat if it
     * was the seat currently occupied by us.
     * 
     * @param seat
     *            The seat to occupy or unoccupy.
     */
    public void toggleSeat(SeatEntity seat) {

        if (this.occupation != null && this.occupation.getSeat().equals(seat)) {
            this.occupation = null;
        } else {
            this.occupation = new SeatOccupationEntity(seat, this.time);
        }
    }

    /**
     * Unset the whole ticket.
     */
    public void resetTicket() {

        this.film = null;
        this.theatre = null;
        this.room = null;
        this.time = null;
        this.occupation = null;
        this.ticket = null;
    }

    /**
     * Unset the selected film (and all other properties that depend on it).
     * 
     * Don't do anything if no film is set.
     */
    public void resetFilm() {

        if (this.film != null) {
            this.film = null;
            this.room = null;
            this.time = null;
            this.occupation = null;
            this.ticket = null;
        }
    }

    /**
     * Unset the selected theatre (and all other properties that depend on it).
     * 
     * Don't do anything if no theatre is set.
     */
    public void resetTheatre() {

        if (this.theatre != null) {
            this.theatre = null;
            this.room = null;
            this.time = null;
            this.occupation = null;
            this.ticket = null;
        }
    }

    /**
     * Unset the selected room (and all other properties that depend on it).
     * 
     * Don't do anything if no room is set.
     */
    public void resetRoom() {

        if (this.room != null) {
            this.room = null;
            this.occupation = null;
            this.ticket = null;
        }
    }

    /**
     * Unset the selected time (and all other properties that depend on it).
     * 
     * Don't do anything if no time is set.
     */
    public void resetTime() {

        if (this.time != null) {
            this.time = null;
            this.occupation = null;
            this.ticket = null;
        }
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> when the given seat is occupied by the user in
     *         this session.
     */
    public static boolean isOccupied(SeatEntity seat) {

        return isSeatSet() && get().getOccupation().getSeat().equals(seat);
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected a seat.
     */
    public static boolean isSeatSet() {

        return get().getOccupation() != null;
    }

    /**
     * @return The session for the current user.
     */
    public static CinemaSession get() {

        return (CinemaSession) Session.get();
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected both a film and
     *         theatre.
     */
    public static boolean isFilmAndTheaterSet() {

        return isFilmSet() && isTheaterSet();
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected film.
     */
    public static boolean isFilmSet() {

        return get().getFilm() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected a theatre.
     */
    public static boolean isTheaterSet() {

        return get().getTheatre() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected both a film and
     *         theatre.
     */
    public static boolean isTimeAndRoomSet() {

        return isTimeSet() && isRoomSet();
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected a show time.
     */
    public static boolean isTimeSet() {

        return get().getTime() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has selected a room.
     */
    public static boolean isRoomSet() {

        return get().getRoom() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the user has made all selections required
     *         for a complete ticket.
     */
    public static boolean isTicketSet() {

        return get().getTicket() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if there is a user logged in and has a
     *         {@link UserEntity} set.
     */
    public static boolean isUserSet() {

        return get().getUser() != null;
    }

    /**
     * @return A string that is the formatted representation of the given date
     *         according to the user's locale in short form.
     */
    public static String format(Date date) {

        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, get().getLocale()).format(date);
    }

    /**
     * @return A string that is the formatted representation of the given amount
     *         of currency according to the user's locale.
     */
    public static String format(Number number) {

        return NumberFormat.getCurrencyInstance(get().getLocale()).format(
                number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupFeedbackMessages() {

    }
}
