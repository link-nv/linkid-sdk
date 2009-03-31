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
 * <h2>{@link FilmServiceTest}<br>
 * <sub>Unit tests for {@link FilmService}.</sub></h2>
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
public class FilmServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private FilmService           filmService;


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
     * @see FilmService#getAllFilms()
     */
    @Test
    public void testAllFilms() {

        // Test data.
        int testFilmAmount = InitializationService.filmNames.length;
        List<String> testFilmNames = new LinkedList<String>(Arrays.asList(InitializationService.filmNames));

        // Get all films.
        List<CinemaFilmEntity> sampleFilms = filmService.getAllFilms();

        // Verify && all films created successfully & accessible.
        int sampleFilmAmount = sampleFilms.size();
        assertTrue(String.format("film amount mismatch: test: %d - sample: %d", testFilmAmount, sampleFilmAmount), //
                testFilmAmount == sampleFilmAmount);
        for (CinemaFilmEntity sampleFilm : sampleFilms) {
            String sampleFilmName = sampleFilm.getName();
            int sampleFilmIndex = testFilmNames.indexOf(sampleFilmName);

            assertFalse(String.format("film not found: test: %s - sample: %s", testFilmNames, sampleFilmName), //
                    sampleFilmIndex == -1);

            testFilmNames.remove(sampleFilmIndex); // Don't let doubles foil the test.
        }
    }

    /**
     * @see FilmService#getFilmsThatPlayIn(CinemaTheatreEntity)
     */
    @Test
    public void testFilmsThatPlayIn() {

        // Test data.
        List<String> testTheatreNames = Arrays.asList(InitializationService.theatreNames);
        Map<String, List<String>> testTheatresFilmNames = new HashMap<String, List<String>>();

        // - For each theatre, find all films that are supposed to play there.
        for (int testTheatreIndex = 0; testTheatreIndex < testTheatreNames.size(); ++testTheatreIndex) {
            List<String> testTheatreFilmNames = new LinkedList<String>();
            for (int testFilmIndex = 0; testFilmIndex < InitializationService.filmTheatres.length; ++testFilmIndex) {
                if (Arrays.binarySearch(InitializationService.filmTheatres[testFilmIndex], testTheatreIndex) >= 0) {
                    String testFilmName = InitializationService.filmNames[testFilmIndex];

                    testTheatreFilmNames.add(testFilmName);
                }
            }

            String testTheatreName = InitializationService.theatreNames[testTheatreIndex];
            testTheatresFilmNames.put(testTheatreName, testTheatreFilmNames);
        }

        // Get all films for each theatre.
        Map<String, List<CinemaFilmEntity>> sampleTheatresFilms = new HashMap<String, List<CinemaFilmEntity>>();
        for (String theatreName : testTheatreNames) {
            CinemaTheatreEntity theatre = (CinemaTheatreEntity) em.createQuery(
                    "SELECT t FROM CinemaTheatreEntity t WHERE t.name = :name").setParameter("name", theatreName).getSingleResult();

            sampleTheatresFilms.put(theatreName, filmService.getFilmsThatPlayIn(theatre));
        }

        // Verify && all films for each theatre accessible per theatre.
        assertTrue(String.format("theatre amount mismatch: test: %s - sample: %s", testTheatreNames, sampleTheatresFilms.keySet()), //
                testTheatreNames.size() == sampleTheatresFilms.keySet().size());
        for (String theatreName : testTheatreNames) {
            List<String> testTheatreFilmNames = testTheatresFilmNames.get(theatreName);
            List<CinemaFilmEntity> sampleTheatreFilms = sampleTheatresFilms.get(theatreName);

            assertTrue(String.format("film amount mismatch for theatre %s: test: %s - sample: %s", theatreName, testTheatreFilmNames,
                    sampleTheatreFilms), //
                    testTheatreFilmNames.size() == sampleTheatreFilms.size());

            for (CinemaFilmEntity sampleTheatreFilm : sampleTheatreFilms) {
                String sampleTheatreFilmName = sampleTheatreFilm.getName();
                int sampleTheatreFilmIndex = testTheatreFilmNames.indexOf(sampleTheatreFilmName);

                assertFalse(String.format("film not found for theatre %s: test: %s - sample: %s", theatreName, testTheatreFilmNames,
                        sampleTheatreFilmName), //
                        sampleTheatreFilmIndex == -1);

                testTheatreFilmNames.remove(sampleTheatreFilmIndex); // Don't let doubles foil the test.
            }
        }
    }
}
