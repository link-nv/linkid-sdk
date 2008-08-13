/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.cinema.entity.UserEntity;


/**
 * <h2>{@link UserService}<br>
 * <sub>Service bean for {@link UserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link UserEntity}s for logged in users.
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

    public static final String BINDING = JNDI_PREFIX + "UserServiceBean/local";


    /**
     * If the given username does not yet exist; create a {@link UserEntity} for it.
     * 
     * @return The {@link UserEntity} that maps the given username in the cinema database.
     */
    public UserEntity getUser(String username);

    /**
     * Update the OLAS attributes for the given user.
     */
    public void updateUser(UserEntity user, HttpServletRequest loginRequest);

    /**
     * @return An attached entity for the given one.
     */
    public UserEntity attach(UserEntity user);
}
