/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;


/**
 * <h2>{@link FilmService}<br>
 * <sub>Service bean for {@link CinemaFilmEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to either all {@link CinemaFilmEntity}s or those that play in a given theatre.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface FilmService extends CinemaService {

    public static final String BINDING = JNDI_PREFIX + "FilmServiceBean/local";


    /**
     * @return All known {@link CinemaFilmEntity}s.
     */
    public List<CinemaFilmEntity> getAllFilms();

    /**
     * @return All films that play in at least one room of the given theatre.
     */
    public List<CinemaFilmEntity> getFilmsThatPlayIn(CinemaTheatreEntity theatre);
}
