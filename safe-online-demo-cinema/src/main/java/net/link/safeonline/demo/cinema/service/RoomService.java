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
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;


/**
 * <h2>{@link RoomService}<br>
 * <sub>Service bean for {@link CinemaRoomEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to {@link CinemaRoomEntity}s based on which theatre you want to view which movie in.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface RoomService extends CinemaService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "RoomServiceBean/local";


    /**
     * @return All rooms from the given theatre that play the given film.
     */
    public List<CinemaRoomEntity> getRoomsFor(CinemaTheatreEntity theatre, CinemaFilmEntity film);

    /**
     * @return The amount of rows of seats this room has.
     */
    public int getRows(CinemaRoomEntity room);

    /**
     * @return The amount of columns of seats this room has.
     */
    public int getColumns(CinemaRoomEntity room);
}
