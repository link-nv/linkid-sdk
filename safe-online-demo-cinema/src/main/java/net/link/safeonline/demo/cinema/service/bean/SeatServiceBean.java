/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.service.SeatService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link SeatServiceBean}<br>
 * <sub>Service bean for {@link SeatService}.</sub></h2>
 *
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = SeatService.BINDING)
public class SeatServiceBean extends AbstractCinemaServiceBean implements SeatService {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<CinemaSeatEntity> getSeatsFor(CinemaRoomEntity room) {

        return this.em.createNamedQuery(CinemaSeatEntity.getFor).setParameter("room", room).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOccupied(CinemaSeatEntity seat, Date start) {

        return !this.em.createNamedQuery(CinemaSeatOccupationEntity.getFor).setParameter("seat", seat).setParameter(
                "start", start).getResultList().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public CinemaSeatOccupationEntity validate(CinemaSeatOccupationEntity occupation) throws IllegalStateException {

        return validate(occupation.getSeat(), occupation.getStart());
    }

    /**
     * {@inheritDoc}
     */
    public CinemaSeatOccupationEntity validate(CinemaSeatEntity seat, Date start) throws IllegalStateException {

        CinemaSeatEntity seatEntity = attach(seat);

        // Check to see if this seat has already been occupied, and if so,
        // whether it was reserved or not.
        try {
            CinemaSeatOccupationEntity existingOccupation = (CinemaSeatOccupationEntity) this.em.createNamedQuery(
                    CinemaSeatOccupationEntity.getFor).setParameter("seat", seatEntity).setParameter("start", start)
                    .getSingleResult();

            if (existingOccupation.isReserved())
                throw new IllegalStateException("Seat " + seatEntity + " has already been reserved for a customer.");

            // An existing occupation that is not yet reserved?
            // Must be a stale ticket registration; give the existing
            // (stale) seat occupation to this ticket registration instead.
            return existingOccupation;
        }

        // Seat not yet occupied; occupy (but do not yet reserve) it.
        catch (NoResultException e) {
            CinemaSeatOccupationEntity occupation = new CinemaSeatOccupationEntity(seat, start);
            this.em.persist(occupation);

            return occupation;
        }
    }
}
