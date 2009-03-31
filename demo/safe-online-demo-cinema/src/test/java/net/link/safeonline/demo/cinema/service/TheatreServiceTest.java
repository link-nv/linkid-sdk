/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;

import org.junit.Test;


/**
 * <h2>{@link TheatreServiceTest}<br>
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
public class TheatreServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private TheatreService        theatreService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
            throws Exception {

        super.setup();

        initializationService.buildEntities();
    }

    /**
     * @see TheatreService#getAllTheatres()
     */
    @Test
    public void testAllTheatres() {

        // Test data.
        int testTheatreAmount = InitializationService.theatreNames.length;
        List<String> testTheatreNames = new LinkedList<String>(Arrays.asList(InitializationService.theatreNames));

        // Get all theatres.
        List<CinemaTheatreEntity> sampleTheatres = theatreService.getAllTheatres();

        // Verify && all theatres created successfully & accessible.
        int sampleTheatreAmount = sampleTheatres.size();
        assertTrue(String.format("theatre amount mismatch: test: %d - sample: %d", testTheatreAmount, sampleTheatreAmount), //
                testTheatreAmount == sampleTheatreAmount);
        for (CinemaTheatreEntity sampleTheatre : sampleTheatres) {
            String sampleTheatreName = sampleTheatre.getName();
            int sampleTheatreIndex = testTheatreNames.indexOf(sampleTheatreName);

            assertFalse(String.format("theatre not found: test: %s - sample: %s", testTheatreNames, sampleTheatreName), //
                    sampleTheatreIndex == -1);

            testTheatreNames.remove(sampleTheatreIndex); // Don't let doubles foil the test.
        }
    }

    /**
     * @see TheatreService#getTheatresThatPlay(CinemaFilmEntity)
     */
    @Test
    public void testTheatresThatPlay() {

        // Test data.
        List<String> testFilmNames = Arrays.asList(InitializationService.filmNames);
        Map<String, List<String>> testFilmsTheatreNames = new HashMap<String, List<String>>();

        // - For each film, find all theatres that play it.
        for (int testFilmIndex = 0; testFilmIndex < testFilmNames.size(); ++testFilmIndex) {
            String testFilmName = InitializationService.filmNames[testFilmIndex];

            List<String> testFilmTheatreNames = new LinkedList<String>();
            for (int testTheatreIndex : InitializationService.filmTheatres[testFilmIndex]) {
                testFilmTheatreNames.add(InitializationService.theatreNames[testTheatreIndex]);
            }

            testFilmsTheatreNames.put(testFilmName, testFilmTheatreNames);
        }

        // Get all theatres for each film.
        Map<String, List<CinemaTheatreEntity>> sampleFilmsTheatres = new HashMap<String, List<CinemaTheatreEntity>>();
        for (String filmName : testFilmNames) {
            CinemaFilmEntity film = (CinemaFilmEntity) em.createQuery("SELECT f FROM CinemaFilmEntity f WHERE f.name = :name")
                                                              .setParameter("name", filmName).getSingleResult();

            sampleFilmsTheatres.put(filmName, theatreService.getTheatresThatPlay(film));
        }

        // Verify && all theatres for each film accessible per film.
        assertTrue(String.format("film amount mismatch: test: %s - sample: %s", testFilmNames, sampleFilmsTheatres.keySet()), //
                testFilmNames.size() == sampleFilmsTheatres.keySet().size());
        for (String filmName : testFilmNames) {
            List<String> testFilmTheatreNames = testFilmsTheatreNames.get(filmName);
            List<CinemaTheatreEntity> sampleFilmTheatres = sampleFilmsTheatres.get(filmName);

            assertTrue(String.format("theatre amount mismatch for film %s: test: %s - sample: %s", filmName, testFilmTheatreNames,
                    sampleFilmTheatres), //
                    testFilmTheatreNames.size() == sampleFilmTheatres.size());

            for (CinemaTheatreEntity sampleFilmTheatre : sampleFilmTheatres) {
                String sampleFilmTheatreName = sampleFilmTheatre.getName();
                int sampleFilmTheatreIndex = testFilmTheatreNames.indexOf(sampleFilmTheatreName);

                assertFalse(String.format("theatre not found for film %s: test: %s - sample: %s", filmName, testFilmTheatreNames,
                        sampleFilmTheatreName), //
                        sampleFilmTheatreIndex == -1);

                testFilmTheatreNames.remove(sampleFilmTheatreIndex); // Don't let doubles foil the test.
            }
        }
    }
}
