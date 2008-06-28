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
 * <h2>{@link TheatreService}<br>
 * <sub>Service bean for {@link TheatreEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to either all {@link TheatreEntity}s or those that play the
 * given film.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface TheatreService extends CinemaService {

    public static final String BINDING = JNDI_PREFIX
                                               + "TheatreServiceBean/local";


    /**
     * @return All known {@link TheatreEntity}s.
     */
    public List<TheatreEntity> getAllTheatres();

    /**
     * @return All theatres that play the given movie in one of their rooms.
     */
    public List<TheatreEntity> getTheatresThatPlay(FilmEntity film);

    /**
     * @return An attached entity for the given one.
     */
    public TheatreEntity attach(TheatreEntity theatre);
}
