/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.webapp;

import java.util.Date;
import java.util.Locale;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.wicket.web.OLASSession;

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
public class CinemaSession extends OLASSession {

    private static final long          serialVersionUID = 1L;
    public static final Locale         CURRENCY         = Locale.FRANCE;

    private CinemaUserEntity           user;
    private CinemaFilmEntity           film;
    private CinemaTheatreEntity        theatre;
    private Date                       time;
    private CinemaRoomEntity           room;
    private CinemaSeatOccupationEntity occupation;
    private CinemaTicketEntity         ticket;


    public CinemaSession(Request request) {

        super(request);
    }

    public void setUser(CinemaUserEntity user) {

        this.user = user;
    }

    public CinemaUserEntity getUser() {

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserOlasId() {

        return isUserSet()? getUser().getOlasId(): null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserSet() {

        return getUser() != null;
    }

    /**
     * @param film
     *            The film the user selected for viewing.
     */
    public void setFilm(CinemaFilmEntity film) {

        this.film = film;
    }

    /**
     * @return The film the user selected for viewing.
     */
    public CinemaFilmEntity getFilm() {

        return film;
    }

    /**
     * @param theatre
     *            The theatre the user will view his film in.
     */
    public void setTheatre(CinemaTheatreEntity theatre) {

        this.theatre = theatre;
    }

    /**
     * @return The theatre the user will view his film in.
     */
    public CinemaTheatreEntity getTheatre() {

        return theatre;
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
    public void setRoom(CinemaRoomEntity room) {

        this.room = room;
    }

    /**
     * @return The time of showing for the selected film.
     */
    public Date getTime() {

        return time;
    }

    /**
     * @return The room of the theatre the user will view the film in.
     */
    public CinemaRoomEntity getRoom() {

        return room;
    }

    /**
     * @return The seat occupied by the user.
     */
    public CinemaSeatOccupationEntity getOccupation() {

        return occupation;
    }

    /**
     * @param ticket
     *            The complete ticket for the selections made by the user.
     */
    public void setTicket(CinemaTicketEntity ticket) {

        this.ticket = ticket;
    }

    /**
     * @return The complete ticket for the selections made by the user or <code>null</code> if not all required selections have been made
     *         yet.
     */
    public CinemaTicketEntity getTicket() {

        return ticket;
    }

    /**
     * Change the seat occupation to the given seat, or unoccupy that seat if it was the seat currently occupied by us.
     * 
     * @param seat
     *            The seat to occupy or unoccupy.
     */
    public void toggleSeat(CinemaSeatEntity seat) {

        if (occupation != null && occupation.getSeat().equals(seat)) {
            occupation = null;
        } else {
            occupation = new CinemaSeatOccupationEntity(seat, time);
        }
    }

    /**
     * Unset the whole ticket.
     */
    public void resetTicket() {

        film = null;
        theatre = null;
        room = null;
        time = null;
        occupation = null;
        ticket = null;
    }

    /**
     * Unset the selected film (and all other properties that depend on it).
     * 
     * Don't do anything if no film is set.
     */
    public void resetFilm() {

        if (film != null) {
            film = null;
            room = null;
            time = null;
            occupation = null;
            ticket = null;
        }
    }

    /**
     * Unset the selected theatre (and all other properties that depend on it).
     * 
     * Don't do anything if no theatre is set.
     */
    public void resetTheatre() {

        if (theatre != null) {
            theatre = null;
            room = null;
            time = null;
            occupation = null;
            ticket = null;
        }
    }

    /**
     * Unset the selected room (and all other properties that depend on it).
     * 
     * Don't do anything if no room is set.
     */
    public void resetRoom() {

        if (room != null) {
            room = null;
            occupation = null;
            ticket = null;
        }
    }

    /**
     * Unset the selected time (and all other properties that depend on it).
     * 
     * Don't do anything if no time is set.
     */
    public void resetTime() {

        if (time != null) {
            time = null;
            occupation = null;
            ticket = null;
        }
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> when the given seat is occupied by the user in this session.
     */
    public static boolean isOccupied(CinemaSeatEntity seat) {

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
     * @return <code>true</code> if the user has selected both a film and theatre.
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
     * @return <code>true</code> if the user has selected both a film and theatre.
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
     * @return <code>true</code> if the user has made all selections required for a complete ticket.
     */
    public static boolean isTicketSet() {

        return get().getTicket() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupFeedbackMessages() {

    }
}
