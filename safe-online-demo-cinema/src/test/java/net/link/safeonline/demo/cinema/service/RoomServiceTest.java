/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;

import org.junit.Test;


/**
 * <h2>{@link RoomServiceTest}<br>
 * <sub>Unit tests for {@link RoomService}.</sub></h2>
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
public class RoomServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private TheatreService        theatreService;

    @EJB
    private FilmService           filmService;

    @EJB
    private RoomService           roomService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
            throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    @Test
    public void testRooms() {

        // Test data.
        Map<String, Map<String, List<String>>> testTheatresFilmsRoomNames = new HashMap<String, Map<String, List<String>>>();
        for (int testFilmIndex = 0; testFilmIndex < InitializationService.filmRooms.length; ++testFilmIndex) {
            String testFilmName = InitializationService.filmNames[testFilmIndex];

            for (int testFilmTheatresIndex = 0; testFilmTheatresIndex < InitializationService.filmTheatres[testFilmIndex].length; ++testFilmTheatresIndex) {
                int testTheatreIndex = InitializationService.filmTheatres[testFilmIndex][testFilmTheatresIndex];
                String testTheatreName = InitializationService.theatreNames[testTheatreIndex];

                // - Enumerate all rooms for film 'filmIndex' playing in theatre 'theatreIndex'
                List<String> testTheatreFilmRoomNames = new LinkedList<String>();
                for (int testRoomIndex : InitializationService.filmRooms[testFilmIndex][testFilmTheatresIndex]) {
                    testTheatreFilmRoomNames.add(InitializationService.theatreRooms[testTheatreIndex][testRoomIndex]);
                }

                if (!testTheatresFilmsRoomNames.containsKey(testTheatreName)) {
                    testTheatresFilmsRoomNames.put(testTheatreName, new HashMap<String, List<String>>());
                }
                testTheatresFilmsRoomNames.get(testTheatreName).put(testFilmName, testTheatreFilmRoomNames);
            }
        }

        // Get rooms.
        List<CinemaTheatreEntity> theatres = this.theatreService.getAllTheatres();
        for (CinemaTheatreEntity theatre : theatres) {
            List<CinemaFilmEntity> films = this.filmService.getFilmsThatPlayIn(theatre);
            for (CinemaFilmEntity film : films) {
                List<CinemaRoomEntity> rooms = this.roomService.getRoomsFor(theatre, film);

                List<String> testRoomNames = testTheatresFilmsRoomNames.get(theatre.getName()).get(film.getName());
                List<String> sampleRoomNames = new LinkedList<String>();
                for (CinemaRoomEntity room : rooms) {
                    sampleRoomNames.add(room.getName());
                }

                assertTrue(String.format("room amount mismatch for theatre %s and film %s: test: %s - sample: %s", theatre.getName(),
                        film.getName(), testRoomNames, sampleRoomNames), //
                        testRoomNames.size() == sampleRoomNames.size());
                assertTrue(String.format("room mismatch for theatre %s and film %s: test: %s - sample: %s", theatre.getName(),
                        film.getName(), testRoomNames, sampleRoomNames), //
                        testRoomNames.containsAll(sampleRoomNames));
            }
        }
    }
}
