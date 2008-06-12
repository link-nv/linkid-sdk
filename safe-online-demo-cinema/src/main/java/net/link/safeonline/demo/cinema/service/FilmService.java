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

/**
 * <h2>{@link FilmService}<br>
 * <sub>Service bean for {@link FilmEntity}.</sub></h2>
 * 
 * <p>
 * Create {@link FilmEntity}s and provide access to {@link FilmEntity}s from
 * different angles.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface FilmService {

	public static final String BINDING = "SafeOnlineDemo/FilmService";


    /**
     * @return All known {@link FilmEntity}s.
     */
	public List<FilmEntity> getAllFilms();
}
