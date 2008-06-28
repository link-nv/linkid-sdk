/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
public class InitializationServiceBean extends AbstractCinemaServiceBean
        implements InitializationService {

    private static final int HOURS = 3600, MINUTES = 60;
    private RoomEntity       gent_A;
    private RoomEntity       gent_B;
    private RoomEntity       gent_C;
    private RoomEntity       kortrijk_A;
    private RoomEntity       kortrijk_B;
    private RoomEntity       hasselt_A;
    private RoomEntity       hasselt_B;
    private RoomEntity       hasselt_C;
    private RoomEntity       hasselt_D;
    private RoomEntity       brussel_A;
    private RoomEntity       brussel_B;
    private RoomEntity       brussel_C;
    private RoomEntity       brussel_D;
    private RoomEntity       leuven_A;
    private RoomEntity       leuven_B;
    private RoomEntity       leuven_C;


    /**
     * {@inheritDoc}
     */
    public void buildEntities() {

        createTheatres();
        createFilms();
    }

    /**
     * [HARDCODED] Create some dummy theatres in different cities.
     * 
     * @return All theatres created.
     */
    private Collection<TheatreEntity> createTheatres() {

        TheatreEntity theatre;
        List<TheatreEntity> theatres = new ArrayList<TheatreEntity>();

        // Gent
        theatres.add(theatre = new TheatreEntity("Kinepolis Gent",
                "Ter Platen 12\n9000 Gent"));
        this.em.persist(theatre);
        this.gent_A = addRoom(theatre, "A", 10, 4);
        this.gent_B = addRoom(theatre, "B", 12, 4);
        this.gent_C = addRoom(theatre, "C", 20, 8);

        // Kortrijk
        theatres.add(theatre = new TheatreEntity("Kinepolis Kortrijk",
                "President Kennedylaan 100A\n8500 Kortrijk"));
        this.em.persist(theatre);
        this.kortrijk_A = addRoom(theatre, "A", 8, 4);
        this.kortrijk_B = addRoom(theatre, "B", 10, 8);

        // Hasselt
        theatres.add(theatre = new TheatreEntity("Kinepolis Hasselt",
                "Via Media 1\n3500 Hasselt"));
        this.em.persist(theatre);
        this.hasselt_A = addRoom(theatre, "A", 8, 4);
        this.hasselt_B = addRoom(theatre, "B", 10, 6);
        this.hasselt_C = addRoom(theatre, "C", 12, 8);
        this.hasselt_D = addRoom(theatre, "D", 16, 12);

        // Brussel
        theatres.add(theatre = new TheatreEntity("Kinepolis Brussel",
                "Eeuwfeestlaan 20\n1020 Brussel"));
        this.em.persist(theatre);
        this.brussel_A = addRoom(theatre, "A", 8, 4);
        this.brussel_B = addRoom(theatre, "B", 10, 6);
        this.brussel_C = addRoom(theatre, "C", 12, 8);
        this.brussel_D = addRoom(theatre, "D", 16, 12);

        // Leuven
        theatres.add(theatre = new TheatreEntity("Kinepolis Leuven",
                "Bondgenotenlaan 145-149\n3000 Leuven"));
        this.em.persist(theatre);
        this.leuven_A = addRoom(theatre, "A", 12, 8);
        this.leuven_B = addRoom(theatre, "B", 16, 12);
        this.leuven_C = addRoom(theatre, "C", 20, 8);

        return theatres;
    }

    /**
     * [HARDCODED] Create some dummy films that play in the created theatres.
     * 
     * @return All created films.
     */
    private Collection<FilmEntity> createFilms() {

        Collection<RoomEntity> rooms;
        Collection<ShowTimeEntity> times;
        Collection<FilmEntity> films = new ArrayList<FilmEntity>();

        // Journey to the Center of the Earth
        rooms = new HashSet<RoomEntity>();
        rooms.add(this.gent_A);
        rooms.add(this.kortrijk_A);
        rooms.add(this.leuven_C);
        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(14 * HOURS, 14 * HOURS, 15 * HOURS,
                14 * HOURS, 14 * HOURS + 30 * MINUTES, 16 * HOURS, 16 * HOURS));
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS + 15
                * MINUTES, 20 * HOURS, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        films
                .add(new FilmEntity(
                        "Journey to the Center of the Earth",
                        "On a quest to find out what happened to his missing brother, a scientist, "
                                + "his nephew and their mountain guide discover a fantastic and "
                                + "dangerous lost world in the center of the earth.",
                        92 * MINUTES, 30, times, rooms));
        persistAll(times, rooms);

        // The Happening
        rooms = new HashSet<RoomEntity>();
        rooms.add(this.gent_C);
        rooms.add(this.brussel_C);
        rooms.add(this.leuven_A);
        rooms.add(this.hasselt_D);
        rooms.add(this.kortrijk_B);
        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(14 * HOURS, 14 * HOURS, 15 * HOURS,
                14 * HOURS, 14 * HOURS + 30 * MINUTES, 16 * HOURS, 16 * HOURS));
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS + 15
                * MINUTES, 20 * HOURS, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        films
                .add(new FilmEntity(
                        "The Happening",
                        "A paranoid thriller about a family on the run from a "
                                + "natural crisis that presents a large-scale threat to humanity..",
                        91 * MINUTES, 30, times, rooms));
        persistAll(times, rooms);

        // The Dark Knight
        rooms = new HashSet<RoomEntity>();
        rooms.add(this.gent_B);
        rooms.add(this.hasselt_A);
        rooms.add(this.leuven_C);
        rooms.add(this.brussel_B);
        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS, 20
                * HOURS + 15 * MINUTES, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        times.add(new ShowTimeEntity(23 * HOURS, 23 * HOURS, 23 * HOURS,
                23 * HOURS, 23 * HOURS, null, null));
        films
                .add(new FilmEntity(
                        "The Dark Knight",
                        "Batman and James Gordon join forces with Gotham's new District Attorney, "
                                + "Harvey Dent, to take on a psychotic bank robber known as The Joker, "
                                + "whilst other forces plot against them, and Joker's crimes grow more "
                                + "and more deadly..", 97 * MINUTES, 25, times,
                        rooms));
        persistAll(times, rooms);

        // Shaun Of The Dead
        rooms = new HashSet<RoomEntity>();
        rooms.add(this.gent_B);
        rooms.add(this.hasselt_C);
        rooms.add(this.leuven_B);
        rooms.add(this.brussel_A);
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
                                + "from the dead to eat the living.",
                        99 * MINUTES, 20, times, rooms));
        persistAll(times, rooms);

        // Hellboy II: The Golden Army
        rooms = new HashSet<RoomEntity>();
        rooms.add(this.gent_C);
        rooms.add(this.hasselt_B);
        rooms.add(this.leuven_C);
        rooms.add(this.brussel_D);
        times = new HashSet<ShowTimeEntity>();
        times.add(new ShowTimeEntity(20 * HOURS, 20 * HOURS, 20 * HOURS, 20
                * HOURS + 15 * MINUTES, 20 * HOURS, 22 * HOURS, 22 * HOURS));
        times.add(new ShowTimeEntity(23 * HOURS, 23 * HOURS, 23 * HOURS,
                23 * HOURS, 23 * HOURS, null, null));
        films
                .add(new FilmEntity(
                        "Hellboy II: The Golden Army",
                        "The mythical world starts a rebellion against humanity in order to rule "
                                + "the Earth, so Hellboy and his team must save the world from "
                                + "the rebellious creatures..", 90 * MINUTES,
                        20, times, rooms));
        persistAll(times, rooms);

        // All done
        persistAll(films);
        return films;
    }

    /**
     * Create a room for the given theatre.
     */
    private RoomEntity addRoom(TheatreEntity theatre, String name, int columns,
            int rows) {

        RoomEntity room = new RoomEntity(name, theatre);
        this.em.persist(room);

        for (int x = 1; x <= columns; ++x) {
            for (int y = 1; y <= rows; ++y) {
                this.em.persist(new SeatEntity(room, x, y));
            }
        }

        return room;
    }

    /**
     * Persist all entities in the given collections.
     */
    private void persistAll(Collection<?>... entityCollections) {

        for (Collection<?> entityCollection : entityCollections) {
            for (Object entity : entityCollection) {
                this.em.persist(entity);
            }
        }
    }
}
