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

import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;


/**
 * <h2>{@link SeatService}<br>
 * <sub>Service bean for {@link CinemaSeatEntity}.</sub></h2>
 * 
 * <p>
 * Provide access to {@link CinemaSeatEntity}s.
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

    public static final String JNDI_BINDING = JNDI_PREFIX + "SeatServiceBean/local";


    /**
     * @return The seats in the given room.
     */
    public List<CinemaSeatEntity> getSeatsFor(CinemaRoomEntity room);

    /**
     * @return <code>true</code> If this seat has already been occupied.
     */
    public boolean isOccupied(CinemaSeatEntity seat, Date start);

    /**
     * Verify that the seat described by the given occupation is not already occupied. If it is not, occupy it (but do not yet reserve it).
     * 
     * @return The persisted seat occupation.
     * @throws IllegalStateException
     *             When at least one of the given seats is not available.
     */
    public CinemaSeatOccupationEntity validate(CinemaSeatOccupationEntity occupation) throws IllegalStateException;

    /**
     * Verify that the given seat is not already occupied on the given time. If it is not, occupy it (but do not yet reserve it).
     * 
     * @return The persisted seat occupation.
     * @throws IllegalStateException
     *             When at least one of the given seats is not available.
     */
    public CinemaSeatOccupationEntity validate(CinemaSeatEntity seat, Date start) throws IllegalStateException;
}
