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
import net.link.safeonline.demo.cinema.service.FilmService;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link FilmServiceBean}<br>
 * <sub>Service bean for {@link FilmService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = FilmService.BINDING)
public class FilmServiceBean extends AbstractCinemaServiceBean implements
        FilmService {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<FilmEntity> getAllFilms() {

        return this.em.createNamedQuery(FilmEntity.getAll).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<FilmEntity> getFilmsThatPlayIn(TheatreEntity theatre) {

        return this.em.createNamedQuery(FilmEntity.getAllFrom).setParameter(
                "theatre", theatre).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public FilmEntity attach(FilmEntity film) {

        if (film == null)
            return null;
        
        return (FilmEntity) this.em.createNamedQuery(FilmEntity.getById)
                .setParameter("id", film.getId()).getSingleResult();
    }
}
