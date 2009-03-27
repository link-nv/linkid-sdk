/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaShowTimeEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.service.InitializationService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link InitializationServiceBean}<br>
 * <sub>Service bean for {@link InitializationService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 23, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = InitializationService.JNDI_BINDING)
public class InitializationServiceBean extends AbstractCinemaServiceBean implements InitializationService {

    private List<List<CinemaRoomEntity>> theatreRoomEntities;


    /**
     * {@inheritDoc}
     */
    public void buildEntities() {

        theatreRoomEntities = new ArrayList<List<CinemaRoomEntity>>();

        // Don't bother if already created.
        Object count = em.createQuery("SELECT count(t) FROM CinemaTheatreEntity t").getSingleResult();
        if (count != null && !count.toString().equals("0"))
            return;

        createTheatres();
        createFilms();
    }

    /**
     * Create some dummy theatres in different cities.
     */
    private void createTheatres() {

        for (int t = 0; t < theatreNames.length; ++t) {
            CinemaTheatreEntity theatre = new CinemaTheatreEntity(theatreNames[t], theatreAdresses[t]);
            em.persist(theatre);

            List<CinemaRoomEntity> currentTheatreRooms = new ArrayList<CinemaRoomEntity>();
            theatreRoomEntities.add(currentTheatreRooms);

            for (int r = 0; r < theatreRooms[t].length; ++r) {
                currentTheatreRooms.add(addRoom(theatre, theatreRooms[t][r], theatreRoomSeats[t][r][0], theatreRoomSeats[t][r][1]));
            }
        }
    }

    /**
     * Create some dummy films that play in the created theatres.
     */
    private void createFilms() {

        for (int f = 0; f < filmNames.length; ++f) {

            // Show times.
            Collection<CinemaShowTimeEntity> times = new ArrayList<CinemaShowTimeEntity>();
            for (CinemaShowTimeEntity time : filmTimes[f]) {
                CinemaShowTimeEntity timeEntity = time.clone();

                em.persist(timeEntity);
                times.add(timeEntity);
            }

            CinemaFilmEntity filmEntity = new CinemaFilmEntity(filmNames[f], filmDescriptions[f], filmDurations[f], filmPrices[f], times);
            em.persist(filmEntity);

            // Rooms.
            for (int t = 0; t < filmTheatres[f].length; ++t) {
                for (int r : filmRooms[f][t]) {
                    CinemaRoomEntity roomEntity = theatreRoomEntities.get(filmTheatres[f][t]).get(r);
                    roomEntity.getFilms().add(filmEntity);
                }
            }
        }
    }

    /**
     * Create a room for the given theatre.
     */
    private CinemaRoomEntity addRoom(CinemaTheatreEntity theatre, String name, int columns, int rows) {

        CinemaRoomEntity room = new CinemaRoomEntity(name, theatre);
        em.persist(room);

        for (int x = 1; x <= columns; ++x) {
            for (int y = 1; y <= rows; ++y) {
                em.persist(new CinemaSeatEntity(room, x, y));
            }
        }

        return room;
    }
}
