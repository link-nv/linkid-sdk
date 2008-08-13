/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.List;

import javax.ejb.Stateless;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.TheatreEntity;
import net.link.safeonline.demo.cinema.service.TheatreService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link TheatreServiceBean}<br>
 * <sub>Service bean for {@link TheatreService}.</sub></h2>
 *
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 *
 * @see TheatreService
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = TheatreService.BINDING)
public class TheatreServiceBean extends AbstractCinemaServiceBean implements TheatreService {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<TheatreEntity> getAllTheatres() {

        return this.em.createNamedQuery(TheatreEntity.getAll).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<TheatreEntity> getTheatresThatPlay(FilmEntity film) {

        return this.em.createNamedQuery(TheatreEntity.getAllFor).setParameter("film", film).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public TheatreEntity attach(TheatreEntity theatre) {

        if (theatre == null)
            return null;

        return (TheatreEntity) this.em.createNamedQuery(TheatreEntity.getById).setParameter("id", theatre.getId())
                .getSingleResult();
    }
}
