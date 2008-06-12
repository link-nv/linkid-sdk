/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.ShowTimeEntity;
import net.link.safeonline.demo.cinema.service.FilmService;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link FilmServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
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
@LocalBinding(jndiBinding = FilmService.BINDING)
public class FilmServiceBean implements FilmService {

    private static final long HOURS = 3600, MINUTES = 60;


    /**
     * {@inheritDoc}
     */
    public List<FilmEntity> getAllFilms() {

        Set<ShowTimeEntity> times;
        List<FilmEntity> films = new ArrayList<FilmEntity>();

        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(14 * HOURS, 14 * HOURS, 15 * HOURS,
                14 * HOURS, 14 * HOURS + 30 * MINUTES, 16 * HOURS, 16 * HOURS));
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS + 15
                * MINUTES, 20 * HOURS, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        films
                .add(new FilmEntity(
                        "The Rise And Fall of the Lin.k Empire.",
                        "A compelling story about how the small company from some unknown country "
                                + "managed to work itself up so far it ended up in control of "
                                + "pretty much the entire authentication market."
                                + "  Until somebody slipped up..", 200, times));

        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS, 20
                * HOURS + 15 * MINUTES, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        times.add(new ShowTimeEntity(23 * HOURS, 23 * HOURS, 23 * HOURS,
                23 * HOURS, 23 * HOURS, null, null));
        films
                .add(new FilmEntity(
                        "Shaun Of The Dead",
                        "A man decides to turn his moribund life around by winning back his ex-"
                                + "girlfriend, reconciling his relationship with his mother, "
                                + "and dealing with an entire community that has returned "
                                + "from the dead to eat the living.", 20, times));

        return films;
    }
}
