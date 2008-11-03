/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;


/**
 * <h2>{@link UserService}<br>
 * <sub>Service bean for {@link CinemaUserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link CinemaUserEntity}s for logged in users.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface UserService extends CinemaService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "UserServiceBean/local";


    /**
     * If the given username does not yet exist; create a {@link CinemaUserEntity} for it.
     * 
     * @return The {@link CinemaUserEntity} that maps the given username in the cinema database.
     */
    public CinemaUserEntity getUser(String olasId);

    /**
     * Update the given user's attributes from OLAS.
     */
    public CinemaUserEntity updateUser(CinemaUserEntity user, HttpServletRequest loginRequest);
}
