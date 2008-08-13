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

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.entity.RoomEntity;
import net.link.safeonline.demo.cinema.entity.SeatEntity;
import net.link.safeonline.demo.cinema.entity.ShowTimeEntity;
import net.link.safeonline.demo.cinema.entity.TheatreEntity;
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
@LocalBinding(jndiBinding = InitializationService.BINDING)
public class InitializationServiceBean extends AbstractCinemaServiceBean implements InitializationService {

    private static List<List<RoomEntity>> theatreRoomEntities = new ArrayList<List<RoomEntity>>();


    /**
     * {@inheritDoc}
     */
    public void buildEntities() {

        createTheatres();
        createFilms();
    }

    /**
     * Create some dummy theatres in different cities.
     */
    private void createTheatres() {

        // Gent
        for (int t = 0; t < theatreNames.length; ++t) {
            TheatreEntity theatre = new TheatreEntity(theatreNames[t], theatreAdresses[t]);
            this.em.persist(theatre);

            List<RoomEntity> currentTheatreRooms = new ArrayList<RoomEntity>();
            theatreRoomEntities.add(currentTheatreRooms);

            for (int r = 0; r < theatreRooms[t].length; ++r) {
                currentTheatreRooms.add(addRoom(theatre, theatreRooms[t][r], theatreRoomSeats[t][r][0],
                        theatreRoomSeats[t][r][1]));
            }
        }
    }

    /**
     * Create some dummy films that play in the created theatres.
     */
    private void createFilms() {

        for (int f = 0; f < filmNames.length; ++f) {

            // Show times.
            Collection<ShowTimeEntity> times = new ArrayList<ShowTimeEntity>();
            for (ShowTimeEntity time : filmTimes[f]) {
                this.em.persist(time);
                times.add(time);
            }

            // Rooms.
            Collection<RoomEntity> rooms = new ArrayList<RoomEntity>();
            LOG.info("film: " + f);
            for (int t = 0; t < filmTheatres[f].length; ++t) {
                LOG.info("film-theatre: " + t);
                for (int r : filmRooms[f][t]) {
                    LOG.info("room: " + r);
                    LOG.info("room theatres: " + theatreRoomEntities.size());
                    LOG.info("theatre rooms: " + theatreRoomEntities.get(filmTheatres[f][t]).size());
                    rooms.add(theatreRoomEntities.get(filmTheatres[f][t]).get(r));
                }
            }

            this.em.persist(new FilmEntity(filmNames[f], filmDescriptions[f], filmDurations[f], filmPrices[f], times,
                    rooms));
        }
    }

    /**
     * Create a room for the given theatre.
     */
    private RoomEntity addRoom(TheatreEntity theatre, String name, int columns, int rows) {

        RoomEntity room = new RoomEntity(name, theatre);
        this.em.persist(room);

        for (int x = 1; x <= columns; ++x) {
            for (int y = 1; y <= rows; ++y) {
                this.em.persist(new SeatEntity(room, x, y));
            }
        }

        return room;
    }
}
