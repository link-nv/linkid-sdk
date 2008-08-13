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

import net.link.safeonline.demo.cinema.entity.RoomEntity;
import net.link.safeonline.demo.cinema.entity.SeatEntity;
import net.link.safeonline.demo.cinema.entity.SeatOccupationEntity;
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
    public List<SeatEntity> getSeatsFor(RoomEntity room) {

        return this.em.createNamedQuery(SeatEntity.getFor).setParameter("room", room).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOccupied(SeatEntity seat, Date start) {

        return !this.em.createNamedQuery(SeatOccupationEntity.getFor).setParameter("seat", seat).setParameter("start",
                start).getResultList().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public SeatOccupationEntity validate(SeatOccupationEntity occupation) throws IllegalStateException {

        SeatEntity seat = (SeatEntity) this.em.createNamedQuery(SeatEntity.getById).setParameter("id",
                occupation.getSeat().getId()).getSingleResult();

        // Check to see if this seat has already been occupied, and if so,
        // whether it was reserved or not.
        try {
            SeatOccupationEntity existingOccupation = (SeatOccupationEntity) this.em.createNamedQuery(
                    SeatOccupationEntity.getFor).setParameter("seat", seat)
                    .setParameter("start", occupation.getStart()).getSingleResult();

            if (existingOccupation.isReserved())
                throw new IllegalStateException("Seat " + seat + " is already occupied.");

            // An existing occupation that is not yet reserved?
            // Must be a stale ticket registration; give the existing
            // (stale) seat occupation to this ticket registration instead.
            return existingOccupation;
        }

        // Seat not yet occupied; occupy (but not yet reserve) it.
        catch (NoResultException e) {
            this.em.persist(occupation);

            return occupation;
        }
    }
}
