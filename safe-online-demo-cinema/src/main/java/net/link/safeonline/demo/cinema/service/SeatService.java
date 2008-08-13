/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.cinema.entity.RoomEntity;
import net.link.safeonline.demo.cinema.entity.SeatEntity;
import net.link.safeonline.demo.cinema.entity.SeatOccupationEntity;


/**
 * <h2>{@link SeatService}<br>
 * <sub>Service bean for {@link SeatEntity}.</sub></h2>
 *
 * <p>
 * Provide access to {@link SeatEntity}s.
 * </p>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface SeatService extends CinemaService {

    public static final String BINDING = JNDI_PREFIX + "SeatServiceBean/local";


    /**
     * @return The seats in the given room.
     */
    public List<SeatEntity> getSeatsFor(RoomEntity room);

    /**
     * @return <code>true</code> If this seat has already been occupied.
     */
    public boolean isOccupied(SeatEntity seat, Date start);

    /**
     * Verify that the given seat is not already occupied. If it is not, persist it.
     *
     * @return The persisted seat occupation.
     * @throws IllegalStateException
     *             When at least one of the given seats is not available.
     */
    public SeatOccupationEntity validate(SeatOccupationEntity occupation) throws IllegalStateException;
}
