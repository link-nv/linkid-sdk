/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.TheatreEntity;


/**
 * <h2>{@link FilmService}<br>
 * <sub>Service bean for {@link FilmEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to either all {@link FilmEntity}s or those that play in a given theatre.
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
     * @return All known {@link FilmEntity}s.
     */
    public List<FilmEntity> getAllFilms();

    /**
     * @return All films that play in at least one room of the given theatre.
     */
    public List<FilmEntity> getFilmsThatPlayIn(TheatreEntity theatre);

    /**
     * @return An attached entity for the given one.
     */
    public FilmEntity attach(FilmEntity film);
}
