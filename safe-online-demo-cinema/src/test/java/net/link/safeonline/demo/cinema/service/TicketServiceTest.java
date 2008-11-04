/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;

import junit.framework.AssertionFailedError;
import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;

import org.junit.Test;


/**
 * <h2>{@link TicketServiceTest}<br>
 * <sub>Unit tests for {@link SeatService}.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class TicketServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private FilmService           filmService;

    @EJB
    private TheatreService        theatreService;

    @EJB
    private RoomService           roomService;

    @EJB
    private UserService           userService;

    @EJB
    private SeatService           seatService;

    @EJB
    private TicketService         ticketService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
            throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    /**
     * @see TicketService#createTicket(net.link.safeonline.demo.cinema.entity.CinemaUserEntity, CinemaFilmEntity, Date,
     *      net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity)
     */
    @Test
    public void testCreateTicket() {

        // Test data.
        String testUserName = "testCinemaUser", testUserNrn = "1234567890";
        int testFilmIndex = 0, testTheatreIndex = 0, testTheatreRoomIndex = 0;
        String testFilmName = InitializationService.filmNames[testFilmIndex];
        String testTheatreName = InitializationService.theatreNames[testTheatreIndex];
        String testRoomName = InitializationService.theatreRooms[testTheatreIndex][testTheatreRoomIndex];
        double testTicketPrice = InitializationService.filmPrices[testFilmIndex];
        Date testSeatTime = new Date();
        int testSeatX = 1, testSeatY = 1;

        // Create a ticket and register it.
        // - Find our film.
        CinemaFilmEntity sampleFilm = null;
        List<CinemaFilmEntity> sampleFilms = this.filmService.getAllFilms();
        for (CinemaFilmEntity film : sampleFilms)
            if (testFilmName.equals(film.getName())) {
                sampleFilm = film;
                break;
            }
        assertNotNull(String.format("film not found: test: %s - sample: %s", testFilmName, sampleFilms), //
                sampleFilms);
        // - Find our theatre entity.
        CinemaTheatreEntity sampleTheatre = null;
        List<CinemaTheatreEntity> sampleTheatres = this.theatreService.getTheatresThatPlay(sampleFilm);
        for (CinemaTheatreEntity theatre : sampleTheatres) {
            if (testTheatreName.equals(theatre.getName())) {
                sampleTheatre = theatre;
                break;
            }
        }
        assertNotNull(String.format("theatre not found: test: %s - sample: %s", testTheatreName, sampleTheatres), //
                sampleTheatre);
        // - Find our room entity.
        CinemaRoomEntity sampleRoom = null;
        List<CinemaRoomEntity> sampleRooms = this.roomService.getRoomsFor(sampleTheatre, sampleFilm);
        for (CinemaRoomEntity room : sampleRooms) {
            if (testRoomName.equals(room.getName())) {
                sampleRoom = room;
                break;
            }
        }
        assertNotNull(String.format("room not found: test: %s - sample: %s", testRoomName, sampleRooms), //
                sampleRoom);
        // - Find our seat.
        CinemaSeatEntity sampleSeat = null;
        List<CinemaSeatEntity> sampleSeats = this.seatService.getSeatsFor(sampleRoom);
        for (CinemaSeatEntity seat : sampleSeats) {
            if (seat.getX() == testSeatX && seat.getY() == testSeatY) {
                sampleSeat = seat;
                break;
            }
        }
        assertNotNull(String.format("seat not found: test: %d, %d - sample: %s", testSeatX, testSeatY, sampleSeats), //
                sampleSeat);
        // - Occupy our seat.
        CinemaSeatOccupationEntity sampleOccupation = null;
        try {
            sampleOccupation = this.seatService.validate(sampleSeat, testSeatTime);
        } catch (IllegalStateException e) {
            throw new AssertionFailedError(String.format("seat not available: %s at %s", sampleSeat, testSeatTime));
        }

        // - Create our user.
        CinemaUserEntity sampleUser = this.userService.getUser(testUserName);
        sampleUser.setNrn(testUserNrn);

        // - Create the ticket and reserve it (pay for it)
        CinemaTicketEntity sampleTicket = this.ticketService.createTicket(sampleUser, sampleFilm, testSeatTime, sampleOccupation);
        double sampleTicketPrice = sampleTicket.getPrice();
        assertEquals(String.format("price mismatch: test: %s - sample: %s", testTicketPrice, sampleTicketPrice), //
                testTicketPrice, sampleTicketPrice, 0);
        this.ticketService.reserve(sampleTicket);

        // Validate && ticket is valid and seat reserved.
        assertTrue("ticket was not valid.", //
                this.ticketService.isValid(testUserNrn, testSeatTime, testTheatreName, testFilmName));
        try {
            this.seatService.validate(sampleSeat, testSeatTime);
            throw new AssertionFailedError("ticket's seat was not reserved.");
        } catch (IllegalStateException e) {
            // expected.
        }
    }
}
