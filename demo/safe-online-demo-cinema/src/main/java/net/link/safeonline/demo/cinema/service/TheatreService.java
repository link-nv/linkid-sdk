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
 * <h2>{@link TheatreService}<br>
 * <sub>Service bean for {@link CinemaTheatreEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to either all {@link CinemaTheatreEntity}s or those that play the given film.
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

    public static final String JNDI_BINDING = JNDI_PREFIX + "TheatreServiceBean/local";


    /**
     * @return All known {@link CinemaTheatreEntity}s.
     */
    public List<CinemaTheatreEntity> getAllTheatres();

    /**
     * @return All theatres that play the given movie in one of their rooms.
     */
    public List<CinemaTheatreEntity> getTheatresThatPlay(CinemaFilmEntity film);
}
