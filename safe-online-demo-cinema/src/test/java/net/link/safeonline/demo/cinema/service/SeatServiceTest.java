/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;

import junit.framework.AssertionFailedError;
import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;

import org.junit.Test;


/**
 * <h2>{@link SeatServiceTest}<br>
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
public class SeatServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private FilmService           filmService;

    @EJB
    private TheatreService        theatreService;

    @EJB
    private RoomService           roomService;

    @EJB
    private SeatService           seatService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    @Test
    public void testSeatOccupation() {

        // Test data.
        int testFilmIndex = 0, testTheatreIndex = 0, testTheatreRoomIndex = 0;
        String testFilmName = InitializationService.filmNames[testFilmIndex];
        String testTheatreName = InitializationService.theatreNames[testTheatreIndex];
        String testRoomName = InitializationService.theatreRooms[testTheatreIndex][testTheatreRoomIndex];
        Date testSeatTime = new Date();
        int testSeatX = 1, testSeatY = 1;

        // Occupy a seat.
        // - Find our film entity.
        CinemaFilmEntity sampleFilm = null;
        List<CinemaFilmEntity> sampleFilms = this.filmService.getAllFilms();
        for (CinemaFilmEntity film : sampleFilms) {
            if (testFilmName.equals(film.getName())) {
                sampleFilm = film;
                break;
            }
        }
        assertNotNull(String.format("film not found: test: %s - sample: %s", testFilmName, sampleFilms), //
                sampleFilm);
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
        try {
            this.seatService.validate(sampleSeat, testSeatTime);
        } catch (IllegalStateException e) {
            throw new AssertionFailedError(String.format("seat not available: %s at %s", sampleSeat, testSeatTime));
        }

        // Check our seat.
        assertTrue(String.format("seat occupation failed for: %s at %s", sampleSeat, testSeatTime), //
                this.seatService.isOccupied(sampleSeat, testSeatTime));
    }
}
