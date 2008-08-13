/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.RoomEntity;
import net.link.safeonline.demo.cinema.entity.SeatEntity;
import net.link.safeonline.demo.cinema.entity.TheatreEntity;
import net.link.safeonline.demo.cinema.service.RoomService;
import net.link.safeonline.demo.cinema.service.SeatService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link RoomServiceBean}<br>
 * <sub>Service bean for {@link RoomService}.</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = RoomService.BINDING)
public class RoomServiceBean extends AbstractCinemaServiceBean implements RoomService {

    @EJB
    private transient SeatService seatService;


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<RoomEntity> getRoomsFor(TheatreEntity theatre, FilmEntity film) {

        return this.em.createNamedQuery(RoomEntity.getFor).setParameter("theatre", theatre).setParameter("film", film)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public int getColumns(RoomEntity room) {

        int maxColumn = 0;
        for (SeatEntity seat : this.seatService.getSeatsFor(room)) {
            maxColumn = Math.max(maxColumn, seat.getX());
        }

        return maxColumn;
    }

    /**
     * {@inheritDoc}
     */
    public int getRows(RoomEntity room) {

        int maxRow = 0;
        for (SeatEntity seat : this.seatService.getSeatsFor(room)) {
            maxRow = Math.max(maxRow, seat.getY());
        }

        return maxRow;
    }

    /**
     * {@inheritDoc}
     */
    public RoomEntity attach(RoomEntity room) {

        if (room == null)
            return null;

        return (RoomEntity) this.em.createNamedQuery(RoomEntity.getById).setParameter("id", room.getId())
                .getSingleResult();
    }

}
